package de.sknauer.alarmmpdremote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;


public class ConnectionSettingsActivity extends ActionBarActivity {


    private TextView tv_sub_host;
    private TextView tv_sub_port;
    private TextView tv_sub_username;
    private TextView tv_sub_key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tv_sub_host = (TextView) findViewById(R.id.tv_sub_host);
        tv_sub_port = (TextView) findViewById(R.id.tv_sub_port);
        tv_sub_username = (TextView) findViewById(R.id.tv_sub_username);
        tv_sub_key = (TextView) findViewById(R.id.tv_sub_key);

        initSubtexts();

    }

    private void initSubtexts(){
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tv_sub_host.setText(sharedPref.getString(getString(R.string.host),"127.0.0.1"));
        tv_sub_port.setText(""+sharedPref.getInt(getString(R.string.port),22));
        tv_sub_username.setText(sharedPref.getString(getString(R.string.username),""));
        tv_sub_key.setText(sharedPref.getString(getString(R.string.key),""));
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_host:
            case R.id.tv_sub_host:
                createDialog("host");
                break;
            case R.id.tv_port:
            case R.id.tv_sub_port:
                createDialog("port");
                break;
            case R.id.tv_username:
            case R.id.tv_sub_username:
                createDialog("username");
                break;
            case R.id.tv_password:
            case R.id.tv_sub_password:
                createDialog("password");
                break;
            case R.id.tv_key:
            case R.id.tv_sub_key:
                createDialog("key");

                break;
            default:
                break;

        }
    }

    private void createDialog(final String key) {
        //final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(Character.toUpperCase(key.charAt(0)) + key.substring(1));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(18, 0, 18, 0);

        final EditText et_input = new EditText(this);
        layout.addView(et_input, params);


        String value = null;
        int port = 22;
        switch (key) {
            case "host":
                value = sharedPref.getString(getString(R.string.host), "");
                break;
            case "port":
                port = sharedPref.getInt(getString(R.string.port), 22);
                et_input.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "username":
                value = sharedPref.getString(getString(R.string.username), "");
                break;
            case "password":
                value = sharedPref.getString(getString(R.string.password), "");
                et_input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case "key":
                value = sharedPref.getString(getString(R.string.key), "");
                break;
            default:
                break;
        }
        if (!key.equals("port"))
            et_input.setText(value);
        else
            et_input.setText("" + port);
        alert.setView(layout);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = et_input.getText().toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                switch (key) {
                    case "host":
                        editor.putString(getString(R.string.host), value);
                        break;
                    case "port":
                        int port = Integer.parseInt(et_input.getText().toString());
                        editor.putInt(getString(R.string.port), port);
                        break;
                    case "username":
                        editor.putString(getString(R.string.username), value);
                        break;
                    case "password":
                        editor.putString(getString(R.string.password), value);
                        break;
                    case "key":
                        editor.putString(getString(R.string.key), value);
                        break;
                    default:
                        break;
                }
                editor.apply();
            }
        });

        if(key.equals("key")) {
            alert.setNeutralButton("Select keyfile", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    final SharedPreferences.Editor editor = sharedPref.edit();

                    File mPath = new File(Environment.getExternalStorageDirectory() + "");
                    FileDialog fileDialog = new FileDialog(ConnectionSettingsActivity.this, mPath);
                    fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                        public void fileSelected(File file) {
                            Log.d(getClass().getName(), "selected file " + file.toString());
                            editor.putString(getString(R.string.key), file.toString());
                            editor.apply();
                        }
                    });
                    fileDialog.showDialog();
                }
            });
        }

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

}
