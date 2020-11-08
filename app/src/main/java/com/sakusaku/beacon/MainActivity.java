package com.sakusaku.beacon;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    //iBeacon認識のためのフォーマット設定
    private static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private BeaconManager beaconManager;

    int count;
    ArrayList<Identifier> uuids;
    ArrayList<Identifier> majors;
    ArrayList<Identifier> minors;
    ArrayList<Integer> rssis;
    ArrayList<Integer> txPowers;
    ArrayList<Double> distances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // デバイスのBLE対応チェック
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // 未対応の場合、Toast表示
            Toast.makeText(this, "このデバイスはBLE未対応です", Toast.LENGTH_LONG).show();
        }

        // API 23以上かのチェック
        if (Build.VERSION.SDK_INT >= 23) {
            // パーミッションの要求
            if (checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 0);
            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
        //サービス開始
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
        //サービス終了
    }

    @Override
    public void onBeaconServiceConnect() {
        final Region mRegion = new Region("iBeacon", null, null, null);

        try {
            //Beacon情報の監視を開始
            beaconManager.startMonitoringBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                //領域への入場を検知
                Log.d("iBeacon", "Enter Region");
            }

            @Override
            public void didExitRegion(Region region) {
                //領域からの退場を検知
                Log.d("iBeacon", "Exit Region");
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                //領域への入退場のステータス変化を検知
                Log.d("MyActivity", "Determine State" + i);
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                count = 0;
                uuids = new ArrayList<Identifier>();
                majors = new ArrayList<Identifier>();
                minors = new ArrayList<Identifier>();
                rssis = new ArrayList<Integer>();
                txPowers = new ArrayList<Integer>();
                distances = new ArrayList<Double>();

                //検出したBeaconの情報を全てlog出力
                for (Beacon beacon : beacons) {
                    Log.d("MyActivity", "UUID:" + beacon.getId1() + ", major:"
                            + beacon.getId2() + ", minor:" + beacon.getId3() + ", RSSI:"
                            + beacon.getRssi() + ", TxPower:" + beacon.getTxPower()
                            + ", Distance:" + beacon.getDistance());

                    uuids.add(beacon.getId1());
                    majors.add(beacon.getId2());
                    minors.add(beacon.getId3());
                    rssis.add(beacon.getRssi());
                    txPowers.add(beacon.getTxPower());
                    distances.add(beacon.getDistance());
                }

                count = beacons.size();
                Log.d("Activity", "total:" + count + "台");
            }
        });
    }

    public void clicked(View view) {
        TextView countView = (TextView) findViewById(R.id.result);
        countView.setText(String.valueOf(count));

        ListView beaconList = (ListView) findViewById(R.id.beacon_list);
        ArrayList<BeaconListItems> listItems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BeaconListItems beaconItem = new BeaconListItems(uuids.get(i), majors.get(i),
                    minors.get(i), rssis.get(i), txPowers.get(i), distances.get(i));
            listItems.add(beaconItem);
        }
        BeaconListAdapter beaconAdapter = new BeaconListAdapter(this, R.layout.beacon_view, listItems);
        beaconList.setAdapter(beaconAdapter);

    }
}