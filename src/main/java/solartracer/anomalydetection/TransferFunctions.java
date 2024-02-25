package solartracer.anomalydetection;

import solartracer.anomalydetection.functions.ReluFunction;
import solartracer.anomalydetection.functions.SigmoidFunction;

/**
 * Transfer functions.
 *
 */
public class TransferFunctions {
    public enum TransferFunction {
        NONE,
        SIGMOID,
        RELU
    }

    private TransferFunctions() {}

    /**
     * Evaluate the transfer function transferFunction.
     * @param transferFunction
     *         the transfer function to use.
     *
     * @param input
     *         the input data.
     *
     * @return
     *         the result of applying the
     *         transfer function on the data.
     */
    public static double evaluate(
            final TransferFunction transferFunction,
            final double input) {
        return switch (transferFunction) {
            case SIGMOID -> new SigmoidFunction().activate(input);
            case RELU -> new ReluFunction().activate(input);
            case NONE -> input;
        };
    }

    /**
     * Evaluate the derivative of the transfer function.
     * @param transferFunction
     *         the transfer function to use.
     *
     * @param input
     *         the input data.
     *
     * @return
     *         the derivative of the transfer function.
     */
    public static double evaluateDerivative(
            final TransferFunction transferFunction,
            final double input) {
        switch (transferFunction) {
            case SIGMOID -> {
                return new SigmoidFunction().derivative(input);
            }
            case RELU -> {
                return new ReluFunction().derivative(input);
            }
            case NONE -> {
                return input;
            }
        }
        return input;
    }
}
