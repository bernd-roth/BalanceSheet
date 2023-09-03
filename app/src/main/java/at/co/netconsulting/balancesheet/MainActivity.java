package at.co.netconsulting.balancesheet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import at.co.netconsulting.enums.Location;
import at.co.netconsulting.enums.Spending;
import at.co.netconsulting.general.StaticFields;

public class MainActivity extends BaseActivity {

    private Toolbar toolbar;
    private FloatingActionButton fabAddButton;
    private ExtendedFloatingActionButton fabListButton;
    private EditText editTextIncome,
            editTextSpending,
            editTextDate,
            editTextComment,
            editText_Id,
            editText_When,
            editText_Person,
            editText_Income,
            editText_Expense,
            editText_Position,
            editText_Location,
            editText_Comment;
    private Spinner spinnerPerson, spinnerLocation, spinnerPosition;
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private static final String TAG = MainActivity.class.getName();
    private SharedPreferences sharedPreferences;
    private String sharedPref_IP, sharedPref_Port, sharedPref_Person, sharedPref_Food, sharedPref_Position, sharedPref_Location;
    private TextView    totalIncome, totalExpense, totalSavings, totalFood, textViewAverageTotalFood, textViewReservedAverageDayFood, textViewTotalYearFood, textViewTotalYearIncome,
                        textViewSumFoodJuliaMonth, textViewSumFoodBerndMonth;
    private String[] splitPerson;
    private ArrayList<String> itemsPerson, arrayListOfIncomeAndExpense;
    private ArrayAdapter<String> adapterPerson, adapter;
    private int totalIncomeInt, totalExpenseInt, totalSavingsInt, totalFoodInt, averageFoodPerDayOfMonthInt, reservedAverageDayFoodInt, totalYearFood;
    public static float totalIncomeStatic, totalSavingsStatic, totalExpenseStatic, totalFoodStatic;
//    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog dialogDetails, dialog;
    private AlertDialog.Builder alertDialog, dialogbuilder;
    String id, expense, income, location, who, orderdate, position, when, person, comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        initializeObjects();
        loadSharedPreferences(StaticFields.SP_PORT);
        loadSharedPreferences(StaticFields.SP_INTERNET_ADDRESS);
        loadSharedPreferences(StaticFields.SP_PERSON);
        loadSharedPreferences(StaticFields.SP_MONEY_FOOD);
        loadSharedPreferences(StaticFields.SP_DEFAULT_POSITION);
        loadSharedPreferences(StaticFields.SP_DEFAULT_LOCATION);
        getOutputFromDatabase(StaticFields.INCOME);
        getOutputFromDatabase(StaticFields.EXPENSE);
        getOutputFromDatabase(StaticFields.SAVINGS);
        getOutputFromDatabase(StaticFields.FOOD);
        getOutputFromDatabase(StaticFields.ALL);
        getOutputFromDatabase(StaticFields.AVERAGE_FOOD);
        getOutputFromDatabase(StaticFields.AVERAGE_FOOD_UNTIL_END_OF_MONTH);
        getOutputFromDatabase(StaticFields.SUM_SPENDING_FOOD_BEGINNING_OF_YEAR);
        getOutputFromDatabase(StaticFields.SUM_INCOME_YEAR);
        sendInputAndReceiveOutputFromDatabase(StaticFields.SUM_FOOD_JULIA_MONTH);
        sendInputAndReceiveOutputFromDatabase(StaticFields.SUM_FOOD_BERND_MONTH);
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
            case StaticFields.SP_MONEY_FOOD:
                sharedPref_Food = s1;
                break;
            case StaticFields.SP_DEFAULT_POSITION:
                sharedPref_Position = s1;
                spinnerPosition.setSelection(getIndex(spinnerPosition, sharedPref_Position));
                break;
            case StaticFields.SP_DEFAULT_LOCATION:
                sharedPref_Location = s1;
                spinnerLocation.setSelection(getIndex(spinnerLocation, sharedPref_Location));
                break;
        }
    }

    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }

    private void initializeObjects() {
        //set the toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);

        editTextDate = findViewById(R.id.editTextDate);
        editTextDate.setText(setDateCorrectly());

        editTextComment = findViewById(R.id.editTextComment);

        totalIncome = findViewById(R.id.textViewTotalIncome);
        totalExpense = findViewById(R.id.textViewTotalExpense);
        totalSavings = findViewById(R.id.textViewTotalSavings);
        totalFood = findViewById(R.id.textViewTotalFood);
        textViewAverageTotalFood = findViewById(R.id.textViewAverageTotalFood);
        textViewReservedAverageDayFood = findViewById(R.id.textViewReservedAverageDayFood);
        textViewTotalYearFood = findViewById(R.id.textViewTotalYearFood);
        textViewTotalYearIncome = findViewById(R.id.textViewTotalYearIncome);
        textViewSumFoodJuliaMonth  = findViewById(R.id.textViewSumFoodJuliaMonth);
        textViewSumFoodBerndMonth  = findViewById(R.id.textViewSumFoodBerndMonth);

        fabAddButton = findViewById(R.id.addButton);
        fabAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInputFields()) {
                    sendInputToDatabase();
                    //deactivate fabAddButton and reset all textfields to 0
                    fabAddButton.setEnabled(false);
                    //update all fields from overview
                    getOutputFromDatabase(StaticFields.INCOME);
                    getOutputFromDatabase(StaticFields.EXPENSE);
                    getOutputFromDatabase(StaticFields.SAVINGS);
                    getOutputFromDatabase(StaticFields.FOOD);
                    getOutputFromDatabase(StaticFields.ALL);
                    getOutputFromDatabase(StaticFields.AVERAGE_FOOD);
                    getOutputFromDatabase(StaticFields.AVERAGE_FOOD_UNTIL_END_OF_MONTH);
                    getOutputFromDatabase(StaticFields.SUM_SPENDING_FOOD_BEGINNING_OF_YEAR);
                    getOutputFromDatabase(StaticFields.SUM_INCOME_YEAR);
                    sendInputAndReceiveOutputFromDatabase(StaticFields.SUM_FOOD_JULIA_MONTH);
                    sendInputAndReceiveOutputFromDatabase(StaticFields.SUM_FOOD_BERND_MONTH);
                    resetEditText();
                }
            }
        });
        fabListButton = (ExtendedFloatingActionButton) findViewById(R.id.listButton);
        fabListButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Month currentMonth = LocalDate.now().getMonth();

                 //when sending a put request and then clicking on show list,
                 //the last entry is always missing, therefore initiating an refresh
                 refreshAndRequestOutputFromDatabase(false);

                 alertDialog = new AlertDialog.Builder(MainActivity.this);
                 alertDialog.setTitle("" + currentMonth);
                 View rowList = getLayoutInflater().inflate(R.layout.row, null);
                 ListView listView = rowList.findViewById(R.id.listView);
                 adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayListOfIncomeAndExpense);
                 if(!adapter.isEmpty()) {
                     listView.setAdapter(adapter);
                     listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                         @Override
                         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                             dialog.dismiss();
                             editableAlertDialog(arrayListOfIncomeAndExpense.get(i));
                         }

                         private void editableAlertDialog(String arrayListOfIncomeAndExpense) {
                             id = split_arrayListOfIncomeAndExpense("id", arrayListOfIncomeAndExpense);
                             when = split_arrayListOfIncomeAndExpense("when", arrayListOfIncomeAndExpense);
                             person = split_arrayListOfIncomeAndExpense("person", arrayListOfIncomeAndExpense);
                             location = split_arrayListOfIncomeAndExpense("location", arrayListOfIncomeAndExpense);
                             income = split_arrayListOfIncomeAndExpense("income", arrayListOfIncomeAndExpense);
                             expense = split_arrayListOfIncomeAndExpense("expense", arrayListOfIncomeAndExpense);
                             position = split_arrayListOfIncomeAndExpense("position", arrayListOfIncomeAndExpense);
                             comment = split_arrayListOfIncomeAndExpense("comment", arrayListOfIncomeAndExpense);

                             LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                             View dialogview = inflater.inflate(R.layout.custom_alert_dialog, null);
                             dialogbuilder = new AlertDialog.Builder(MainActivity.this);
                             dialogbuilder.setTitle("Update fields");
                             dialogbuilder.setView(dialogview);
                             dialogDetails = dialogbuilder.create();
                             dialogDetails.show();

                             editText_Id = dialogview.findViewById(R.id.editText_Id);
                                editText_Id.setText(id);
                             editText_When = dialogview.findViewById(R.id.editText_When);
                                editText_When.setText(when);
                             editText_Person = dialogview.findViewById(R.id.editText_Person);
                                editText_Person.setText(person);
                             editText_Location = dialogview.findViewById(R.id.editText_Location);
                                editText_Location.setText(location);
                             editText_Income = dialogview.findViewById(R.id.editText_Income);
                                editText_Income.setText(income);
                             editText_Expense = dialogview.findViewById(R.id.editText_Expense);
                                editText_Expense.setText(expense);
                             editText_Position = dialogview.findViewById(R.id.editText_Position);
                                editText_Position.setText(position);
                             editText_Comment = dialogview.findViewById(R.id.editText_Comment);
                                editText_Comment.setText(comment);
                         }

                         private String split_arrayListOfIncomeAndExpense(String split_string, String arrayListOfIncomeAndExpense) {
                             String result_split = null;
                             String[] splitted_id;

                             switch (split_string) {
                                 case "id":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     String[] id = splitted_id[0].split("\\s");
                                     result_split = id[1];
                                     return result_split;
                                 case "when":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     result_split = splitted_id[1].substring(6);
                                     return result_split;
                                 case "person":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     String[] person = splitted_id[2].split("\\s");
                                     result_split = person[1];
                                     return result_split;
                                 case "location":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     String[] location = splitted_id[3].split("\\s");
                                     result_split = location[1];
                                     return result_split;
                                 case "income":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     String[] income = splitted_id[4].split("\\s");
                                     result_split = income[1];
                                     return result_split;
                                 case "expense":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     String[] expense = splitted_id[5].split("\\s");
                                     result_split = expense[1];
                                     return result_split;
                                 case "position":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     String[] position = splitted_id[6].split("\\s");
                                     result_split = position[1];
                                     return result_split;
                                 case "comment":
                                     splitted_id = arrayListOfIncomeAndExpense.split("\n");
                                     String[] split_comment = splitted_id[7].split("\\s", 1);
                                     String[] result_splitted = split_comment[0].split("\\s");
                                     if(result_splitted[1]==null) {
                                         result_splitted[1]=getString(R.string.general_placeholder);
                                         result_split=result_splitted[1];
                                     } else {
                                         result_split = result_splitted[1];
                                     }
                                     return result_split;
                             }
                             return result_split;
                         }
                     });
                 }
                 adapter.notifyDataSetChanged();
                 alertDialog.setView(rowList);
                 alertDialog.setPositiveButton(R.string.button_return, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         refreshAndRequestOutputFromDatabase(false);
                     }
                 });
                 dialog = alertDialog.create();
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
        spinnerPosition = findViewById(R.id.spinnerPosition);
        String[] itemsPosition = new String[]{
                String.valueOf(Spending.Amazon),
                String.valueOf(Spending.Car),
                String.valueOf(Spending.Depot),
                String.valueOf(Spending.Expense),
                String.valueOf(Spending.Dr_W_W_Donath_Immobilienverwaltung),
                String.valueOf(Spending.Food),
                String.valueOf(Spending.GTE_Gebäude_Technik_Energie_Betrieb),
                String.valueOf(Spending.IKEA),
                String.valueOf(Spending.Income),
                String.valueOf(Spending.Internet),
                String.valueOf(Spending.Loan_Bank99),
                String.valueOf(Spending.Loan_Unicredit_Bank_Austria),
                String.valueOf(Spending.Moebel_Ludwig),
                String.valueOf(Spending.Netflix),
                String.valueOf(Spending.Obi),
                String.valueOf(Spending.Pharmacy),
                String.valueOf(Spending.Strom_Wien_Energie),
                String.valueOf(Spending.Squandering),
                String.valueOf(Spending.Telephone),
                String.valueOf(Spending.Wiener_Netze),
                String.valueOf(Spending.Wiener_Staedtische_Versicherung_AG),
                String.valueOf(Spending.XXXLutz),
                String.valueOf(Spending.Youtube),};
            ArrayAdapter<String> adaperPosition = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsPosition);
            spinnerPosition.setAdapter(adaperPosition);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        String[] itemsLocation = new String[]{
                String.valueOf(Location.Hollgasse_1_1),
                String.valueOf(Location.Hollgasse_1_54),
                String.valueOf(Location.Stipcakgasse_8_1_4),
                String.valueOf(Location.Ludwika_Pasteura)};
        ArrayAdapter<String> adapterLocation = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsLocation);
        spinnerLocation.setAdapter(adapterLocation);
        editTextDate = findViewById(R.id.editTextDate);
        arrayListOfIncomeAndExpense = new ArrayList<>();
//        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        // Refresh  the layout
//        swipeRefreshLayout.setOnRefreshListener(
//                new SwipeRefreshLayout.OnRefreshListener() {
//                    @Override
//                    public void onRefresh() {
//                        refreshAndRequestOutputFromDatabase(false);
//                    }
//                }
//        );
    }

    private void sendInputAndReceiveOutputFromDatabase(String sumFood) {
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url = null;

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        if(sumFood.equals("sumFoodJuliaMonth"))
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_FOOD_SPEND_JULIA +
                    sharedPref_Food;
        else if(sumFood.equals("sumFoodBerndMonth")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_FOOD_SPEND_BERND +
                    sharedPref_Food;
        }

        StringRequest request = new StringRequest(Request.Method.GET, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //on below line we are parsing the response
                //to json object to extract data from it.
                try {
                    JSONObject respObj = new JSONObject(response);
                    if(respObj != null) {
                        JSONObject locs = respObj.getJSONObject("incomeexpense");
                        JSONArray recs = locs.getJSONArray("Total income");

                        String repl = recs.getString(0);
                        if (sumFood.equals("sumFoodJuliaMonth")) {
                            textViewSumFoodJuliaMonth.setText(repl);
                        } else if (sumFood.equals("sumFoodBerndMonth")) {
                            textViewSumFoodBerndMonth.setText(repl);
                        }
                    }
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
        });
        queue.add(request);
    }

    private void refreshAndRequestOutputFromDatabase(boolean isRefreshing) {
        getOutputFromDatabase(StaticFields.INCOME);
        getOutputFromDatabase(StaticFields.EXPENSE);
        getOutputFromDatabase(StaticFields.SAVINGS);
        getOutputFromDatabase(StaticFields.FOOD);
        getOutputFromDatabase(StaticFields.ALL);
        getOutputFromDatabase(StaticFields.AVERAGE_FOOD);
        getOutputFromDatabase(StaticFields.AVERAGE_FOOD_UNTIL_END_OF_MONTH);
        getOutputFromDatabase(StaticFields.SUM_SPENDING_FOOD_BEGINNING_OF_YEAR);
        getOutputFromDatabase(StaticFields.SUM_INCOME_YEAR);
        getOutputFromDatabase(StaticFields.SUM_FOOD_JULIA_MONTH);
        getOutputFromDatabase(StaticFields.SUM_FOOD_BERND_MONTH);
//        swipeRefreshLayout.setRefreshing(isRefreshing);
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
                || spinnerPosition.getSelectedItem().toString().isEmpty()
                || editTextDate.getText().toString().isEmpty()
                || spinnerLocation.getSelectedItem().toString().isEmpty()) {
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
        alertDialog = new AlertDialog.Builder(MainActivity.this);
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
                String position = spinnerPosition.getSelectedItem().toString();
                String income = editTextIncome.getText().toString();
                String expense = editTextSpending.getText().toString();
                String location = spinnerLocation.getSelectedItem().toString();
                String comment = editTextComment.getText().toString();

                String[] orderDate = orderdate.split("/");
                String orderDateAsYYYYMMDD = orderDate[2] + "-" + orderDate[1] + "-" + orderDate[0];

                params.put("orderdate", orderDateAsYYYYMMDD);
                params.put("who", who);
                params.put("position", position);
                params.put("income", income);
                params.put("expense", expense);
                params.put("location", location);
                params.put("comment", comment);

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
        } else if (incomeOrExpenseOrSavingsOrFood.equals("averageDayPerMonth")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_AVERAGE_FOOD_DAY_OF_MONTH;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("averageDayUntilEndOfMonth")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_RESERVED_PER_DAY_UNTIL_END_OF_MONTH;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("sumSpendingFoodBeginningOfYear")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_SPENDING_FOOD_BEGINNING_OF_YEAR;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("sumIncomeYear")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_INCOME_YEAR;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("sumFoodJuliaMonth")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_FOOD_SPEND_JULIA;
        } else if (incomeOrExpenseOrSavingsOrFood.equals("sumFoodBerndMonth")) {
            url = StaticFields.PROTOCOL +
                    sharedPref_IP +
                    StaticFields.COLON +
                    sharedPref_Port +
                    StaticFields.REST_URL_GET_SUM_FOOD_SPEND_BERND;
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
                            JSONObject locs = obj.getJSONObject("incomeexpense");
                            JSONArray recs = locs.getJSONArray("Total income");

                            String repl = recs.getString(0);

                            if (incomeOrExpenseOrSavingsOrFood.equals("income") && repl.equals("null")) {
                                totalIncome.setText("0");
                                totalIncomeInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("income") && !repl.equals("null")) {
                                totalIncome.setText(repl);
                                totalIncomeStatic = Float.valueOf(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("expense") && repl.equals("null")) {
                                totalExpense.setText("0");
                                totalExpenseInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("expense") && !repl.equals("null")) {
                                totalExpense.setText(repl);
                                totalExpenseStatic = Float.valueOf(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("savings") && repl.equals("null")) {
                                totalSavings.setText("0");
                                totalSavingsInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("savings") && !repl.equals("null")) {
                                totalSavings.setText(repl);
                                totalSavingsStatic = Float.valueOf(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("food") && repl.equals("null")) {
                                totalFood.setText("0");
                                totalFoodInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("food") && !repl.equals("null")) {
                                totalFood.setText(repl);
                                totalFoodStatic = Float.valueOf(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("all") && repl.equals("null")) {
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("all") && !repl.equals("null")) {
                                arrayListOfIncomeAndExpense.add(repl.toString());
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("averageDayPerMonth") && repl.equals("null")) {
                                textViewAverageTotalFood.setText("0");
                                averageFoodPerDayOfMonthInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("averageDayPerMonth") && !repl.equals("null")) {
                                textViewAverageTotalFood.setText(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("averageDayUntilEndOfMonth") && repl.equals("null")) {
                                textViewReservedAverageDayFood.setText("0");
                                reservedAverageDayFoodInt = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("averageDayUntilEndOfMonth") && !repl.equals("null")) {
                                textViewReservedAverageDayFood.setText(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("sumSpendingFoodBeginningOfYear") && repl.equals("null")) {
                                textViewTotalYearFood.setText("0");
                                totalYearFood = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("sumSpendingFoodBeginningOfYear") && !repl.equals("null")) {
                                textViewTotalYearFood.setText(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("sumIncomeYear") && repl.equals("null")) {
                                textViewTotalYearIncome.setText("0");
                                totalYearFood = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("sumIncomeYear") && !repl.equals("null")) {
                                textViewTotalYearIncome.setText(repl);
                            }/* else if (incomeOrExpenseOrSavingsOrFood.equals("sumFoodJuliaMonth") && repl.equals("null")) {
                                textViewSumFoodJuliaMonth.setText("0");
                                totalYearFood = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("sumFoodJuliaMonth") && !repl.equals("null")) {
                                textViewSumFoodJuliaMonth.setText(repl);
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("sumFoodBerndMonth") && repl.equals("null")) {
                                textViewSumFoodBerndMonth.setText("0");
                                totalYearFood = 0;
                            } else if (incomeOrExpenseOrSavingsOrFood.equals("sumFoodBerndMonth") && !repl.equals("null")) {
                                textViewSumFoodBerndMonth.setText(repl);
                            }*/
                        } else {
                            JSONArray array = obj.optJSONArray("incomeexpense");
                            //when coming back from AlertDialog, array has to be emptied,
                            //if not array will be doubled, tripled, ...
                            arrayListOfIncomeAndExpense.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsn = array.getJSONObject(i);
                                id = jsn.getString("id");
                                expense = jsn.getString("expense");
                                income = jsn.getString("income");
                                location = jsn.getString("location");
                                who = jsn.getString("who");
                                orderdate = jsn.getString("orderdate");
                                position = jsn.getString("position");
                                comment = jsn.getString("comment");
                                arrayListOfIncomeAndExpense.add("Id: " + id + "\nWhen: " + orderdate +
                                        "\nPerson: " + who + "\nLocation: " + location +
                                        "\nIncome: " + income + "\nExpense: " + expense +
                                        "\nPosition: " + position +
                                        "\nComment: " + comment);
                            }
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
    }

    private void checkPermissions() {
        checkPermission(Manifest.permission.CAMERA, StaticFields.CAMERA_PERMISSION_CODE);
        checkPermission(Manifest.permission.INTERNET, StaticFields.INTERNET_PERMISSION_CODE);
        checkPermission(Manifest.permission.ACCESS_WIFI_STATE, StaticFields.WIFI_PERMISSION_CODE);
    }

    public void showMenu(MenuItem item) {
        getOutputFromDatabase(StaticFields.INCOME);
        onOptionsItemSelected(item);
    }
    public void showBarChart(MenuItem item) {
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
        loadSharedPreferences(StaticFields.SP_MONEY_FOOD);
        resetEditText();
    }

    private void resetEditText() {
        editTextIncome.setText("0");
        editTextSpending.setText("0");
     }

    public void cancelButton(View view) {
        dialogDetails.dismiss();
    }

    private void updateFields(String id) {
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        // on below line we are calling a string
        // request method to post the data to our API
        // in this we are calling a post method.
        String url = StaticFields.PROTOCOL +
                sharedPref_IP +
                StaticFields.COLON +
                sharedPref_Port +
                StaticFields.REST_URL_PUT + id;
        StringRequest request = new StringRequest(Request.Method.PUT, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //on below line we are parsing the response
                //to json object to extract data from it.
                try {
                    Log.d("UpdateFields", response);
                    dialogDetails.dismiss();
                    JSONObject respObj = new JSONObject(response);
                    Toast.makeText(getApplicationContext(), "Entry was updated successfully!", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Log.e(TAG, getString(R.string.log_json_message, e.toString()));
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // method to handle errors.
                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e("volley error", e.toString());
                }
                Toast.makeText(MainActivity.this, getString(R.string.error_fail_response, error), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // below line we are creating a map for
                // storing our values in key and value pair.
                Map<String, String> params = new HashMap<String, String>();

                java.util.Date date = new Date(editText_When.getText().toString());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String orderDateAsYYYYMMDD = formatter.format(date);

                params.put("id", editText_Id.getText().toString());
                params.put("orderdate", orderDateAsYYYYMMDD);
                params.put("who", editText_Person.getText().toString());
                params.put("position", editText_Position.getText().toString());
                params.put("income", editText_Income.getText().toString());
                params.put("expense", editText_Expense.getText().toString());
                params.put("location", editText_Location.getText().toString());
                params.put("comment", editText_Comment.getText().toString());

                // at last we are
                // returning our params.
                return params;
            }
        };
        //below line is to make
        //a json object request.
        queue.add(request);
    }

    public void updateButton(View view) {
        String id = editText_Id.getText().toString();
        updateFields(id);
    }
}