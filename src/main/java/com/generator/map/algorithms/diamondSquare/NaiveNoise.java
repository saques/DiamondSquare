package com.generator.map.algorithms.diamondSquare;

import java.util.Random;
import java.util.stream.DoubleStream;

public class NaiveNoise implements Noise {

    private Random random;
    private double reductionFactor, v;

    public NaiveNoise(double init, double reductionFactor){
        random = new Random(System.currentTimeMillis());
        this.reductionFactor = reductionFactor;
        this.v = init;
    }

    @Override
    public double getNext(double v) {
        return v*random.nextDouble()*this.v;
    }

    @Override
    public void reduce() {
        if(v*reductionFactor>=1)
            v*=reductionFactor;
    }
}