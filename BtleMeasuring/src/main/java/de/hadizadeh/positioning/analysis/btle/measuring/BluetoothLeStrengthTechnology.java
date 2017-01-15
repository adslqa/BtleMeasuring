package de.hadizadeh.positioning.analysis.btle.measuring;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import de.hadizadeh.positioning.model.SignalInformation;

import java.util.List;
import java.util.Map;

public class BluetoothLeStrengthTechnology extends BluetoothLeTechnology {

    public BluetoothLeStrengthTechnology(String name, long validityTime,
                                         List<String> allowedBtLeDevices) {
        super(name, allowedBtLeDevices, validityTime);

        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                BluetoothLeDevice btDevice = new BluetoothLeDevice(device, scanRecord, rssi);
                if ("Ind.Positioning".equals(btDevice.getUuidText())) {
                //if ("Neuendorfstrasse".equals(btDevice.getUuidText())) {
                    btLeDevices.put(btDevice.getIdentificator(), btDevice);
                }
            }
        };
    }

    @Override
    public Map<String, SignalInformation> getSignalData() {
        signalData.clear();
        for (Map.Entry<String, BluetoothLeDevice> btLeDevice : btLeDevices.entrySet()) {
            //if (btLeDevice.getValue().getTimeStamp() + validityTime >= currentTime) {
            signalData.put(btLeDevice.getValue().getIdentificator(), new SignalInformation(btLeDevice.getValue().getRssi()));
            //}
        }
        return signalData;
    }
}
