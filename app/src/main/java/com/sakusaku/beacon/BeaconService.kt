package com.sakusaku.beacon

import android.app.*
import android.content.Intent
import android.os.*
import android.util.Log
import org.altbeacon.beacon.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.util.function.Consumer

class BeaconService : Service(), BeaconConsumer {
    private lateinit var beaconManager: BeaconManager
    private lateinit var ws: WsClientListener

    companion object {
        // iBeacon認識のためのフォーマット設定
        private const val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        private const val uuidString: String = "CCCCCCCC-CCCC-CCCC-CCCC-CCCCCCCCCCCC"
        private const val ServerIP: String = "192.168.43.127"
        private const val ServerPORT: String = "8081"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // サーバーの接続準備
        try {
            ws = WsClientListener(URI("ws://$ServerIP:$ServerPORT/"))
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        if (!ws.isOpen()) {
            ws.connect()
        }

        // ビーコン取得ライブラリのセットアップ
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))

        // フォアグラウンド通知設定
        val channelId = "Foreground"
        val title = "ビーコン取得通知"
        val notificationIntent = Intent(this, BeaconActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
                channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
        channel.setSound(null, null)
        channel.enableLights(false)
        channel.enableVibration(false)
        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(this, channelId)
                .setContentTitle("ビーコン情報取得中")
                .setSmallIcon(R.drawable.ic_baseline_wifi_tethering_24)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()
        startForeground(1, notification)
        beaconManager.backgroundBetweenScanPeriod = 5000L
        beaconManager.foregroundBetweenScanPeriod = 5000L
        beaconManager.bind(this)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
        ws.close()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onBeaconServiceConnect() {
        // 二重登録されるので一旦削除
        beaconManager.removeAllMonitorNotifiers()
        beaconManager.removeAllRangeNotifiers()
        beaconManager.rangedRegions.forEach(Consumer { region: Region ->
            try {
                beaconManager.stopRangingBeaconsInRegion(region)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        })
        val uuid = Identifier.parse(uuidString)
        val mRegion = Region("iBeacon", null, null, null)
        try {
            //Beacon情報の監視を開始
            beaconManager.startMonitoringBeaconsInRegion(mRegion)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        beaconManager.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region?) {
                Log.d("iBeacon", "Enter Region")
            }

            override fun didExitRegion(region: Region?) {
                Log.d("iBeacon", "Exit Region")
            }

            override fun didDetermineStateForRegion(i: Int, region: Region?) {
                Log.d("iBeacon", "Determine State$i")
            }
        })
        try {
            beaconManager.startRangingBeaconsInRegion(mRegion)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        beaconManager.addRangeNotifier { beacons: MutableCollection<Beacon>, _: Region ->
            for (beacon in beacons) {
                Log.d("BeaconInfo", "UUID:" + beacon.id1 + ", major:"
                        + beacon.id2 + ", minor:" + beacon.id3 + ", RSSI:"
                        + beacon.rssi + ", TxPower:" + beacon.txPower
                        + ", Distance:" + beacon.distance)
            }
            val firstBeacon = if (beacons.iterator().hasNext()) beacons.iterator().next() else null

            // WebSocketによるビーコンデータ送信
            if (firstBeacon != null) {
                val json1 = JSONObject()
                try {
                    json1.put("major", firstBeacon.id2.toString())
                    json1.put("minor", firstBeacon.id2.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val jsonArray = JSONArray()
                jsonArray.put(json1)
                val jsonData = jsonArray.toString()
                if (ws.isOpen) {
                    ws.send(jsonData)
                    Log.d("WebSocket", "send:$jsonData")
                }
            }
            Log.d("BeaconInfo", "total:" + beacons.size + "台")
        }
    }

    private class WsClientListener(serverUri: URI?) : WebSocketClient(serverUri) {
        override fun onOpen(serverHandshake: ServerHandshake?) {
            Log.d("WebSocket", "Connected")
        }

        override fun onMessage(message: String) {
            Log.d("WebSocket", message)
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.d("WebSocket", "Disconnected")
        }

        override fun onError(ex: Exception) {
            Log.d("WebSocket", "error")
        }
    }
}