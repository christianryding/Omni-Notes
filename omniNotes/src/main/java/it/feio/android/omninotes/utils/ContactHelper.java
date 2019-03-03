package it.feio.android.omninotes.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import it.feio.android.omninotes.models.Attachment;

/**
 * Class to help retrieve information about contactattachment
 */
public class ContactHelper {

    private Attachment contactAttachment;
    private Context context;
    private String contactID;
    private Cursor contactCursor;

    public ContactHelper(Attachment contactAttachment, Context context){
        this.contactAttachment = contactAttachment;
        this.context = context;
        this.contactID = "";
        this.contactCursor = context.getContentResolver().query(contactAttachment.getUri(), null, null, null, null);

        contactCursor.moveToFirst();
        this.contactID = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
    }

    /**
     * Return name of contact attachment
     * @return Display name
     */
    public String getName(){

        return contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    }



}
