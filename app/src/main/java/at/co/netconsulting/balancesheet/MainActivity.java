package at.co.netconsulting.balancesheet;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import at.co.netconsulting.general.StaticFields;

public class MainActivity extends BaseActivity {

    private Toolbar toolbar;
    private FloatingActionButton fabAddButton, fabDeleteButton;
    private EditText editTextIncome,
            editTextSpending,
            editTextPerson,
            editTextLocation,
            editTextDate;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private static final String TAG = MainActivity.class.getName();
    private SharedPreferences sharedPreferences;
    private String sharedPref_IP, sharedPref_Port;
    private TextView totalIncome, totalExpense, totalSavings, totalFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        initializeObjects();
        getOutputFromDatabase(StaticFields.INCOME);
        getOutputFromDatabase(StaticFields.EXPENSE);
        getOutputFromDatabase(StaticFields.SAVINGS);
        getOutputFromDatabase(StaticFields.FOOD);
        loadSharedPreferences(StaticFields.SP_PORT);
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
    }

    //SharedPreferences
    private void loadSharedPreferences(String shared_pref_key) {
        sharedPreferences = getSharedPreferences(shared_pref_key, Context.MODE_PRIVATE);

        String s1 = sharedPreferences.getString(shared_pref_key, "");

        switch(shared_pref_key) {
            case StaticFields.SP_PORT:
                sharedPref_Port = s1;
                break;
            case StaticFields.SP_INTERNET_ADDRESS:
                sharedPref_IP = s1;
                break;
        }
    }

    private void initializeObjects() {
        //set the toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);

        editTextDate = findViewById(R.id.editTextDate);
        //editTextDate.addTextChangedListener(new DateInputMask(editTextDate));
        editTextDate.setText(setDateCorrectly());

        totalIncome = findViewById(R.id.textViewTotalIncome);
        totalExpense = findViewById(R.id.textViewTotalExpense);
        totalSavings = findViewById(R.id.textViewTotalSavings);
        totalFood = findViewById(R.id.textViewTotalFood);

        fabAddButton = findViewById(R.id.addButton);
        fabAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInputFields();
                sendInputToDatabase();
            }
        });
        //fabDeleteButton = (FloatingActionButton) findViewById(R.id.deleteButton);
        //fabDeleteButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {}
        //});

        editTextIncome = findViewById(R.id.editTextIncome);
        editTextSpending = findViewById(R.id.editTextSpending);
        editTextPerson = findViewById(R.id.editTextPerson);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
    }

    private String setDateCorrectly() {
        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);
        return today;
    }

    private void checkInputFields() {
        if (editTextIncome.getText().toString().isEmpty() || editTextSpending.getText().toString().isEmpty()
                || editTextPerson.getText().toString().isEmpty() || editTextLocation.getText().toString().isEmpty()
                || editTextDate.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "All fields must be filled", Toast.LENGTH_LONG).show();
        }
    }

    private void sendInputToDatabase() {
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        String url = StaticFields.PROTOCOL +
                sharedPref_IP +
                StaticFields.COLON +
                sharedPref_Port +
                StaticFields.REST_URL_ADD;
        StringRequest request = new StringRequest(Request.Method.POST,
                url, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // on below line we are displaying a success toast message.
                    Toast.makeText(MainActivity.this, "Data added to API", Toast.LENGTH_SHORT).show();
                        // on below line we are parsing the response
                        // to json object to extract data from it.
                        try {
                            JSONObject respObj = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // method to handle errors.
                    Toast.makeText(MainActivity.this, "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    // below line we are creating a map for
                    // storing our values in key and value pair.
                    Map<String, String> params = new HashMap<String, String>();

                    // on below line we are passing our key
                    // and value pair to our parameters.
                    String orderdate = editTextDate.getText().toString();
                    String who = editTextPerson.getText().toString();
                    String location = editTextLocation.getText().toString();
                    String income = editTextIncome.getText().toString();
                    String expense = editTextSpending.getText().toString();

                    String[] orderDate = orderdate.split("/");
                    String orderDateAsYYYYMMDD = orderDate[2] + "-" + orderDate[1] + "-" + orderDate[0];

                    params.put("orderdate", orderDateAsYYYYMMDD);
                    params.put("who", who);
                    params.put("location", location);
                    params.put("income", income);
                    params.put("expense", expense);

                    // at last we are
                    // returning our params.
                    return params;
                }
            };
            //below line is to make
            //a json object request.
            queue.add(request);
            //updating all fields from overview
            getOutputFromDatabase(StaticFields.INCOME);
            getOutputFromDatabase(StaticFields.EXPENSE);
            getOutputFromDatabase(StaticFields.SAVINGS);
            getOutputFromDatabase(StaticFields.FOOD);
        }

    private void getOutputFromDatabase(String incomeOrExpenseOrSavingsOrFood) {
        //RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this);

        // REST URL
        String url = null;
        if(incomeOrExpenseOrSavingsOrFood.equals("income")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_INCOME;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("expense")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_EXPENSE;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("savings")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_SAVINGS;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("food")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_FOOD;
        }

        //String Request initialized
        mStringRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Toast.makeText(getApplicationContext(),"Response :" + response.toString(), Toast.LENGTH_LONG).show();

                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(obj);

                    JSONObject locs = obj.getJSONObject("incomeexpense");
                    JSONArray recs = locs.getJSONArray("Total income");

                    String repl = recs.getString(0);

                    if(incomeOrExpenseOrSavingsOrFood.equals("income") && repl.equals("null")) {
                        totalIncome.setText("0");
                    } else if(incomeOrExpenseOrSavingsOrFood.equals("income") && !repl.equals("null")){
                        totalIncome.setText(repl);
                    } else if(incomeOrExpenseOrSavingsOrFood.equals("expense") && repl.equals("null")) {
                        totalExpense.setText("0");
                    } else if(incomeOrExpenseOrSavingsOrFood.equals("expense") && !repl.equals("null")) {
                        totalExpense.setText(repl);
                    } else if(incomeOrExpenseOrSavingsOrFood.equals("savings") && repl.equals("null")) {
                        totalSavings.setText("0");
                    } else if(incomeOrExpenseOrSavingsOrFood.equals("savings") && !repl.equals("null")) {
                        totalSavings.setText(repl);
                    } else if(incomeOrExpenseOrSavingsOrFood.equals("food") && repl.equals("null")) {
                        totalFood.setText("0");
                    } else if(incomeOrExpenseOrSavingsOrFood.equals("food") && !repl.equals("null")) {
                        totalFood.setText(repl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,"Error :" + error.toString());
            }
        });
        mRequestQueue.add(mStringRequest);
    }

    private void checkPermissions() {
        checkPermission(Manifest.permission.CAMERA, StaticFields.CAMERA_PERMISSION_CODE);
        checkPermission(Manifest.permission.INTERNET, StaticFields.INTERNET_PERMISSION_CODE);
        checkPermission(Manifest.permission.ACCESS_WIFI_STATE, StaticFields.WIFI_PERMISSION_CODE);
    }

    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }

    // check permission is done here
    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == StaticFields.CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == StaticFields.STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSharedPreferences(StaticFields.SP_PORT);
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
        getOutputFromDatabase(StaticFields.INCOME);
        getOutputFromDatabase(StaticFields.EXPENSE);
        getOutputFromDatabase(StaticFields.SAVINGS);
        getOutputFromDatabase(StaticFields.FOOD);
    }
}