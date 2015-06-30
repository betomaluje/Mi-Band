package com.betomaluje.android.miband;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.betomaluje.android.miband.core.MiBand;
import com.betomaluje.android.miband.core.bluetooth.MiBandService;
import com.betomaluje.android.miband.core.bluetooth.MiBandWrapper;
import com.betomaluje.android.miband.core.bluetooth.NotificationConstants;
import com.betomaluje.android.miband.core.colorpicker.ColorPickerDialog;
import com.betomaluje.android.miband.core.model.BatteryInfo;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    private final String TAG = getClass().getSimpleName();

    private int BT_REQUEST_CODE = 1001;

    private Button btn_connect, btn_lights, btn_lights_2, btn_vibrate, btn_baterry;
    private TextView textView_status;

    private boolean isConnected = false;

    private BroadcastReceiver bluetoothStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            String action = b.getString("type");

            if (action.equals(NotificationConstants.MI_BAND_CONNECT)) {
                isConnected = true;

                btn_connect.setEnabled(true);

                startMiBand();
            } else if (action.equals(NotificationConstants.MI_BAND_DISCONNECT)) {
                isConnected = false;

                int errorCode = b.getInt("errorCode");

                if (errorCode == NotificationConstants.BLUETOOTH_OFF) {
                    //turn on bluetooth
                    Log.d(TAG, "turn on Bluetooth");
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_REQUEST_CODE, null);
                } else {
                    Log.d(TAG, "not found");
                    stopMiBand();
                }

            } else if (action.equals(NotificationConstants.MI_BAND_BATTERY)) {
                BatteryInfo batteryInfo = b.getParcelable("battery");
                textView_status.setText(batteryInfo.toString());
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_lights = (Button) findViewById(R.id.btn_lights);
        btn_lights_2 = (Button) findViewById(R.id.btn_lights_2);
        btn_vibrate = (Button) findViewById(R.id.btn_vibrate);
        btn_baterry = (Button) findViewById(R.id.btn_baterry);

        textView_status = (TextView) findViewById(R.id.textView_status);

        btn_connect.setOnClickListener(btnListener);
        btn_lights.setOnClickListener(btnListener);
        btn_lights_2.setOnClickListener(btnListener);
        btn_vibrate.setOnClickListener(btnListener);
        btn_baterry.setOnClickListener(btnListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothStatusReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //we are listening from the mi band service
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(bluetoothStatusReceiver, new IntentFilter(NotificationConstants.ACTION_MIBAND_SERVICE));
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_connect:
                    if (!isConnected)
                        connectToMiBand();
                    else
                        disconnectMiBand();
                    break;
                case R.id.btn_lights:

                    new ColorPickerDialog(MainActivity.this, 255, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int rgb) {
                            Log.i(TAG, "selected color: " + rgb);

                            textView_status.setText("Playing with lights! Color: " + rgb);

                            HashMap<String, Integer> params = new HashMap<String, Integer>();
                            params.put("color", rgb);

                            MiBand.sendAction(MiBandWrapper.ACTION_LIGHTS, params);
                        }
                    }).show();

                    break;
                case R.id.btn_lights_2:

                    break;
                case R.id.btn_vibrate:
                    textView_status.setText("Vibrating");
                    MiBand.sendAction(MiBandWrapper.ACTION_VIBRATE);
                    break;
                case R.id.btn_baterry:
                    MiBand.sendAction(MiBandWrapper.ACTION_BATTERY);
                    break;
            }
        }
    };


    private void connectToMiBand() {
        MiBand.init(MainActivity.this);

        btn_connect.setEnabled(false);

        textView_status.setText("Connecting...");
    }

    private void disconnectMiBand() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set title
        alertDialogBuilder.setTitle("Disconnect to Mi Band");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to Disconnect to your Mi Band?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MiBand.disconnect();
                        stopMiBand();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void startMiBand() {
        btn_connect.setText("Disconnect");

        textView_status.setText("Connected");

        btn_lights.setEnabled(true);
        btn_lights_2.setEnabled(true);
        btn_vibrate.setEnabled(true);
        btn_baterry.setEnabled(true);
    }

    private void stopMiBand() {
        btn_connect.setText("Connect");

        textView_status.setText("Disconnected");

        btn_connect.setEnabled(true);

        btn_lights.setEnabled(false);
        btn_lights_2.setEnabled(false);
        btn_vibrate.setEnabled(false);
        btn_baterry.setEnabled(false);

        isConnected = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                connectToMiBand();
            } else {
                stopMiBand();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
