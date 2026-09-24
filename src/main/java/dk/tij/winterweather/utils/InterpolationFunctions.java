package dk.tij.winterweather.utils;

import java.util.function.Function;

public enum InterpolationFunctions {
    LINEAR("linear", value -> value),
    SMOOTHSTEP("smoothstep", value -> value * value * (3f - 2f * value)),
    SMOOTHERSTEP("smootherstep", value -> value * value * value * (value * (value * 6 - 15) + 10));

    private final String name;
    private final Function<Double, Double> function;

    InterpolationFunctions(String name, Function<Double, Double> function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public Function<Double, Double> getFunction() {
        return function;
    }

    public double apply(double value) {
        return function.apply(value);
    }

    public static InterpolationFunctions by(String name) {
        for (InterpolationFunctions func : InterpolationFunctions.values()) {
            if (func.getName().equals(name)) {
                return func;
            }
        }
        return SMOOTHERSTEP;
    }
}
