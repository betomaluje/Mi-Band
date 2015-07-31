package com.betomaluje.android.miband.example.activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.betomaluje.android.miband.example.R;
import com.betomaluje.android.miband.example.WaterReceiver;
import com.betomaluje.android.miband.example.WaterScheduler;
import com.betomaluje.android.miband.example.services.MiBandService;
import com.betomaluje.miband.MiBand;
import com.betomaluje.miband.bluetooth.MiBandWrapper;
import com.betomaluje.miband.bluetooth.NotificationConstants;
import com.betomaluje.miband.colorpicker.ColorPickerDialog;
import com.betomaluje.miband.model.BatteryInfo;

import java.util.HashMap;

public class MainServiceActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private int BT_REQUEST_CODE = 1001;

    private Button btn_connect, btn_lights, btn_lights_2, btn_notification, btn_vibrate, btn_battery, btn_water, btn_water_cancel, btn_apps, btn_sync, btn_chart, btn_chart_2;
    private TextView textView_status;

    private boolean isConnected = false;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

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
            } else if (action.equals(NotificationConstants.MI_BAND_REQUEST_CONNECTION) && b.containsKey("isConnected")) {
                isConnected = b.getBoolean("isConnected", false);
                Log.d(TAG, "requested connection: " + isConnected);

                if (isConnected) {
                    startMiBand();
                } else {
                    stopMiBand();
                }
            } else if (action.equals("CANCEL_WATER")) {
                if (alarmManager != null)
                    alarmManager.cancel(pendingIntent);
            } else if (action.equals(NotificationConstants.MI_BAND_SYNC_SUCCESS)) {
                textView_status.setText("Sync completed");
            }else if (action.equals(NotificationConstants.MI_BAND_SYNC_FAIL)) {
                textView_status.setText("Sync error: " + b.getString("errorCode", "default"));
            }
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main_band;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent alarmIntent = new Intent(this, WaterReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_lights = (Button) findViewById(R.id.btn_lights);
        btn_lights_2 = (Button) findViewById(R.id.btn_lights_2);
        btn_notification = (Button) findViewById(R.id.btn_notification);
        btn_vibrate = (Button) findViewById(R.id.btn_vibrate);
        btn_battery = (Button) findViewById(R.id.btn_battery);
        btn_water = (Button) findViewById(R.id.btn_water);
        btn_water_cancel = (Button) findViewById(R.id.btn_water_cancel);
        btn_apps = (Button) findViewById(R.id.btn_apps);
        btn_sync = (Button) findViewById(R.id.btn_sync);
        btn_chart = (Button) findViewById(R.id.btn_chart);
        btn_chart_2 = (Button) findViewById(R.id.btn_chart_2);

        textView_status = (TextView) findViewById(R.id.textView_status);

        btn_connect.setOnClickListener(btnListener);
        btn_lights.setOnClickListener(btnListener);
        btn_lights_2.setOnClickListener(btnListener);
        btn_notification.setOnClickListener(btnListener);
        btn_vibrate.setOnClickListener(btnListener);
        btn_battery.setOnClickListener(btnListener);
        btn_water.setOnClickListener(btnListener);
        btn_water_cancel.setOnClickListener(btnListener);
        btn_apps.setOnClickListener(btnListener);
        btn_sync.setOnClickListener(btnListener);
        btn_chart.setOnClickListener(btnListener);
        btn_chart_2.setOnClickListener(btnListener);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getBoolean("service", false)) {
                stopService(new Intent(MainServiceActivity.this, MiBandService.class));
            } else {
                connectToMiBand();
            }
        } else {
            connectToMiBand();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(MainServiceActivity.this).unregisterReceiver(bluetoothStatusReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //resetToolbar();

        //we are listening from the mi band service
        LocalBroadcastManager.getInstance(MainServiceActivity.this).registerReceiver(bluetoothStatusReceiver, new IntentFilter(NotificationConstants.ACTION_MIBAND_SERVICE));

        isConnected = false;
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MiBandService.class.getName().equals(service.service.getClassName())) {
                MiBandWrapper.getInstance(MainServiceActivity.this).sendAction(MiBandWrapper.ACTION_REQUEST_CONNECTION);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MiBandWrapper.getInstance(MainServiceActivity.this).sendAction(MiBandWrapper.ACTION_STOP_SYNC);
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
                    new ColorPickerDialog(MainServiceActivity.this, 255, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int rgb) {
                            Log.i(TAG, "selected color: " + rgb);

                            textView_status.setText("Playing with lights! Color: " + rgb);

                            HashMap<String, Integer> params = new HashMap<String, Integer>();
                            params.put(NotificationConstants.KEY_COLOR, rgb);

                            MiBand.sendAction(MiBandWrapper.ACTION_LIGHTS, params);
                        }
                    }).show();

                    break;
                case R.id.btn_lights_2:
                    new ColorPickerDialog(MainServiceActivity.this, 255, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int rgb) {
                            Log.i(TAG, "cool selected color: " + rgb);

                            textView_status.setText("Playing with cool lights! Color: " + rgb);

                            HashMap<String, Integer> params = new HashMap<String, Integer>();
                            params.put(NotificationConstants.KEY_COLOR, rgb);
                            params.put(NotificationConstants.KEY_PAUSE_TIME, 500);

                            MiBand.sendAction(MiBandWrapper.ACTION_NOTIFY, params);
                        }
                    }).show();

                    break;
                case R.id.btn_notification:
                    createNotification("Test", MainServiceActivity.this);
                    break;
                case R.id.btn_vibrate:
                    textView_status.setText("Vibrating");

                    HashMap<String, Integer> params = new HashMap<String, Integer>();
                    params.put(NotificationConstants.KEY_TIMES, 3);
                    params.put(NotificationConstants.KEY_ON_TIME, 250);
                    params.put(NotificationConstants.KEY_PAUSE_TIME, 250);

                    MiBand.sendAction(MiBandWrapper.ACTION_VIBRATE_CUSTOM, params);
                    break;
                case R.id.btn_battery:
                    MiBand.sendAction(MiBandWrapper.ACTION_BATTERY);
                    break;
                case R.id.btn_water:
                    scheduleWater();
                    break;
                case R.id.btn_water_cancel:
                    if (alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                        Toast.makeText(MainServiceActivity.this, "Water alarm deactivated", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.btn_apps:
                    thumbNailScaleAnimation(v);
                    break;
                case R.id.btn_sync:
                    MiBand.sendAction(MiBandWrapper.ACTION_START_SYNC);
                    break;
                case R.id.btn_chart:
                    startActivity(new Intent(MainServiceActivity.this, SleepChartActivity.class));
                    break;
                case R.id.btn_chart_2:
                    startActivity(new Intent(MainServiceActivity.this, ActivitiesChartActivity.class));
                    break;
            }
        }
    };

    public void createNotification(String text, Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Test notification")
                        .setContentText(text);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainServiceActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainServiceActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(676, mBuilder.build());
    }

    private void connectToMiBand() {
        //MiBand.initService(MainServiceActivity.this);
        Intent miBandService = new Intent(MainServiceActivity.this, MiBandService.class);
        miBandService.setAction(NotificationConstants.MI_BAND_CONNECT);
        startService(miBandService);

        btn_connect.setEnabled(false);

        textView_status.setText("Connecting...");
    }

    private void disconnectMiBand() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainServiceActivity.this);

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
        //btn_notification.setEnabled(true);
        btn_vibrate.setEnabled(true);
        btn_battery.setEnabled(true);
        btn_sync.setEnabled(true);
        btn_chart.setEnabled(true);
        btn_chart_2.setEnabled(true);
    }

    private void stopMiBand() {
        btn_connect.setText("Connect");

        textView_status.setText("Disconnected");

        btn_connect.setEnabled(true);

        btn_lights.setEnabled(false);
        btn_lights_2.setEnabled(false);
        //btn_notification.setEnabled(false);
        btn_vibrate.setEnabled(false);
        btn_battery.setEnabled(false);
        btn_sync.setEnabled(false);
        btn_chart.setEnabled(false);
        btn_chart_2.setEnabled(false);

        isConnected = false;
    }

    private void scheduleWater() {
        WaterScheduler.getInstance(MainServiceActivity.this).schedule(new WaterScheduler.ScheduleCallback() {
            @Override
            public void OnScheduleNext() {
                Toast.makeText(MainServiceActivity.this, "Water alarm activated every 45 minutes", Toast.LENGTH_LONG).show();
            }

            @Override
            public void OnScheduleTomorrow() {
                Toast.makeText(MainServiceActivity.this, "Next Water alarm set for tomorrow at 8 am", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void thumbNailScaleAnimation(View view) {
        view.setDrawingCacheEnabled(true);
        view.setPressed(false);
        view.refreshDrawableState();
        Bitmap bitmap = view.getDrawingCache();
        ActivityOptionsCompat opts = ActivityOptionsCompat.makeThumbnailScaleUpAnimation(
                view, bitmap, 0, 0);
        // Request the activity be started, using the custom animation options.
        startActivity(new Intent(MainServiceActivity.this, AppsPreferencesActivity.class),
                opts.toBundle());
        view.setDrawingCacheEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //connectToMiBand();
                btn_connect.setEnabled(false);

                textView_status.setText("Connecting...");
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
