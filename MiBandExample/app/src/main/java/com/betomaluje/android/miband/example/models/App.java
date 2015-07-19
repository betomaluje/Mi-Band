package com.betomaluje.android.miband.example.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by betomaluje on 7/6/15.
 */
public class App implements Parcelable {

    private String source;
    private String name;
    private int color;
    private boolean notify = false;
    private int pauseTime = 500;
    private int onTime = 500;
    private int notificationTimes = 3;

    private int startTime = -1;
    private int endTime = -1;

    private int red = -1;
    private int green = -1;
    private int blue = -1;

    public App() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getColorForView() {
        return Color.rgb(red, green, blue);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        convertRgb(color);
    }

    private void convertRgb(int rgb) {
        red = Color.red(rgb);
        green = Color.green(rgb);
        blue = Color.blue(rgb);
    }

    public void setNotify(int shouldWe) {
        notify = shouldWe == 1;
    }

    public void setNotify(boolean shouldWe) {
        notify = shouldWe;
    }

    public boolean isNotify() {
        return notify;
    }

    public int getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(int pauseTime) {
        this.pauseTime = pauseTime;
    }

    public int getOnTime() {
        return onTime;
    }

    public void setOnTime(int onTime) {
        this.onTime = onTime;
    }

    public int getNotificationTimes() {
        return notificationTimes;
    }

    public void setNotificationTimes(int notificationTimes) {
        this.notificationTimes = notificationTimes;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public boolean shouldWeNotify() {
        //if the time doesn't matter

        //Log.i("App", "startTime: " + startTime + " endTime: " + endTime);

        if (startTime == -1 || endTime == -1) {

            //Log.i("App", "not setted: " + isNotify());

            return isNotify();
        } else {
            Calendar mStartPeriod = new GregorianCalendar();
            mStartPeriod.set(Calendar.HOUR_OF_DAY, startTime);
            mStartPeriod.set(Calendar.MINUTE, 0);

            Calendar mEndPeriod = new GregorianCalendar();
            mEndPeriod.set(Calendar.HOUR_OF_DAY, endTime);
            mEndPeriod.set(Calendar.MINUTE, 0);

            Calendar now = Calendar.getInstance();
            now.setTimeInMillis(System.currentTimeMillis());

            //Log.i("App", "setted: " + isNotify() + " && " + now.after(mStartPeriod) + " && " + now.before(mEndPeriod));

            //only if we are in between the time
            return isNotify() && now.after(mStartPeriod) && now.before(mEndPeriod);
        }
    }

    protected App(Parcel in) {
        source = in.readString();
        name = in.readString();
        color = in.readInt();
        notify = in.readByte() != 0x00;
        pauseTime = in.readInt();
        onTime = in.readInt();
        notificationTimes = in.readInt();
        startTime = in.readInt();
        endTime = in.readInt();
        red = in.readInt();
        green = in.readInt();
        blue = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(source);
        dest.writeString(name);
        dest.writeInt(color);
        dest.writeByte((byte) (notify ? 0x01 : 0x00));
        dest.writeInt(pauseTime);
        dest.writeInt(onTime);
        dest.writeInt(notificationTimes);
        dest.writeInt(startTime);
        dest.writeInt(endTime);
        dest.writeInt(red);
        dest.writeInt(green);
        dest.writeInt(blue);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<App> CREATOR = new Parcelable.Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };
}
