package de.hadizadeh.positioning.analysis.btle.measuring;


import android.bluetooth.BluetoothAdapter;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BluetoothLeTechnology extends Technology {
    protected Map<String, SignalInformation> signalData;
    protected BluetoothAdapter bluetoothAdapter;
    protected int cacheSize = 1;
    protected Map<String, BluetoothLeDevice> btLeDevices;
    protected long validityTime;
    protected List<String> allowedBtLeDevices;
    protected boolean scanning;

    protected BluetoothAdapter.LeScanCallback leScanCallback;

    public BluetoothLeTechnology(String name, List<String> allowedBtLeDevices, long validityTime) {
        super(name, null);
        this.validityTime = validityTime;
        this.allowedBtLeDevices = allowedBtLeDevices;
        this.btLeDevices = new HashMap<String, BluetoothLeDevice>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.signalData = new HashMap<String, SignalInformation>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setCachingManager(new BalanceCachingManager(cacheSize));
    }

    @Override
    public void startScanning() {
        super.startScanning();
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    public void resetBtData() {
        btLeDevices.clear();
    }

    @Override
    public void stopScanning() {
        super.stopScanning();
        scanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    public static int idToNumber(String id) {
        BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(id);
        return majorMinorToNumber(bluetoothLeDevice.getMajor(), bluetoothLeDevice.getMinor());
    }

    public static int majorMinorToNumber(int major, int minor) {
        String majorBits = String.format("%3s", Integer.toBinaryString(major)).replace(' ', '0');
        String minorBits = String.format("%6s", Integer.toBinaryString(minor)).replace(' ', '0');
        return Integer.parseInt(majorBits + minorBits, 2);
    }

    public boolean enableHardware() {
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            return true;
        } else {
            return false;
        }
    }

    public void disableHardware() {
        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }
}
