package com.sakusaku.beacon

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.preference.PreferenceManager
import com.sakusaku.beacon.firebase.RealtimeDatabaseUtils
import com.sakusaku.beacon.ui.location.BeaconInfo
import com.sakusaku.beacon.ui.location.FloorMapBeacon
import org.altbeacon.beacon.*
import java.util.function.Consumer

class BeaconService : Service(), BeaconConsumer {
    private lateinit var beaconManager: BeaconManager

    companion object {
        // iBeacon認識のためのフォーマット設定
        private const val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        private const val uuidString: String = "82fc3dbe-24d0-ef62-df80-50ead855daf8"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

        // スキャンの間隔を設定
        val scanPeriod = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString("preference_scan_period", "短い")
        beaconManager.foregroundBetweenScanPeriod = when (scanPeriod) {
            "短い" -> 5000L
            "中間" -> 10000L
            "長い" -> 20000L
            else -> 5000L
        }

        beaconManager.bind(this)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        RealtimeDatabaseUtils.deleteUserLocation(applicationContext)
        beaconManager.unbind(this)
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
        val mRegion = Region("iBeacon", uuid, null, null)
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
            var firstBeacon: Beacon? = null
            for (beacon in beacons) {
                Log.d("BeaconInfo", "UUID:" + beacon.id1 + ", major:"
                        + beacon.id2 + ", minor:" + beacon.id3 + ", RSSI:"
                        + beacon.rssi + ", TxPower:" + beacon.txPower
                        + ", Distance:" + beacon.distance)

                if (firstBeacon == null || firstBeacon.distance > beacon.distance) firstBeacon = beacon
            }
            Log.d("BeaconInfo", "total:" + beacons.size + "台")

            // 一番近いビーコンから位置を割り出し
            val passLocation = firstBeacon?.let { beacon ->
                val major = beacon.id2.toInt()
                val minor = beacon.id3.toInt()
                val key = BeaconInfo(major, minor)

                val location = FloorMapBeacon.LOCATION.map[key]
                if (location != null) RealtimeDatabaseUtils.writeUserLocation(applicationContext, major, location)
                location
            }

            // ビーコンがない場合はデータベースから削除
            if (passLocation == null) RealtimeDatabaseUtils.deleteUserLocation(applicationContext)

            // Fragmentに現在位置を渡す
            val broadcast = Intent()
            broadcast.putExtra("location", passLocation ?: "なし")
            broadcast.action = "DO_ACTION"
            baseContext.sendBroadcast(broadcast)
        }
    }
}