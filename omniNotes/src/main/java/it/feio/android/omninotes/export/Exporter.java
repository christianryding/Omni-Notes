package it.feio.android.omninotes.export;

import java.io.OutputStream;

import it.feio.android.omninotes.models.Note;

public interface Exporter {
    void  export(Note note, OutputStream os);
}
