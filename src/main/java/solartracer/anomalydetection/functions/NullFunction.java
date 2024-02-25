package solartracer.anomalydetection.functions;

/**
 * Null function, always returns 0.
 */
public class NullFunction extends BaseFunction {
    public NullFunction() {
        super(new Function(), FunctionType.NULL);
    }
}
