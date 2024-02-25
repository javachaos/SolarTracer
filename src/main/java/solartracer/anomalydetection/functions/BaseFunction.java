package solartracer.anomalydetection.functions;

public abstract class BaseFunction implements ActivationFunction {

    private final ActivationFunction function;
    private final FunctionType type;

    protected BaseFunction(final ActivationFunction function, final FunctionType type) {
        this.function = function;
        this.type = type;
    }

    public FunctionType getType() {
        return type;
    }

    @Override
    public double activate(final double v) {
        return function.activate(v);
    }

    @Override
    public double derivative(final double v) {
        return function.derivative(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseFunction that = (BaseFunction) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
