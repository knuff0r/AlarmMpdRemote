package de.sknauer.alarmmpdremote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import de.sknauer.alarmmpdremote.model.Alarm;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<Alarm> alarms;
    private AlarmArrayAdapter adapter;

    private WrongSettingsDialog wsd;
    static Status status;
    private Menu mymenu;

    private Session session = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarms = new ArrayList<>();
        adapter = new AlarmArrayAdapter(this, alarms);
        wsd = new WrongSettingsDialog();
        wsd.setCancelable(false);
        status = Status.NOT_CONNECTED;
        final ListView listview = (ListView) findViewById(R.id.listview);

        listview.setAdapter(adapter);



    }

    @Override
    protected void onResume() {
        super.onResume();
        new ConnectTask().execute("bla");
    }

    public void stopCheckingAnimation() {
        if (mymenu != null) {
            // Get our refresh item from the menu
            MenuItem m = mymenu.findItem(R.id.action_status);
            if (m.getActionView() != null) {
                // Remove the animation.
                m.getActionView().clearAnimation();
                m.setActionView(null);
            }
        }
    }


    @SuppressLint("ValidFragment")
    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY) + 8;
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), TimePickerDialog.THEME_HOLO_DARK, this, hour, minute,
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
                alarms.remove(position);
                adapter.notifyDataSetChanged();
                break;
            case R.id.bt_add:
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
                break;
            case R.id.sw_enable:
                alarms.get(position).toggle();

            default:
                break;
        }
    }

    @SuppressLint("ValidFragment")
    public class WrongSettingsDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.content_wrong_settings)
                    .setCancelable(false)
                    .setNeutralButton("Edit Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(MainActivity.this, ConnectionSettingsActivity.class));
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private Session sshConnect() {
        status = de.sknauer.alarmmpdremote.Status.CHECKING_CONNECTION;
        invalidateOptionsMenu();

        JSch jsch = new JSch();
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String host = sharedPref.getString(getString(R.string.host), "127.0.0.1");
        String username = sharedPref.getString(getString(R.string.username), "");
        int port = sharedPref.getInt(getString(R.string.port), 22);
        String password = sharedPref.getString(getString(R.string.password), "");
        String key = sharedPref.getString(getString(R.string.key), "no_key_specified");

        Session session;
        try {
            assert host != null;
            session = jsch.getSession(username, host, port);
        } catch (JSchException e) {
            Log.e(TAG, "session:" + e.getMessage());
            return null;
        }

        try {
            jsch.addIdentity(key);
        } catch (JSchException e) {
            Log.e(TAG, "try add identity key " + e.getMessage() + " - using pwd auth");
            session.setPassword(password);
        }


        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        try {
            session.connect();
            if (wsd.isVisible())
                wsd.dismiss();
            status = de.sknauer.alarmmpdremote.Status.CONNECTED;
        } catch (JSchException e) {
            Log.e(TAG, "try connect: " + e.getMessage());
            status = de.sknauer.alarmmpdremote.Status.NOT_CONNECTED;
            wsd.show(getFragmentManager(), "wrongsettings");
        }

        return session;
    }

    private class ConnectTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.listview).setEnabled(false);
                    findViewById(R.id.bt_add).setEnabled(false);
                }
            });
        }


        @Override
        protected String doInBackground(String... params) {

            session = sshConnect();

            return "k";
        }

        @Override
        protected void onPostExecute(String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status == de.sknauer.alarmmpdremote.Status.CONNECTED) {

                        findViewById(R.id.listview).setEnabled(true);
                        findViewById(R.id.bt_add).setEnabled(true);
                    }
                    invalidateOptionsMenu();
                    stopCheckingAnimation();

                }
            });
        }
    }

    private class ExecSSHAlarmTask extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... urls) {

            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            // SSH Channel
            ChannelExec channelssh;
            try {
                channelssh = (ChannelExec)
                        session.openChannel("exec");
            } catch (JSchException e) {
                Log.e(TAG, e.getMessage());
                return "openChannelError";
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);

            InputStream inputStream;
            try {
                inputStream = channelssh.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return "inputStreamError";
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            StringBuilder cronfile = new StringBuilder();
            cronfile.append("##BEGIN AlarmMpdRemote\n");
            for (Alarm a : alarms) {
                cronfile.append("#").append(a.getName()).append("\n");
                if (!a.isEnabled())
                    cronfile.append("#");
                cronfile.append(a.getMinute()).append(" ").append(a.getHour()).append(" ");
                cronfile.append("* * * ");
                cronfile.append("mpc enable 1;mpc disable 4;mpc clear;mpc volume 80;mpc random on;mpc repeat on;");
                cronfile.append("mpc load ").append(a.getPlaylist()).append(";");
                cronfile.append("/home/").append(sharedPref.getString("username", "")).append("/mpc-fade ");
                cronfile.append("75 600\n");
            }
            cronfile.append("##END AlarmMpdRemote\n");
            // Execute command
            String command = "echo '" + cronfile.toString() + "'>cronfile;crontab cronfile";
            Log.d("bla", command);
            channelssh.setCommand(command);


            try {
                channelssh.connect();
            } catch (JSchException e) {
                e.printStackTrace();
            }
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG,stringBuilder.toString());

            channelssh.disconnect();

            return "okok";


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mymenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_status);
        //stopCheckingAnimation();
        if (item.getActionView() != null) {
            // Remove the animation.
            Log.d("bla", "stop anim");
            item.getActionView().clearAnimation();
            item.setActionView(null);
        }
        //depending on your conditions, either enable/disable
        switch (status) {
            case NOT_CONNECTED:
                item.setIcon(R.drawable.ic_error_red_48dp);
                Log.d("bla", "set not conncted icon");
                break;
            case CHECKING_CONNECTION:
                //item.setIcon(R.drawable.ic_autorenew_white_48dp);
                // Do animation start
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ImageView iv = (ImageView) inflater.inflate(R.layout.iv_check, (ViewGroup) item.getActionView());
                Animation rotation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_check);
                rotation.setRepeatCount(Animation.INFINITE);
                iv.startAnimation(rotation);
                item.setActionView(iv);
                Log.d("bla", "set anim");
                break;
            case CONNECTED:
                item.setIcon(R.drawable.ic_done_all_white_48dp);
                Log.d("bla", "set conncted icon");
                break;
            default:
                break;

        }
        super.onPrepareOptionsMenu(menu);
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
            startActivity(new Intent(this, ConnectionSettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
