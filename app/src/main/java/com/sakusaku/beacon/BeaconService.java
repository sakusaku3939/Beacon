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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class BeaconService extends Service implements BeaconConsumer {
    // iBeacon認識のためのフォーマット設定
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
        // サーバーの接続準備
        try {
            ws = new BeaconService.myWsClientListener(new URI("ws://" + ServerIP + ":" + ServerPORT + "/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if(!ws.isOpen()) {
            ws.connect();
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        // フォアグラウンド通知設定
        String channelId = "Foreground";
        String title = "ビーコン取得通知";
        Intent notificationIntent = new Intent(this, BeaconActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) this.
                        getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.enableLights(false);
        channel.enableVibration(false);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("ビーコン情報取得中")
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
                Log.d("iBeacon", "Enter Region");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d("iBeacon", "Exit Region");
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.d("MyActivity", "Determine State" + i);
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.addRangeNotifier((beacons, region) -> {
            for (Beacon beacon : beacons) {
                Log.d("BeaconInfo", "UUID:" + beacon.getId1() + ", major:"
                        + beacon.getId2() + ", minor:" + beacon.getId3() + ", RSSI:"
                        + beacon.getRssi() + ", TxPower:" + beacon.getTxPower()
                        + ", Distance:" + beacon.getDistance());
            }

            Beacon firstBeacon = beacons.iterator().hasNext() ? beacons.iterator().next() : null;

            // WebSocketによるビーコンデータ送信
            if (firstBeacon != null) {
                JSONObject json1 = new JSONObject();
                try {
                    json1.put("major", firstBeacon.getId2().toString());
                    json1.put("minor", firstBeacon.getId2().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(json1);
                String jsonData = jsonArray.toString();

                if (ws.isOpen()){
                    ws.send(jsonData);
                    Log.d("WebSocket", "send:" + jsonData);
                }
            }

            Log.d("BeaconInfo", "total:" + beacons.size() + "台");
        });
    }

    private static class myWsClientListener extends WebSocketClient {
        public myWsClientListener(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            Log.d("WebSocket", "Connected");
        }

        @Override
        public void onMessage(final String message) {
            Log.d("WebSocket", message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Disconnected");
        }

        @Override
        public void onError(Exception ex) {
            Log.d("WebSocket", "error");
        }
    }
}
