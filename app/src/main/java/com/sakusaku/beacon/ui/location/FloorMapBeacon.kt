package com.sakusaku.beacon.ui.location

enum class FloorMapBeacon(val map: Map<BeaconInfo, String>) {
    LOCATION(mapOf(
            BeaconInfo(1, 1) to "図書室" ,
            BeaconInfo(1, 2) to "司書室",
            BeaconInfo(1, 3) to "小講義室",
            BeaconInfo(1, 4) to "保険室",
            BeaconInfo(1, 5) to "環境整備準備室",
            BeaconInfo(1, 6) to "カウンセリング室" ,
            BeaconInfo(1, 7) to "アドバイザー室",
            BeaconInfo(1, 8) to "NT準備室",
            BeaconInfo(1, 9) to "材料実験室",
            BeaconInfo(1, 10) to "精密加工室",
            BeaconInfo(1, 11) to "NT基礎実習室1",
            BeaconInfo(1, 12) to "NT基礎実習室2" ,
            BeaconInfo(1, 13) to "NT標本室",
            BeaconInfo(1, 14) to "材料顕微鏡室",
            BeaconInfo(1, 15) to "ミニレーザー室",
            BeaconInfo(1, 16) to "経営企画室",
            BeaconInfo(1, 17) to "サイエンスホール" ,
            BeaconInfo(1, 18) to "保護者控室",
            BeaconInfo(1, 19) to "メモリアルルーム" ,
            BeaconInfo(1, 20) to "新素材実習室1",
            BeaconInfo(1, 21) to "新素材実習室2",
            BeaconInfo(1, 22) to "101ゼミ室",
            BeaconInfo(1, 23) to "102ゼミ室"
    ))
}

data class BeaconInfo(val major: Int, val minor: Int)