package it.feio.android.omninotes.export;

/**
 * This is the only access point to the exporter for the rest of the application.
 */
public class ExporterFactory {
    public static Exporter createTextExporter() {
        return new TextExporter();
    }
    public static Exporter createHtmlExporter() {
        return new HtmlExporter();
    }
    public static Exporter createPdfExporter() {
        return new PdfExporter();
    }
}
