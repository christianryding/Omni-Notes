package it.feio.android.omninotes.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class HtmlExporter extends ExporterBase {
    @Override
    protected void createDocument(NoteFacade facade) throws ExporterException {

    }

    @Override
    protected void writeDocument(OutputStream os) throws ExporterException {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
             PrintWriter pw = new PrintWriter(osw))
        {
            pw.write("HTML document");
        } catch (IOException e) {
            throw new ExporterException("IO error when writing to file.");
        }
    }
}
