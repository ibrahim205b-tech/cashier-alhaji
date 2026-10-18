package iq.alhaji.cashier;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BtPrinter {

    private final Activity activity;

    public BtPrinter(Activity activity) {
        this.activity = activity;
    }

    public boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            return activity.checkSelfPermission(
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            activity.requestPermissions(
                new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                },
                400
            );
        }
    }

    public List<BluetoothDevice> pairedDevices()
            throws Exception {

        if (!hasPermission()) {
            requestPermission();
            throw new Exception(
                "امنح صلاحية البلوتوث ثم أعد المحاولة"
            );
        }

        BluetoothAdapter adapter =
            BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            throw new Exception(
                "هذا الجهاز لا يدعم البلوتوث"
            );
        }

        if (!adapter.isEnabled()) {
            throw new Exception(
                "شغّل البلوتوث من إعدادات الجهاز"
            );
        }

        return new ArrayList<>(
            adapter.getBondedDevices()
        );
    }

    public void print(
        BluetoothDevice device,
        String receipt
    ) throws Exception {

        if (!hasPermission()) {
            throw new Exception(
                "صلاحية البلوتوث غير متوفرة"
            );
        }

        UUID uuid = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB"
        );

        BluetoothSocket socket =
            device.createRfcommSocketToServiceRecord(uuid);

        try {
            socket.connect();

            OutputStream output = socket.getOutputStream();

            // تهيئة الطابعة
            output.write(new byte[]{0x1B, 0x40});

            // محاذاة وسطية لاسم المطعم
            output.write(new byte[]{0x1B, 0x61, 0x01});

            output.write(
                "مطعم الحجي\n".getBytes(
                    Charset.forName("UTF-8")
                )
            );

            // محاذاة النص إلى اليسار
            output.write(new byte[]{0x1B, 0x61, 0x00});

            output.write(
                receipt.getBytes(
                    Charset.forName("UTF-8")
                )
            );

            output.write(
                "\n\n\n".getBytes(
                    Charset.forName("UTF-8")
                )
            );

            // أمر القص إن كانت الطابعة تدعمه
            output.write(new byte[]{0x1D, 0x56, 0x00});

            output.flush();

        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}
