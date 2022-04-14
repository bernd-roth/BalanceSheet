package at.co.netconsulting.balancesheet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextFirstName;
    private FloatingActionButton fabAddButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeObjects();
    }

    private void initializeObjects() {
        editTextFirstName = findViewById(R.id.editTextPersonName);
        fabAddButton = (FloatingActionButton) findViewById(R.id.addButton);
        //onClickListener
        fabAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSharedPreferences(editTextFirstName.getText().toString());
            }
        });
    }

    private void saveSharedPreferences(String input) {
    }
}