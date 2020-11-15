package com.sakusaku.beacon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;

public class BeaconActivity extends AppCompatActivity{
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

    private RegionBootstrap regionBootstrap;

    String uuidString = "CCCCCCCC-CCCC-CCCC-CCCC-CCCCCCCCCCCC";
    Identifier uuid = Identifier.parse(uuidString);

    //通知オブジェクトの用意と初期化
    Notification notification = null;
    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent serviceIntent = new Intent(getApplication(), BeaconService.class);
        startForegroundService(serviceIntent);
//
//        beaconManager = BeaconManager.getInstanceForApplication(this);
//        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
//
//        Notification.Builder builder = new Notification.Builder(this);
//        builder.setSmallIcon(R.drawable.ic_launcher_background);
//        builder.setContentTitle("Scanning for Beacons");
//        Intent intent = new Intent(this, BeaconActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
//        );
//        builder.setContentIntent(pendingIntent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
//                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("My Notification Channel Description");
//            NotificationManager notificationManager = (NotificationManager) getSystemService(
//                    Context.NOTIFICATION_SERVICE);
//            notificationManager.createNotificationChannel(channel);
//            builder.setChannelId(channel.getId());
//        }
//        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
//        // For the above foreground scanning service to be useful, you need to disable
//        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
//        // cycle that would otherwise be disallowed by the operating system.
//        //
//        beaconManager.setEnableScheduledScanJobs(false);
//        beaconManager.setForegroundScanPeriod(1000L);
//
//        beaconManager.bind(this);
    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // iBeaconのデータを受信できるようにParserを設定
//        beaconManager = BeaconManager.getInstanceForApplication(this);
//        beaconManager.getBeaconParsers()
//                .add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
//
//        // UUID, major, minorの指定はしない
//        final Region region = new Region("iBeacon", uuid, null, null);
//        regionBootstrap = new RegionBootstrap(this, region);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        beaconManager.unbind(this);
//        beaconManager.disableForegroundServiceScanning();
    }

//    @Override
//    public void didEnterRegion(Region region) {
//        // 領域に入場した
//        Log.d("iBeacon", "Enter Region");
//    }
//
//    @Override
//    public void didExitRegion(Region region) {
//        // 領域から退場した
//        Log.d("iBeacon", "Exit Region");
//    }
//
//    @Override
//    public void didDetermineStateForRegion(int i, Region region) {
//        // 入退場状態が変更された
//        Log.d("iBeacon", "Determine State: " + i);
//        Log.d("iBeacon", "uuid: " + region.getId1());
//
//        //通知の発行
//        //システムから通知マネージャー取得
//        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        //アプリ名をチャンネルIDとして利用
//        String chID = getString(R.string.app_name);
//
//        //アンドロイドのバージョンで振り分け
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {     //APIが「26」以上の場合
//
//            //通知チャンネルIDを生成してインスタンス化
//            NotificationChannel notificationChannel = new NotificationChannel(chID, chID, NotificationManager.IMPORTANCE_DEFAULT);
//            //通知の説明のセット
//            notificationChannel.setDescription(chID);
//            //通知チャンネルの作成
//            notificationManager.createNotificationChannel(notificationChannel);
//            //通知の生成と設定とビルド
//            notification = new Notification.Builder(this, chID)
//                    .setContentTitle(getString(R.string.app_name))  //通知タイトル
//                    .setContentText("ビーコンの入退場状態が変更されました: " + i)        //通知内容
//                    .setSmallIcon(R.drawable.ic_launcher_background)                  //通知用アイコン
//                    .build();                                       //通知のビルド
//        } else {
//            //APIが「25」以下の場合
//            //通知の生成と設定とビルド
//            notification = new Notification.Builder(this)
//                    .setContentTitle(getString(R.string.app_name))
//                    .setContentText("ビーコンの入退場状態が変更されました: " + i)
//                    .setSmallIcon(R.drawable.ic_launcher_background)
//                    .build();
//        }
//        notificationManager.notify(1, notification);
//    }
//
//    @Override
//    public void onBeaconServiceConnect() {
//        final Region mRegion = new Region("iBeacon", null, null, null);
//
//        try {
//            //Beacon情報の監視を開始
//            beaconManager.startMonitoringBeaconsInRegion(mRegion);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//        beaconManager.addMonitorNotifier(new MonitorNotifier() {
//            @Override
//            public void didEnterRegion(Region region) {
//                //領域への入場を検知
//                Log.d("iBeacon", "Enter Region");
//            }
//
//            @Override
//            public void didExitRegion(Region region) {
//                //領域からの退場を検知
//                Log.d("iBeacon", "Exit Region");
//            }
//
//            @Override
//            public void didDetermineStateForRegion(int i, Region region) {
//                //領域への入退場のステータス変化を検知
//                Log.d("MyActivity", "Determine State" + i);
//            }
//
//        });
//
//        try {
//            beaconManager.startRangingBeaconsInRegion(mRegion);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//        beaconManager.addRangeNotifier((beacons, region) -> {
//            count = 0;
//            uuids = new ArrayList<>();
//            majors = new ArrayList<>();
//            minors = new ArrayList<>();
//            rssis = new ArrayList<>();
//            txPowers = new ArrayList<>();
//            distances = new ArrayList<>();
//
//            //検出したBeaconの情報を全てlog出力
//            for (Beacon beacon : beacons) {
//                Log.d("MyActivity", "UUID:" + beacon.getId1() + ", major:"
//                        + beacon.getId2() + ", minor:" + beacon.getId3() + ", RSSI:"
//                        + beacon.getRssi() + ", TxPower:" + beacon.getTxPower()
//                        + ", Distance:" + beacon.getDistance());
//
//                uuids.add(beacon.getId1());
//                majors.add(beacon.getId2());
//                minors.add(beacon.getId3());
//                rssis.add(beacon.getRssi());
//                txPowers.add(beacon.getTxPower());
//                distances.add(beacon.getDistance());
//            }
//
//            count = beacons.size();
//            Log.d("Activity", "total:" + count + "台");
//
//            TextView countView = (TextView) findViewById(R.id.result);
//            countView.setText(String.valueOf(count));
//
//            ListView beaconList = (ListView) findViewById(R.id.beacon_list);
//            ArrayList<BeaconListItems> listItems = new ArrayList<>();
//            for (int i = 0; i < count; i++) {
//                BeaconListItems beaconItem = new BeaconListItems(uuids.get(i), majors.get(i),
//                        minors.get(i), rssis.get(i), txPowers.get(i), distances.get(i));
//                listItems.add(beaconItem);
//            }
//            BeaconListAdapter beaconAdapter = new BeaconListAdapter(this, R.layout.beacon_view, listItems);
//            beaconList.setAdapter(beaconAdapter);
//        });
//    }

    public void click() {
        Intent serviceIntent = new Intent(getApplication(), BeaconService.class);
        stopService(serviceIntent);
    }
}
