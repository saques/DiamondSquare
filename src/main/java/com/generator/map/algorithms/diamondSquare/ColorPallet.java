package com.generator.map.algorithms.diamondSquare;

public class ColorPallet {

    public static final int [] PALLET_1 = {0x4575b4, 0x74add1, 0xabd9e9, 0xe0f3f8, 0xffffbf, 0xfee090, 0xfdae61, 0xf46d43, 0xd73027};
    public static final int [] PALLET_2 = {0x543005,0x8c510a,0xbf812d,0xdfc27d,0xf6e8c3,0xf5f5f5,0xc7eae5,0x80cdc1,0x35978f, 0x01665e,0x003c30};

    public int[] scale;
    public int n;

    public ColorPallet(int[] scale){
        this.scale = scale;
        this.n = scale.length;
    }

    /**
     * Expects val to be in [0,1]
     * @param val
     * @return
     */
    public int getColor(double val){
        int i = (int)Math.floor(n*val);
        i = i>=n? i-1:i;
        return scale[i];
    }
}
