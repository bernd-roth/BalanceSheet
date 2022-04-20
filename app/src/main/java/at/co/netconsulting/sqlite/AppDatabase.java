package at.co.netconsulting.sqlite;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import at.co.netconsulting.dao.IncomeSpendingDao;
import at.co.netconsulting.general.Converters;

@Database(entities = {IncomeSpending.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract IncomeSpendingDao incomeSpendingDao();
}