#Mi Band
Sweet and simple Android implementation to control some aspects of your Xiaomi Mi Band.

* Connect and Disconnect
* Start vibration (with pre defined and customs values)
* Change led's colors (with pre defined and customs values)
* Get battery info
* Sync with your Mi Band (steps, slept hours, etc)
* Real time step counter

##Usage
Mi-Band is ultra simple to use! just follow this 1 step:

0. In your app's dependencies add the library:

        dependencies {
            compile 'com.betomaluje.miband:app:1.0.3'
        }

that's it! You're ready!

Note: this is thanks to the new Android Studio that uses jCenter instead of MavenCentral. To see this, you can open your main `build.gradle` file, and under the repositories you should see `jcenter()`. If not, add this as a repository

       allprojects {
           repositories {
                mavenCentral()
                maven {
                    url 'https://dl.bintray.com/betomaluje/maven/'
                }
            }
        }

Now, to start testing you can use this library in [activities, fragments](https://github.com/betomaluje/Mi-Band/blob/master/MiBandExample/app/src/main/java/com/betomaluje/android/miband/example/activities/MainNormalActivity.java) an even [services](https://github.com/betomaluje/Mi-Band/blob/master/MiBandExample/app/src/main/java/com/betomaluje/android/miband/example/activities/MainServiceActivity.java)!
As an alternative you can use the pre defined [MiBandService](https://github.com/betomaluje/Mi-Band/wiki/MiBandService)

1. Define your Mi Band variable (globaly or localy, it doesn't matter because it's a Singleton) and pass it the current Context variable:

        MiBand miBand = MiBand.getInstance(MyActivity.this);
        
2. Now, to toggle connection you can use

        if (!miBand.isConnected()) {
            miBand.connect(new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    Log.d(TAG, "Connected with Mi Band!");
                    //show SnackBar/Toast or something
                }

                @Override
                public void onFail(int errorCode, String msg) {
                    Log.d(TAG, "Connection failed: " + msg);
                }
            });
        } else {
            miBand.disconnect();
        }

##Actions
Now the fun part: sending commands to your band.

###Vibration
For vibration you can use the following methods:

        //to vibrate using the default band color
        miBand.startVibration(VibrationMode.VIBRATION_WITH_LED);
        
        //to vibrate until you manually stop it
        miBand.startVibration(VibrationMode.VIBRATION_UNTIL_CALL_STOP);
        
        //to vibrate without the led
        miBand.startVibration(VibrationMode.VIBRATION_WITHOUT_LED);
        
        //to stop vibration
        miBand.stopVibration();
        
Also there's a custom vibration method

        miBand.customVibration(times, on_time, off_time);
        
where `times` is an int value to determine **how many times** will vibrate(I recommend to use between 1-3 times only)
and `on_time` is the time in milliseconds that each vibration will be **On** (not more than 500 milliseconds)
and `off_time` is the **pause** between each consecutive vibration
        
###LED Color
To change the LED color, you can use

        miBand.setLedColor(color);

where `color` is an int value representing the color youg want

For convenience, there's a `ColorPickerDialog` class to help choose the int value of a color

        new ColorPickerDialog(MyActivity.this, 255, new ColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int rgb) {
                Log.i(TAG, "selected color: " + rgb);
                miBand.setLedColor(rgb);
            }
        }).show();
        
or you can use 

        miBand.setLedColor(flash_time, color, pause_time);

where `flash_time` is an int value to determine **how many times** will the led flash (I recommend using 1-3 values only)
and `color` is the int **value of the color**
and `pause_time` is the **pause** in milliseconds between each flash

###Battery Info
To get the battery information just use

        miBand.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(final Object data) {
                BatteryInfo battery = (BatteryInfo) data;
                //get the cycle count, the level and other information
                Log.e(TAG, "Battery: " + battery.toString());
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.e(TAG, "Fail battery: " + msg);
            }
        });
        
###Syncronization
To sync data with yor Mi Band use

        miBand.startListeningSync();
        
this will start the sync process. Also you can 

        if (miBand.isSyncNotification())
            miBand.stopListeningSync();
            
After you are finished syncing with the band, you can access the information using

        Calendar before = Calendar.getInstance();
        //7 days before
        before.add(Calendar.DAY_OF_WEEK, -7);
        long temp = before.getTimeInMillis() / 1000;
        before.setTimeInMillis(temp);

        Calendar today = Calendar.getInstance();
        //now
        today.setTimeInMillis(System.currentTimeMillis() / 1000);

        //use DateUtils to display the time in the format "yyyy-MM-dd HH:mm:ss"
        Log.i(TAG, "data from " + DateUtils.convertString(before) + " to " + DateUtils.convertString(today));

        //all our data is stored in ActivitySQLite as ActivityData objects
        ArrayList<ActivityData> allActivities = ActivitySQLite.getInstance(ActivitiesChartActivity.this)
                .getActivitySamples((int) before.getTimeInMillis(), (int) today.getTimeInMillis());
                
to get the sleeping data use

        ActivitySQLite.getInstance(MySleepActivity.this).getSleepSamples(int timestamp_from, int timestamp_to)
        
and to get the activity data use
  
        ActivitySQLite.getInstance(MyActivitiesActivity.this).getActivitySamples(int timestamp_from, int timestamp_to)
        
to get ALL the data use

        ActivitySQLite.getInstance(MyAllActivity.this).getAllActivitiesSamples(int timestamp_from, int timestamp_to)
        
####Activities example

        Calendar before = Calendar.getInstance();
        //7 days before
        before.add(Calendar.DAY_OF_WEEK, -7);
        long temp = before.getTimeInMillis() / 1000;
        before.setTimeInMillis(temp);

        Calendar today = Calendar.getInstance();
        //now
        today.setTimeInMillis(System.currentTimeMillis() / 1000);

        //use DateUtils to display the time in the format "yyyy-MM-dd HH:mm:ss"
        Log.i(TAG, "data from " + DateUtils.convertString(before) + " to " + DateUtils.convertString(today));

        //all our data is stored in ActivitySQLite as ActivityData objects
        ArrayList<ActivityData> allActivities = ActivitySQLite.getInstance(ActivitiesChartActivity.this)
                .getActivitySamples((int) before.getTimeInMillis(), (int) today.getTimeInMillis());
                
        float movement_divisor = 180.0f;

        float value;

        String dateString = "";
        for (ActivityData ad : allActivities) {

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(ad.getTimestamp() * 1000L);

            dateString = DateUtils.convertString(date);

            Log.i(TAG, "date " + dateString);
            Log.i(TAG, "steps " + ad.getSteps());

            short movement = ad.getIntensity();

            byte steps = ad.getSteps();
            if (steps != 0) {
                // I'm not sure using steps for this is actually a good idea
                movement = steps;
            }
            
            //the value
            value = ((float) movement) / movement_divisor;
        }
        
####Sleeping example

        Calendar before = Calendar.getInstance();
        //7 days before
        before.add(Calendar.DAY_OF_WEEK, -7);
        long temp = before.getTimeInMillis() / 1000;
        before.setTimeInMillis(temp);

        Calendar today = Calendar.getInstance();
        //now
        today.setTimeInMillis(System.currentTimeMillis() / 1000);

        //use DateUtils to display the time in the format "yyyy-MM-dd HH:mm:ss"
        Log.i(TAG, "data from " + DateUtils.convertString(before) + " to " + DateUtils.convertString(today));

        ArrayList<ActivityData> allActivities = ActivitySQLite.getInstance(SleepChartActivity.this)
                .getSleepSamples((int) before.getTimeInMillis(), (int) today.getTimeInMillis());

        float movement_divisor = 180.0f;

        float value;

        String dateString = "";
        for (ActivityData ad : allActivities) {

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(ad.getTimestamp() * 1000L);

            dateString = DateUtils.convertString(date);

            Log.i(TAG, "date " + dateString);
            Log.i(TAG, "steps " + ad.getSteps());

            value = ((float) ad.getIntensity()) / movement_divisor;

            switch (ad.getType()) {
                case ActivityData.TYPE_DEEP_SLEEP:
                    //DEEP SLEEP TYPE. Only here we need to adjust the value
                    value += ActivityData.Y_VALUE_DEEP_SLEEP;
                    doSomethingWithDeepSleep(value);
                    break;
                case ActivityData.TYPE_LIGHT_SLEEP:
                    //LIGHT SLEEP TYPE
                    doSomethingWithLightSleep(value);
                    break;
                default:
                    //UNKNOWN TYPE
                    doSomethingWithUnknownSleep(value);
                    break;
            }
        }
        
Also you can track the "Sleep comparison"

        private void refreshSleepAmounts(List<ActivityData> samples) {
            ActivityAnalysis analysis = new ActivityAnalysis();
            ActivityAmounts amounts = analysis.calculateActivityAmounts(samples);
            
            float hoursOfSleep = amounts.getTotalSeconds() / (float) (60 * 60);
            
             Log.i(TAG, "hoursOfSleep " + hoursOfSleep + " h");
            
            for (ActivityAmount amount : amounts.getAmounts()) {
                Log.i(TAG, "name " + amount.getName());
                Log.i(TAG, "total seconds " + amount.getTotalSeconds());
                Log.i(TAG, "kind " + amount.getActivityKind());
            }
        }

##Known issues
###First time connection
The first time you connect with your Mi Band please be patient. It takes around 45 seconds. After this time, if you can't connect to it, try the following

1. Try disconnecting and again connecting to Bluetooth
2. Uninstall and install again your app

###Mi Fit app incompatibility
If you also have the Mi Fit app installed, you may lose some information on the syncing because Mi Fit and your app will be "fighting" to sync the Mi Band data. Once the Mi Band data is synced, it will be "deleted" from the band and lost forever. 

Also the pairing process may ocurr several times each time you switch from apps because you will lose th pairing info from each app.

Nevertheless you can still send commands to the Mi Band if you are using both apps. It's not mandatory to uninstall Mi Fit to use this library.

##Acknowledge
Thanks to

1. [Pangliang](https://github.com/pangliang/miband-sdk-android) with his library I started with mine
2. [Gadgetbridge](https://github.com/Freeyourgadget/Gadgetbridge) I got all the activities part and some command ideas from them

##Contribute
Contributions are welcome, be it feedback, bugreports, documentation, translation, research or code. Feel free to work on any of the open [issues](https://github.com/betomaluje/Mi-Band/issues); just leave a comment that you're working on one to avoid duplicated work.
