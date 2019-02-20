package it.feio.android.omninotes.export;

/**
 * This is the only access point to the exporter for the rest of the application.
 */
public class ExporterFactory {
    public Exporter createTextExporter() {
        return new ExporterImpl<>(new TextExporter());
    }
    public Exporter createHtmlExporter() {
        return new ExporterImpl<>(new HtmlExporter());
    }
    public Exporter createPdfExporter() {
        return new ExporterImpl<>(new PdfExporter());
    }
}
