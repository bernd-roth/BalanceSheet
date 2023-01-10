package at.co.netconsulting.balancesheet;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
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
    long numOfDays;
    //    List<LocalDate> listOfDates;
//    ArrayList<String> finalDateArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chart);
        initializeObjects();
        drawChart();

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        setData(5, 100);
    }

    private void drawChart() {
        //get selected date
//        String date = spinnerDate.getSelectedItem().toString();
    }

    private void createListOfWeekDays() {
        //create day, month, and year list
//        startDate = LocalDate.of(2022, 8, 01);
//        endDate = startDate.plusMonths(29);

//        numOfDays = ChronoUnit.DAYS.between(startDate, endDate);

//        listOfDates = Stream.iterate(startDate, date -> date.plusDays(1))
//                .limit(numOfDays)
//                .collect(Collectors.toList());

//        for(int i =0 ; i < listOfDates.size(); i++)
//        {
//            finalDateArrayList.add(listOfDates.get(i).toString());
//        }
    }

    private void initializeObjects() {
        //findViewById
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_chart);
//        spinnerDate = findViewById(R.id.spinnerDate);
        chart = findViewById(R.id.barChart);

        //initialize common objects
//        finalDateArrayList = new ArrayList();
//        createListOfWeekDays();
//        createListOfMonths();

//        adapterDate = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, finalDateArrayList);
//        spinnerDate.setAdapter(adapterDate);
    }

    private void createListOfMonths() {
        //create day, month, and year list
//        startDate = LocalDate.of(2022, 8, 01);
//        endDate = startDate.plus(31);

//        numOfDays = ChronoUnit.DAYS.between(startDate, endDate);

//        listOfDates = Stream.iterate(startDate, date -> date.plusDays(1))
//                .limit(numOfDays)
//                .collect(Collectors.toList());

//        for(int i =0 ; i < listOfDates.size(); i++)
//        {
//            finalDateArrayList.add(listOfDates.get(i).toString());
//        }
    }

    private void setData(int count, float range) {

        float start = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = (int) start; i < start + count; i++) {
            float val = (float) (Math.random() * (range + 1));

//            if (Math.random() * 100 < 25) {
//                values.add(new BarEntry(i, val, getResources().getDrawable(R.drawable.star)));
//            } else {
                values.add(new BarEntry(i, val));
//            }
        }

        BarDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();

        } else {
            set1 = new BarDataSet(values, "The year 2021");

            set1.setDrawIcons(false);

            int startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light);
            int startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light);
            int startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light);
            int endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            int endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple);
            int endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark);
            int endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark);
            int endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark);

            List<GradientColor> gradientFills = new ArrayList<>();
            gradientFills.add(new GradientColor(startColor1, endColor1));
            gradientFills.add(new GradientColor(startColor2, endColor2));
            gradientFills.add(new GradientColor(startColor3, endColor3));
            gradientFills.add(new GradientColor(startColor4, endColor4));
            gradientFills.add(new GradientColor(startColor5, endColor5));

            set1.setGradientColors(gradientFills);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            chart.setData(data);
        }

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