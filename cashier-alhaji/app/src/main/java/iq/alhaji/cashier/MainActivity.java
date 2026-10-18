package iq.alhaji.cashier;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private Db db;
    private LinearLayout root;
    private LinearLayout content;

    private EditText phoneInput;
    private EditText addressInput;
    private TextView basketText;

    private String callerNumber = "";
    private double total = 0;

    private long selectedDriver = 0;
    private long lastSavedOrder = -1;

    private final LinkedHashMap<Long, BasketLine> basket =
        new LinkedHashMap<>();

    private static class BasketLine {
        String name;
        double price;
        int quantity = 1;

        BasketLine(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        db = new Db(this);
        showLogin();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        receiveCaller(intent);
    }

    private void receiveCaller(Intent intent) {
        if (intent == null) return;

        if (!"iq.alhaji.cashier.INCOMING_NUMBER"
                .equals(intent.getAction())) {
            return;
        }

        String number = intent.getStringExtra("caller_number");

        if (number == null || number.trim().isEmpty()) {
            return;
        }

        callerNumber = number;

        // لا نستبدل رقم طلب بدأ الموظف بإدخال أصنافه.
        if (basket.isEmpty() && phoneInput != null) {
            phoneInput.setText(number);
        }
    }

    private void showLogin() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(0xFFFFF3DE);

        TextView title = text(
            "مطعم الحجي\nكاشير الدليفري",
            28
        );
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        EditText password = new EditText(this);
        password.setHint("رمز الدخول");
        password.setInputType(
            InputType.TYPE_CLASS_NUMBER |
            InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );
        root.addView(password);

        Button login = button("دخول");
        root.addView(login);

        login.setOnClickListener(v -> {
            if ("8876".equals(
                    password.getText().toString())) {
                showMain();
            } else {
                toast("رمز الدخول غير صحيح");
            }
        });

        setContentView(root);
    }

    private void showMain() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFF8EC);

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.setBackgroundColor(0xFFB3261E);

        addMenuButton(menu, "طلب جديد", this::newOrder);
        addMenuButton(menu, "الأصناف", this::manageDishes);
        addMenuButton(menu, "السائقون", this::manageDrivers);
        addMenuButton(menu, "الطلبات", this::showOrders);
        addMenuButton(menu, "التقارير", this::showReports);
        addMenuButton(menu, "الطابعة", this::testPrinter);

        root.addView(menu);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content
