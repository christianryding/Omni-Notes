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
        public final String name;
        public final String phone;
        public final String email;

        public Contact(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }
    }

    public class ChecklistItem {
        public final String text;
        public final boolean isChecked;

        public ChecklistItem(String text, boolean isChecked) {
            this.text = text;
            this.isChecked = isChecked;
        }
    }

    private final Note note;
    private final Context context;
    private final List<ContactHelper> contactHelpers;

    public NoteFacade(Note note) {
        this.note = note;
        this.context = OmniNotes.getAppContext();
        this.contactHelpers = ContactHelper.getAllContacts(note, context);
    }

    public String getTitle() {
        return emptyIfNull(note.getTitle());
    }

    public boolean hasCategory() {
        return note.getCategory() != null;
    }

    public String getCategoryName() {
        if (!hasCategory()) {
            throw new IllegalStateException("Note has no category!");
        }
        return emptyIfNull(note.getCategory().getName());
    }

    public String getCategoryColor() {
        if (!hasCategory()) {
            throw new IllegalStateException("Note has no category!");
        }

        // The color is stored as signed decimal number, so convert it to a HTML hex color value.
        int color = Integer.parseInt(note.getCategory().getColor());
        return String.format("#%06X", (0xFFFFFF & color));
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
        return !TextUtils.isEmpty(note.getAddress());
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
        return !contactHelpers.isEmpty();
    }

    public List<Contact> getContacts() {
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
