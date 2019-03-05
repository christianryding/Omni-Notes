package it.feio.android.omninotes.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Exports a note to a HTML file.
 */
public class HtmlExporter extends ExporterBase {
    private final static String[] HTML_TEMPLATE = {
            "<!DOCTYPE html>",
            "<html lang=\"en\">",
            "<head>",
            "<meta charset=\"utf-8\">",
            "<title>", "$TITLE", "</title>",
            "<style>",
            "body {",
            "font-family: sans-serif;",
            "color: #464646;",
            "background: #cccccc;",
            "padding: 0;",
            "margin: 0;",
            "}",
            "",
            "h1 {",
            "padding: 0;",
            "margin: 0;",
            "}",
            "",
            "header, section, footer {",
            "margin: 10px;",
            "}",
            "",
            "header, section {",
            "padding: 10px;",
            "}",
            "",
            "header, section {",
            "background: #ffffff;",
            "}",
            "",
            "header {",
            "border-left: solid;",
            "border-color: ", "$CATEGORY_COLOR", ";",
            "border-width: 8px;",
            "}",
            "",
            "ul {",
            "list-style-type: none;",
            "padding-left: 10px;",
            "}",
            "",
            "footer {",
            "font-size: smaller;",
            "}",
            ".contacts {",
            "margin-bottom: 1em;",
            "}",
            "",
            "</style>",
            "</head>",
            "",
            "<body>",
            "<header>",
            "<h1>", "$TITLE", "</h1>",
            "</header>",
            "    ",
            "<main>",
            "",
            "$CONTENT",
            "",
            "$ATTACHMENTS",
            "",
            "</main>",
            "",
            "<footer>",
            "<p>", "$TIME_STAMP", "</p>",
            "</footer>",
            "",
            "</body>",
            "</html>"
    };

    private final static String[] TEXT_CONTENT_TEMPLATE = {
            "<section class=\"content\">",
            "$TEXT",
            "</section>"
    };

    private final static String[] CHECKLIST_CONTENT_TEMPLATE = {
            "<section class=\"content\">",
            "<ul>",
            "$CHECKLIST_ITEMS",
            "</ul>",
            "</section>"
    };

    private final static String[] CHECKED_ITEM_TEMPLATE = {
            "<li>☑ ", "$TEXT", "</li>",
    };

    private final static String[] UNCHECKED_ITEM_TEMPLATE = {
            "<li>☐ ", "$TEXT", "</li>",
    };

    private final static String[] ATTACHMENTS_TEMPLATE = {
            "<section class=\"attachments\">",
            "<h2>", "$ATTACHMENTS_TITLE", "</h2>",
            "$ATTACHMENT_LIST",
            "</section>"
    };

    private final static String[] LOCATION_TEMPLATE = {
            "<h3>", "$LOCATION_TITLE", "</h3>",
            "<p>", "$LOCATION", "</p>"
    };

    private final static String[] REMINDER_TEMPLATE = {
            "<h3>", "$REMINDER_TITLE", "</h3>",
            "<p>", "$REMINDER", "</p>"
    };

    private final static String[] CONTACTS_TEMPLATE = {
            "<h3>", "$CONTACTS_TITLE", "</h3>",
            "$CONTACT_ITEMS",
    };

    private final static String[] CONTACT_ITEM_TEMPLATE = {
            "<table class=\"contacts\">",
            "    <tr>",
            "        <td>", "$NAME_LABEL", "</td>",
            "        <td>", "$NAME", "</td>",
            "    </tr>",
            "    <tr>",
            "        <td>", "$PHONE_LABEL", "</td>",
            "        <td>", "$PHONE", "</td>",
            "    </tr>",
            "    <tr>",
            "        <td>", "$EMAIL_LABEL", "</td>",
            "        <td><a href=\"", "$EMAIL", "\">", "$EMAIL", "</a></td>",
            "    </tr>",
            "</table>"
    };

    private String document = null;
    NoteFacade facade = null;

    @Override
    protected void createDocument(NoteFacade facade) throws ExporterException {
        this.facade = facade;

        document =
        Replacer.make(HTML_TEMPLATE)
                .variable("TITLE", this::getTitle)
                .variable("CATEGORY_COLOR", this::getCatColor)
                .variable("CONTENT", this::getContent)
                .variable("ATTACHMENTS", this::getAttachments)
                .variable("TIME_STAMP", facade::getTimestamp)
                .replace();
    }

    @Override
    protected void writeDocument(OutputStream os) throws ExporterException {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
             PrintWriter pw = new PrintWriter(osw))
        {
            pw.write(document);
        } catch (IOException e) {
            throw new ExporterException("IO error when writing to file.");
        }
    }

    private String getTitle() {
        if (facade.hasCategory()) {
            return facade.getTitle() + " (" + facade.getCategoryName() + ")";
        } else {
            return facade.getTitle();
        }
    }

    private String getCatColor() {
        if (facade.hasCategory()) {
            return facade.getCategoryColor();
        } else {
            return "#ffffff";
        }
    }

    private String getContent() {
        if (!facade.isNoteChecklist()) {
            final String text = facade.getTextContent().replace("\n", "<br>");
            return Replacer
                    .make(TEXT_CONTENT_TEMPLATE)
                    .variable("TEXT", () -> text)
                    .replace();

        } else {
            return Replacer
                    .make(CHECKLIST_CONTENT_TEMPLATE)
                    .variable("CHECKLIST_ITEMS", this::getChecklistItems)
                    .replace();
        }

    }

    private String getAttachments() {
        String attachments  = getLocation() + getReminder() + getContacts();

        if (attachments.isEmpty()) {
            return "";
        }

        return Replacer.make(ATTACHMENTS_TEMPLATE)
                .variable("ATTACHMENTS_TITLE", () -> facade.getString(NoteFacade.STRING_ATTACHMENTS))
                .variable("ATTACHMENT_LIST", () -> attachments)
                .replace();
    }


    private String getLocation() {
        if (facade.hasLocation()) {
            return Replacer
                    .make(LOCATION_TEMPLATE)
                    .variable("LOCATION_TITLE", () -> facade.getString(NoteFacade.STRING_LOCATION))
                    .variable("LOCATION", facade::getLocation)
                    .replace();
        } else {
            return "";
        }
    }

    private String getReminder() {
        if (facade.hasReminder()) {
            return Replacer
                    .make(REMINDER_TEMPLATE)
                    .variable("REMINDER_TITLE", () -> facade.getString(NoteFacade.STRING_REMINDER))
                    .variable("REMINDER", facade::getReminder)
                    .replace();
        } else {
            return "";
        }
    }

    private String getContacts() {
        if (!facade.hasContacts()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (NoteFacade.Contact contact : facade.getContacts()) {
            sb.append(Replacer.make(CONTACT_ITEM_TEMPLATE)
                    .variable("NAME_LABEL", () -> facade.getString(NoteFacade.STRING_NAME))
                    .variable("PHONE_LABEL", () -> facade.getString(NoteFacade.STRING_PHONE))
                    .variable("EMAIL_LABEL", () -> facade.getString(NoteFacade.STRING_EMAIL))
                    .variable("NAME", () -> contact.name)
                    .variable("PHONE", () -> contact.phone)
                    .variable("EMAIL", () -> contact.email)
                    .replace());
        }

        return Replacer
                .make(CONTACTS_TEMPLATE)
                .variable("CONTACTS_TITLE", () -> facade.getString(NoteFacade.STRING_CONTACTS))
                .variable("CONTACT_ITEMS", sb::toString)
                .replace();
    }

    private String getChecklistItems() {
        StringBuilder all = new StringBuilder();

        for (NoteFacade.ChecklistItem item : facade.getChecklist()) {
            if (item.isChecked) {
                String str = Replacer
                        .make(CHECKED_ITEM_TEMPLATE)
                        .variable("TEXT", () -> item.text)
                        .replace();
                all.append(str);

            } else {
                String str = Replacer
                        .make(UNCHECKED_ITEM_TEMPLATE)
                        .variable("TEXT", () -> item.text)
                        .replace();
                all.append(str);
            }
        }

        return all.toString();
    }
}
