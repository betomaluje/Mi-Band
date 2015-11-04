package com.betomaluje.android.miband.example.activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import com.betomaluje.miband.ActionCallback;
import com.betomaluje.miband.MiBand;
import com.betomaluje.miband.MiBandService;
import com.betomaluje.miband.bluetooth.MiBandWrapper;
import com.betomaluje.miband.bluetooth.NotificationConstants;
import com.betomaluje.miband.colorpicker.ColorPickerDialog;
import com.betomaluje.miband.model.BatteryInfo;
import com.betomaluje.miband.model.VibrationMode;

/**
 * Created by betomaluje on 7/20/15.
 */
public class MainNormalActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private int BT_REQUEST_CODE = 1001;

    private Button btn_connect, btn_lights, btn_lights_2, btn_notification, btn_vibrate, btn_battery, btn_water, btn_water_cancel, btn_apps, btn_sync, btn_chart, btn_chart_2;
    private TextView textView_status;

    private boolean isConnected = false;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    private MiBand miBand;

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

        miBand = MiBand.getInstance(MainNormalActivity.this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getBoolean("service", false)) {
                stopService(new Intent(MainNormalActivity.this, MiBandService.class));
            } else {
                connectToMiBand();
            }
        } else {
            //connectToMiBand();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //resetToolbar();

        isConnected = miBand.isConnected();

        if (isConnected) {
            startMiBand();
        } else {
            stopMiBand();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MiBandWrapper.getInstance(MainNormalActivity.this).sendAction(MiBandWrapper.ACTION_STOP_SYNC);
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
                    new ColorPickerDialog(MainNormalActivity.this, 255, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int rgb) {
                            Log.i(TAG, "selected color: " + rgb);

                            textView_status.setText("Playing with lights! Color: " + rgb);

                            miBand.setLedColor(3, rgb, 500);
                        }
                    }).show();

                    break;
                case R.id.btn_lights_2:
                    new ColorPickerDialog(MainNormalActivity.this, 255, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int rgb) {
                            Log.i(TAG, "cool selected color: " + rgb);

                            textView_status.setText("Playing with cool lights! Color: " + rgb);

                            miBand.notifyBand(rgb);
                        }
                    }).show();

                    break;
                case R.id.btn_notification:
                    createNotification("Test", MainNormalActivity.this);
                    break;
                case R.id.btn_vibrate:
                    textView_status.setText("Vibrating");

                    //miBand.customVibration(4, 50, 250);
                    miBand.startVibration(VibrationMode.VIBRATION_NEW_FIRMWARE);
                    break;
                case R.id.btn_battery:
                    miBand.getBatteryInfo(new ActionCallback() {
                        @Override
                        public void onSuccess(final Object data) {

                            final BatteryInfo battery = (BatteryInfo) data;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView_status.setText(battery.toString());
                                }
                            });

                        }

                        @Override
                        public void onFail(int errorCode, String msg) {
                            Log.e(TAG, "Fail battery: " + msg);
                        }
                    });
                    break;
                case R.id.btn_water:
                    scheduleWater();
                    break;
                case R.id.btn_water_cancel:
                    if (alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                        Toast.makeText(MainNormalActivity.this, "Water alarm deactivated", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.btn_apps:
                    thumbNailScaleAnimation(v);
                    break;
                case R.id.btn_sync:
                    miBand.startListeningSync(new ActionCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            if (data != null && data.equals("sync complete")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView_status.setText("Sync completed");
                                    }
                                });

                                MiBandWrapper.getInstance(MainNormalActivity.this).sendAction(MiBandWrapper.ACTION_STOP_SYNC);
                            } else {
                                Log.e(TAG, "Sync fail: data null");
                            }
                        }

                        @Override
                        public void onFail(int errorCode, final String msg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView_status.setText("Sync error: " + msg);
                                }
                            });

                            MiBandWrapper.getInstance(MainNormalActivity.this).sendAction(MiBandWrapper.ACTION_STOP_SYNC);
                        }
                    });
                    break;
                case R.id.btn_chart:
                    startActivity(new Intent(MainNormalActivity.this, SleepChartActivity.class));
                    break;
                case R.id.btn_chart_2:
                    startActivity(new Intent(MainNormalActivity.this, ActivitiesChartActivity.class));
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
        Intent resultIntent = new Intent(context, MainNormalActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainNormalActivity.class);
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
        if (!miBand.isConnected()) {
            miBand.connect(myConnectionCallback);
        }

        btn_connect.setEnabled(false);

        textView_status.setText("Connecting...");
    }

    private ActionCallback myConnectionCallback = new ActionCallback() {
        @Override
        public void onSuccess(Object data) {
            Log.d(TAG, "Connected with Mi Band!");

            isConnected = true;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startMiBand();

                }
            });
        }

        @Override
        public void onFail(int errorCode, String msg) {
            Log.e(TAG, "Fail: " + msg);
            isConnected = false;

            if (errorCode == NotificationConstants.BLUETOOTH_OFF) {
                //turn on bluetooth
                Log.d(TAG, "turn on Bluetooth");
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_REQUEST_CODE, null);
            } else {
                Log.d(TAG, "not found");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopMiBand();
                    }
                });
            }
        }
    };

    private void disconnectMiBand() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainNormalActivity.this);

        // set title
        alertDialogBuilder.setTitle("Disconnect to Mi Band");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to Disconnect to your Mi Band?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        miBand.disconnect();
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

        btn_connect.setEnabled(true);

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
        WaterScheduler.getInstance(MainNormalActivity.this).schedule(new WaterScheduler.ScheduleCallback() {
            @Override
            public void OnScheduleNext() {
                Toast.makeText(MainNormalActivity.this, "Water alarm activated every 45 minutes", Toast.LENGTH_LONG).show();
            }

            @Override
            public void OnScheduleTomorrow() {
                Toast.makeText(MainNormalActivity.this, "Next Water alarm set for tomorrow at 8 am", Toast.LENGTH_LONG).show();
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
        startActivity(new Intent(MainNormalActivity.this, AppsPreferencesActivity.class),
                opts.toBundle());
        view.setDrawingCacheEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                connectToMiBand();
                //btn_connect.setEnabled(false);

                //textView_status.setText("Connecting...");
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
