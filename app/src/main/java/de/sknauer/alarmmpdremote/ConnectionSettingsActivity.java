package de.sknauer.alarmmpdremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


public class ConnectionSettingsActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


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
                port = sharedPref.getInt("port", 22);
                break;
            case "username":
                value = sharedPref.getString(getString(R.string.username), "");
                break;
            case "password":
                value = sharedPref.getString(getString(R.string.password), "");
                et_input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            default:
                break;
        }
        if (!key.equals("port"))
            et_input.setText(value);
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
                    default:
                        break;
                }
                editor.commit();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }




/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
    */
}
