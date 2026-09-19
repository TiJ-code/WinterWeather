package dk.tij.winterweather.client;

public final class HeatStatePayloadState {
    private static double actualFreezeTicks;
    private static boolean serverEnabled;
    private static boolean initialized;

    private HeatStatePayloadState() {
    }

    static double actualFreezeTicks() {
        return actualFreezeTicks;
    }

    static void updateFromServer(boolean enabled, double value) {
        serverEnabled = enabled;
        actualFreezeTicks = value;
        initialized = true;
    }

    static void update(double value) {
        actualFreezeTicks = value;
    }

    static void reset() {
        actualFreezeTicks = 0;
        serverEnabled = false;
        initialized = false;
    }

    public static boolean serverEnabled() {
        return serverEnabled;
    }

    public static boolean initialized() {
        return initialized;
    }
}
