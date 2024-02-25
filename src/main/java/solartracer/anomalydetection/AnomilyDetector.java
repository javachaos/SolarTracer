package solartracer.anomalydetection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.anomalydetection.csv.CSVParser;
import solartracer.utils.Constants;
import solartracer.utils.MathUtils;

import java.io.IOException;
import java.util.Objects;

public class AnomilyDetector {
    double desiredTrainError = 0.001;
    double inferenceThreshold = 25.0;

    /** Logger. */
    public static final Logger LOGGER = LogManager.getLogger(AnomilyDetector.class);

    private boolean ready;
    private boolean train;

    private BackpropagationNetwork bpn;
    public AnomilyDetector(boolean train) {
        this.train = train;
        if (!train) {
            bpn = create();
            try {
                bpn = bpn.loadNet(getClass().getResourceAsStream("/aidata/main.json"));
                ready = true;
            } catch (Exception ignored) {
            }
        }
    }

    public boolean inference(String dataPoint) {
        if (ready && !train) {
            String[] data = dataPoint.split(":");
            double[] inputData = new double[Constants.DEFAULT_DATA_LENGTH - 1];
            if (data.length != inputData.length) {
                return false;
            }
            for (int i = 0; i < inputData.length; i++) {
                inputData[i] = Double.parseDouble(data[i]);
            }
            double[] norm = MathUtils.getNormalized(inputData);
            double[] outputData = bpn.run(norm);
            double rmse = MathUtils.rmse(inputData, outputData);
            return rmse < inferenceThreshold;
        } else {
            return false;
        }
    }

    public void train() {
        create();
        double learnRate = 0.061803398875;
        double[][] data;
        try {
            data = CSVParser.parseCSV(Objects.requireNonNull(
                    getClass().getResourceAsStream("/aidata/clean_solar_data.csv")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bpn.train(data, data, learnRate, desiredTrainError);

        bpn.saveNet(System.getProperty("user.home")+"/Documents/main.json");
    }

    public BackpropagationNetwork create() {
        int[] layerSizes = new int[] { 10, 15, 12, 10, 6, 4, 6, 10, 12, 15, 10 };//autoencoder
        TransferFunctions.TransferFunction[] tFuncs = new TransferFunctions.TransferFunction[] {
                TransferFunctions.TransferFunction.NONE,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.SIGMOID,
                TransferFunctions.TransferFunction.SIGMOID,
                TransferFunctions.TransferFunction.SIGMOID,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.SIGMOID};
        bpn = new BackpropagationNetwork(layerSizes, tFuncs);
        return bpn;
    }


}
