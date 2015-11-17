package com.betomaluje.miband.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;

public class UserInfo {

    public static final String KEY_PREFERENCES = "user_info_preferences";
    public static final String KEY_BT_ADDRESS = "bt_address";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_AGE = "age";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_ALIAS = "alias";
    public static final String KEY_TYPE = "type";

    private String btAddress;
    private int uid;
    private byte gender;
    private byte age;
    private byte height;        // cm
    private byte weight;        // kg
    private String alias = "";
    private byte type;

    private byte[] data = new byte[20];

    private UserInfo() {

    }

    public UserInfo(String btAdress, int gender, int age, int height, int weight, String alias, int type) {
        this.btAddress = btAdress;
        this.gender = (byte) gender;
        this.age = (byte) age;
        this.height = (byte) (height & 0xFF);
        this.weight = (byte) weight;
        this.alias = alias;
        this.uid = calculateUidFrom(alias);
        this.type = (byte) type;

        byte[] sequence = new byte[20];

        int uid = calculateUidFrom(alias);
        String normalizedAlias = ensureTenCharacters(alias);
        sequence[0] = (byte) uid;
        sequence[1] = (byte) (uid >>> 8);
        sequence[2] = (byte) (uid >>> 16);
        sequence[3] = (byte) (uid >>> 24);

        sequence[4] = (byte) (gender & 0xff);
        sequence[5] = (byte) (age & 0xff);
        sequence[6] = (byte) (height & 0xff);
        sequence[7] = (byte) (weight & 0xff);
        sequence[8] = (byte) (type & 0xff);

        for (int u = 9; u < 19; u++)
            sequence[u] = normalizedAlias.getBytes()[u - 9];

        byte[] crcSequence = new byte[19];
        System.arraycopy(sequence, 0, crcSequence, 0, crcSequence.length);

        sequence[19] = (byte) ((getCRC8(crcSequence) ^ Integer.parseInt(btAdress.substring(btAdress.length() - 2), 16)) & 0xff);

        this.data = sequence;
    }

    public static UserInfo create(String address, int gender, int age, int height, int weight, String alias, int type) throws IllegalArgumentException {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        try {
            return new UserInfo(address, gender, age, height, weight, alias, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Illegal user info data", ex);
        }
    }

    /**
     * Creates a default user info.
     *
     * @param btAddress the address of the MI Band to connect to.
     */
    public static UserInfo getDefault(String btAddress, Context context) {
        try {
            return getSavedUser(context);
        } catch (IllegalArgumentException e) {
            return new UserInfo(btAddress, 0, 26, 180, 80, "1550050550", 0);
        }
    }

    private String ensureTenCharacters(String alias) {
        char[] result = new char[10];
        //int aliasLen = alias.length();
        int maxLen = Math.min(10, alias.length());
        int diff = 10 - maxLen;
        for (int i = 0; i < maxLen; i++) {
            result[i + diff] = alias.charAt(i);
        }
        for (int i = 0; i < diff; i++) {
            result[i] = '0';
        }
        return new String(result);
    }

    private int calculateUidFrom(String alias) {
        int uid = 0;
        try {
            uid = Integer.parseInt(alias);
        } catch (NumberFormatException ex) {
            uid = alias.hashCode(); // simple as that
        }
        return uid;
    }

    public static UserInfo fromByteData(byte[] data) {
        if (data.length < 9) {
            return null;
        }
        UserInfo info = new UserInfo();

        info.uid = data[3] << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
        info.gender = data[4];
        info.age = data[5];
        info.height = data[6];
        info.weight = data[7];
        try {
            info.alias = data.length == 9 ? "" : new String(data, 8, data.length - 9, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            info.alias = "";
        }
        info.type = data[data.length - 1];

        return info;
    }

    public byte[] getBytes(String mBTAddress) {
        byte[] aliasBytes;
        try {
            aliasBytes = this.alias.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            aliasBytes = new byte[0];
        }
        ByteBuffer bf = ByteBuffer.allocate(20);
        bf.put((byte) (uid & 0xff));
        bf.put((byte) (uid >> 8 & 0xff));
        bf.put((byte) (uid >> 16 & 0xff));
        bf.put((byte) (uid >> 24 & 0xff));
        bf.put(this.gender);
        bf.put(this.age);
        bf.put(this.height);
        bf.put(this.weight);
        bf.put(this.type);
        if (aliasBytes.length <= 10) {
            bf.put(aliasBytes);
            bf.put(new byte[10 - aliasBytes.length]);
        } else {
            bf.put(aliasBytes, 0, 10);
        }

        byte[] crcSequence = new byte[19];
        for (int u = 0; u < crcSequence.length; u++)
            crcSequence[u] = bf.array()[u];

        byte crcb = (byte) ((getCRC8(crcSequence) ^ Integer.parseInt(mBTAddress.substring(mBTAddress.length() - 2), 16)) & 0xff);
        bf.put(crcb);
        return bf.array();
    }

    private int getCRC8(byte[] seq) {
        int len = seq.length;
        int i = 0;
        byte crc = 0x00;

        while (len-- > 0) {
            byte extract = seq[i++];
            for (byte tempI = 8; tempI != 0; tempI--) {
                byte sum = (byte) ((crc & 0xff) ^ (extract & 0xff));
                sum = (byte) ((sum & 0xff) & 0x01);
                crc = (byte) ((crc & 0xff) >>> 1);
                if (sum != 0) {
                    crc = (byte) ((crc & 0xff) ^ 0x8c);
                }
                extract = (byte) ((extract & 0xff) >>> 1);
            }
        }
        return (crc & 0xff);
    }

    public String toString() {
        return "uid:" + this.uid
                + ",gender:" + this.gender
                + ",age:" + this.age
                + ",height:" + this.getHeight()
                + ",weight:" + this.getWeight()
                + ",alias:" + this.alias
                + ",type:" + this.type;
    }

    /**
     * @return the uid
     */
    public int getUid() {
        return uid;
    }

    /**
     * @return the gender
     */
    public byte getGender() {
        return gender;
    }

    /**
     * @return the age
     */
    public byte getAge() {
        return age;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return (height & 0xFF);
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight & 0xFF;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return the type
     */
    public byte getType() {
        return type;
    }

    public byte[] getData() {
        return this.data;
    }

    public static UserInfo getSavedUser(Context context) {

        SharedPreferences sharedPrefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE);

        String btAddress = sharedPrefs.getString(KEY_BT_ADDRESS, "");
        int gender = sharedPrefs.getInt(KEY_GENDER, -1);
        int age = sharedPrefs.getInt(KEY_AGE, -1);
        int height = sharedPrefs.getInt(KEY_HEIGHT, -1);
        int weight = sharedPrefs.getInt(KEY_WEIGHT, -1);
        String alias = sharedPrefs.getString(KEY_ALIAS, "");
        int type = sharedPrefs.getInt(KEY_TYPE, -1);

        //String TAG = "UserInfo";
        //Log.e(TAG, "address: " + btAddress);
        //Log.e(TAG, "gender: " + gender);
        //Log.e(TAG, "age: " + age);
        //Log.e(TAG, "height: : " + height);
        //Log.e(TAG, "weight: " + weight);
        //Log.e(TAG, "alias: " + alias);

        return create(btAddress, gender, age, height, weight, alias, type);
    }

    public static String generateAlias() {
        char[] chars = "0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        return sb.toString();
    }
}
