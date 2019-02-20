package it.feio.android.omninotes.models.listeners;

public interface OnNoteExported {
    /**
     * Called to report the result of the export.
     * @param exportedOk true if export went ok, otherwise false.
     */
    void onNoteExported(boolean exportedOk);
}
