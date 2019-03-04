package it.feio.android.omninotes.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Exports a note to a text file.
 */
public class TextExporter extends ExporterBase {
    private final static String NEWLINE = "\n";
    private final static char UNDERLINE_CHAR = '=';

    private StringBuilder sb = null;
    private NoteFacade facade = null;

    /**
     * Creates the text document by filling up the string builder with the information from the
     * note. The resulting string will be the content of the exported textile.
     * @param facade
     * @throws ExporterException
     */
    @Override
    protected void createDocument(NoteFacade facade) throws ExporterException {
        sb = new StringBuilder();
        this.facade = facade;

        exportTitle();
        exportContent();
        exportAttachments();
        exportTimeStamp();
    }

    /**
     * Writes the content of the StringBuilder to the output stream.
     * @param os
     * @throws ExporterException Thrown if an IO-error occurs.
     */
    @Override
    protected void writeDocument(OutputStream os) throws ExporterException {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
             PrintWriter pw = new PrintWriter(osw))
        {
            pw.write(sb.toString());
        } catch (IOException e) {
            throw new ExporterException("IO error when writing to file.");
        }
    }

    //
    // Methods to add the different note parts to the StringBuilder
    //

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
                sb.append(name).append(contact.firstname).append(" ").append(contact.lastname).append(NEWLINE);
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
        StringBuilder sb = new StringBuilder();
        sb.append(str).append(": ");
        final int spaceLen = longestStr - str.length();
        for (int i = 0; i < spaceLen; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
