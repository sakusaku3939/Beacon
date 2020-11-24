package com.sakusaku.beacon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class BeaconService extends Service implements BeaconConsumer {
    //iBeacon認識のためのフォーマット設定
    private static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private BeaconManager beaconManager;
    String uuidString = "CCCCCCCC-CCCC-CCCC-CCCC-CCCCCCCCCCCC";

    private myWsClientListener ws;
    private static final String ServerIP = "192.168.43.127";
    private static final String ServerPORT = "8081";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //サーバーの接続準備
        try {
            ws = new BeaconService.myWsClientListener(new URI("ws://" + ServerIP + ":" + ServerPORT + "/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if(!ws.isOpen()) {
            ws.connect();
        }

        if (ws.isOpen()){
            ws.send("hello");
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        String channelId = "Foreground";
        String title = "ビーコン取得通知";
        Intent notificationIntent = new Intent(this, BeaconActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // ForegroundにするためNotificationが必要、Contextを設定
        NotificationManager notificationManager =
                (NotificationManager) this.
                        getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification　Channel 設定
        NotificationChannel channel = new NotificationChannel(
                channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
        // 通知音を消す
        channel.setSound(null, null);
        // 通知ランプを消す
        channel.enableLights(false);
        // 通知バイブレーション無し
        channel.enableVibration(false);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("ビーコン情報取得中")
                    // 本来なら衛星のアイコンですがandroid標準アイコンを設定
                    .setSmallIcon(R.drawable.ic_baseline_wifi_tethering_24)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();

            startForeground(1, notification);
        }

        beaconManager.setBackgroundBetweenScanPeriod(5000L);
        beaconManager.setForegroundBetweenScanPeriod(5000L);
        beaconManager.bind(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        ws.close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {
        // 二重登録されるので一旦削除
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
        beaconManager.getRangedRegions().forEach(region ->
        {
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        final Identifier uuid = Identifier.parse(uuidString);
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
            //検出したBeaconの情報を全てlog出力
            for (Beacon beacon : beacons) {
                Log.d("MyActivity", "UUID:" + beacon.getId1() + ", major:"
                        + beacon.getId2() + ", minor:" + beacon.getId3() + ", RSSI:"
                        + beacon.getRssi() + ", TxPower:" + beacon.getTxPower()
                        + ", Distance:" + beacon.getDistance());
            }

            Log.d("iBeacon", beacons.iterator().next().getId1().toString());

            Log.d("Activity", "total:" + beacons.size() + "台");
        });
    }

    //WS Lister
    private static class myWsClientListener extends WebSocketClient {
        public myWsClientListener(URI serverUri) {
            super(serverUri);
        }

        @Override
        //接続
        public void onOpen(ServerHandshake serverHandshake) {
            Log.d("WebSocket", "Connected");
        }

        @Override
        //Serverからのメッセージの受信
        public void onMessage(final String message) {
            Log.d("WebSocket", message);
        }

        @Override
        //Serverの切断
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Disconnected");
        }

        @Override
        //エラー
        public void onError(Exception ex) {
            Log.d("WebSocket", "error");
        }
    }
}
