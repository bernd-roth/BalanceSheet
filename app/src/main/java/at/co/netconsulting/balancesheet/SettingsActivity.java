package at.co.netconsulting.balancesheet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import at.co.netconsulting.general.StaticFields;

public class SettingsActivity extends BaseActivity {

    private EditText    editTextPort,
                        editTextIPAddress,
                        editTextPerson,
                        editTextMoneyOnFoodToSpend,
                        editTextDefaultPosition,
                        editTextDefaultLocation;
    private FloatingActionButton fabSaveButton, fabReturnButton;
    private SharedPreferences sharedPreferences;
    private String[] splitPerson;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeObjects();
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
        loadSharedPreferences(StaticFields.SP_PORT);
        loadSharedPreferences(StaticFields.SP_PERSON);
        loadSharedPreferences(StaticFields.SP_MONEY_FOOD);
        loadSharedPreferences(StaticFields.SP_DEFAULT_LOCATION);
        loadSharedPreferences(StaticFields.SP_DEFAULT_POSITION);
    }

    private void initializeObjects() {
        //set the toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_settings);

        editTextIPAddress = (EditText) findViewById(R.id.editTextIPAddress);
        editTextPort = (EditText) findViewById(R.id.editTextPort);
        editTextPerson = (EditText) findViewById(R.id.editTextPerson);
        editTextMoneyOnFoodToSpend = (EditText) findViewById(R.id.editTextMoneyOnFoodToSpend);
        editTextDefaultPosition = (EditText) findViewById(R.id.editTextDefaultPosition);
        editTextDefaultLocation = (EditText) findViewById(R.id.editTextDefaultLocation);

        fabSaveButton = findViewById(R.id.saveFab);
        fabSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSharedPreferences(StaticFields.SP_PORT);
                saveSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
                saveSharedPreferences(StaticFields.SP_PERSON);
                saveSharedPreferences(StaticFields.SP_MONEY_FOOD);
                saveSharedPreferences(StaticFields.SP_DEFAULT_LOCATION);
                saveSharedPreferences(StaticFields.SP_DEFAULT_POSITION);
            }
        });

        fabReturnButton = findViewById(R.id.returnFab);
        fabReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
            case StaticFields.SP_PERSON:
                editTextPerson.setText(s1);
                break;
            case StaticFields.SP_MONEY_FOOD:
                editTextMoneyOnFoodToSpend.setText(s1);
                break;
            case StaticFields.SP_DEFAULT_POSITION:
                editTextDefaultPosition.setText(s1);
                break;
            case StaticFields.SP_DEFAULT_LOCATION:
                editTextDefaultLocation.setText(s1);
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
            case StaticFields.SP_PERSON:
                editor.putString(shared_pref_key, editTextPerson.getText().toString());
                editor.commit();
                break;
            case StaticFields.SP_MONEY_FOOD:
                editor.putString(shared_pref_key, editTextMoneyOnFoodToSpend.getText().toString());
                editor.commit();
                break;
            case StaticFields.SP_DEFAULT_POSITION:
                editor.putString(shared_pref_key, editTextDefaultPosition.getText().toString());
                editor.commit();
                break;
            case StaticFields.SP_DEFAULT_LOCATION:
                editor.putString(shared_pref_key, editTextDefaultLocation.getText().toString());
                editor.commit();
                break;
        }
    }

    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }
    public void showBarChart(MenuItem item) {
        onOptionsItemSelected(item);
    }
}