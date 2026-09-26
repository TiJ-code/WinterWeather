package dk.tij.winterweather.server.config;

/**
 * Provides config parse exception functionality for Winter Weather.
 */
public class ConfigParseException extends RuntimeException {
    /**
     * Performs the config parse exception operation.
     *
     * @param message the message value
     */
    public ConfigParseException(String message) {
        super(message);
    }

    /**
     * Performs the config parse exception operation.
     *
     * @param message the message value
     * @param cause   the cause value
     */
    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
