package solartracer.anomalydetection.csv;

import static solartracer.utils.MathUtils.getNormalized;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CSVParser {

    public static double[][] parseCSV(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        String csvContent = baos.toString(StandardCharsets.UTF_8);
        String[] lines = csvContent.split("\r\n|\r|\n");

        // Count number of lines to determine array size
        int numRows = lines.length;

        double[][] data = new double[numRows][];
        int row = 0;
        for (String line : lines) {
            String[] values = line.split(",");
            double[] rowValues = new double[values.length - 2];
            for (int i = 1; i < values.length - 1; i++) {
                rowValues[i - 1] = Double.parseDouble(values[i]);
            }
            data[row++] = getNormalized(rowValues);
        }

        return data;
    }

}
