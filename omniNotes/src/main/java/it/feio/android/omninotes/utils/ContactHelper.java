package it.feio.android.omninotes.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;

/**
 * PLACEHOLDER!
 * Replace when merging with feature/contact_attachment
 *
 */
public class ContactHelper {

    /**
     * Constructor
     *
     * @param contactAttachment Contacts attachment
     * @param context Context
     */
    public ContactHelper(Attachment contactAttachment, Context context){
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

        helpers.add(new ContactHelper(null, null));
        helpers.add(new ContactHelper(null, null));

        return helpers;
    }

    /**
     * Return name of contact attachment
     *
     * @return Display name of contact
     */
    public String getName(){
        return "Firstname Lastname";
    }


    /**
     * Get contacts phone numbers
     *
     * @return List of contacts, that holds contacts phone numbers and their types
     */
    public List<Contact> getPhoneNumbers() {
        ArrayList<Contact> list = new ArrayList<>();

        Contact c1 = new Contact();
        c1.data = "123234-234";
        Contact c2 = new Contact();
        c2.data = "345345-356";

        list.add(c1);
        list.add(c2);

        return list;
    }

    /**
     * Get contacts emails
     *
     * @return List of contacts, that holds contacts mail addresses and their types
     */
    public List<Contact> getMailAddresses() {
        ArrayList<Contact> list = new ArrayList<>();

        Contact c1 = new Contact();
        c1.data = "sadfs@asdf.com";
        Contact c2 = new Contact();
        c2.data = "fghfgh@sdf.com";

        list.add(c1);
        list.add(c2);

        return list;
    }

    /**
     * Close contact cursor, this must be called after class use
     *
     */
    public void close(){

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
