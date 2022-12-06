package at.co.netconsulting.balancesheet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import at.co.netconsulting.enums.Spending;
import at.co.netconsulting.general.StaticFields;

public class MainActivity extends BaseActivity {

    private Toolbar toolbar;
    private FloatingActionButton fabAddButton;
    private ExtendedFloatingActionButton fabListButton;
    private EditText editTextIncome,
            editTextSpending,
            editTextDate;
    private Spinner spinnerPerson, spinnerLocation;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private static final String TAG = MainActivity.class.getName();
    private SharedPreferences sharedPreferences;
    private String sharedPref_IP, sharedPref_Port, sharedPref_Person;
    private TextView totalIncome, totalExpense, totalSavings, totalFood;
    private String[] splitPerson;
    private ArrayList<String> itemsPerson, arrayListOfIncomeAndExpense;
    private ArrayAdapter<String> adapterPerson, adapter;
    private PieChart pieChart;
    private int totalIncomeInt, totalExpenseInt, totalSavingsInt, totalFoodInt;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        initializeObjects();
        loadSharedPreferences(StaticFields.SP_PORT);
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
        loadSharedPreferences(StaticFields.SP_PERSON);
        getOutputFromDatabase(StaticFields.INCOME);
        getOutputFromDatabase(StaticFields.EXPENSE);
        getOutputFromDatabase(StaticFields.SAVINGS);
        getOutputFromDatabase(StaticFields.FOOD);
        getOutputFromDatabase(StaticFields.ALL);
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
            case StaticFields.SP_PERSON:
                sharedPref_Person = s1;

                adapterPerson.clear();
                if(sharedPref_Person != null) {
                    splitPerson = sharedPref_Person.split(" ");
                    for (int i = 0; i<splitPerson.length; i++) {
                        itemsPerson.add(splitPerson[i]);
                    }
                } else {
                    itemsPerson.add(getString(R.string.general_placeholder));
                }
                adapterPerson = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsPerson);
                spinnerPerson.setAdapter(adapterPerson);
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
                if(checkInputFields()) {
                    sendInputToDatabase();
                    pieChart.clearChart();
                    //update all fields from overview
                    getOutputFromDatabase(StaticFields.INCOME);
                    getOutputFromDatabase(StaticFields.EXPENSE);
                    getOutputFromDatabase(StaticFields.SAVINGS);
                    getOutputFromDatabase(StaticFields.FOOD);
                    //deactivate fabAddButton and reset all textfields to 0
                    fabAddButton.setEnabled(false);
                    resetEditText();
                }
            }
        });
        fabListButton = (ExtendedFloatingActionButton) findViewById(R.id.listButton);
        fabListButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Month currentMonth = LocalDate.now().getMonth();

                 AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                 alertDialog.setTitle("" + currentMonth);
                 View rowList = getLayoutInflater().inflate(R.layout.row, null);
                 ListView listView = rowList.findViewById(R.id.listView);
                 adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayListOfIncomeAndExpense);
                 if(!adapter.isEmpty())
                    listView.setAdapter(adapter);
                 adapter.notifyDataSetChanged();
                 alertDialog.setView(rowList);
                 alertDialog.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         refreshAndRequestOutputFromDatabase(false);
                     }
                 });
                 AlertDialog dialog = alertDialog.create();
                 dialog.show();
             }
        });

        editTextIncome = findViewById(R.id.editTextIncome);
        editTextIncome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editTextIncome.getText().length() !=0) {
                    Double income = Double.parseDouble(editTextIncome.getText().toString());
                    if(editTextSpending.getText().length() != 0) {
                        Double spending = Double.parseDouble(editTextSpending.getText().toString());
                        if(income > 0 && spending == 0) {
                            fabAddButton.setEnabled(true);
                        } else {
                            fabAddButton.setEnabled(false);
                        }
                    }
                } else {
                    fabAddButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editTextSpending = findViewById(R.id.editTextSpending);
        editTextSpending.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editTextSpending.getText().length() !=0) {
                    Double spending = Double.parseDouble(editTextSpending.getText().toString());
                    if(editTextIncome.getText().length() !=0) {
                        Double income = Double.parseDouble(editTextIncome.getText().toString());
                        if(spending > 0 && income == 0) {
                            fabAddButton.setEnabled(true);
                        } else {
                            fabAddButton.setEnabled(false);
                        }
                    }
                } else {
                    fabAddButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        spinnerPerson = findViewById(R.id.spinner);
        itemsPerson = new ArrayList<>();
        if(sharedPref_Person != null) {
            splitPerson =  sharedPref_Person.split(" ");
            for (int i = 0; i<=splitPerson.length; i++) {
                itemsPerson.add(splitPerson[i]);
            }
        } else {
            itemsPerson.add("Placeholder");
        }
        adapterPerson = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsPerson);
        spinnerPerson.setAdapter(adapterPerson);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        String[] itemsLocation = new String[]{
                String.valueOf(Spending.Amazon),
                String.valueOf(Spending.Depot),
                String.valueOf(Spending.Expense),
                String.valueOf(Spending.Facility_Management),
                String.valueOf(Spending.Food),
                String.valueOf(Spending.GTE_Gebäude_Technik_Energie_Betrieb),
                String.valueOf(Spending.Income),
                String.valueOf(Spending.Internet),
                String.valueOf(Spending.Loan),
                String.valueOf(Spending.Squandering),
                String.valueOf(Spending.Telephone),
                String.valueOf(Spending.Wiener_Netze),
                String.valueOf(Spending.Wiener_Staedtische_Versicherung_AG)};
            ArrayAdapter<String> adapterLocation = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsLocation);
            spinnerLocation.setAdapter(adapterLocation);
        editTextDate = findViewById(R.id.editTextDate);
        pieChart = findViewById(R.id.piechart);
        arrayListOfIncomeAndExpense = new ArrayList<>();
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        // Refresh  the layout
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshAndRequestOutputFromDatabase(false);
                    }
                }
        );
    }

    private void refreshAndRequestOutputFromDatabase(boolean isRefreshing) {
        pieChart.clearChart();
        getOutputFromDatabase(StaticFields.INCOME);
        getOutputFromDatabase(StaticFields.EXPENSE);
        getOutputFromDatabase(StaticFields.SAVINGS);
        getOutputFromDatabase(StaticFields.FOOD);
        getOutputFromDatabase(StaticFields.ALL);
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    private String setDateCorrectly() {
        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);
        return today;
    }

    private boolean checkInputFields() {
        boolean isInputSave = true;
        if (editTextIncome.getText().toString().isEmpty() || editTextSpending.getText().toString().isEmpty()
                || spinnerPerson.getSelectedItem().toString().isEmpty()
                || spinnerLocation.getSelectedItem().toString().isEmpty()
                || editTextDate.getText().toString().isEmpty()) {
            //Toast.makeText(getApplicationContext(), R.string.warning_all_fields, Toast.LENGTH_LONG).show();
            isInputSave = showAlertDialog(R.string.warning_all_fields);
        }
        double income = Double.parseDouble(editTextIncome.getText().toString());
        double expense = Double.parseDouble(editTextSpending.getText().toString());

        if (income==expense) {
            //Toast.makeText(getApplicationContext(), R.string.warning_income_expense_equality, Toast.LENGTH_LONG).show();
            isInputSave = showAlertDialog(R.string.warning_income_expense_equality);
        } else if(income > 0 && expense > 0){
            isInputSave = showAlertDialog(R.string.warning_income_expense_greater_than_zero);
        }
        return isInputSave;
    }

    private boolean showAlertDialog(int alertMessage) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Alert!");
        alertDialog.setMessage(alertMessage);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
        return false;
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
        StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //on below line we are parsing the response
                //to json object to extract data from it.
                try {
                    JSONObject respObj = new JSONObject(response);
                } catch (JSONException e) {
                    Log.e(TAG, getString(R.string.log_json_message, e.toString()));
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                Toast.makeText(MainActivity.this, getString(R.string.error_fail_response, error), Toast.LENGTH_SHORT).show();
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
                String who = spinnerPerson.getSelectedItem().toString();
                String location = spinnerLocation.getSelectedItem().toString();
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
        } else if (incomeOrExpenseOrSavingsOrFood.equals("all")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_ALL;
        }

        //String Request initialized
        StringRequest mStringRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(obj);

                    if (obj.has("incomeexpense")) {
                        JSONObject dataObject = obj.optJSONObject("incomeexpense");
                        if (dataObject != null) {
                            Log.d(TAG, dataObject.toString());
                            JSONObject locs = obj.getJSONObject("incomeexpense");
                            JSONArray recs = locs.getJSONArray("Total income");

                            String repl = recs.getString(0);

                            if (incomeOrExpenseOrSavingsOrFood.equals("income") && repl.equals("null")) {
                                totalIncome.setText("0");
                                totalIncomeInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("income") && !repl.equals("null")) {
                                totalIncome.setText(repl);
                                pieChart.addPieSlice(
                                        new PieModel(
                                                "Total income",
                                                Float.parseFloat(repl),
                                                Color.parseColor("#99CC00")));
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("expense") && repl.equals("null")) {
                                totalExpense.setText("0");
                                totalExpenseInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("expense") && !repl.equals("null")) {
                                totalExpense.setText(repl);
                                pieChart.addPieSlice(
                                        new PieModel(
                                                "Total spending",
                                                Float.parseFloat(repl),
                                                Color.parseColor("#FF4444")));
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("savings") && repl.equals("null")) {
                                totalSavings.setText("0");
                                totalSavingsInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("savings") && !repl.equals("null")) {
                                totalSavings.setText(repl);
                                pieChart.addPieSlice(
                                        new PieModel(
                                                "Total savings",
                                                Float.parseFloat(repl),
                                                Color.parseColor("#33B5E5")));
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("food") && repl.equals("null")) {
                                totalFood.setText("0");
                                totalFoodInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("food") && !repl.equals("null")) {
                                totalFood.setText(repl);
                                pieChart.addPieSlice(
                                        new PieModel(
                                                "Food/day",
                                                Float.parseFloat(repl),
                                                Color.parseColor("#FFBB33")));
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("all") && repl.equals("null")) {
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("all") && !repl.equals("null")) {
                                arrayListOfIncomeAndExpense.add(repl.toString());
                            }
                        } else {
                            JSONArray array = obj.optJSONArray("incomeexpense");
                            //when coming back from AlertDialog, array has to be emptied,
                            //if not array will be doubled, tripled, ...
                            arrayListOfIncomeAndExpense.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsn = array.getJSONObject(i);
                                String expense = jsn.getString("expense");
                                String income = jsn.getString("income");
                                String location = jsn.getString("location");
                                String who = jsn.getString("who");
                                String orderdate = jsn.getString("orderdate");
                                arrayListOfIncomeAndExpense.add("When: " + ":" + orderdate + "\nPerson: " + who + "\nWhere: " + location + "\nIncome: " + income + "\nExpense: " + expense);
                            }
                        }
                        if (totalIncomeInt == 0 && totalExpenseInt == 0 && totalSavingsInt == 0 && totalFoodInt == 0) {
                            pieChart.addPieSlice(
                                    new PieModel(
                                            "No income, no expenses",
                                            0,
                                            Color.parseColor("#dfe533")));
                            // To animate the pie chart
                            pieChart.startAnimation();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, getString(R.string.log_message, error.toString()));
            }
        });
        mStringRequest.setShouldCache(false);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        mStringRequest.setRetryPolicy(retryPolicy);
        mRequestQueue.add(mStringRequest);
        // To animate the pie chart
        pieChart.startAnimation();
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
                Toast.makeText(MainActivity.this, R.string.permission_camera_granted, Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(MainActivity.this, R.string.permission_camera_denied, Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == StaticFields.STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, R.string.permission_storage_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSharedPreferences(StaticFields.SP_PORT);
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
        loadSharedPreferences(StaticFields.SP_PERSON);
        resetEditText();
    }

    private void resetEditText() {
        editTextIncome.setText("0");
        editTextSpending.setText("0");
    }
}