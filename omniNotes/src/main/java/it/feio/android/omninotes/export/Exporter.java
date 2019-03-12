package it.feio.android.omninotes.export;

import java.io.OutputStream;

import it.feio.android.omninotes.models.Note;

/**
 * An note exporter has to implement this interface. This is used by the rest of the application to
 * access the exporter functionality.
 */
public interface Exporter {
    /**
     * Takes a note and exports all of it's information to an <code>OutputStream</code> in a format
     * specific to the implementing exporter. Which information is exported depends on the resulting
     * file format of the exporter, for example pictures might not be exported to a text based
     * format.
     *
     * @param note  the note to be exported.
     * @param os    outputstream to be used.
     * @throws ExporterException if an error occurs during export.
     */
    void export(Note note, OutputStream os);
}
