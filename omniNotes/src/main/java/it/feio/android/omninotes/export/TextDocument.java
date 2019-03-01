package it.feio.android.omninotes.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TextDocument extends Document {
    private final static String NEWLINE = "\n";
    private final static char UNDERLINE_CHAR = '=';

    class ContactPair {
        final String label;
        final String value;

        ContactPair(String key, String value) {
            this.label = key;
            this.value = value;
        }
    }

    private StringBuilder sb = null;
    private ArrayList<ContactPair> contactInfo = new ArrayList<>();

    @Override
    protected void begin() {
        sb = new StringBuilder();
    }

    @Override
    protected void write(OutputStream os) {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
             PrintWriter pw = new PrintWriter(osw))
        {
            pw.write(sb.toString());
        } catch (IOException e) {
            // TODO: Error handling
        }
    }

    //
    // Container components
    //

    @Override
    protected void content(String noteTitle, String category, String color) {
        if (category.isEmpty()) {
            appendTitle(noteTitle);
        } else {
            appendTitle(noteTitle + " (" + category + ")");
        }

        sb.append(NEWLINE);

        super.content(noteTitle, category, color);

        sb.append(NEWLINE);
    }

    @Override
    protected void contacts(String contactsLabel) {
        appendLabel(contactsLabel);
        super.contacts(contactsLabel);
    }

    @Override
    protected void contact() {
        contactInfo.clear();
        super.contact();

        int longestLabel = 0;
        for (ContactPair p : contactInfo) {
            longestLabel = Math.max(longestLabel, p.label.length());
        }

        for (ContactPair p : contactInfo) {
            appendContactLine(p, longestLabel);
        }

        sb.append(NEWLINE);
    }

    @Override
    protected void attachments(String title) {
        appendTitle(title);
        super.attachments(title);
        sb.append(NEWLINE);
    }

    //
    // Leaf components
    //

    @Override
    protected void textContent(String text) {
        sb.append(text).append(NEWLINE);
    }

    @Override
    protected void checklistItem(String text, boolean isChecked) {
        final char checkedChar = isChecked ? 'X' : ' ';
        sb.append(" - [").append(checkedChar).append("] ").append(text).append(NEWLINE);
    }

    @Override
    protected void location(String locationLabel, String location) {
        appendLabel(locationLabel);
        sb.append(location).append(NEWLINE);
    }

    @Override
    protected void reminder(String reminderLabel, String reminder) {
        appendLabel(reminderLabel);
        sb.append(reminder).append(NEWLINE);
    }

    @Override
    protected void contactName(String nameKey, String first, String last) {
        contactInfo.add(new ContactPair(nameKey, first + " " + last));
    }

    @Override
    protected void contactPhone(String phoneKey, String phone) {
        contactInfo.add(new ContactPair(phoneKey, phone));
    }

    @Override
    protected void contactEmail(String emailKey, String email) {
        contactInfo.add(new ContactPair(emailKey, email));
    }

    @Override
    protected void timestamp(String timestamp) {
        sb.append(timestamp).append(NEWLINE);
    }

    //
    // Helper methods
    //

    private void appendTitle(String title) {
        sb.append(title).append(NEWLINE);

        for (int i = 0; i < title.length(); i++) {
            sb.append(UNDERLINE_CHAR);
        }
        sb.append(NEWLINE);
    }

    private void appendLabel(String label) {
        sb.append(NEWLINE).append(label).append(": ").append(NEWLINE);
    }

    private void appendContactLine(ContactPair pair, int longestLabel) {
        sb.append(pair.label).append(": ");

        final int spaceLen = longestLabel - pair.label.length();
        for (int i = 0; i < spaceLen; i++) {
            sb.append(" ");
        }

        sb.append(pair.value).append(NEWLINE);
    }
}
