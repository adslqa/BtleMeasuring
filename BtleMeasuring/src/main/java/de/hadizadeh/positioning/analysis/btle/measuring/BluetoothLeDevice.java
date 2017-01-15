package de.hadizadeh.positioning.analysis.btle.measuring;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

public class BluetoothLeDevice implements Comparable<BluetoothLeDevice> {
    public enum DistanceCategory {
        UNKNOWN(0.0f),
        IMMEDIATE(1.0f), // <= 0,5 meters
        NEAR(2.0f), // <= 2 meters
        FAR(3.0f); // <= 30 meters
        private final float value;

        DistanceCategory(final float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    private String name;
    private String address;
    private String companyId;
    private String uuid;
    private int rssi;
    private int major;
    private int minor;
    private int txPower;
    private long timeStamp;
    private String uuidText;
    private double distance;
    private DistanceCategory distanceCategory;

    public BluetoothLeDevice(String name, String address, String companyId, String uuid, int rssi, int major, int minor, int txPower, long
            timeStamp) {
        initialize(name, address, companyId, uuid, rssi, major, minor, txPower, timeStamp);
    }

    public BluetoothLeDevice(String name, String address, String companyId, String uuid, int rssi, String major, String minor, String txPower, long
            timeStamp) {
        initialize(name, address, companyId, uuid, rssi, Integer.parseInt(major, 16), Integer.parseInt(minor, 16), Integer.valueOf(txPower, 16)
                .shortValue(), timeStamp);
    }

    public BluetoothLeDevice(BluetoothDevice device, byte[] scanRecord, int rssi) {
        extractBtData(device, scanRecord, rssi);
    }

    public BluetoothLeDevice(String id) {
        String[] idParts = id.split("\\|");
        if (idParts.length == 2) {
            initialize(name, address, companyId, uuid, rssi, Integer.valueOf(idParts[0]), Integer.valueOf(idParts[1]), txPower, timeStamp);
        }
    }

    public BluetoothLeDevice(BluetoothDevice device, ScanRecord scanRecord, int rssi) {
        extractBtData(device, scanRecord.getBytes(), rssi);
    }

    private void initialize(String name, String address, String companyId, String uuid, int rssi, int major, int minor, int txPower, long timeStamp) {
        this.name = name;
        this.address = address;
        this.companyId = companyId;
        this.uuid = uuid;
        this.rssi = rssi;
        this.major = major;
        this.minor = minor;
        this.txPower = txPower;
        this.timeStamp = timeStamp;
        this.uuidText = hexToAscii(uuid);
        this.distance = calculateDistance(rssi, txPower);
        distanceCategory = determineDistanceCategory(this.distance);
    }

    private void extractBtData(BluetoothDevice device, byte[] scanRecord, int rssi) {
        String uuid = "";
        String companyId = String.format("%02x", scanRecord[5]) + String.format("%02x", scanRecord[6]);
        String major = String.format("%02x", scanRecord[25]) + String.format("%02x", scanRecord[26]);
        String minor = String.format("%02x", scanRecord[27]) + String.format("%02x", scanRecord[28]);
        String txPower = String.format("%02x", scanRecord[29]);
        for (int i = 0; i < scanRecord.length; i++) {
            if (i >= 9 && i <= 24) {
                uuid += String.format("%02x", scanRecord[i]);
            }
        }
        initialize(device.getName(), device.getAddress(), companyId, uuid, rssi, Integer.parseInt(major, 16), Integer.parseInt(minor, 16), Short
                .valueOf(txPower, 16).byteValue(), System.currentTimeMillis());
    }

    private double calculateDistance(int rssi, int txPower) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    private DistanceCategory determineDistanceCategory(double distance) {
        if (distance <= 0.5) {
            return DistanceCategory.IMMEDIATE;
        } else if (distance <= 2.0) {
            return DistanceCategory.NEAR;
        } else if (distance <= 30.0) {
            return DistanceCategory.FAR;
        } else {
            return DistanceCategory.UNKNOWN;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getIdentificator() {
        return major + "|" + minor;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public double getDistance() {
        return distance;
    }

    public DistanceCategory getDistanceCategory() {
        return distanceCategory;
    }

    public String getUuidText() {
        return uuidText;
    }

    private static String hexToAscii(String hexValue) {
        if (hexValue != null && hexValue.length() > 0) {
            StringBuilder output = new StringBuilder("");
            for (int i = 0; i < hexValue.length(); i += 2) {
                String str = hexValue.substring(i, i + 2);
                output.append((char) Integer.parseInt(str, 16));
            }
            return output.toString().trim();
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(BluetoothLeDevice another) {
        if (rssi > another.rssi) {
            return 1;
        } else if (rssi < another.rssi) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "BluetoothLeDevice{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", companyId='" + companyId + '\'' +
                ", uuid='" + uuid + '\'' +
                ", rssi=" + rssi +
                ", major=" + major +
                ", minor=" + minor +
                ", txPower=" + txPower +
                ", timeStamp=" + timeStamp +
                ", uuidText='" + uuidText + '\'' +
                ", distance=" + distance +
                ", distanceCategory=" + distanceCategory +
                '}';
    }
}
