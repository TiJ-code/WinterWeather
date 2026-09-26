package dk.tij.winterweather.utils;

import java.util.function.Function;

/**
 * Available interpolation functions values.
 */
public enum InterpolationFunctions {
    LINEAR("linear", value -> value),
    SMOOTHSTEP("smoothstep", value -> value * value * (3f - 2f * value)),
    SMOOTHERSTEP("smootherstep", value -> value * value * value * (value * (value * 6 - 15) + 10));
    /**
     * Stores the name value.
     */
    private final String name;
    /**
     * Stores the function value.
     */
    private final Function<Double, Double> function;

    InterpolationFunctions(String name, Function<Double, Double> function) {
        this.name = name;
        this.function = function;
    }

    /**
     * Performs the by operation.
     *
     * @param name the name value
     */
    public static InterpolationFunctions by(String name) {
        for (InterpolationFunctions func : InterpolationFunctions.values()) {
            if (func.getName().equals(name)) {
                return func;
            }
        }
        return SMOOTHERSTEP;
    }

    /**
     * Returns the name value.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the function value.
     */
    public Function<Double, Double> getFunction() {
        return function;
    }

    /**
     * Performs the apply operation.
     *
     * @param value the value value
     */
    public double apply(double value) {
        return function.apply(value);
    }
}
