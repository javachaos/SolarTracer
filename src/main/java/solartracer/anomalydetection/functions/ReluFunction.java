package solartracer.anomalydetection.functions;

public class ReluFunction extends BaseFunction {
    public ReluFunction() {
        super(new Function(), FunctionType.RELU);
    }

    @Override
    public double activate(double v) {
        if (Double.isNaN(v))
            return 0.0;
        return Math.max(0.0, v);
    }

    @Override
    public double derivative(final double v) {
        if (Double.isNaN(v))
            return 0.0;
        return (v > 0.000000000000000000001) ? 1.0 : 0.0;
    }
}
