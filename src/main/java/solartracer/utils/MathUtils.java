package solartracer.utils;

import java.util.Arrays;

public class MathUtils {

    public static double rmse(double[] input, double[] output) {
        if (input.length != output.length || input.length == 0) {
            throw new IllegalArgumentException("Input and output arrays must be of the same non-zero length");
        }

        double sumSquaredErrors = 0.0;
        for (int i = 0; i < input.length; i++) {
            double error = input[i] - output[i];
            sumSquaredErrors += error * error;
        }

        double meanSquaredError = sumSquaredErrors / input.length;
        return Math.sqrt(meanSquaredError);
    }

    public static double[] getNormalized(double[] data) {
        double[] normal = new double[data.length];
        double min = Arrays.stream(data).min().orElseThrow();
        double max = Arrays.stream(data).max().orElseThrow();
        for (int j = 0; j < normal.length; j++) {
            normal[j] = (data[j] - min) / (max - min);
        }
        return normal;
    }
}
