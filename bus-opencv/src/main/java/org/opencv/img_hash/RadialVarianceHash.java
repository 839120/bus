//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.img_hash;

// C++: class RadialVarianceHash

/**
 * Image hash based on Radon transform.
 * <p>
 * See CITE: tang2012perceptual for details.
 */
public class RadialVarianceHash extends ImgHashBase {

    protected RadialVarianceHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static RadialVarianceHash __fromPtr__(long addr) {
        return new RadialVarianceHash(addr);
    }

    //
    // C++: static Ptr_RadialVarianceHash cv::img_hash::RadialVarianceHash::create(double sigma = 1, int numOfAngleLine = 180)
    //

    public static RadialVarianceHash create(double sigma, int numOfAngleLine) {
        return RadialVarianceHash.__fromPtr__(create_0(sigma, numOfAngleLine));
    }

    public static RadialVarianceHash create(double sigma) {
        return RadialVarianceHash.__fromPtr__(create_1(sigma));
    }

    public static RadialVarianceHash create() {
        return RadialVarianceHash.__fromPtr__(create_2());
    }


    //
    // C++:  double cv::img_hash::RadialVarianceHash::getSigma()
    //

    // C++: static Ptr_RadialVarianceHash cv::img_hash::RadialVarianceHash::create(double sigma = 1, int numOfAngleLine = 180)
    private static native long create_0(double sigma, int numOfAngleLine);


    //
    // C++:  int cv::img_hash::RadialVarianceHash::getNumOfAngleLine()
    //

    private static native long create_1(double sigma);


    //
    // C++:  void cv::img_hash::RadialVarianceHash::setNumOfAngleLine(int value)
    //

    private static native long create_2();


    //
    // C++:  void cv::img_hash::RadialVarianceHash::setSigma(double value)
    //

    // C++:  double cv::img_hash::RadialVarianceHash::getSigma()
    private static native double getSigma_0(long nativeObj);

    // C++:  int cv::img_hash::RadialVarianceHash::getNumOfAngleLine()
    private static native int getNumOfAngleLine_0(long nativeObj);

    // C++:  void cv::img_hash::RadialVarianceHash::setNumOfAngleLine(int value)
    private static native void setNumOfAngleLine_0(long nativeObj, int value);

    // C++:  void cv::img_hash::RadialVarianceHash::setSigma(double value)
    private static native void setSigma_0(long nativeObj, double value);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    public double getSigma() {
        return getSigma_0(nativeObj);
    }

    public void setSigma(double value) {
        setSigma_0(nativeObj, value);
    }

    public int getNumOfAngleLine() {
        return getNumOfAngleLine_0(nativeObj);
    }

    public void setNumOfAngleLine(int value) {
        setNumOfAngleLine_0(nativeObj, value);
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
