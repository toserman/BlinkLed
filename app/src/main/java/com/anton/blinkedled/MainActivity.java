package com.anton.blinkedled;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 300;
    private static final String GPIO_PIN_NAME = "BCM13"; // Physical Pin #33 on Raspberry Pi3
    private Handler mHandler = new Handler();
    private Gpio mLedGpio;

    Button start_btn, stop_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        start_btn = (Button)findViewById(R.id.button_start);
        stop_btn = (Button)findViewById(R.id.button_stop);

        // Step 1. Create GPIO connection.
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            mLedGpio = service.openGpio(GPIO_PIN_NAME);
            // Step 2. Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            // Step 4. Repeat using a handler.
            //mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

        start_btn.setOnClickListener(this);
        stop_btn.setOnClickListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                Toast.makeText(getApplicationContext(), "Start Button", Toast.LENGTH_LONG).show();
                mHandler.post(mBlinkRunnable);
                break;
            case R.id.button_stop:
                Toast.makeText(getApplicationContext(), "Stop Button", Toast.LENGTH_LONG).show();
                mHandler.removeCallbacks(mBlinkRunnable);
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Step 4. Remove handler events on close.
        //mHandler.removeCallbacks(mBlinkRunnable);

        // Step 5. Close the resource.
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }


    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit if the GPIO is already closed
            if (mLedGpio == null) {
                return;
            }

            try {
                // Step 3. Toggle the LED state
                mLedGpio.setValue(!mLedGpio.getValue());

                // Step 4. Schedule another event after delay.
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}
