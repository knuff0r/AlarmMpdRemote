package de.sknauer.alarmmpdremote;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import de.sknauer.alarmmpdremote.model.Alarm;


public class MainActivity extends ActionBarActivity {

    private ArrayList<Alarm> alarms;
    private AlarmArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarms = new ArrayList<Alarm>();
        adapter = new AlarmArrayAdapter(this, alarms);

        final ListView listview = (ListView) findViewById(R.id.listview);

        listview.setAdapter(adapter);


    }


    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY) + 8;
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            if (view.isShown()) {
                alarms.add(new Alarm(hourOfDay, minute, true));
                adapter.notifyDataSetChanged();
                new ExecSSHAlarmTask().execute("bla");
            }
        }
    }

    public void onClick(View v) {
        ListView lv = (ListView) findViewById(R.id.listview);
        int position = lv.getPositionForView(v);
        switch (v.getId()) {
            case R.id.bt_delete:
                lv = (ListView) findViewById(R.id.listview);
                position = lv.getPositionForView(v);
                alarms.remove(position);
                adapter.notifyDataSetChanged();
                break;
            case R.id.bt_add:
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
                break;
            case R.id.sw_enable:
                lv = (ListView) findViewById(R.id.listview);
                position = lv.getPositionForView(v);
                alarms.get(position).toggle();

            default:
                break;
        }
    }


    private class ExecSSHAlarmTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            try {
                JSch jsch = new JSch();
                //final SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Session session = jsch.getSession(sharedPref.getString("username", ""),
                        sharedPref.getString("host", ""),sharedPref.getInt("port", 22));
                session.setPassword(sharedPref.getString("password", ""));

                //session.setPassword("hanf#55");

                // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

                session.connect();

                // SSH Channel
                ChannelExec channelssh = (ChannelExec)
                        session.openChannel("exec");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channelssh.setOutputStream(baos);

                InputStream inputStream = channelssh.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                StringBuilder cronfile = new StringBuilder();
                cronfile.append("##Alarms\n");
                for (Alarm a : alarms) {
                    cronfile.append("#" + a.getName() + "\n");
                    if (!a.isEnabled())
                        cronfile.append("#");
                    cronfile.append(a.getMinute() + " " + a.getHour() + " ");
                    cronfile.append("* * * ");
                    cronfile.append("mpc clear;mpc volume 80;mpc random on;mpc repeat on;");
                    cronfile.append("mpc load " + a.getPlaylist() + ";");
                    cronfile.append("/home/" + sharedPref.getString("username", "") + "/mpc-fade ");
                    cronfile.append("75 600\n");


                }
                // Execute command
                String command = "echo '" + cronfile.toString() + "'>cronfile;crontab cronfile";
                Log.d("bla", command);
                channelssh.setCommand(command);


                channelssh.connect();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                }

                channelssh.disconnect();

                return "okok";

            } catch (Exception ex) {
                String err = (ex.getMessage() == null) ? "SD Card failed" : ex.getMessage();
                Log.e("sdcard-err2:", err);
            }
            return "";
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            Log.d("bla", result);
        }
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
