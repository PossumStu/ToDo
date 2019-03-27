package net.ddns.gloryweb.todo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public ArrayList<String> myActives = new ArrayList<String>();
    TaskDBHelper dbHelper;
    Date date = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        dbHelper = new TaskDBHelper(this);

        /////////////////////////////////////
        //Delete and create tasks hardcoded//
        /////////////////////////////////////
        /*
        dbHelper.deleteTable();
        dbHelper.createTable();
        dbHelper.insertTask("Feed fish and Lo", 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0);
        dbHelper.insertTask("10-15 min clean", 1,1, 0, 1, 1, 1, 1, 1, 1, 1, 0);
        dbHelper.insertTask("Laundry",1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0);
        dbHelper.insertTask("Water plants", 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0);
        dbHelper.insertTask("Shave", 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0);
        dbHelper.insertTask("New razor", 1, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0);
        dbHelper.insertTask("Vacuum", 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0);
        dbHelper.insertTask("Piano", 1,1, 0, 1, 1, 1, 1, 1, 1, 1, 0);
        dbHelper.insertTask("Yoga", 1,1, 0, 1, 1, 1, 1, 1, 1, 1, 0);

        dbHelper.ActivateTasks();
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper = new TaskDBHelper(this);

        /////////////////////////////////////////////////////////////////
        //Activate tasks once per day using date stored in shared prefs//
        /////////////////////////////////////////////////////////////////

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Date date1 = new Date();
        String date = String.valueOf(date1.getDate());
        String prevDate = sharedPreferences.getString("prevDate", date);
        int cDate = Integer.parseInt(date);
        int pDate = Integer.parseInt(prevDate);
        if (cDate != pDate) {
            dbHelper.ActivateTasks();
        }
        editor.putString("prevDate", date);
        editor.commit();

        //*******//
        //*DEBUG*//--Bypasses daily task activator to activate tasks immediately
        //*******//
        //dbHelper.ActivateTasks();

        //////////////////////////////////
        //SQL data to Clickable ListView//
        //////////////////////////////////

        //Gets string array of activated task names
        myActives = dbHelper.getActiveNames();

        //Passes the string array data to an array adapter, then sets the listview to use that adapter
        final ArrayAdapter adapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, myActives);
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        //Sets listview to listen for clicks and within, defines action to be taken on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                dbHelper.DeactivateTask(myActives.get(position));
                myActives.remove(position);
                adapter.notifyDataSetChanged();
                WidgetProvider.sendRefreshBroadcast(getApplicationContext());

            }
        });

    }
}
