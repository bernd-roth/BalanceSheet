package at.co.netconsulting.balancesheet;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChartActivity extends BaseActivity {

    private Toolbar toolbar;
    private BarChart chart;
    private FloatingActionButton fabReturnButton;
    //    private Spinner spinnerDate;
    private ArrayAdapter<String> adapterDate;
    //    private LocalDate startDate, endDate;
    private long numOfDays;
    //    List<LocalDate> listOfDates;
//    ArrayList<String> finalDateArrayList;
    private float totalIncome, totalExpense, totalSavings, totalFood;
    private BarDataSet set;
    private BarData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chart);
        initializeObjects();
        getBundleValues();
        drawChart();
    }

    private void drawChart() {
//        chart.setDrawBarShadow(false);
//        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
//        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
//        chart.setPinchZoom(false);
//        chart.setDrawGridBackground(false);

        setLegend();
        setData();
    }

    private void setLegend() {
        Legend l = chart.getLegend();
//        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
//        l.setFormSize(9f);
//        l.setTextSize(9f);
//        l.setXEntrySpace(4f);

        LegendEntry l1=new LegendEntry("Total Income",Legend.LegendForm.CIRCLE,10f,2f,null, Color.GREEN);
        LegendEntry l2=new LegendEntry("Total Expense", Legend.LegendForm.CIRCLE,10f,2f,null,Color.RED);
        LegendEntry l3=new LegendEntry("Total Savings", Legend.LegendForm.CIRCLE,10f,2f,null,Color.BLUE);
        LegendEntry l4=new LegendEntry("Total Food", Legend.LegendForm.CIRCLE,10f,2f,null,Color.YELLOW);

        l.setCustom(new LegendEntry[]{l1,l2, l3, l4});
        l.setEnabled(true);
    }

    private void getBundleValues() {
        Bundle getBundle = this.getIntent().getExtras();
        totalIncome = getBundle.getFloat("totalIncome");
        totalExpense = getBundle.getFloat("totalExpense");
        totalSavings = getBundle.getFloat("totalSavings");
        totalFood = getBundle.getFloat("totalFood");
    }

    private void initializeObjects() {
        //findViewById
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_chart);
//        spinnerDate = findViewById(R.id.spinnerDate);
        chart = findViewById(R.id.barChart);
    }

    private void setData() {
        ArrayList<BarEntry> values = new ArrayList<>();

        values.add(new BarEntry(1, totalIncome));
        values.add(new BarEntry(2, totalExpense));
        values.add(new BarEntry(3, totalSavings));
        values.add(new BarEntry(4, totalFood));

//        if (chart.getData() != null &&
//                chart.getData().getDataSetCount() > 0) {
//            set = (BarDataSet) chart.getData().getDataSetByIndex(0);
//            set.setValues(values);
//            chart.getData().notifyDataChanged();
//            chart.notifyDataSetChanged();
//
//        } else {
            set = new BarDataSet(values, "Income, Expense, Savings, and Food for the year 2023");
            set.setColors(new int[]{Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW});
//            set.setDrawIcons(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set);

            data = new BarData(dataSets);
            data.setValueTextSize(12f);
//            data.setBarWidth(0.9f);

            chart.setData(data);
//        }

        fabReturnButton = findViewById(R.id.returnFab);
        fabReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }
    public void showBarChart(MenuItem item) {
        onOptionsItemSelected(item);
    }
}