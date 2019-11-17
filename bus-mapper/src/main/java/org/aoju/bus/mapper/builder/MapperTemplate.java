/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.mapper.builder;

import org.aoju.bus.mapper.MapperException;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.aoju.bus.mapper.entity.EntityTable;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.aoju.bus.mapper.reflection.Reflector.getMapperClass;
import static org.aoju.bus.mapper.reflection.Reflector.getMethodName;

/**
 * 通用Mapper模板类，扩展通用Mapper时需要继承该类
 *
 * @author Kimi Liu
 * @version 5.2.1
 * @since JDK 1.8+
 */
public abstract class MapperTemplate {

    private static final XMLLanguageDriver languageDriver = new XMLLanguageDriver();
    protected Map<String, Method> methodMap = new ConcurrentHashMap<String, Method>();
    protected Map<String, Class<?>> entityClassMap = new ConcurrentHashMap<String, Class<?>>();
    protected Class<?> mapperClass;
    protected MapperBuilder mapperBuilder;

    public MapperTemplate(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        this.mapperClass = mapperClass;
        this.mapperBuilder = mapperBuilder;
    }

    /**
     * 该方法仅仅用来初始化ProviderSqlSource
     *
     * @param record 对象
     * @return string
     */
    public String dynamicSQL(Object record) {
        return "dynamicSQL";
    }

    /**
     * 添加映射方法
     *
     * @param methodName 方法名称
     * @param method     方法
     */
    public void addMethodMap(String methodName, Method method) {
        methodMap.put(methodName, method);
    }

    /**
     * 获取Identity值的表达式
     *
     * @param column 列信息
     * @return the string
     */
    public String getIDENTITY(EntityColumn column) {
        return MessageFormat.format(mapperBuilder.getConfig().getIdentity(), column.getSequenceName(), column.getColumn(), column.getProperty(), column.getTable().getName());
    }

    /**
     * 是否支持该通用方法
     *
     * @param msId 方法ID
     * @return the boolean
     */
    public boolean supportMethod(String msId) {
        Class<?> mapperClass = getMapperClass(msId);
        if (mapperClass != null && this.mapperClass.isAssignableFrom(mapperClass)) {
            String methodName = getMethodName(msId);
            return methodMap.get(methodName) != null;
        }
        return false;
    }

    /**
     * 设置返回值类型 - 为了让typeHandler在select时有效，改为设置resultMap
     *
     * @param ms          MappedStatement
     * @param entityClass 对象
     */
    protected void setResultType(MappedStatement ms, Class<?> entityClass) {
        EntityTable entityTable = EntityBuilder.getEntityTable(entityClass);
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        resultMaps.add(entityTable.getResultMap(ms.getConfiguration()));
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        metaObject.setValue("resultMaps", Collections.unmodifiableList(resultMaps));
    }

    /**
     * 重新设置SqlSource
     *
     * @param ms        MappedStatement
     * @param sqlSource SqlSource
     */
    protected void setSqlSource(MappedStatement ms, SqlSource sqlSource) {
        MetaObject msObject = SystemMetaObject.forObject(ms);
        msObject.setValue("sqlSource", sqlSource);
    }

    /**
     * 通过xmlSql创建sqlSource
     *
     * @param ms     MappedStatement
     * @param xmlSql String
     * @return SqlSource
     */
    public SqlSource createSqlSource(MappedStatement ms, String xmlSql) {
        return languageDriver.createSqlSource(ms.getConfiguration(), "<script>\n\t" + xmlSql + "</script>", null);
    }

    /**
     * 获取返回值类型 - 实体类型
     *
     * @param ms MappedStatement
     * @return the class
     */
    public Class<?> getEntityClass(MappedStatement ms) {
        String msId = ms.getId();
        if (entityClassMap.containsKey(msId)) {
            return entityClassMap.get(msId);
        } else {
            Class<?> mapperClass = getMapperClass(msId);
            Type[] types = mapperClass.getGenericInterfaces();
            for (Type type : types) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType t = (ParameterizedType) type;
                    if (t.getRawType() == this.mapperClass || this.mapperClass.isAssignableFrom((Class<?>) t.getRawType())) {
                        Class<?> returnType = (Class<?>) t.getActualTypeArguments()[0];
                        //获取该类型后，第一次对该类型进行初始化
                        EntityBuilder.initEntityNameMap(returnType, mapperBuilder.getConfig());
                        entityClassMap.put(msId, returnType);
                        return returnType;
                    }
                }
            }
        }
        throw new MapperException("无法获取 " + msId + " 方法的泛型信息!");
    }

    /**
     * 获取序列下个值的表达式
     *
     * @param column 列
     * @return the string
     */
    protected String getSeqNextVal(EntityColumn column) {
        return MessageFormat.format(mapperBuilder.getConfig().getSeqFormat(), column.getSequenceName(), column.getColumn(), column.getProperty(), column.getTable().getName());
    }

    /**
     * 获取实体类的表名
     *
     * @param entityClass 对象
     * @return the string
     */
    protected String tableName(Class<?> entityClass) {
        EntityTable entityTable = EntityBuilder.getEntityTable(entityClass);
        String prefix = entityTable.getPrefix();
        if (Assert.isEmpty(prefix)) {
            //使用全局配置
            prefix = mapperBuilder.getConfig().getPrefix();
        }
        if (Assert.isNotEmpty(prefix)) {
            return prefix + "." + entityTable.getName();
        }
        return entityTable.getName();
    }

    public String getIdentity() {
        return mapperBuilder.getConfig().getIdentity();
    }

    public String getUUID() {
        return mapperBuilder.getConfig().getUUID();
    }

    public boolean isBEFORE() {
        return mapperBuilder.getConfig().isBEFORE();
    }

    public boolean isCheckEntityClass() {
        return mapperBuilder.getConfig().isCheckEntityClass();
    }

    public boolean isNotEmpty() {
        return mapperBuilder.getConfig().isNotEmpty();
    }

    /**
     * 重新设置SqlSource
     *
     * @param ms MappedStatement
     */
    public void setSqlSource(MappedStatement ms) {
        if (this.mapperClass == getMapperClass(ms.getId())) {
            throw new MapperException("请不要配置或扫描通用Mapper接口类：" + this.mapperClass);
        }
        Method method = methodMap.get(getMethodName(ms));
        try {
            //第一种，直接操作ms，不需要返回值
            if (method.getReturnType() == Void.TYPE) {
                method.invoke(this, ms);
            }
            //第二种，返回SqlNode
            else if (SqlNode.class.isAssignableFrom(method.getReturnType())) {
                SqlNode sqlNode = (SqlNode) method.invoke(this, ms);
                DynamicSqlSource dynamicSqlSource = new DynamicSqlSource(ms.getConfiguration(), sqlNode);
                setSqlSource(ms, dynamicSqlSource);
            }
            //第三种，返回xml形式的sql字符串
            else if (String.class.equals(method.getReturnType())) {
                String xmlSql = (String) method.invoke(this, ms);
                SqlSource sqlSource = createSqlSource(ms, xmlSql);
                //替换原有的SqlSource
                setSqlSource(ms, sqlSource);
            } else {
                throw new MapperException("自定义Mapper方法返回类型错误,可选的返回类型为void,SqlNode,String三种!");
            }
        } catch (IllegalAccessException e) {
            throw new MapperException(e);
        } catch (InvocationTargetException e) {
            throw new MapperException(e.getTargetException() != null ? e.getTargetException() : e);
        }
    }

}
