package at.co.netconsulting.balancesheet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import at.co.netconsulting.general.StaticFields;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextPort, editTextIPAddress;
    private FloatingActionButton fabSaveButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeObjects();
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
        loadSharedPreferences(StaticFields.SP_PORT);
    }

    private void initializeObjects() {
        editTextIPAddress = (EditText) findViewById(R.id.editTextIPAddress);
        editTextPort = (EditText) findViewById(R.id.editTextPort);

        fabSaveButton = findViewById(R.id.saveFab);
        fabSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSharedPreferences(StaticFields.SP_PORT);
                saveSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
            }
        });
    }

    //------------------------------------Methods----------------------------------------//
    //SharedPreferences
    private void loadSharedPreferences(String shared_pref_key) {
        sharedPreferences = getSharedPreferences(shared_pref_key, Context.MODE_PRIVATE);

        String s1 = sharedPreferences.getString(shared_pref_key, "");

        switch(shared_pref_key) {
            case StaticFields.SP_PORT:
                editTextPort.setText(s1);
                break;
            case StaticFields.SP_INTERNET_ADDRESS:
                editTextIPAddress.setText(s1);
                break;
        }
    }

    private void saveSharedPreferences(String shared_pref_key) {
        sharedPreferences = getSharedPreferences(shared_pref_key,MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (shared_pref_key) {
            case StaticFields.SP_PORT:
                editor.putString(shared_pref_key, editTextPort.getText().toString());
                editor.commit();
                break;
            case StaticFields.SP_INTERNET_ADDRESS:
                editor.putString(shared_pref_key, editTextIPAddress.getText().toString());
                editor.commit();
                break;
        }
    }
}