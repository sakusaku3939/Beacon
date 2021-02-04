package com.sakusaku.beacon.ui.location;

enum class FloorMapPosition(val map: Map<String, Position>) {
    P_1F(mapOf(
            "図書室" to
                    Position(12.2725525F, 24.25896F),
            "司書室" to
                    Position(22.060974F, 37.313305F),
            "小講義室" to
                    Position(11.140828F, 50.267117F),
            "保険室" to
                    Position(9.109018F, 63.22811F),
            "環境整備準備室" to
                    Position(3.0863936F, 75.17664F),
            "カウンセリング室" to
                    Position(22.060974F, 71.68687F),
            "アドバイザー室" to
                    Position(37.345867F, 62.8619F),
            "NT準備室" to
                    Position(28.38473F, 93.3723F),
            "材料実験室" to
                    Position(37.345867F, 80.04509F),
            "精密加工室" to
                    Position(37.57089F, 93.3723F),
            "NT基礎実習室1" to
                    Position(49.69556F, 86.75896F),
            "NT基礎実習室2" to
                    Position(62.346382F, 87.03182F),
            "NT標本室" to
                    Position(71.83367F, 80.41849F),
            "材料顕微鏡室" to
                    Position(77.85629F, 81.15091F),
            "ミニレーザー室" to
                    Position(74.99721F, 93.73851F),
            "経営企画室" to
                    Position(68.895164F, 7.8081913F),
            "サイエンスホール" to
                    Position(65.50992F, 50.267117F),
            "保護者控室" to
                    Position(85.01065F, 7.8081913F),
            "メモリアルルーム" to
                    Position(91.334404F, 8.174402F),
            "新素材実習室1" to
                    Position(90.50712F, 75.542854F),
            "新素材実習室2" to
                    Position(90.80826F, 65.985466F),
            "101ゼミ室" to
                    Position(90.50712F, 93.3723F),
            "102ゼミ室" to
                    Position(84.183365F, 93.3723F)
    ))
}

const val FLOOR_MAP_MEASURE_WIDTH = 1328
const val FLOOR_MAP_MEASURE_HEIGHT = 1088

data class Position(val x: Float, val y: Float)