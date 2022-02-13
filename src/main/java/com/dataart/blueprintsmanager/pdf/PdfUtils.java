package com.dataart.blueprintsmanager.pdf;

public class PdfUtils {
    public static float millimetersToPoints(float valueInMillimeters) {
        return (float) (valueInMillimeters * 2.834645669291339);
    }
    public static float getFieldAreaCorrection(){
        return 0.32F;
    }
}
