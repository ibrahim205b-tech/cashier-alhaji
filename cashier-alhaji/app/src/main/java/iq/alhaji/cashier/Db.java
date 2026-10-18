package iq.alhaji.cashier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Db extends SQLiteOpenHelper {

    public Db(Context context) {
        super(context, "alhaji_cashier.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
            "CREATE TABLE dishes (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "price REAL NOT NULL," +
            "category TEXT DEFAULT 'عام')"
        );

        db.execSQL(
            "CREATE TABLE drivers (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "phone TEXT DEFAULT ''," +
            "active INTEGER DEFAULT 1)"
        );

        db.execSQL(
            "CREATE TABLE orders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "created TEXT NOT NULL," +
            "phone TEXT DEFAULT ''," +
            "address TEXT DEFAULT ''," +
            "driver_id INTEGER DEFAULT 0," +
            "status TEXT DEFAULT 'جديد'," +
            "total REAL NOT NULL," +
            "items TEXT NOT NULL)"
        );

        db.execSQL(
            "CREATE TABLE audit (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "created TEXT," +
            "action TEXT," +
            "details TEXT)"
        );
    }

    @Override
    public void onUpgrade(
        SQLiteDatabase db,
        int oldVersion,
        int newVersion
    ) {
        // لا تحذف بيانات المستخدم عند تحديث التطبيق.
    }

    public static String now() {
        return new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(new Date());
    }

    public void audit(String action, String details) {
        ContentValues values = new ContentValues();
        values.put("created", now());
        values.put("action", action);
        values.put("details", details);

        getWritableDatabase().insert(
            "audit", null, values
        );
    }

    public Cursor rows(String sql) {
        return getReadableDatabase().rawQuery(sql, null);
    }

    public long addDish(
        String name,
        double price,
        String category
    ) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("price", price);
        values.put("category", category);

        long id = getWritableDatabase().insert(
            "dishes", null, values
        );

        audit("إضافة صنف", name + " / " + price);
        return id;
    }

    public void updateDish(
        long id,
        String name,
        double price,
        String category
    ) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("price", price);
        values.put("category", category);

        getWritableDatabase().update(
            "dishes",
            values,
            "id=?",
            new String[]{String.valueOf(id)}
        );

        audit("تعديل صنف", name + " / " + price);
    }

    public void deleteDish(long id, String name) {
        getWritableDatabase().delete(
            "dishes",
            "id=?",
            new String[]{String.valueOf(id)}
        );

        audit("حذف صنف", name);
    }

    public long addDriver(String name, String phone) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);

        long id = getWritableDatabase().insert(
            "drivers", null, values
        );

        audit("إضافة سائق", name);
        return id;
    }

    public long addOrder(
        String phone,
        String address,
        long driverId,
        double total,
        String items
    ) {
        ContentValues values = new ContentValues();
        values.put("created", now());
        values.put("phone", phone);
        values.put("address", address);
        values.put("driver_id", driverId);
        values.put("status", "جديد");
        values.put("total", total);
        values.put("items", items);

        long id = getWritableDatabase().insert(
            "orders", null, values
        );

        audit(
            "إنشاء طلب",
            "#" + id + " / " + total
        );

        return id;
    }

    public void changeOrderStatus(long id, String status) {
        ContentValues values = new ContentValues();
        values.put("status", status);

        getWritableDatabase().update(
            "orders",
            values,
            "id=?",
            new String[]{String.valueOf(id)}
        );

        audit(
            "تغيير حالة الطلب",
            "#" + id + " / " + status
        );
    }
}
