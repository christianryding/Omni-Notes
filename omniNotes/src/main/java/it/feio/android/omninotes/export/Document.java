package it.feio.android.omninotes.export;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.OutputStream;

import it.feio.android.checklistview.interfaces.Constants;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;

import static java.lang.Long.parseLong;

public abstract class Document implements Exporter {
    private Note note;
    private Context context;

    final public void export(Note note, OutputStream os) {
        this.note = note;
        this.context = OmniNotes.getAppContext();

        begin();
        document();
        write(os);
    }

    protected void document() {
        // Export content
        String catName = "";
        String catColor = "";
        Category category = note.getCategory();
        if (category != null) {
            catName = category.getName();
            catColor = category.getColor();
        }
        content(note.getTitle(), catName, catColor);

        // Export attachments
        attachments(getString(R.string.attachments_title));

        // Export creation and modification time
        final Long lastMod = note.getLastModification();
        final Long creation = note.getCreation();
        if (lastMod != null && creation != null) {
            timestamp(formatTimeStamp(lastMod, creation));
        }
    }

    /**
     * Handles exporting of note content.
     * @param noteTitle Note title.
     * @param category Category name. Empty string if no category is set.
     * @param color Category color. Empty string if no category is set.
     */
    protected void content(String noteTitle, String category, String color) {
        if (note.isChecklist()) {
            checklistContent();
        } else {
            textContent(note.getContent());
        }
    }

    protected void contacts(String contactsLabel) {
        // TODO: Get information from attached contacts and replace the placeholder text in contact()
        contact();
        contact();
    }

    protected void contact() {
        contactName(getString(R.string.name_label), "First", "Person");
        contactPhone(getString(R.string.phonenumber_label), "123-123456");
        contactEmail(getString(R.string.email_label), "email@address.com");
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
        final String address = note.getAddress();
        if (address != null && !address.isEmpty()) {
            location(context.getString(R.string.location), address);
        }

        final String reminder = getReminderText();
        if (!reminder.isEmpty()) {
            reminder(getString(R.string.reminder_label), reminder);
        }

        contacts(getString(R.string.contacts_label));
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
        return getString(R.string.last_update)
                + " "
                + DateHelper.getFormattedDate(modified, false)
                + " ("
                + getString(R.string.creation)
                + " "
                + DateHelper.getFormattedDate(created, false)
                + ")";
    }


    /**
     * Returns the reminder time string used when exporting the note.
     *
     * TODO: Reduce code duplication. This method is a copy of DetailFragment.initReminder.
     */
    private String getReminderText() {
        if (note.getAlarm() == null) {
            return "";
        }
        long reminder = parseLong(note.getAlarm());
        String rrule = note.getRecurrenceRule();
        if (!TextUtils.isEmpty(rrule)) {
            return DateHelper.getNoteRecurrentReminderText(reminder, rrule);
        } else {
            return DateHelper.getNoteReminderText(reminder);
        }
    }

    private String getString(int id) {
        return context.getString(id);
    }
}
