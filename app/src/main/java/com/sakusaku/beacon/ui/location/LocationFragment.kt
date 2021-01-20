package com.sakusaku.beacon.ui.location

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.rijckaert.tim.animatedvector.FloatingMusicActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sakusaku.beacon.BeaconService
import com.sakusaku.beacon.R
import com.skyfishjy.library.RippleBackground


class LocationFragment : Fragment() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_location, container, false)

        val floorMapImage = root.findViewById<ImageView>(R.id.floorMapImage)
        val mapPin = root.findViewById<View>(R.id.mapPin)
        val mapPinRipple = root.findViewById<RippleBackground>(R.id.mapPinRipple)
        val coordinate1F = coordinate1F()
        mapPin.visibility = View.VISIBLE
//        mapPin.translationX = convertCoordinateToTranslation(
//                coordinate1F["図書室"]?.first ?: 0F, floorMapImage.x, floorMapImage.width)
//        mapPin.translationY = convertCoordinateToTranslation(
//                coordinate1F["図書室"]?.second ?: 0F, floorMapImage.y, floorMapImage.height)
//        mapPin.translationX = (coordinate1F["経営企画室"]?.first ?: 0F) / 1.43F
//        Log.d("test", mapPin.translationX.toString())
//        mapPin.translationX = 90F
        mapPin.translationX = 645F
//        mapPin.animate().translationX(0F).translationXBy(1000F).duration = 10000

        floorMapImage.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
//                Log.d("test", "${event.x}F, ${event.y}F")
//                mapPinRipple.startRippleAnimation()
//                floorMapImage.translationX = convertCoordinateToTranslation(event.x, floorMapImage.x, floorMapImage.width)
//                floorMapImage.translationY = convertCoordinateToTranslation(event.y, floorMapImage.y, floorMapImage.height)
//                floorMapImage.scaleX = 1.5F
//                floorMapImage.scaleY = 1.5F
//                mapPin.visibility = View.VISIBLE
            } else if (event.action == MotionEvent.ACTION_UP) {
//                mapPinRipple.stopRippleAnimation()
//                floorMapImage.translationX = 0F
//                floorMapImage.translationY = 0F
//                floorMapImage.scaleX = 1.0F
//                floorMapImage.scaleY = 1.0F
//                mapPin.visibility = View.GONE
            }
            true
        }

        // 校内図
        val floorTab = root.findViewById<RadioGroup>(R.id.floorTab)
        floorTab.setOnCheckedChangeListener { _, checkedId ->
            val imageResource = when (checkedId) {
                R.id.floorTab1F -> R.drawable.school_map_1f
                R.id.floorTab2F -> R.drawable.school_map_2f
                R.id.floorTab3F -> R.drawable.school_map_3f
                R.id.floorTab4F -> R.drawable.school_map_4f
                R.id.floorTab5F -> R.drawable.school_map_5f
                else -> null
            }
            imageResource?.let { floorMapImage.setImageResource(it) }
        }

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

    private fun convertCoordinateToTranslation(coordinate: Float, imageCoordinate: Float, imageWidth: Int): Float {
        return -(coordinate - imageCoordinate - imageWidth / 2) * 1.5F
    }

    private fun coordinate1F() : Map<String, Pair<Float, Float>> {
        return mapOf(
                "図書室" to Pair(162.97949F, 263.9375F),
                "司書室" to Pair(292.96973F, 405.96875F),
                "小講義室" to Pair(147.9502F, 546.90625F),
                "保険室" to Pair(120.96777F, 687.9219F),
                "環境整備準備室" to Pair(40.987305F, 817.9219F),
                "カウンセリング室" to Pair(292.96973F, 779.9531F),
                "アドバイザー室" to Pair(495.95312F, 683.9375F),
                "NT準備室" to Pair(376.94922F, 1015.8906F),
                "材料実験室" to Pair(495.95312F, 870.8906F),
                "精密加工室" to Pair(498.9414F, 1015.8906F),
                "NT基礎実習室1" to Pair(659.95703F, 943.9375F),
                "NT基礎実習室2" to Pair(827.95996F, 946.90625F),
                "NT標本室" to Pair(953.9512F, 874.9531F),
                "材料顕微鏡室" to Pair(1033.9316F, 882.9219F),
                "ミニレーザー室" to Pair(995.9629F, 1019.875F),
                "経営企画室" to Pair(914.92773F, 84.953125F),
                "サイエンスホール" to Pair(869.9717F, 546.90625F),
                "保護者控室" to Pair(1128.9414F, 84.953125F),
                "メモリアルルーム" to Pair(1212.9209F, 88.9375F),
                "新素材実習室1" to Pair(1201.9346F, 821.90625F),
                "新素材実習室2" to Pair(1205.9336F, 717.9219F),
                "101ゼミ室" to Pair(1201.9346F, 1015.8906F),
                "102ゼミ室" to Pair(1117.9551F, 1015.8906F),
        )
    }
}