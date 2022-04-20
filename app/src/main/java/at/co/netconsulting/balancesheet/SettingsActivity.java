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

    private EditText editTextFirstName, editTextInternetAddress;
    private FloatingActionButton fabSaveButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeObjects();
        loadSharedPreferences(StaticFields.SP_PERSON_NAME);
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
    }

    private void initializeObjects() {
        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextInternetAddress = (EditText) findViewById(R.id.editTextInternetAddress);

        fabSaveButton = findViewById(R.id.saveFab);
        fabSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSharedPreferences(StaticFields.SP_PERSON_NAME);
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
            case StaticFields.SP_PERSON_NAME:
                editTextFirstName.setText(s1);
                break;
            case StaticFields.SP_INTERNET_ADDRESS:
                editTextInternetAddress.setText(s1);
                break;
        }
    }

    private void saveSharedPreferences(String shared_pref_key) {
        sharedPreferences = getSharedPreferences(shared_pref_key,MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (shared_pref_key) {
            case StaticFields.SP_PERSON_NAME:
                editor.putString(shared_pref_key, editTextFirstName.getText().toString());
                editor.commit();
                break;
            case StaticFields.SP_INTERNET_ADDRESS:
                editor.putString(shared_pref_key, editTextInternetAddress.getText().toString());
                editor.commit();
                break;
        }


    }
}