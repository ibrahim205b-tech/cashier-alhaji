package iq.alhaji.cashier;

import android.content.Intent;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CallerScreeningService
        extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details details) {

        CallResponse response =
            new CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(true)
                .setSkipNotification(false)
                .build();

        respondToCall(details, response);

        Uri handle = details.getHandle();

        if (handle == null ||
                !"tel".equals(handle.getScheme())) {
            return;
        }

        String number = handle.getSchemeSpecificPart();

        if (number == null || number.trim().isEmpty()) {
            return;
        }

        Intent intent = new Intent(
            this,
            MainActivity.class
        );

        intent.setAction(
            "iq.alhaji.cashier.INCOMING_NUMBER"
        );

        intent.putExtra("caller_number", number);

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
    }
}
