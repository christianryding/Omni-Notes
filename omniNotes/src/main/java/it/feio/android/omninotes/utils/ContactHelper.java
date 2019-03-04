package it.feio.android.omninotes.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import it.feio.android.omninotes.models.Attachment;

/**
 * Class to help retrieve information about contact attachment
 *
 */
public class ContactHelper {

    //private Attachment contactAttachment;
    private Context context;
    private String contactID;
    private Cursor contactCursor;

    /**
     * Constructor
     *
     * @param contactAttachment Contacts attachment
     * @param context Context
     */
    public ContactHelper(Attachment contactAttachment, Context context){
        //this.contactAttachment = contactAttachment;
        this.context = context;
        this.contactCursor = context.getContentResolver().query(contactAttachment.getUri(), null, null, null, null);
        contactCursor.moveToFirst();
        this.contactID = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
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

        String data = "";
        String type = "";

        // Constructor
        public Contact(){        }

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
