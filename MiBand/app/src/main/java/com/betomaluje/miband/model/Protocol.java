package com.betomaluje.miband.model;

public class Protocol {

    public static final byte[] LOW_LATENCY_LEPARAMS = new byte[]{0x27, 0x00, 0x31, 0x00, 0x00, 0x00, (byte) 0xf4, 0x01, 0x00, 0x00, 0x00, 0x00};

    public static final byte[] HIGH_LATENCY_LEPARAMS = new byte[]{(byte) 0xcc, 0x01, (byte) 0xf4, 0x01, 0x00, 0x00, (byte) 0xf4, 0x01, 0x00, 0x00, 0x00, 0x00};

    public static final byte[] PAIR = {2};
    public static final byte[] VIBRATION_WITH_LED = {0x8, 0};
    public static final byte[] VIBRATION_UNTIL_CALL_STOP = {0x8, 1};
    public static final byte[] VIBRATION_WITHOUT_LED = {0x8, 2};
    public static final byte[] VIBRATION_NEW_FIRMWARE = {0x4};

    public static final byte[] STOP_VIBRATION = {0x13};
    public static final byte[] ENABLE_REALTIME_STEPS_NOTIFY = {3, 1};
    public static final byte[] DISABLE_REALTIME_STEPS_NOTIFY = {3, 0};
    public static final byte[] COLOR_RED = {14, 6, 1, 2, 1};
    public static final byte[] COLOR_BLUE = {14, 0, 6, 6, 1};
    public static final byte[] COLOR_ORANGE = {14, 6, 2, 0, 1};
    public static final byte[] COLOR_GREEN = {14, 4, 5, 0, 1};
    public static final byte[] COLOR_TEST = {14, 0, 1, 0, 1};
    //center-only blue (byte[] { 8, 1 };)

    public static final byte[] REBOOT = {12};
    public static final byte[] REMOTE_DISCONNECT = {1};
    public static final byte[] FACTORY_RESET = {9};
    public static final byte[] SELF_TEST = {2};

    public static final byte[] FETCH_DATA = {0x6};

    public static final byte COMMAND_CONFIRM_ACTIVITY_DATA_TRANSFER_COMPLETE = 0xa;
}
