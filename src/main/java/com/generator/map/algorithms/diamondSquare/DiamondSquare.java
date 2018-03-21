package com.generator.map.algorithms.diamondSquare;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public final class DiamondSquare {

    public static void main(String... args) throws IOException{


        double [] corners = DoubleStream.generate(Math::random).limit(4).toArray();


        /**
         * Some nice noises:
         * 1.       .4   .8  Ideal for large terrains with many different biomes
         * 2.       .2   .4  Generates a single biome in nice detail
         * 3.       .1   .4
         * 4.       .6   .8  More exacerbated version of 1
         * 5.       .3   .6  A nice balance between 1 and 2
          */

        int exp = 10;
        Noise noise = new NaiveNoise(.4,.8);
        printMapToImg(normalize(new ColorPallet(ColorPallet.PALLET_1),smoothen(diamondSquare(corners, exp, noise,.6f),20)));
    }


    private static void debug(){
        int exp = 2;
        double[] corners1 = {20, 30, 40, 50};
        Noise zero = new ZeroNoise();
        printMapDebug(diamondSquare(corners1, exp, zero,0), exp);
    }


    private DiamondSquare(){}



    public static double[][] diamondSquare(double[] corners, int n, Noise noise, double p) {
        Objects.requireNonNull(corners);
        if (corners.length != 4)
            throw new IllegalArgumentException();

        int dim = (int) Math.pow(2, n) + 1;
        double[][] map = initializeMap(corners, dim);

        int step = (dim - 1) / 2;
        while (step > 0) {
            for (int x = step; x < dim - 1; x += 2 * step) {
                for (int y = step; y < dim - 1; y += 2 * step) {

                    boolean b = Math.random() < p;


                    map[x][y] = diamondStep(x, y, step, map);

                    double noi = 0;
                    if(b) {
                        noi = noise.getNext(map[x][y]);
                        noise.reduce();
                    }

                    map[x][y] += noi;

                    //square steps

                    map[x - step][y] = squareStep(x - step, y, step, map, dim) ;
                    map[x + step][y] = squareStep(x + step, y, step, map, dim) ;
                    map[x][y - step] = squareStep(x, y - step, step, map, dim) ;
                    map[x][y + step] = squareStep(x, y + step, step, map, dim) ;


                    if(b) {
                        map[x - step][y] += noise.getNext(map[x - step][y]);
                        map[x + step][y] += noise.getNext(map[x + step][y]);
                        map[x][y - step] += noise.getNext(map[x][y - step]);
                        map[x][y + step] += noise.getNext(map[x][y + step]);
                        noise.reduce();
                    }

                }
            }
            step /= 2;

        }
        return map;
    }


    private static void printMapToImg(int map[][]) throws IOException{
        int dim = map.length;

        BufferedImage b = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_RGB);

        for(int x=0; x<dim; x++){
            for (int y=0; y<dim; y++){
                b.setRGB(x, y, map[x][y]);
            }
        }
        ImageIO.write(b, "jpg", new File("Heightmap.jpg"));
    }

    private static int[][] normalize(ColorPallet pallet, double[][] map){
        int dim = map.length;

        int[][] ans = new int[dim][dim];
        double max = getMax(map, dim);
        for(int x=0; x<dim; x++){
            for (int y=0; y<dim; y++){
                map[x][y] /= max;
                ans[x][y] = 0x00FFFFFF & pallet.getColor(map[x][y]);
            }
        }
        return ans;
    }

    private static double getMax(double map[][], int dim){
        double tmp = Double.MIN_VALUE;
        for(int x=0; x<dim; x++){
            for (int y=0; y<dim; y++){
                if (map[x][y]>tmp)
                    tmp = map[x][y];
            }
        }
        return tmp;
    }

    private static void printMapDebug(double map[][], int exp){

        int dim = (int)Math.pow(2, exp) + 1;

        for(int x=0; x<dim; x++){
            for (int y=0; y<dim; y++){
                System.out.print(map[x][y] + " ");
            }
            System.out.println("");
        }
    }

    private static double diamondStep(int x, int y, int step, double map[][]){
        return (map[x-step][y-step]+map[x-step][y+step]+
                map[x+step][y-step]+map[x+step][y+step])
                /4.0f;
    }

    private static Optional<Double> ifWithinBoundariesInclude(int x, int y, double map[][], int dim){
        if(x<0) {
            x = dim + x;
        }else if(x>=dim) {
            x = x - dim;
        }
        if(y<0) {
            y = dim + y;
        } else if(y>=dim) {
            y = y - dim;
        }
        return Optional.of(map[x][y]);
    }

    private static boolean isWithinBoundaries(int dim, int x, int y){
        return x >= 0 && x < dim && y >= 0 && y < dim;
    }

    private static Optional<Double> ifWithinBoundariesExclude(int x, int y, double map[][], int dim){
        if(!isWithinBoundaries(dim,x,y))
            return Optional.empty();
        return Optional.of(map[x][y]);
    }


    private static double squareStep(int x, int y, int step, double map[][], int dim){
        return Stream.of(ifWithinBoundariesExclude(x,y-step,map,dim),
                ifWithinBoundariesExclude(x,y+step,map,dim),
                ifWithinBoundariesExclude(x-step,y,map,dim),
                ifWithinBoundariesExclude(x+step,y,map,dim))
                .filter(Optional::isPresent)
                .collect(Collectors.averagingDouble(Optional::get));
    }


    private static double[][] initializeMap(double[] corners, int dim){
        double[][] ans = new double[dim][dim];
        ans[0][0] = corners[0];
        ans[0][dim-1] = corners[1];
        ans[dim-1][0] = corners[2];
        ans[dim-1][dim-1] = corners[3];
        return ans;
    }

    private static double[][] smoothen(double[][] map, int times){
        int dim = map.length;
        while (times > 0) {
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    map[i][j] = smoothenPos(map, i, j, dim);
                }
            }
            times--;
        }
        return map;
    }

    private static double smoothenPos(double[][] map, int x, int y, int dim){
        double ans = 0;
        double count = 0;
        for(int i = x-1; i<=x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (isWithinBoundaries(dim, i, j)) {
                    count++;
                    ans += map[i][j];
                }
            }
        }
        return ans / count;
    }

}