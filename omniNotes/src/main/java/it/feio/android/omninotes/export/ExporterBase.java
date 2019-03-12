package it.feio.android.omninotes.export;

import android.content.Context;

import java.io.OutputStream;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Note;

/**
 * Implements things common to all exporters. Exporter should extend from this class.
 */
abstract class ExporterBase implements Exporter {
    /**
     * Context used during export
     */
    private Context context;

    /**
     * @param note  the note to be exported.
     * @param os    outputstream to be used.
     * @throws ExporterException
     */
    public final void export(Note note, OutputStream os) {
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

    /**
     * Used by exporters that needs a Context.
     * @return current context.
     */
    protected final Context getContext() {
        return context;
    }

    /**
     * Constructs a document containing all information in the note. This document will than be
     * written to a file in <code>writeDocument</code>, which is called after this method returns.
     *
     * @param facade gives access to the information in a note.
     * @throws ExporterException if a document couldn't be created, in that case, writeDocument
     * will note be called.
     */
    protected abstract void createDocument(NoteFacade facade);

    /**
     * Handles the writing of the document to the <code>OutputStream</code>. Is called after
     * <code>createDocument</code>.
     *
     * @param os the document will be written to this.
     * @throws ExporterException if an error occurs during export.
     */
    protected abstract void writeDocument(OutputStream os);
}
