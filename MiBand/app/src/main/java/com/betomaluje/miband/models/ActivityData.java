package com.betomaluje.miband.models;

/**
 * Created by betomaluje on 7/9/15.
 */
public class ActivityData {

    public static final float Y_VALUE_DEEP_SLEEP = 0.01f;
    public static final float Y_VALUE_LIGHT_SLEEP = 0.016f;

    public static final byte PROVIDER_MIBAND = 0;

//    public static final byte TYPE_CHARGING = 6;
//    public static final byte TYPE_NONWEAR = 3;
//    public static final byte TYPE_NREM = 5; // DEEP SLEEP
//    public static final byte TYPE_ONBED = 7;
//    public static final byte TYPE_REM = 4; // LIGHT SLEEP
//    public static final byte TYPE_RUNNING = 2;
//    public static final byte TYPE_SLIENT = 0;
//    public static final byte TYPE_USER = 100;
//    public static final byte TYPE_WALKING = 1;

    public static final byte TYPE_DEEP_SLEEP = 5;
    public static final byte TYPE_LIGHT_SLEEP = 4;
    public static final byte TYPE_UNKNOWN = -1;
    // add more here

    private final int timestamp;
    private final byte provider;
    private final short intensity;
    private final byte steps;
    private final byte type;

    public ActivityData(int timestamp, byte provider, short intensity, byte steps, byte type) {
        this.timestamp = timestamp;
        this.provider = provider;
        this.intensity = intensity;
        this.steps = steps;
        this.type = type;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public byte getProvider() {
        return provider;
    }

    public short getIntensity() {
        return intensity;
    }

    public byte getSteps() {
        return steps;
    }

    public byte getType() {
        return type;
    }
}
