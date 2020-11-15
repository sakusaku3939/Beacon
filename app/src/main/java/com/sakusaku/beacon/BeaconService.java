package com.sakusaku.beacon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
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

public class BeaconService extends Service implements BeaconConsumer {
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

    String uuidString = "CCCCCCCC-CCCC-CCCC-CCCC-CCCCCCCCCCCC";
    Identifier uuid = Identifier.parse(uuidString);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Scanning for Beacons");
        Intent notificationIntent = new Intent(this, BeaconActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
            startForeground(1, builder.build());
        }

//        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
//        beaconManager.setEnableScheduledScanJobs(false);
//        beaconManager.setForegroundScanPeriod(1000L);

        beaconManager.bind(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

        beaconManager.addRangeNotifier((beacons, region) -> {
            count = 0;
            uuids = new ArrayList<>();
            majors = new ArrayList<>();
            minors = new ArrayList<>();
            rssis = new ArrayList<>();
            txPowers = new ArrayList<>();
            distances = new ArrayList<>();

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
        });
    }
}
