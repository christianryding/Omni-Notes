package it.feio.android.omninotes.export;

/**
 * This class should be used by the application if an exporter instance is needed.
 */
public class ExporterFactory {

    private ExporterFactory() {
    }

    /**
     * Creates an exporter that exports to a text file.
     * @return exporter instance
     * @see TextExporter
     */
    public static Exporter createTextExporter() {
        return new TextExporter();
    }

    /**
     * Creates an exporter that exports to a HTML file.
     * @return exporter instance
     * @see HtmlExporter
     */
    public static Exporter createHtmlExporter() {
        return new HtmlExporter();
    }

    /**
     * Creates an exporter that exports to a PDF file.
     * @return exporter instance
     * @see PdfExporter
     */
    public static Exporter createPdfExporter() {
        return new PdfExporter();
    }
}
