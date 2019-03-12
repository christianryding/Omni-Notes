package it.feio.android.omninotes.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Exports a note to a text file.
 */
public class TextExporter extends ExporterBase {
    /**
     * String to be used as new line character sequence.
     */
    private static final String NEWLINE = "\n";
    /**
     * Character to use for the underscore
     */
    private static final char UNDERLINE_CHAR = '=';

    /**
     * Contains the resulting text file content.
     */
    private StringBuilder sb = null;
    /**
     * The note that is exported.
     */
    private NoteFacade facade = null;

    @Override
    protected void createDocument(NoteFacade facade) {
        sb = new StringBuilder();
        this.facade = facade;

        exportTitle();
        exportContent();
        exportAttachments();
        exportTimeStamp();
    }


    @Override
    protected void writeDocument(OutputStream os) {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
             PrintWriter pw = new PrintWriter(osw))
        {
            pw.write(sb.toString());
        } catch (IOException e) {
            throw new ExporterException("IO error when writing to file.");
        }
    }


    /**
     * Add note title
     */
    private void exportTitle() {
        if (facade.hasCategory()) {
            appendTitle(facade.getTitle() + " (" + facade.getCategoryName() + ")");
        } else {
            appendTitle(facade.getTitle());
        }

        sb.append(NEWLINE);
    }

    /**
     * Add note content, note text or checklist
     */
    private void exportContent() {
        if (facade.isNoteChecklist()) {
            for (NoteFacade.ChecklistItem item : facade.getChecklist()) {
                final char checkedChar = item.isChecked ? 'X' : ' ';
                sb.append(" - [")
                  .append(checkedChar)
                  .append("] ")
                  .append(item.text)
                  .append(NEWLINE);
            }
        } else {
            sb.append(facade.getTextContent())
              .append(NEWLINE);
        }
    }

    /**
     * Add note attachments
     */
    private void exportAttachments() {
        if (!facade.hasContacts() && !facade.hasLocation() && !facade.hasReminder()) {
            return;
        }

        sb.append(NEWLINE);

        appendTitle(facade.getString(NoteFacade.STRING_ATTACHMENTS));

        exportLocation();
        exportReminder();
        exportContacts();
    }

    /**
     * Add location attachment
     */
    private void exportLocation() {
        if (facade.hasLocation()) {
            appendLabel(facade.getString(NoteFacade.STRING_LOCATION));
            sb.append(facade.getLocation()).append(NEWLINE);
        }
    }

    /**
     * Add reminder attachment
     */
    private void exportReminder() {
        if (facade.hasReminder()) {
            appendLabel(facade.getString(NoteFacade.STRING_REMINDER));
            sb.append(facade.getReminder()).append(NEWLINE);
        }
    }

    /**
     * Add contact attachments
     */
    private void exportContacts() {
        if (facade.hasContacts()) {
            appendLabel(facade.getString(NoteFacade.STRING_CONTACTS));

            String name = facade.getString(NoteFacade.STRING_NAME);
            String phone = facade.getString(NoteFacade.STRING_PHONE);
            String email = facade.getString(NoteFacade.STRING_EMAIL);

            // Find which label is longest to be able to make the columns the right width
            int longest = name.length();
            longest = Math.max(longest, phone.length());
            longest = Math.max(longest, email.length());

            // Make the columns have the same width
            name = makeColumn(name, longest);
            phone = makeColumn(phone, longest);
            email = makeColumn(email, longest);

            // Add the contact info for all contacts
            for (NoteFacade.Contact contact : facade.getContacts()) {
                sb.append(name).append(contact.name).append(NEWLINE);
                sb.append(phone).append(contact.phone).append(NEWLINE);
                sb.append(email).append(contact.email).append(NEWLINE);
                sb.append(NEWLINE);
            }

            sb.append(NEWLINE);
        }
    }

    /**
     * Add note time stamp
     */
    private void exportTimeStamp() {
        sb.append(NEWLINE).append(facade.getTimestamp()).append(NEWLINE);
    }


    //
    // Helper methods
    //

    /**
     * Adds an underlined title text
     * @param title Title text
     */
    private void appendTitle(String title) {
        sb.append(title).append(NEWLINE);

        for (int i = 0; i < title.length(); i++) {
            sb.append(UNDERLINE_CHAR);
        }
        sb.append(NEWLINE);
    }

    /**
     * Adds a sub-title in the attachment part
     * @param label Title text
     */
    private void appendLabel(String label) {
        sb.append(NEWLINE).append(label).append(": ").append(NEWLINE);
    }

    /**
     * Fills out a string with spaces until it has the right width.
     * @param str Column text
     * @param longestStr Longest string of the row group
     */
    private String makeColumn(String str, int longestStr) {
        StringBuilder colSb = new StringBuilder();
        colSb.append(str).append(": ");
        final int spaceLen = longestStr - str.length();
        for (int i = 0; i < spaceLen; i++) {
            colSb.append(" ");
        }
        return colSb.toString();
    }
}
