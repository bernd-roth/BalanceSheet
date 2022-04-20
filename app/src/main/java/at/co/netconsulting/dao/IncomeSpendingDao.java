package at.co.netconsulting.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import at.co.netconsulting.sqlite.IncomeSpending;

@Dao
public interface IncomeSpendingDao {
        @Query("SELECT * FROM IncomeSpending")
        List<IncomeSpending> getAll();

        @Query("SELECT * FROM IncomeSpending WHERE uid IN (:userIds)")
        List<IncomeSpending> loadAllByIds(int[] userIds);

        @Query("SELECT * FROM IncomeSpending WHERE first_name = :first_name")
        IncomeSpending findByName(String first_name);

        @Query("SELECT * FROM IncomeSpending WHERE whenIncomeSpending = :when")
        IncomeSpending findByDate(String when);

        @Insert
        void insertAll(IncomeSpending... users);

        @Delete
        void delete(IncomeSpending user);
}
