package it.feio.android.omninotes.export;

import android.util.Log;

import java.io.OutputStream;

import it.feio.android.checklistview.interfaces.Constants;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.models.Note;

/**
 * TODO: Replace placeholder texts with info from the note.
 */
public abstract class Document implements Exporter {
    private Note note;
    private OutputStream os;

    final public void export(Note note, OutputStream os) {
        this.note = note;
        this.os = os;

        begin();
        document();
        write(os);
    }

    protected void document() {
        content(note.getTitle(), note.getCategory().toString(), note.getCategory().getColor());

        attachments("Attachments");

        timestamp(formatTimeStamp(note.getLastModification(), note.getCreation()));
    }

    protected void content(String noteTitle, String category, String color) {
        if (note.isChecklist()) {
            checklistContent();
        } else {
            textContent(note.getContent());
        }
    }

    protected void contacts(String contactsLabel) {
        contact();
        contact();
    }

    protected void contact() {
        contactName("Name", "First", "Person");
        contactPhone("Phonenumber", "123-123456");
        contactEmail("Email address", "email@address.com");
    }

    protected void checklistContent() {
        final String lines[] = note.getContent().split("\n");
        for (String line : lines) {
            if (line.startsWith(Constants.CHECKED_SYM)) {
                final String text = line.substring(Constants.CHECKED_SYM.length());
                checklistItem(text, true);
            } else if (line.startsWith(Constants.UNCHECKED_SYM)) {
                final String text = line.substring(Constants.UNCHECKED_SYM.length());
                checklistItem(text, false);
            } else {
                Log.w(Constants.TAG, "Checklist item wasn't prefixed with CHECKED_SYM or UNCHECKED_SYM.");
            }
        }
    }

    protected void attachments(String attachmentsTitle) {
        location("Location", "1600 Amphitheatre Pkwy, Montaina View, CA 94043, USA");
        reminder("Reminder", "Weekley on Thuesday Starting from Wed, Feb 7 8:00 PM");
        contacts("Contacts");
    }

    abstract protected void begin();
    abstract protected void write(OutputStream os);

    abstract protected void textContent(String text);
    abstract protected void checklistItem(String text, boolean isChecked);
    abstract protected void location(String locationLabel, String location);
    abstract protected void reminder(String reminderLabel, String reminder);
    abstract protected void contactName(String nameLabel, String first, String last);
    abstract protected void contactPhone(String phoneLabel, String phone);
    abstract protected void contactEmail(String emailLabel, String email);
    abstract protected void timestamp(String timestamp);


    private String formatTimeStamp(long modified, long created) {
        final String modifiedStr = DateHelper.getFormattedDate(modified, false);
        final String createdStr =  DateHelper.getFormattedDate(created, false);
        return "Last modified " + modifiedStr + " (Created " + createdStr + ")";
    }
}
