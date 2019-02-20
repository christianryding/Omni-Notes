package it.feio.android.omninotes.export;

import java.io.OutputStream;

/**
 * Implementation of text exporter
 */
class TextExporter implements DocExporter<String>, ElementFactory<String> {
    @Override
    public ElementFactory<String> getElementFactory() {
        return this;
    }

    @Override
    public boolean init() {
        // TODO: Init stuff related to document creation
        return false;
    }

    @Override
    public boolean create(Node<String> document) {
        // TODO: Create document by walking through node hierarchy
        return false;
    }

    @Override
    public boolean write(OutputStream os) {
        // TODO: Write resulting data from() create to output stream
        return false;
    }

    @Override
    public boolean close() {
        // TODO: Do some cleanup if necessary
        return false;
    }

    //
    // ElementFactory implementation
    //

    @Override
    public String document() {
        return null;
    }

    @Override
    public String title(String title, String color, String category) {
        return null;
    }

    @Override
    public String content(String content) {
        return null;
    }

    @Override
    public String checklistItem(String itemText, boolean isChecked) {
        return null;
    }

    @Override
    public String attachments(String title) {
        return null;
    }

    @Override
    public String contact(String firstname, String lastname, String phone, String email) {
        return null;
    }

    @Override
    public String location(String address) {
        return null;
    }

    @Override
    public String reminder(String time) {
        return null;
    }

    @Override
    public String modificationDate(String lastModified, String created) {
        return null;
    }
}
