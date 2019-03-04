package it.feio.android.omninotes.export;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.feio.android.checklistview.interfaces.Constants;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.date.DateHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;

import static java.lang.Long.parseLong;

public class NoteFacade {
    // Ids for string resources used in the final document
    public static final int STRING_ATTACHMENTS = R.string.attachments_title;
    public static final int STRING_CONTACTS    = R.string.contacts_label;
    public static final int STRING_NAME        = R.string.name_label;
    public static final int STRING_PHONE       = R.string.phonenumber_label;
    public static final int STRING_EMAIL       = R.string.email_label;
    public static final int STRING_REMINDER    = R.string.reminder_label;
    public static final int STRING_LOCATION    = R.string.location;

    public class Contact {
        final String firstname;
        final String lastname;
        final String phone;
        final String email;

        public Contact(String firstname, String lastname, String phone, String email) {
            this.firstname = firstname;
            this.lastname = lastname;
            this.phone = phone;
            this.email = email;
        }
    }

    public class ChecklistItem {
        final String text;
        final boolean isChecked;

        public ChecklistItem(String text, boolean isChecked) {
            this.text = text;
            this.isChecked = isChecked;
        }
    }

    private final Note note;
    private final Context context;

    public NoteFacade(Note note) {
        this.note = note;
        this.context = OmniNotes.getAppContext();
    }

    public String getTitle() {
        return emptyIfNull(note.getTitle());
    }

    public String getCategoryName() {
        Category category = note.getCategory();
        if (category != null) {
            return category.getName();
        }  else {
            return "";
        }
    }

    public String getCategoryColor() {
        Category category = note.getCategory();
        if (category != null) {
            return category.getColor();
        }  else {
            return "";
        }
    }

    public boolean isNoteChecklist() {
        return note.isChecklist();
    }

    public String getTextContent() {
        if (isNoteChecklist()) {
            throw new IllegalStateException("Note is a checklist!");
        } else {
            return note.getContent();
        }
    }

    public List<ChecklistItem> getChecklist() {
        if (!isNoteChecklist()) {
            throw new IllegalStateException("Note is not a checklist!");
        }

        ArrayList<ChecklistItem> items = new ArrayList<>();

        final String[] lines = note.getContent().split("\n");
        for (String line : lines) {
            if (line.startsWith(Constants.CHECKED_SYM)) {
                final String text = line.substring(Constants.CHECKED_SYM.length());
                items.add(new ChecklistItem(text, true));
            } else if (line.startsWith(Constants.UNCHECKED_SYM)) {
                final String text = line.substring(Constants.UNCHECKED_SYM.length());
                items.add(new ChecklistItem(text, false));
            } else {
                Log.w(Constants.TAG, "Checklist item wasn't prefixed with CHECKED_SYM or UNCHECKED_SYM.");
            }
        }

        return items;
    }

    public boolean hasLocation() {
        return TextUtils.isEmpty(note.getAddress());
    }

    /**
     * TODO: Does a note with a location always have an address? What about longitude/latitude?
     * @return The address attached to the note.
     */
    public String getLocation() {
        if (!hasLocation()) {
            throw new IllegalStateException("Note doesn't have a location");
        }

        return note.getAddress();
    }

    public boolean hasReminder() {
        return note.getAlarm() != null;
    }

    /**
     * Returns the reminder time string used when exporting the note.
     *
     * TODO: Reduce code duplication. This method is a copy of DetailFragment.initReminder.
     */
    public String getReminder() {
        if (!hasReminder()) {
            throw new IllegalStateException("Note doesn't have a reminder");
        }

        long reminder = parseLong(note.getAlarm());
        String rrule = note.getRecurrenceRule();
        if (!TextUtils.isEmpty(rrule)) {
            return DateHelper.getNoteRecurrentReminderText(reminder, rrule);
        } else {
            return DateHelper.getNoteReminderText(reminder);
        }
    }

    public boolean hasContacts() {
        // TODO: Check to see if contacts is attached to the note
        return false;
    }

    public List<Contact> getContacts() {
        if (!hasContacts()) {
            throw new IllegalStateException("Note doesn't have any contacts");
        }

        ArrayList<Contact> contacts = new ArrayList<>();

        // TODO: Read contacts from note attachments
        //contacts.add(new Contact("First", "Person","123-123456", "email@address.com"));

        return contacts;
    }

    public String getTimestamp() {
        final Long lastMod = note.getLastModification();
        final Long creation = note.getCreation();

        // Note sure if this can be null, check to be on the safe side.
        if (lastMod == null || creation == null) {
            return "";
        }

        return getString(R.string.last_update)
                + " "
                + DateHelper.getFormattedDate(lastMod, false)
                + " ("
                + getString(R.string.creation)
                + " "
                + DateHelper.getFormattedDate(creation, false)
                + ")";
    }

    /**
     * Returns a translated string related to a note. The strings is used to describe the different
     * things in a note in the final exported document.
     * @param id One of STRING_???
     * @return Translated string
     */
    public String getString(int id) {
        return context.getString(id);
    }

    private String emptyIfNull(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }
}
