package it.feio.android.omninotes.async.notes;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.export.Exporter;
import it.feio.android.omninotes.export.ExporterException;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnNoteExported;
import it.feio.android.omninotes.utils.Constants;

/**
 * Exports a note.
 */
public class ExportNoteTask extends AsyncTask<Note, Void, Boolean> {
    private final WeakReference<Context> contextRef;
    private final Exporter exporter;
    private final Uri uri;
    private final OnNoteExported onNoteExported;

    /**
     *
     * @param exporter Exporter to use when exporting the file.
     * @param uri URI representing the file.
     * @param onNoteExported Called to report the result of the export. Can be null.
     */
    public ExportNoteTask(OnNoteExported onNoteExported, Exporter exporter, Uri uri) {
        this.contextRef = new WeakReference<>(OmniNotes.getAppContext());
        this.exporter = exporter;
        this.uri = uri;
        this.onNoteExported = onNoteExported;
    }

    /**
     * Opens the file represented by the URI and then calls the supplied exporter.
     * @param params Note to be exported.
     * @return true if everything went ok, otherwise false.
     */
    @Override
    protected Boolean doInBackground(Note... params) {
        final Context context = contextRef.get();
        final Note note = params[0];
        ParcelFileDescriptor pfd = null;
        FileOutputStream fos = null;
        boolean result = false;

        // Don't export if context somehow is lost
        if (context == null) {
            return false;
        }

        try {
            pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            if (pfd != null) {
                fos = new FileOutputStream(pfd.getFileDescriptor());
                exporter.export(note, fos);
            }
            result = true;
        } catch (ExporterException e) {
            Log.d(Constants.TAG, "Export: Exporter failed: " + e.getMessage());
            // "result" will be false
        } catch (FileNotFoundException e) {
            Log.d(Constants.TAG, "Export: FileNotFoundException");
            // "result" will be false
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (pfd != null) {
                    pfd.close();
                }
            } catch (IOException e) {
                // "result" will be false
            }
        }

        return result;
    }


    /**
     * Reports result to the caller of this task
     * @param exportOk
     */
    @Override
    protected void onPostExecute(Boolean exportOk) {
        if (onNoteExported != null) {
            onNoteExported.onNoteExported(exportOk);
        }
    }
}
