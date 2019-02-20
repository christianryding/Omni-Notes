package it.feio.android.omninotes.export;

/**
 * The purpose is to translate Note information to T type. Implemented for each file format.
 * @param <T> Type representing document data.
 *
 * TODO: Make color, lastModified and created use a more suitable type.
 */
interface ElementFactory<T> {
    T document();
    T title(String title, String color, String category);
    T content(String content);
    T checklistItem(String itemText, boolean isChecked);
    T attachments(String title);
    T contact(String firstname, String lastname, String phone, String email);
    T location(String address);
    T reminder(String time);
    T modificationDate(String lastModified, String created);
}
