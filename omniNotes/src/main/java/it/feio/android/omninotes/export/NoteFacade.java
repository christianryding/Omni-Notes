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
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.ContactHelper;

import static java.lang.Long.parseLong;

/**
 * Implements an interface to a note that is more suited for the exporter code. This take care of
 * retrieving the information from the note and converts it to string data. The resulting string
 * data could be used directly in the resulting documents by the exporters. The class also take care
 * of any translation that is necessary.
 */
public class NoteFacade {
    /**
     * Attachments header text
     */
    public static final int STRING_ATTACHMENTS = R.string.attachments_title;
    /**
     * Contacts header text
     */
    public static final int STRING_CONTACTS    = R.string.contacts_label;
    /**
     * Name label in contact entry
     */
    public static final int STRING_NAME        = R.string.name_label;
    /**
     * Phone number label in contact entry
     */
    public static final int STRING_PHONE       = R.string.phonenumber_label;
    /**
     * Email label in contact entry
     */
    public static final int STRING_EMAIL       = R.string.email_label;
    /**
     * Reminder attachment label
     */
    public static final int STRING_REMINDER    = R.string.reminder_label;
    /**
     * Location attachment label
     */
    public static final int STRING_LOCATION    = R.string.location;

    /**
     * Represent a contact attached to a note.
     */
    public class Contact {
        public final String name;
        public final String phone;
        public final String email;

        public Contact(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }
    }

    /**
     * Represents a item in the checklist
     */
    public class ChecklistItem {
        /**
         * Item text
         */
        public final String text;
        /**
         * If the item should appear checked or not in the final document
         */
        public final boolean isChecked;

        public ChecklistItem(String text, boolean isChecked) {
            this.text = text;
            this.isChecked = isChecked;
        }
    }

    /**
     * The note that is used when retrieving information
     */
    private final Note note;
    /**
     * Current application context
     */
    private final Context context;
    /**
     * Stores on ContactHelper for each contact in the note.
     */
    private final List<ContactHelper> contactHelpers;

    /**
     * Constructs a NoteFacade for a specific note
     * @param note which note to construct the facade for.
     */
    public NoteFacade(Note note) {
        this.note = note;
        this.context = OmniNotes.getAppContext();
        this.contactHelpers = ContactHelper.getAllContacts(note, context);
    }

    /**
     * @return title of the note. Empty string if the note lacks a title.
     */
    public String getTitle() {
        return emptyIfNull(note.getTitle());
    }

    /**
     * @return true if the note belongs to a category, else false.
     */
    public boolean hasCategory() {
        return note.getCategory() != null;
    }

    /**
     * @return category name.
     * @throws IllegalStateException if <code>hasCategory</code> returns false.
     */
    public String getCategoryName() throws IllegalStateException {
        if (!hasCategory()) {
            throw new IllegalStateException("Note has no category!");
        }
        return emptyIfNull(note.getCategory().getName());
    }

    /**
     * @return category color as a HTML hex color value.
     * @throws IllegalStateException if <code>hasCategory</code> returns false.
     */
    public String getCategoryColor() throws IllegalStateException {
        if (!hasCategory()) {
            throw new IllegalStateException("Note has no category!");
        }

        // The color is stored as signed decimal number, so convert it to a HTML hex color value.
        int color = Integer.parseInt(note.getCategory().getColor());
        return String.format("#%06X", (0xFFFFFF & color));
    }

    /**
     * @return true if the note is a checklist, false if it's a text note.
     */
    public boolean isNoteChecklist() {
        return note.isChecklist();
    }

    /**
     * @return A string containg the note text.
     * @throws IllegalStateException If this note is a checklist, <code>isNoteChecklist</code>
     * returns true in that case.
     */
    public String getTextContent() throws IllegalStateException {
        if (isNoteChecklist()) {
            throw new IllegalStateException("Note is a checklist!");
        } else {
            return note.getContent();
        }
    }

    /**
     * Creates a list of <code>CheckListItem</code>, one for each item in the checklist.
     * @return list of items.
     * @throws IllegalStateException if isNoteChecklist returns false.
     */
    public List<ChecklistItem> getChecklist() throws IllegalStateException {
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

    /**
     * @return if this note has a location attachment.
     */
    public boolean hasLocation() {
        return !TextUtils.isEmpty(note.getAddress());
    }

    /**
     * TODO: Does a note with a location always have an address? What about longitude/latitude?
     * @return The address attached to the note.
     */
    public String getLocation() throws IllegalStateException {
        if (!hasLocation()) {
            throw new IllegalStateException("Note doesn't have a location");
        }

        return note.getAddress();
    }

    /**
     * @return if this note has a reminder attachment.
     */
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

    /**
     * @return if this note has contacts attached to it.
     */
    public boolean hasContacts() {
        return !contactHelpers.isEmpty();
    }

    /**
     * Retrieves all contacts attached to a note and saves it to a list.
     * @return a list with contacts.
     * @throws IllegalStateException if <code>hasContacts</code> returns false.
     */
    public List<Contact> getContacts() throws IllegalStateException {
        if (!hasContacts()) {
            throw new IllegalStateException("Note doesn't have any contacts");
        }

        // Collect all contact information
        ArrayList<Contact> contacts = new ArrayList<>();
        for (ContactHelper helper: contactHelpers) {
            String name = helper.getName();
            StringBuilder phone = new StringBuilder();
            StringBuilder email = new StringBuilder();

            for (ContactHelper.Contact c: helper.getPhoneNumbers()) {
                if (phone.length() != 0) {
                    phone.append(", ");
                }
                phone.append(c.getData());
            }

            for (ContactHelper.Contact c: helper.getMailAddresses()) {
                if (email.length() != 0) {
                    email.append(", ");
                }
                email.append(c.getData());
            }

            contacts.add(new Contact(name, phone.toString(), email.toString()));
        }

        return contacts;
    }

    /**
     * Constructs a localized time stamp string that tells when the note was created and when it
     * was last modified.
     * @return timestamp string.
     */
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

    /**
     * @param str the string.
     * @return an empty string if <code>str</code> is null, otherwise <code>str</code>.
     */
    private String emptyIfNull(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }
}
