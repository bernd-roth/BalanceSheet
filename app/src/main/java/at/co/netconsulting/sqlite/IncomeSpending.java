package at.co.netconsulting.sqlite;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "IncomeSpending")
public class IncomeSpending {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "whenIncomeSpending")
    public Date whenIncomeSpending;

    @ColumnInfo(name = "location")
    public String location;

    @ColumnInfo(name = "income")
    public Float income;

    @ColumnInfo(name = "spending")
    public Float spending;
}