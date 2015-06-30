# Mi Band
Sweet and simple Android implementation for the Xiaomi Mi Band.

It's based on [pangliang's implementation](https://github.com/pangliang/miband-sdk-android) but changed some things.

## Usage
1. Register a `BroadcastReceiver` to listen to Mi Band actions

        private BroadcastReceiver bluetoothStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                String action = b.getString("type");

                if (action.equals(NotificationConstants.MI_BAND_CONNECT)) {
                    // Mi Band connected, we could enable different buttons
                } else if (action.equals(NotificationConstants.MI_BAND_DISCONNECT)) {
                    // Mi Band disconnected

                    //we get an extra int showing the error code
                    int errorCode = b.getInt("errorCode");

                    if (errorCode == NotificationConstants.BLUETOOTH_OFF) {
                        //Bluetooth is off. We start a new activity for result to turn it on.
                        //BT_REQUEST_CODE is just an int to listen in onActivityResult callback
                        Log.d(TAG, "turn on Bluetooth");
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_REQUEST_CODE, null);
                    } else {
                        //Mi Band was disconnected. We could enable again the connect button or something
                        //disableConnectButton();
                    }
                } else if (action.equals(NotificationConstants.MI_BAND_BATTERY)) {
                    //we get the battery information from Mi Band
                    BatteryInfo batteryInfo = b.getParcelable("battery");
                    textView_status.setText(batteryInfo.toString());
                }
            }
        };

2. In `onPause` and `onResume` you should register this `BroadcastReceiver`

        @Override
        protected void onPause() {
            super.onPause();
            LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(bluetoothStatusReceiver);
        }
    
        @Override
        protected void onResume() {
            super.onResume();
            //we are listening from the mi band service
            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(bluetoothStatusReceiver, new IntentFilter(NotificationConstants.ACTION_MIBAND_SERVICE));
        }

3. Then you're ready to connect to Mi Band

        MiBand.init(MainActivity.this);

And to disconnect

        MiBand.disconnect();

As simple as that! 

## Actions

Now the fun part. To send commands you use `MiBand.sendAction(int action, HashMap<String, ? extends Object> params)` or `MiBand.sendAction(int action)` where "action" could be
* MiBandWrapper.ACTION_CONNECT
2. MiBandWrapper.ACTION_DISCONNECT
3. MiBandWrapper.ACTION_LIGHTS
4. MiBandWrapper.ACTION_VIBRATE
5. MiBandWrapper.ACTION_BATTERY

for example to make the Mi Band vibrate
    
    MiBand.sendAction(MiBandWrapper.ACTION_VIBRATE);

Also there's a `ColorPickerDialog` class to help choose the int value of a color

    new ColorPickerDialog(MainActivity.this, 255, new ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int rgb) {
                            Log.i(TAG, "selected color: " + rgb);

                            //really important!! use "color" key in the params                           
                            HashMap<String, Integer> params = new HashMap<String, Integer>();
                            params.put("color", rgb);

                            MiBand.sendAction(MiBandWrapper.ACTION_LIGHTS, params);
                        }
                    }).show();
