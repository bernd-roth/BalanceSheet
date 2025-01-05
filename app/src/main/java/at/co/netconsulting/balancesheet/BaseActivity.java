package at.co.netconsulting.balancesheet;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.math.BigInteger;

public class BaseActivity extends Activity {
    private Intent intent;
    private float totalIncome;
    private float totalExpense;
    private float totalSavings;
    private float totalFood;
    protected static boolean isAddButtonOperation = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main:
                intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.action_barChart:
                getValuesForChart();
                intent = new Intent(this, ChartActivity.class);
                intent.putExtra("totalIncome",totalIncome);
                intent.putExtra("totalExpense",totalExpense);
                intent.putExtra("totalSavings",totalSavings);
                intent.putExtra("totalFood",totalFood);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getValuesForChart() {
        totalIncome = MainActivity.totalIncomeStatic;
        totalExpense = MainActivity.totalExpenseStatic;
        totalSavings = MainActivity.totalSavingsStatic;
        totalFood = MainActivity.totalFoodStatic;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAddButtonOperation = false;
    }
}
