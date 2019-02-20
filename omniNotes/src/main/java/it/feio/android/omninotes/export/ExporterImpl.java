package it.feio.android.omninotes.export;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import it.feio.android.omninotes.models.Note;

/**
 * Handles the translation of the note to the document structure used when exporting.
 * @param <T>
 */
class ExporterImpl<T> implements Exporter {
    final DocExporter<T> docExporter;


    public ExporterImpl(DocExporter<T> docExporter) {
        this.docExporter = docExporter;
    }

    @Override
    public void export(Note note, OutputStream os) {
        Log.d("export_tag", "Exporting note" + note.getTitle());
        try {
            os.write("Exported note".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

      /*  // Steps to export a note:
        // 1. Translate note to nodes
        // 2. Call docExporter.create
        // 3. Call docExporter.write

        //  1. Translate note to nodes, example code:
        ElementFactory<T> elemFact = docExporter.getElementFactory();

        // Document root
        ArrayList<Node<T>> docChildren = new ArrayList<>();
        Node doc = new Node<>(elemFact.document(), null, docChildren);

        // All the note nodes
        docChildren.add(new Node<T>(elemFact.title("test", "#ffff00", "category"), doc, null));
        docChildren.add(new Node<T>(elemFact.content("A string containing the note content"), doc, null));

        // TODO: Implement the rest of the translation part


        // 2. Create resulting document
        docExporter.create(doc);


        // 3. Write document to storage
        docExporter.write(os);*/
    }
}
