package com.betomaluje.miband.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Wristband Battery Related Info
 */
public class BatteryInfo implements Parcelable {
    /**
     * Current state of the battery
     */
    public static enum Status {
        UNKNOWN, LOW, FULL, CHARGING, NOT_CHARGING;

        public static Status fromByte(byte b) {
            switch (b) {
                case 1:
                    return LOW;
                case 2:
                    return CHARGING;
                case 3:
                    return FULL;
                case 4:
                    return NOT_CHARGING;
                default:
                    return UNKNOWN;
            }
        }
    }

    private int level;
    private int cycles;
    private Status status;
    private Calendar lastChargedDate;

    private BatteryInfo() {

    }

    public static BatteryInfo fromByteData(byte[] data) {
        if (data.length < 10) {
            return null;
        }
        BatteryInfo info = new BatteryInfo();

        info.level = data[0];
        info.status = Status.fromByte(data[9]);
        info.cycles = 0xffff & (0xff & data[7] | (0xff & data[8]) << 8);
        info.lastChargedDate = Calendar.getInstance();

        info.lastChargedDate.set(Calendar.YEAR, data[1] + 2000);
        info.lastChargedDate.set(Calendar.MONTH, data[2]);
        info.lastChargedDate.set(Calendar.DATE, data[3]);

        info.lastChargedDate.set(Calendar.HOUR_OF_DAY, data[4]);
        info.lastChargedDate.set(Calendar.MINUTE, data[5]);
        info.lastChargedDate.set(Calendar.SECOND, data[6]);

        return info;
    }

    public String toString() {
        return "cycles:" + this.getCycles()
                + ",level:" + this.getLevel()
                + ",status:" + this.getStatus()
                + ",last:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.getDefault()).format(this.getLastChargedDate().getTime());
    }

    /**
     * Percentage of battery power, level = 40 represent 40% of the battery
     */
    public int getLevel() {
        return level;
    }

    /**
     * Charging cycles
     */
    public int getCycles() {
        return cycles;
    }

    /**
     * Current state
     *
     * @see Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Last charge time
     */
    public Calendar getLastChargedDate() {
        return lastChargedDate;
    }


    protected BatteryInfo(Parcel in) {
        level = in.readInt();
        cycles = in.readInt();
        status = (Status) in.readValue(Status.class.getClassLoader());
        lastChargedDate = (Calendar) in.readValue(Calendar.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(level);
        dest.writeInt(cycles);
        dest.writeValue(status);
        dest.writeValue(lastChargedDate);
    }

    @SuppressWarnings("unused")
    public static final Creator<BatteryInfo> CREATOR = new Creator<BatteryInfo>() {
        @Override
        public BatteryInfo createFromParcel(Parcel in) {
            return new BatteryInfo(in);
        }

        @Override
        public BatteryInfo[] newArray(int size) {
            return new BatteryInfo[size];
        }
    };
}

