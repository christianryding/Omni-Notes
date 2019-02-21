package it.feio.android.omninotes.export;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

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

        // TODO: Build from note timestamps
        timestamp("Last modified 2019-02-19 (Created 2019-02-10)");
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
        // TODO: Find out how the checklist is implemented
        checklistItem("Checklist not implemented", true);
        checklistItem("Implement check list", false);
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
}
