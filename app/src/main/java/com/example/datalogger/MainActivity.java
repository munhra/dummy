
package com.example.datalogger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    TextView info;
    List<String> macs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        Button getInfo = (Button) findViewById(R.id.getInfo);
        info = (TextView) findViewById(R.id.info);
        getInfo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                mBluetoothAdapter.startDiscovery();
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wInfo = wifiManager.getConnectionInfo();
                String mac = wInfo.getMacAddress();

                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;

                String id = Secure.getString(getApplicationContext().getContentResolver(),
                        Secure.ANDROID_ID);

                Boolean spotify = appInstalledOrNot("com.spotify.music");

                String ssid = wInfo.getSSID();
                
                AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
                Account[] list = manager.getAccounts();
                
                String accounts = "";
                for(int i=0; i<list.length;i++){
                    accounts=accounts+ "Name: " + list[i].name + "\n" + "Type: " + list[i].type + "\n\n";
                }

                info.setText("MAC: " + mac + "\n\n" + "Device Name: " + manufacturer + " " + model
                        + "\n\n" + "ID: " + id + "\n\n" + "Spotify: " + spotify.toString() + "\n\n"
                        + "Wi-Fi: " + ssid + "\n\nAccounts: \n" + accounts);
            }
        });

    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public boolean IsCurrentConnectedWifi(ScanResult scanResult)
    {
        WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo currentWifi = mainWifi.getConnectionInfo();
        if (currentWifi != null)
        {
            if (currentWifi.getSSID() != null)
            {
                if (currentWifi.getSSID() == scanResult.SSID)
                    return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!macs.contains(device.getName() + " " + device.getAddress())) {
                    macs.add(device.getName() + " " + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("TAG", "Finished");
                info.setText(info.getText() + "\n" + "Bluetooth Devices:\n");
                for (int i = 0; i < macs.size(); i++) {
                    Log.d("TAG", macs.get(i));
                    info.setText(info.getText() + macs.get(i) + "\n");
                }
            }
        }

    };

}
