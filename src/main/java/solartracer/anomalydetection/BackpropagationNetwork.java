package solartracer.anomalydetection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Backpropagation Network.
 *
 */
public final class BackpropagationNetwork implements Serializable {

    private static final Logger LOGGER =
            LogManager.getLogger(BackpropagationNetwork.class);

    @Serial
    private static final long serialVersionUID = -8666581554984473793L;
    private static final int PARALLEL_THRESHOLD = 100;

    private int numLayers;
    private int inputSize;
    private int[] layerSize;
    private TransferFunctions.TransferFunction[] transferFunction;

    private double[][] layerOutput;
    private double[][] layerInput;
    private double[][] bias;
    private double[][] delta;
    private double[][] previousBiasDelta;
    private double[][][] weight;
    private double[][][] previousWeightDelta;
    private AtomicInteger iterations = new AtomicInteger(0);
    private static final int UPDATE_EVERY = 100;

    /**
     * Create and initialize new back-propagation network.
     *
     * @param layerSizes
     *         the array of layer sizes.
     *
     * @param transferFunctions
     *         the array of transfer functions for each layer.
     */
    public BackpropagationNetwork(final int[] layerSizes,
                                  final TransferFunctions.TransferFunction[] transferFunctions) {
        if (transferFunctions.length != layerSizes.length) {
            throw new IllegalArgumentException("Size mismatch: "+transferFunctions.length+", "+layerSizes.length);
        }
        numLayers = layerSizes.length - 1;
        inputSize = layerSizes[0];
        layerSize = new int[numLayers];

        System.arraycopy(layerSizes, 1, layerSize, 0, numLayers);
        transferFunction = new TransferFunctions.TransferFunction[numLayers];
        System.arraycopy(transferFunctions, 1, transferFunction, 0, numLayers);

        bias = new double[numLayers][];
        previousBiasDelta = new double[numLayers][];
        delta = new double[numLayers][];
        layerOutput = new double[numLayers][];
        layerInput = new double[numLayers][];

        weight = new double[numLayers][][];
        previousWeightDelta = new double[numLayers][][];

        // Fill arrays.
        for (int l = 0; l < numLayers; l++) {
            bias[l] = new double[layerSize[l]];
            previousBiasDelta[l] = new double[layerSize[l]];
            delta[l] = new double[layerSize[l]];
            layerOutput[l] = new double[layerSize[l]];
            layerInput[l] = new double[layerSize[l]];

            weight[l] = new double[getLayer(l)][];
            previousWeightDelta[l] =
                    new double[getLayer(l)][];

            for (int i = 0; i < getLayer(l); i++) {
                weight[l][i] = new double[layerSize[l]];
                previousWeightDelta[l][i] = new double[layerSize[l]];
            }
        }

        // Init weights and bias.
        for (int l = 0; l < numLayers; l++) {
            SplittableRandom rand = new SplittableRandom();
            for (int j = 0; j < layerSize[l]; j++) {
                bias[l][j] = 0.0;
                previousBiasDelta[l][j] = 0.0;
                layerOutput[l][j] = 0.0;
                layerInput[l][j] = 0.0;
                delta[l][j] = 0.0;
            }
            for (int i = 0; i < getLayer(l); i++) {
                for (int j = 0; j < layerSize[l]; j++) {
                    weight[l][i][j] = 0.01 * rand.nextGaussian();
                    previousWeightDelta[l][i][j] = 0.0;
                }
            }
        }
    }

    /**
     * Return the index to the layer.
     * 
     * @param l
     *         the layer index to transform.
     *         
     * @return
     *         the new index to the correct layer.
     */
    private int getLayer(final int l) {
        if (l == 0) {
            return inputSize;
        } else {
            return layerSize[l - 1];
        }
    }

    /**
     * Run the network.
     * 
     * @param input
     *         the input to run.
     *         
     * @return
     *         the result of running the network.
     */
    public double[] run(final double[] input) {
        double[] output;
        if (input.length != inputSize) {
            throw new IllegalArgumentException();
        }
        output = new double[layerSize[numLayers - 1]];
        // Run the network.
        for (int l = 0; l < numLayers; l++) {
            for (int j = 0; j < layerSize[l]; j++) {
                double sum = 0.0;
                for (int i = 0; i < getLayer(l); i++) {
                    sum += weight[l][i][j]
                            * getValue(l, i, input);
                }
                sum += bias[l][j];
                layerInput[l][j] = sum;
                layerOutput[l][j] =
                        TransferFunctions.evaluate(transferFunction[l], sum);
            }
        }
        if (layerSize[numLayers - 1] >= 0)
            System.arraycopy(layerOutput[numLayers - 1], 0, output, 0, layerSize[numLayers - 1]);

        return output;
    }

    /**
     * Get value from the network.
     * 
     * @param l
     *         the layer.
     *         
     * @param i
     *         the index of that layer.
     *         
     * @param input
     *         the input vector.
     *         
     * @return
     *         the value
     */
    private double getValue(final int l,
            final int i, final double[] input) {
        if (l == 0) {
            return input[i];
        } else {
            return layerOutput[l - 1][i];
        }
    }

    /**
     * Train on a vector of training samples.
     * 
     * @param trainingData
     *         the vector of training samples.
     *         
     * @param desiredData
     *         the vector of desired training samples.
     *         
     * @param trainingRate
     *         the training rate.
     *
     * @param desiredError
     *         the desired network error.
     *         
     * @return
     *         the final network error.
     */
    public double train(final double[][] trainingData,
            final double[][] desiredData,
            final double trainingRate,
            final double desiredError) {
        double[] error = new double[1];
        double totalError = Double.MAX_VALUE;
        for (int i = 0; i < trainingData.length; i++) {
            do {
                error[0] = train(trainingData[i],
                        desiredData[i], trainingRate, trainingRate * 1.618);
                run(trainingData[i]);
                totalError = error[0] / trainingData.length;
            } while (totalError > desiredError);
        }
        return totalError;
    }

    /**
     * Train the neural network.
     * 
     * @param input
     *         the input to train.
     *         
     * @param desired
     *         the desired output.
     *         
     * @param trainingRate
     *         the trainingRate
     *         
     * @param momentum
     *         the momentum
     *         
     * @return
     *         the mse of the network after training.
     */
    public double train(final double[] input,
            final double[] desired, final double trainingRate,
            final double momentum) {
        if (input.length != inputSize
        ||  desired.length != layerSize[numLayers - 1]) {
            LOGGER.error("Input dims: expected({})  actual({})", inputSize, input.length);
            LOGGER.error("Desired dims: expected({})  actual({})", layerSize[numLayers - 1], desired.length);
            throw new IllegalArgumentException();
        }
        double error = 0.0;
        double[] output = run(input);
        error = backProp(output, desired, delta, error);
        updateWeights(trainingRate, momentum, input);
        updateBiases(trainingRate, momentum);

        if (iterations.getAndIncrement() % UPDATE_EVERY  == 0) {
            prettyPrint(input, output, error, iterations.get());
        }
        return error;
    }

    private void updateBiases(double trainingRate, double momentum) {
        double biasDelta;
        for (int l = 0; l < numLayers; l++) {
            for (int i = 0; i < layerSize[l]; i++) {
                biasDelta = trainingRate * delta[l][i];
                bias[l][i] -= biasDelta + momentum * previousBiasDelta[l][i];
                previousBiasDelta[l][i] = biasDelta;
            }
        }
    }

    private void updateWeights(double trainingRate, double momentum, double[] input) {
        double weightDelta;
        // Update the weights and biases
        for (int l = 0; l < numLayers; l++) {
            for (int i = 0; i < getLayer(l); i++) {
                for (int j = 0; j < layerSize[l]; j++) {
                    weightDelta =
                            trainingRate * delta[l][j]
                                    * getValue(l, i, input);
                    weight[l][i][j] -=
                            weightDelta + momentum * previousWeightDelta[l][i][j];
                    previousWeightDelta[l][i][j] = weightDelta;
                }
            }
        }
    }

    private double backProp(double[] output, double[] desired, double[][] delta, double error) {
        double[] sum = new double[1];
        double errorCopy = error;
        // Back-propagation pass.
        for (int l = numLayers - 1; l >= 0; l--) {
            // Output layer
            if (l == numLayers - 1) {
                for (int k = 0; k < layerSize[l]; k++) {
                    delta[l][k] = output[k] - desired[k];
                    errorCopy += Math.pow(delta[l][k], 2);
                    delta[l][k] *= TransferFunctions.evaluateDerivative(
                            transferFunction[l], layerInput[l][k]);
                }
            } else { // Hidden Layer
                if (layerSize[l] > PARALLEL_THRESHOLD) {// parallel
                    int finalL = l;
                    IntStream.range(0, layerSize[l]).parallel().forEach(i -> {
                        sum[0] = 0.0;
                        for (int j = 0; j < layerSize[finalL + 1]; j++) {
                            sum[0] += weight[finalL + 1][i][j] * delta[finalL + 1][j];
                        }
                        sum[0] += TransferFunctions.evaluateDerivative(
                                transferFunction[finalL], layerInput[finalL][i]);
                        delta[finalL][i] = sum[0];
                    });
                } else {// sequential
                    for (int i = 0; i < layerSize[l]; i++) {
                        sum[0] = 0.0;
                        for (int j = 0; j < layerSize[l + 1]; j++) {
                            sum[0] += weight[l + 1][i][j] * delta[l + 1][j];
                        }
                        sum[0] += TransferFunctions.evaluateDerivative(
                                transferFunction[l], layerInput[l][i]);
                        delta[l][i] = sum[0];
                    }
                }
            }
        }
        return errorCopy;
    }

    /**
     * Clear the iteration counter.
     */
    public void clearIterationCounter() {
        iterations.set(0);
    }

    /**
     * Print results.
     *
     * @param input
     *         the input to be printed.
     *         
     * @param output
     *         the output to be printed.
     *         
     * @param error
     *         the error value.
     *         
     * @param i
     *         the iteration number.
     *         
     */
    private void prettyPrint(final double[] input,
            final double[] output,
            final double error,
            final int i) {
        StringBuilder sb = new StringBuilder();
        StringBuilder ssb = new StringBuilder();
        for (double d : input) {
            ssb.append("[").append(d).append("] ");
        }
        for (double d : output) {
            sb.append("[").append(d).append("] ");
        }
        LOGGER.info(
                "Iteration {}:\n\t\tInput {}\n\t\tOutput {}\n\t\tError {}",
                i, ssb, sb, error / input.length);
    }

    /**
     * Save the neural network to file.
     *
     * @param fileName
     *         the file path to store the network as.
     *
     */
    public void saveNet(final String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Load the neural network from file and
     * update all references in the current instance
     * of the class.
     *
     * @param fileName
     *         the filename of the file to load.
     *         
     * @return
     *         the reference to the loaded network.
     *
     */
    public BackpropagationNetwork loadNet(final String fileName) {
        BackpropagationNetwork net = null;
        try(FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn)) {
            net = (BackpropagationNetwork) in.readObject();
            this.bias = net.bias;
            this.delta = net.delta;
            this.inputSize = net.inputSize;
            this.iterations = net.iterations;
            this.layerInput = net.layerInput;
            this.layerOutput = net.layerOutput;
            this.layerSize = net.layerSize;
            this.numLayers = net.numLayers;
            this.previousBiasDelta = net.previousBiasDelta;
            this.previousWeightDelta = net.previousWeightDelta;
            this.weight = net.weight;
            this.transferFunction = net.transferFunction;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        return net;
    }

    /**
     * Load the neural network from file and
     * update all references in the current instance
     * of the class.
     *
     * @param fileName
     *         the filename of the file to load.
     *
     * @return
     *         the reference to the loaded network.
     *
     */
    public BackpropagationNetwork loadNet(final InputStream fileIn) throws IOException, ClassNotFoundException {
        BackpropagationNetwork net = null;
        ObjectInputStream in = new ObjectInputStream(fileIn);
        net = (BackpropagationNetwork) in.readObject();
        this.bias = net.bias;
        this.delta = net.delta;
        this.inputSize = net.inputSize;
        this.iterations = net.iterations;
        this.layerInput = net.layerInput;
        this.layerOutput = net.layerOutput;
        this.layerSize = net.layerSize;
        this.numLayers = net.numLayers;
        this.previousBiasDelta = net.previousBiasDelta;
        this.previousWeightDelta = net.previousWeightDelta;
        this.weight = net.weight;
        this.transferFunction = net.transferFunction;
        return net;
    }
}
