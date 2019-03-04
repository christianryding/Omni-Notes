package it.feio.android.omninotes.export;

import android.content.Context;

import java.io.OutputStream;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Note;

abstract class ExporterBase implements Exporter {
    private Note note;
    private Context context;

    final public void export(Note note, OutputStream os) throws ExporterException {
        this.note = note;
        this.context = OmniNotes.getAppContext();

        final NoteFacade facade = new NoteFacade(note);

        try {
            createDocument(facade);
        } catch (ExporterException e) {
            throw new ExporterException("Exporter failed to create document: " + e.getMessage());
        }

        try {
            writeDocument(os);
        } catch (ExporterException e) {
            throw new ExporterException("Exporter failed when writing document: " + e.getMessage());
        }
    }

    final protected Context getContext() {
        return context;
    }

    abstract protected void createDocument(NoteFacade facade) throws ExporterException;
    abstract protected void writeDocument(OutputStream os) throws ExporterException;
}
