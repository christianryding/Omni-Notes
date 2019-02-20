package it.feio.android.omninotes.export;

import java.io.OutputStream;

/**
 * Implementation of HTML exporter
 */
class HtmlExporter implements DocExporter<String>, ElementFactory<String> {
    @Override
    public ElementFactory<String> getElementFactory() {
        return this;
    }

    @Override
    public boolean init() {
        return false;
    }

    @Override
    public boolean create(Node<String> document) {
        return false;
    }

    @Override
    public boolean write(OutputStream os) {
        return false;
    }

    @Override
    public boolean close() {
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
