package dk.tij.winterweather.client;

final class HeatStatePayloadState {
    private static boolean nearHeatSource;
    private static boolean initialized;

    private HeatStatePayloadState() {
    }

    static boolean matches(boolean value) {
        return initialized && nearHeatSource == value;
    }

    static void update(boolean value) {
        nearHeatSource = value;
        initialized = true;
    }

    static void reset() {
        initialized = false;
    }
}
