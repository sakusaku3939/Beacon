package com.sakusaku.beacon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

    private final static String[] PERMISSION_CAMERA = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private final static int PERMISSION_REQUEST_CODE = 1;

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

        // 権限があるか確認
        if (RuntimePermission.hasSelfPermissions(MainActivity.this, PERMISSION_CAMERA)) {
            // 権限がある場合は、そのまま通常処理を行う
            launchCamera();
        } else {
            // 権限がない場合は、パーミッション確認アラートを表示する
            requestPermissions(PERMISSION_CAMERA, PERMISSION_REQUEST_CODE);
        }

        // Android 6, API 23以上でパーミッシンの確認
//        if (Build.VERSION.SDK_INT >= 23) {
//            checkPermission();
//        }
//        else{
//            startLocationActivity();
//        }

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

//    // 位置情報許可の確認
//    public void checkPermission() {
//        // 既に許可している
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//
////            startLocationActivity();
//        }
//        // 拒否していた場合
//        else {
//            requestLocationPermission();
//        }
//    }
//
//    // 許可を求める
//    private void requestLocationPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)) {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    1000);
//
//        } else {
//            Toast.makeText(this, "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    // 結果の受け取り
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == 1000) {
//            // 使用が許可された
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                startLocationActivity();
//            } else {
//                // それでも拒否された時の対応
//                Toast.makeText(this, "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_LONG).show();
//            }
//        }
//    }

//    // Intent でLocation
//    private void startLocationActivity() {
//        Intent intent = new Intent(getApplication(), LocationActivity.class);
//        startActivity(intent);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // アラート表示中に画面回転すると length ０でコールバックされるのでガードする
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            // 失敗した場合
            if (!RuntimePermission.checkGrantResults(grantResults)) {
                // 「今後は確認しない」にチェックされているかどうか
                if (RuntimePermission.shouldShowRequestPermissionRationale(MainActivity.this, PERMISSION_CAMERA[0])) {
                    Toast.makeText(MainActivity.this, "ビーコンの取得には位置情報の許可が必要です", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    new Handler().post(() -> {
                        RuntimePermission.showAlertDialog(getSupportFragmentManager(), "位置情報");
                    });
                }
            } else {
                // 権限が取れた場合は通常の処理を行う
                launchCamera();
            }
        }
    }

    private void launchCamera() {

//        Intent intent = new Intent();
//        startActivityForResult(intent, 0);
    }
}