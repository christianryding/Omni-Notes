package it.feio.android.omninotes.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;

/**
 * Class to help retrieve information about contact attachment
 *
 */
public class ContactHelper {

    private Context context;
    private String contactID;
    private Cursor contactCursor;
    private boolean contactExist;

    /**
     * Constructor
     *
     * @param contactAttachment Contacts attachment
     * @param context Context
     */
    public ContactHelper(Attachment contactAttachment, Context context){
        this.context = context;
        this.contactCursor = context.getContentResolver().query(contactAttachment.getUri(), null, null, null, null);
        if(contactCursor.moveToFirst()) {
            this.contactID = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
            contactExist = true;
        }else{
            contactExist = false;
        }
    }



    /**
     * Takes a note and returns a list of ContactHelper objects, one for each contact attachment.
     *
     * @param note Note with contact attachments
     * @param context Context used when constructing a helper.
     * @return The list of ContactHelpers
     */
    public static List<ContactHelper> getAllContacts(Note note, Context context) {
        ArrayList<ContactHelper> helpers = new ArrayList<>();

        for (Attachment attachment: note.getAttachmentsList()) {
            if (Constants.MIME_TYPE_CONTACT.equals(attachment.getMime_type())) {
                helpers.add(new ContactHelper(attachment, context));
            }
        }

        return helpers;
    }

    /**
     * Do contact exist
     *
     * @return true if contact exist, false if not
     */
    public boolean contactExist(){
        return contactExist;
    }

    /**
     * Return name of contact attachment
     *
     * @return Display name of contact
     */
    public String getName(){
        return contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    }


    /**
     * Return contacts photo if available
     *
     * @param mAttachment contact
     * @param convertView view
     * @return contacts photo or null
     */
    public Bitmap getContactPhoto(Attachment mAttachment, View convertView){

        Bitmap thumbnailBm = null;
        try {
            InputStream s = ContactsContract.Contacts.openContactPhotoInputStream(
                    convertView.getContext().getContentResolver()
                    , mAttachment.getUri()
                    , true);
            thumbnailBm = BitmapFactory.decodeStream(s);
            s.close();
        } catch (Exception ioExc) {
            Log.e(Constants.TAG_CONTACT, "Could not retrieve thumbnail for contact");
        }
        return thumbnailBm;
    }

    /**
     * Get contacts phone numbers
     *
     * @return List of contacts, that holds contacts phone numbers and their types
     */
    public List<Contact> getPhoneNumbers() {

        List<Contact> contactPhoneInfo = new ArrayList<>();

        if (contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("1")) {

            Cursor phoneCursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    , null
                    , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                    , new String[]{contactID}
                    , null);

            while (phoneCursor.moveToNext()) {
                Contact contact = new Contact();
                contact.setData(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        contact.setType("home");
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        contact.setType("cell");
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        contact.setType("work");
                        break;
                    default:
                        contact.setType("other");
                        break;
                }

                contactPhoneInfo.add(contact);
            }

            phoneCursor.close();
        }

        return contactPhoneInfo;
    }

    /**
     * Get contacts emails
     *
     * @return List of contacts, that holds contacts mail addresses and their types
     */
    public List<Contact> getMailAddresses() {
        List<Contact> contactMailInfo = new ArrayList<>();

        Cursor mailCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI
                , null
                , ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?"
                , new String[]{contactID}
                , null);

        while (mailCursor.moveToNext()) {
            Contact contact = new Contact();
            String email = mailCursor.getString(mailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            int type = mailCursor.getInt(mailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

            if (email != null) {
                contact.setData(email);
                switch (type) {
                    case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                        contact.setType("home");
                        break;
                    case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                        contact.setType("work");
                        break;
                    default:
                        contact.setType("other");
                        break;
                }
            }

            contactMailInfo.add(contact);
        }

        mailCursor.close();
        return contactMailInfo;
    }

    /**
     * Close contact cursor, this must be called after class use
     *
     */
    public void close(){
        contactCursor.close();
    }


    /**
     * Class to hold information about contact
     *
     * Variable "data" used to hold email/phonenumber, variable "type" used to hold their types
     */
    public class Contact{

        String data;
        String type;

        // Constructor
        public Contact(){
            this.data = "";
            this.type = "";
        }

        // Setters
        public void setData(String data) {
            this.data = data;
        }
        public void setType(String type) {
            this.type = type;
        }

        // Getters
        public String getType() {
            return type;
        }
        public String getData() {
            return data;
        }
    }
} 
