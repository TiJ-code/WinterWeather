package dk.tij.winterweather.client;


/**
 * Represents the current heat state payload state.
 */
public final class HeatStatePayloadState {
    private static final long VISUAL_TRANSITION_NANOS = 50_000_000L;
    /**
     * Stores the actual freeze ticks value.
     */
    private static double actualFreezeTicks;
    /**
     * Stores the visual from value.
     */
    private static float visualFrom;
    /**
     * Stores the visual to value.
     */
    private static float visualTo;
    /**
     * Stores the visual transition started nanos value.
     */
    private static long visualTransitionStartedNanos;
    /**
     * Stores the server enabled value.
     */
    private static boolean serverEnabled;
    /**
     * Stores the debug enabled value.
     */
    private static boolean debugEnabled;
    /**
     * Stores the initialized value.
     */
    private static boolean initialized;

    /**
     * Performs the heat state payload state operation.
     */
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

    /**
     * Performs the visual progress operation.
     */
    public static float visualProgress() {
        return interpolatedVisualProgress(System.nanoTime());
    }

    /**
     * Performs the interpolated visual progress operation.
     *
     * @param now the now value
     */
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

    /**
     * Performs the server enabled operation.
     */
    public static boolean serverEnabled() {
        return serverEnabled;
    }

    /**
     * Performs the initialized operation.
     */
    public static boolean initialized() {
        return initialized;
    }
}
