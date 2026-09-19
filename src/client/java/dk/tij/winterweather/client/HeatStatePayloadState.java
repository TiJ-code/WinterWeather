package dk.tij.winterweather.client;

final class HeatStatePayloadState {
    private static double actualFreezeTicks;
    private static boolean initialized;

    private HeatStatePayloadState() {
    }

    static double actualFreezeTicks() {
        return actualFreezeTicks;
    }

    static void update(double value) {
        actualFreezeTicks = value;
        initialized = true;
    }

    static void reset() {
        actualFreezeTicks = 0;
        initialized = false;
    }

    static boolean initialized() {
        return initialized;
    }
}
