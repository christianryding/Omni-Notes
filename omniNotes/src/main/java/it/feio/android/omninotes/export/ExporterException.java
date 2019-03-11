package it.feio.android.omninotes.export;

/**
 * Used be the exporter to report errors, that occurs during export, to the caller.
 */
public class ExporterException extends RuntimeException {
    /**
     *
     * @param message describes what the problem is.
     */
    public ExporterException(String message) {
        super(message);
    }
}
