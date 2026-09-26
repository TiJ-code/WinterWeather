package dk.tij.winterweather.client;

public final class HeatStatePayloadState {
    private static double actualFreezeTicks;
    private static float visualFrom;
    private static float visualTo;
    private static long visualTransitionStartedNanos;
    private static final long VISUAL_TRANSITION_NANOS = 50_000_000L;
    private static boolean serverEnabled;
    private static boolean debugEnabled;
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

    static void updateDebug(boolean enabled) {
        debugEnabled = enabled;
    }

    static boolean debugEnabled() {
        return debugEnabled;
    }

    static void updateVisualProgress(float value) {
        long now = System.nanoTime();
        visualFrom = interpolatedVisualProgress(now);
        visualTo = Math.clamp(value, 0, 1);
        visualTransitionStartedNanos = now;
    }

    public static float visualProgress() {
        return interpolatedVisualProgress(System.nanoTime());
    }

    private static float interpolatedVisualProgress(long now) {
        float amount = Math.clamp((float) (now - visualTransitionStartedNanos)
                / VISUAL_TRANSITION_NANOS, 0, 1);
        return visualFrom + (visualTo - visualFrom) * amount;
    }

    static void reset() {
        actualFreezeTicks = 0;
        visualFrom = 0;
        visualTo = 0;
        visualTransitionStartedNanos = System.nanoTime();
        serverEnabled = false;
        debugEnabled = false;
        initialized = false;
    }

    public static boolean serverEnabled() {
        return serverEnabled;
    }

    public static boolean initialized() {
        return initialized;
    }
}
