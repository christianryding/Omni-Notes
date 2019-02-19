package it.feio.android.omninotes;

import android.content.Intent;
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

public class ContactActivity extends AppCompatActivity {

    private ArrayList<Integer> test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        initUI();

        ListView listview = (ListView) findViewById(R.id.listview_contacts);

        Intent intent=getIntent();
        test = intent.getIntegerArrayListExtra("test");



        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // selected item
                String selected = ((TextView) view.findViewById(R.id.contact_name)).getText().toString();

                Toast toast = Toast.makeText(getApplicationContext(), selected, Toast.LENGTH_SHORT);
                toast.show();
                finish();
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


    class ContactAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return test.size();
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
            textview_name.setText("asd\n" + test.get(i).toString());

            Log.d("TEST", test.get(i).toString());
            return view;
        }
    }

}
