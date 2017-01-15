package de.hadizadeh.positioning.analysis.btle.measuring;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import de.hadizadeh.positioning.model.SignalInformation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Measuring extends Activity {
    private int logSeconds = 10 * 60;
    private Map<String, List<Integer>> loggedData;
    private Map<Integer, String> identificationTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measuring);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        identificationTable = new HashMap<Integer, String>();
        identificationTable.put(72, "0 Meter");
        identificationTable.put(73, "2 Meter");
        identificationTable.put(74, "5 Meter");
        identificationTable.put(75, "10 Meter");
        final BluetoothLeStrengthTechnology bluetoothLeStrengthTechnology = new BluetoothLeStrengthTechnology("BTLE", 1000, null);
        bluetoothLeStrengthTechnology.startScanning();
        loggedData = new HashMap<String, List<Integer>>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000); // Initialize reading
                    for (int i = 0; i <= logSeconds; i++) {
                        Log.i("Time", i + ". second");
                        logSignalData(bluetoothLeStrengthTechnology.getSignalData());
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                createCsvFile();
            }
        }).start();
    }

    private void logSignalData(Map<String, SignalInformation> signalData) {
        for (Map.Entry<String, SignalInformation> beaconData : signalData.entrySet()) {
            String[] majorMinor = beaconData.getKey().split("\\|");
            int beaconId = BluetoothLeTechnology.majorMinorToNumber(Integer.valueOf(majorMinor[0]), Integer.valueOf(majorMinor[1]));
            if (identificationTable.containsKey(beaconId)) {
                if (!loggedData.containsKey(identificationTable.get(beaconId))) {
                    loggedData.put(identificationTable.get(beaconId), new ArrayList<Integer>());
                }
                loggedData.get(identificationTable.get(beaconId)).add((int) beaconData.getValue().getStrength());
            }
        }
    }

    private void createCsvFile() {
        String text = "Time;";
        for (int i = 0; i <= logSeconds; i++) {
            text += i + ";";
        }
        text += "\n";
        for (Map.Entry<String, List<Integer>> dataLine : loggedData.entrySet()) {
            text += formatRssiLine(dataLine.getKey(), dataLine.getValue()) + "\n";
        }
        System.out.println(text);

        File file = new File(Environment.getExternalStorageDirectory(), "btleRssiData.csv");
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(text);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("Persistence", "File created.");
        Toast.makeText(this, "File created.", Toast.LENGTH_LONG).show();
    }

    private String formatRssiLine(String title, List<Integer> rssiValues) {
        String text = title + ";";
        for (Integer rssiValue : rssiValues) {
            text += rssiValue + ";";
        }
        return text;
    }
}
