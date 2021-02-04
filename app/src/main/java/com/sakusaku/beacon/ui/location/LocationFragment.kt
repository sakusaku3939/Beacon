package com.sakusaku.beacon.ui.location

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.rijckaert.tim.animatedvector.FloatingMusicActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sakusaku.beacon.*
import com.skyfishjy.library.RippleBackground


class LocationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_location, container, false)

//        RealtimeDatabaseUtils.getFloorMapData(1)

        // 校内図
        val floorMap = root.findViewById<FrameLayout>(R.id.floorMap)
        val floorTab = root.findViewById<RadioGroup>(R.id.floorTab)
        val floorMapImage = root.findViewById<ImageView>(R.id.floorMapImage)

        // ユーザーピンを追加
        var floorMapHeight1F = 0
        floorMapImage.afterMeasured {
            floorMapHeight1F = floorMapImage.height
            floorMap.layoutParams.height = floorMapImage.height
            addUserPin(root, FloorMapPosition.P_1F.map, "図書室")
            addUserPin(root, FloorMapPosition.P_1F.map, "環境整備準備室")
            addUserPin(root, FloorMapPosition.P_1F.map, "経営企画室")
            addUserPin(root, FloorMapPosition.P_1F.map, "NT準備室")
            addUserPin(root, FloorMapPosition.P_1F.map, "メモリアルルーム")
        }

        // 校内図のタブ切り替え
        floorTab.setOnCheckedChangeListener { _, checkedId ->
            val imageResource = when (checkedId) {
                R.id.floorTab1F -> {
                    floorMapImage.afterMeasured {
                        addUserPin(root, FloorMapPosition.P_1F.map, "図書室")
                        addUserPin(root, FloorMapPosition.P_1F.map, "環境整備準備室")
                        addUserPin(root, FloorMapPosition.P_1F.map, "経営企画室")
                        addUserPin(root, FloorMapPosition.P_1F.map, "NT準備室")
                        addUserPin(root, FloorMapPosition.P_1F.map, "メモリアルルーム")
                    }
                    R.drawable.school_map_1f
                }
                R.id.floorTab2F -> {
                    floorMapImage.afterMeasured {
                        addUserPin(root, FloorMapPosition.P_1F.map, "経営企画室")
                        addUserPin(root, FloorMapPosition.P_1F.map, "NT準備室")
                        addUserPin(root, FloorMapPosition.P_1F.map, "メモリアルルーム")
                    }
                    R.drawable.school_map_2f
                }
                R.id.floorTab3F -> R.drawable.school_map_3f
                R.id.floorTab4F -> R.drawable.school_map_4f
                R.id.floorTab5F -> R.drawable.school_map_5f
                else -> null
            }
            imageResource?.let { floorMapImage.setImageResource(it) }

            // ユーザーピンを削除
            val mapPinLayout = root.findViewById<FrameLayout>(R.id.mapPinLayout)
            mapPinLayout.removeAllViews()

            // 画像の高さを動的に設定
            floorMap.layoutParams.height = when (checkedId) {
                R.id.floorTab1F -> floorMapHeight1F
                else -> floorMapHeight1F * 0.94F
            }.toInt()
        }

        // タッチ座標をログに書き出し
//        FloorMapPositionTest.logTouchPosition(floorMapImage)

        // 同じ階にいる先生の表示
        val teacherList = listOf(
                PeopleGrid(R.drawable.user, "Name1", "101"),
                PeopleGrid(R.drawable.user, "Name2", "102"),
                PeopleGrid(R.drawable.user, "Name3", "103"),
                PeopleGrid(R.drawable.user, "Name4", "104"),
                PeopleGrid(R.drawable.user, "Name5", "105"),
                PeopleGrid(R.drawable.user, "Name6", "106"),
        )
        setPeopleGrid(teacherList, root.findViewById(R.id.teacherPeopleGrid))

        // 同じ階にいる生徒の表示
        val studentList = listOf(
                PeopleGrid(R.drawable.user, "Name1", "101"),
                PeopleGrid(R.drawable.user, "Name2", "102"),
                PeopleGrid(R.drawable.user, "Name3", "103"),
        )
        setPeopleGrid(studentList, root.findViewById(R.id.studentPeopleGrid))

        // FABの設定
        val fab: FloatingActionButton = root.findViewById(R.id.fab)
        val customFab = fab as FloatingMusicActionButton

        // フォアグラウンド実行中か
        val manager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (serviceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BeaconService::class.java.name == serviceInfo.service.className) {
                fabToggle(fab, FloatingMusicActionButton.Mode.PAUSE_TO_PLAY)
                customFab.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY)
            }
        }

        fab.setOnMusicFabClickListener(object : FloatingMusicActionButton.OnMusicFabClickListener {
            override fun onClick(view: View) {
                when (customFab.getOppositeMode()) {
                    // ビーコン取得開始
                    FloatingMusicActionButton.Mode.PAUSE_TO_PLAY -> {
                        // デバイスのBLE対応チェック
                        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                            Toast.makeText(activity, "このデバイスはBLE未対応です", Toast.LENGTH_LONG).show()
                            Handler().postDelayed({
                                customFab.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_PAUSE)
                                fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_500))
                            }, 300)
                        } else {
                            fabToggle(fab, FloatingMusicActionButton.Mode.PAUSE_TO_PLAY)
                            requireActivity().startForegroundService(Intent(activity, BeaconService::class.java))
                        }
                    }
                    // ビーコン取得停止
                    FloatingMusicActionButton.Mode.PLAY_TO_PAUSE -> {
                        fabToggle(fab, FloatingMusicActionButton.Mode.PLAY_TO_PAUSE)
                        requireActivity().stopService(Intent(activity, BeaconService::class.java))
                    }
                    else -> {
                    }
                }

                // 連続クリックを無効化
                customFab.isClickable = false
                Handler().postDelayed({
                    customFab.isClickable = true
                }, 400)
            }
        })

        return root
    }

    private fun setPeopleGrid(list: List<PeopleGrid>, peopleRecyclerView: RecyclerView?) {
        val customAdapter = PeopleGridAdapter(list, object : PeopleGridAdapter.ListListener {
            override fun onClickItem(tappedView: View, name: String, location: String) {
            }
        })

        peopleRecyclerView?.apply {
            val manager = LinearLayoutManager(requireContext())
            manager.orientation = LinearLayoutManager.HORIZONTAL
            layoutManager = manager
            isNestedScrollingEnabled = false
            adapter = customAdapter
            setHasFixedSize(true)
        }
    }

    private fun fabToggle(fab: FloatingActionButton, fabMode: FloatingMusicActionButton.Mode) {
        val toolbar = requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val progress = requireActivity().findViewById<ProgressBar>(R.id.progress)
        val actionBarHeight = resources.getDimension(R.dimen.mtrl_toolbar_default_height).toInt()

        when (fabMode) {
            FloatingMusicActionButton.Mode.PAUSE_TO_PLAY -> {
                progress.visibility = View.VISIBLE
                toolbar.layoutParams.height = actionBarHeight + 12
                toolbar.subtitle = "現在位置: test"
                fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            }
            FloatingMusicActionButton.Mode.PLAY_TO_PAUSE -> {
                progress.visibility = View.GONE
                toolbar.subtitle = ""
                toolbar.layoutParams.height = actionBarHeight
                fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_500))
            }
            else -> {
            }
        }
    }

    private fun addUserPin(root: View, positionMap: Map<String, Position>, positionName: String) {
        val floorMapImage = root.findViewById<ImageView>(R.id.floorMapImage)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val positionX = (positionMap[positionName]?.x ?: -10F) / 100
        val positionY = (positionMap[positionName]?.y ?: -10F) / 100
        params.marginStart = (floorMapImage.width * positionX).toInt() - 32F.dp()
        params.topMargin = (floorMapImage.height * positionY).toInt() - 32F.dp()

        val view = layoutInflater.inflate(R.layout.map_pin, FrameLayout(requireContext()))
        val mapPinRipple = view.findViewById<RippleBackground>(R.id.mapPinRipple)
        mapPinRipple.startRippleAnimation()

        val mapPinLayout = root.findViewById<FrameLayout>(R.id.mapPinLayout)
        mapPinLayout.addView(view, params)
    }

    private fun Float.dp() = (this * resources.displayMetrics.density).toInt()
}

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}