package com.sakusaku.beacon.ui.location;

enum class FloorMapPosition(val map: Map<String, Position>) {
    P_1F(mapOf(
            "図書室" to Position(162.97949F, 263.9375F),
            "司書室" to Position(292.96973F, 405.96875F),
            "小講義室" to Position(147.9502F, 546.90625F),
            "保険室" to Position(120.96777F, 687.9219F),
            "環境整備準備室" to Position(40.987305F, 817.9219F),
            "カウンセリング室" to Position(292.96973F, 779.9531F),
            "アドバイザー室" to Position(495.95312F, 683.9375F),
            "NT準備室" to Position(376.94922F, 1015.8906F),
            "材料実験室" to Position(495.95312F, 870.8906F),
            "精密加工室" to Position(498.9414F, 1015.8906F),
            "NT基礎実習室1" to Position(659.95703F, 943.9375F),
            "NT基礎実習室2" to Position(827.95996F, 946.90625F),
            "NT標本室" to Position(953.9512F, 874.9531F),
            "材料顕微鏡室" to Position(1033.9316F, 882.9219F),
            "ミニレーザー室" to Position(995.9629F, 1019.875F),
            "経営企画室" to Position(914.92773F, 84.953125F),
            "サイエンスホール" to Position(869.9717F, 546.90625F),
            "保護者控室" to Position(1128.9414F, 84.953125F),
            "メモリアルルーム" to Position(1212.9209F, 88.9375F),
            "新素材実習室1" to Position(1201.9346F, 821.90625F),
            "新素材実習室2" to Position(1205.9336F, 717.9219F),
            "101ゼミ室" to Position(1201.9346F, 1015.8906F),
            "102ゼミ室" to Position(1117.9551F, 1015.8906F),
    ))
}

data class Position(val x: Float, val y: Float)