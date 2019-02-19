package it.feio.android.omninotes;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private List<String> chosenContactInfo;
    private List<Contact> listAndroidContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        listAndroidContacts = new ArrayList<>();
        addContacts();
        initUI();

        ListView listview = findViewById(R.id.listview_contacts);

        // return chosen contact
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // selected item
                String selected = ((TextView) view.findViewById(R.id.contact_name)).getText().toString();

                Toast toast = Toast.makeText(getApplicationContext(), selected, Toast.LENGTH_SHORT);
                toast.show();


                //finish();
            }
        });



        ContactAdapter contactAdapter = new ContactAdapter();
        listview.setAdapter(contactAdapter);
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onNavigateUp());
    }


    // add contact
    private void addContacts() {

        // get all contacts
        Cursor contactCursor = null;
        ContentResolver contentResolver = getContentResolver();

        try {
            contactCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        } catch (Exception ex) {
            Log.e("Error on contact", ex.getMessage());
        }

        // check if contacts exist
        if (contactCursor.getCount() > 0) {

            // loop all contacts
            while (contactCursor.moveToNext()){
                Contact android_contact = new Contact();
                String contact_id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));

                // test LOOKUPKEY
                String lookup_key = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Log.d("CONTACTS", "LOOKUP_KEY: " + lookup_key);

                // get name
                android_contact.setFullName(contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                //Log.d("CONTACTS", "Name: " + android_contact.getFullName());

                // get phone numbers
                int hasPhoneNumber = Integer.parseInt(contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            , null
                            , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                            , new String[]{contact_id}
                            , null);

                    while (phoneCursor.moveToNext()) {
                        android_contact.getPhoneNrs().add(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                    phoneCursor.close();
                }


                // get emailaddress
                Cursor mailCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI
                        ,null
                        ,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?"
                        , new String[]{contact_id}
                        ,null);

                while (mailCursor.moveToNext()) {
                    String email = mailCursor.getString(mailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					if(email!=null){
						android_contact.getMailAddresses().add(email);
					}
                }
                mailCursor.close();



                // add contact to ArrayList
                listAndroidContacts.add(android_contact);
            }
        }
    }



    // class for listview
    private class ContactAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listAndroidContacts.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.contact_layout, null);
            TextView textview_name = (TextView) view.findViewById(R.id.contact_name);
            textview_name.setText("Name: " + listAndroidContacts.get(i).getFullName());

            String mailAddresses = "";
            for(int j = 0; j < listAndroidContacts.get(i).getMailAddresses().size(); j++){
                 mailAddresses += listAndroidContacts.get(i).getMailAddresses().get(j);
                if(j < (listAndroidContacts.get(i).getMailAddresses().size()-1) ){
                    mailAddresses += ", ";
                }
            }
            TextView textview_mail = (TextView) view.findViewById(R.id.contact_mail);
            textview_mail.setText("Email: " + mailAddresses);

            String phoneNumbers = "";
            for(int j = 0; j < listAndroidContacts.get(i).getPhoneNrs().size(); j++){
                phoneNumbers += listAndroidContacts.get(i).getPhoneNrs().get(j);
                if(j < (listAndroidContacts.get(i).getPhoneNrs().size()-1) ){
                    phoneNumbers += ", ";
                }
            }
            TextView textview_phone = (TextView) view.findViewById(R.id.contact_phone);
            textview_phone.setText("Phone: " + phoneNumbers);

            return view;
        }
    }

    //class for contacts information
    private class Contact {
        private String ID = "";
        private String fullName = "";

        private List<String> phoneNrs;
        private List<String> mailAddresses;

        // constructor
        public Contact(){
            phoneNrs = new ArrayList<>();
            mailAddresses = new ArrayList<>();
        }

        // setters
        public void setID(String ID) {
            this.ID = ID;
        }
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        // getters
        public String getID() {
            return ID;
        }
        public String getFullName() {
            return fullName;
        }
        public List<String> getMailAddresses() {
            return mailAddresses;
        }
        public List<String> getPhoneNrs() {
            return phoneNrs;
        }
    }
}
