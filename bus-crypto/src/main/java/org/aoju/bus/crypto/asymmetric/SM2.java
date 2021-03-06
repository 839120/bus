/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.crypto.asymmetric;

import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.HexKit;
import org.aoju.bus.crypto.Builder;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.DSAEncoding;
import org.bouncycastle.crypto.signers.PlainDSAEncoding;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 国密SM2算法实现，基于BC库
 * SM2算法只支持公钥加密，私钥解密
 *
 * @author Kimi Liu
 * @version 5.9.6
 * @since JDK 1.8+
 */
public class SM2 extends Safety<SM2> {

    protected SM2Engine engine;
    protected SM2Signer signer;

    private SM2Engine.Mode mode = SM2Engine.Mode.C1C3C2;
    private ECPrivateKeyParameters privateKeyParams;
    private ECPublicKeyParameters publicKeyParams;
    private DSAEncoding encoding = StandardDSAEncoding.INSTANCE;
    private Digest digest = new SM3Digest();

    /**
     * 构造，生成新的私钥公钥对
     */
    public SM2() {
        this(null, (byte[]) null);
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyStr 私钥Hex或Base64表示
     * @param publicKeyStr  公钥Hex或Base64表示
     */
    public SM2(String privateKeyStr, String publicKeyStr) {
        this(Builder.decode(privateKeyStr), Builder.decode(publicKeyStr));
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public SM2(byte[] privateKey, byte[] publicKey) {
        this(
                Builder.generatePrivateKey(Algorithm.SM2, privateKey),
                Builder.generatePublicKey(Algorithm.SM2, publicKey)
        );
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public SM2(PrivateKey privateKey, PublicKey publicKey) {
        this(Builder.toParams(privateKey), Builder.toParams(publicKey));
        if (null != privateKey) {
            this.privateKey = privateKey;
        }
        if (null != publicKey) {
            this.publicKey = publicKey;
        }
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyHex      私钥16进制
     * @param publicKeyPointXHex 公钥X16进制
     * @param publicKeyPointYHex 公钥Y16进制
     */
    public SM2(String privateKeyHex, String publicKeyPointXHex, String publicKeyPointYHex) {
        this(Builder.toSm2Params(privateKeyHex), Builder.toSm2Params(publicKeyPointXHex, publicKeyPointYHex));
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey      私钥
     * @param publicKeyPointX 公钥X
     * @param publicKeyPointY 公钥Y
     */
    public SM2(byte[] privateKey, byte[] publicKeyPointX, byte[] publicKeyPointY) {
        this(Builder.toSm2Params(privateKey), Builder.toSm2Params(publicKeyPointX, publicKeyPointY));
    }

    /**
     * 构造
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyParams 私钥
     * @param publicKeyParams  公钥
     */
    public SM2(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams) {
        super(Algorithm.SM2, null, null);
        this.privateKeyParams = privateKeyParams;
        this.publicKeyParams = publicKeyParams;
        this.init();
    }

    /**
     * 初始化
     * 私钥和公钥同时为空时生成一对新的私钥和公钥
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密(签名)或者解密(校验)
     *
     * @return this
     */
    public SM2 init() {
        if (null == this.privateKeyParams && null == this.publicKeyParams) {
            super.initKeys();
            this.privateKeyParams = Builder.toParams(this.privateKey);
            this.publicKeyParams = Builder.toParams(this.publicKey);
        }
        return this;
    }

    @Override
    public SM2 initKeys() {
        // 阻断父类中自动生成密钥对的操作，此操作由本类中进行。
        // 由于用户可能传入Params而非key，因此此时key必定为null，故此不再生成
        return this;
    }

    /**
     * 加密，SM2非对称加密的结果由C1,C2,C3三部分组成，其中：
     *
     * <pre>
     * C1 生成随机数的计算出的椭圆曲线点
     * C2 密文数据
     * C3 SM3的摘要值
     * </pre>
     *
     * @param data    被加密的bytes
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     * @throws InstrumentException 包括InvalidKeyException和InvalidCipherTextException的包装异常
     */
    @Override
    public byte[] encrypt(byte[] data, KeyType keyType) throws InstrumentException {
        if (KeyType.PublicKey != keyType) {
            throw new IllegalArgumentException("Encrypt is only support by public key");
        }

        lock.lock();
        final SM2Engine engine = getEngine();
        try {
            engine.init(true, new ParametersWithRandom(getCipherParameters(keyType)));
            return engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 解密
     *
     * @param data    SM2密文，实际包含三部分：ECC公钥、真正的密文、公钥和原文的SM3-HASH值
     * @param keyType 私钥或公钥 {@link KeyType}
     * @return 加密后的bytes
     */
    @Override
    public byte[] decrypt(byte[] data, KeyType keyType) {
        if (KeyType.PrivateKey != keyType) {
            throw new IllegalArgumentException("Decrypt is only support by private key");
        }

        lock.lock();
        final SM2Engine engine = getEngine();
        try {
            engine.init(false, getCipherParameters(keyType));
            return engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param dataHex 被签名的数据数据
     * @return 签名
     */
    public String sign(String dataHex) {
        return sign(dataHex, null);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @return 签名
     */
    public byte[] sign(byte[] data) {
        return sign(data, null);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param dataHex 被签名的数据数据
     * @param idHex   可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @return 签名
     */
    public String sign(String dataHex, String idHex) {
        return HexKit.encodeHexStr(sign(HexKit.decodeHex(dataHex), HexKit.decodeHex(idHex)));
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 被签名的数据数据
     * @param id   可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @return 签名
     */
    public byte[] sign(byte[] data, byte[] id) {
        lock.lock();
        final SM2Signer signer = getSigner();
        try {
            CipherParameters param = new ParametersWithRandom(getCipherParameters(KeyType.PrivateKey));
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(true, param);
            signer.update(data, 0, data.length);
            return signer.generateSignature();
        } catch (CryptoException e) {
            throw new InstrumentException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param dataHex 数据签名后的数据
     * @param signHex 签名
     * @return 是否验证通过
     */
    public boolean verify(String dataHex, String signHex) {
        return verify(dataHex, signHex, null);
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param data 签名后的数据
     * @param sign 签名
     * @return 是否验证通过
     */
    public boolean verify(byte[] data, byte[] sign) {
        return verify(data, sign, null);
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param dataHex 数据签名后的数据的Hex值
     * @param signHex 签名的Hex值
     * @param idHex   ID的Hex值
     * @return 是否验证通过
     */
    public boolean verify(String dataHex, String signHex, String idHex) {
        return verify(HexKit.decodeHex(dataHex), HexKit.decodeHex(signHex), HexKit.decodeHex(idHex));
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param data 数据签名后的数据
     * @param sign 签名
     * @param id   可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @return 是否验证通过
     */
    public boolean verify(byte[] data, byte[] sign, byte[] id) {
        lock.lock();
        final SM2Signer signer = getSigner();
        try {
            CipherParameters param = getCipherParameters(KeyType.PublicKey);
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(false, param);
            signer.update(data, 0, data.length);
            return signer.verifySignature(sign);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SM2 setPrivateKey(PrivateKey privateKey) {
        super.setPrivateKey(privateKey);

        // 重新初始化密钥参数，防止重新设置密钥时导致密钥无法更新
        this.privateKeyParams = Builder.toParams(privateKey);

        return this;
    }

    /**
     * 设置私钥参数
     *
     * @param privateKeyParams 私钥参数
     * @return this
     */
    public SM2 setPrivateKeyParams(ECPrivateKeyParameters privateKeyParams) {
        this.privateKeyParams = privateKeyParams;
        return this;
    }

    @Override
    public SM2 setPublicKey(PublicKey publicKey) {
        super.setPublicKey(publicKey);

        // 重新初始化密钥参数，防止重新设置密钥时导致密钥无法更新
        this.publicKeyParams = Builder.toParams(publicKey);

        return this;
    }

    /**
     * 设置公钥参数
     *
     * @param publicKeyParams 公钥参数
     * @return this
     */
    public SM2 setPublicKeyParams(ECPublicKeyParameters publicKeyParams) {
        this.publicKeyParams = publicKeyParams;
        return this;
    }

    /**
     * 设置加密类型
     *
     * @param mode {@link SM2Engine.Mode}
     * @return this
     */
    public SM2 setMode(SM2Engine.Mode mode) {
        this.mode = mode;
        this.engine = null;
        return this;
    }

    /**
     * 获取密钥类型对应的加密参数对象{@link CipherParameters}
     *
     * @param keyType Key类型枚举，包括私钥或公钥
     * @return {@link CipherParameters}
     */
    private CipherParameters getCipherParameters(KeyType keyType) {
        switch (keyType) {
            case PublicKey:
                Assert.notNull(this.publicKeyParams, "PublicKey must be not null !");
                return this.publicKeyParams;
            case PrivateKey:
                Assert.notNull(this.privateKeyParams, "PrivateKey must be not null !");
                return this.privateKeyParams;
        }

        return null;
    }

    /**
     * 获取{@link SM2Engine}，此对象为懒加载模式
     *
     * @return {@link SM2Engine}
     */
    private SM2Engine getEngine() {
        if (null == this.engine) {
            this.engine = new SM2Engine(this.digest, this.mode);
        }
        return this.engine;
    }

    /**
     * 获取{@link SM2Signer}，此对象为懒加载模式
     *
     * @return {@link SM2Signer}
     */
    private SM2Signer getSigner() {
        if (null == this.signer) {
            this.signer = new SM2Signer(this.encoding, this.digest);
        }
        return this.signer;
    }

    /**
     * 设置DSA signatures的编码为PlainDSAEncoding
     *
     * @return this
     */
    public SM2 setEncoding() {
        return setEncoding(PlainDSAEncoding.INSTANCE);
    }

    /**
     * 设置DSA signatures的编码
     *
     * @param encoding {@link DSAEncoding}实现
     * @return this
     */
    public SM2 setEncoding(DSAEncoding encoding) {
        this.encoding = encoding;
        this.signer = null;
        return this;
    }

    /**
     * 设置Hash算法
     *
     * @param digest {@link Digest}实现
     * @return this
     */
    public SM2 setDigest(Digest digest) {
        this.digest = digest;
        this.engine = null;
        this.signer = null;
        return this;
    }

}
