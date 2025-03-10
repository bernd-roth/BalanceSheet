package at.co.netconsulting.general;

public class StaticFields {
    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    public static final int CAMERA_PERMISSION_CODE = 100;
    public static final int STORAGE_PERMISSION_CODE = 101;
    public static final int INTERNET_PERMISSION_CODE = 102;
    public static final int WIFI_PERMISSION_CODE = 103;
    //Settings Activity
    public static final String SP_PORT = "PORT";
    public static final String SP_INTERNET_ADDRESS = "INTERNET_ADDRESS";
    public static final String SP_PERSON = "PERSON";
    public static final String SP_MONEY_FOOD = "MONEY_FOOD";
    public static final String SP_DEFAULT_POSITION = "DEFAULT_POSITION";
    public static final String SP_DEFAULT_LOCATION = "DEFAULT_LOCATION";
    //Settings Activity
    public static final String PROTOCOL = "http://";
    public static final String REST_URL_ADD = "/incomeexpense/add";
    public static final String REST_URL_GET_ALL = "/incomeexpense/all";
    public static final String REST_URL_GET_SUM_INCOME = "/incomeexpense/sum_income";
    public static final String REST_URL_GET_SUM_EXPENSE = "/incomeexpense/sum_expense";
    public static final String REST_URL_GET_SUM_SAVINGS = "/incomeexpense/sum_savings";
    public static final String REST_URL_GET_SUM_FOOD = "/incomeexpense/sum_food";
    public static final String REST_URL_GET_AVERAGE_FOOD_DAY_OF_MONTH = "/incomeexpense/sum_average_spending_day_of_month";
    public static final String REST_URL_GET_SUM_RESERVED_PER_DAY_UNTIL_END_OF_MONTH = "/incomeexpense/sum_reserved_per_day_until_end_of_month";
    public static final String REST_URL_GET_SUM_SPENDING_FOOD_BEGINNING_OF_YEAR = "/incomeexpense/sum_spending_food_since_beginning_of_year";
    public static final String REST_URL_GET_SUM_INCOME_YEAR = "/incomeexpense/sum_income_year";
    public static final String REST_URL_GET_SUM_FOOD_SPEND_PER_PERSON = "/incomeexpense/sum_spending_food_per_person_per_month?person=";
    public static final String REST_URL_GET_SUM_FOOD_SPEND_BERND = "/incomeexpense/sum_spending_food_by_bernd_current_month?bernd_food=";
    public static final String REST_URL_PUT = "/incomeexpense/put/";
    public static final String COLON = ":";
    public static final String INCOME = "income";
    public static final String EXPENSE = "expense";
    public static final String SAVINGS = "savings";
    public static final String FOOD = "food";
    public static final String ALL = "all";
    public static final String AVERAGE_FOOD = "averageDayPerMonth";
    public static final String AVERAGE_FOOD_UNTIL_END_OF_MONTH = "averageDayUntilEndOfMonth";
    public static final String SUM_SPENDING_FOOD_BEGINNING_OF_YEAR = "sumSpendingFoodBeginningOfYear";
    public static final String SUM_INCOME_YEAR = "sumIncomeYear";
    public static final String SUM_FOOD_JULIA_MONTH = "sumFoodJuliaMonth";
    public static final String SUM_FOOD_BERND_MONTH = "sumFoodBerndMonth";
    public static final String SUM_FOOD_PERSON_MONTH = "sumFoodPersonMonth";
}
