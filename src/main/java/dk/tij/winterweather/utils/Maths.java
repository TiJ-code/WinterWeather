package dk.tij.winterweather.utils;

public final class Maths {
    public static double smoothstep(double value) {
        return value * value * (3 - 2 * value);
    }

    public static double smootherstep(double value) {
        return value * value * value * (value * (value * 6 - 15) + 10);
    }

    private Maths() {}
}
