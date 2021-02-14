package com.sakusaku.beacon.ui.location

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import be.rijckaert.tim.animatedvector.FloatingMusicActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sakusaku.beacon.*
import kotlin.concurrent.thread


class LocationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_location, container, false)
        val handler = Handler()

        // 先生が読み込み中または同じ階層に居ない時の処理
        val teacherPeopleGrid = root.findViewById<RecyclerView>(R.id.teacherPeopleGrid)
        val teacherNoUser = root.findViewById<TextView>(R.id.teacherNoUser)

        fun teacherFirstLoad(floor: Int) {
            val beforeGrid = root.findViewById<FrameLayout>(R.id.teacherBeforeGrid)
            val progress = root.findViewById<ProgressBar>(R.id.teacherProgress)
            progress.visibility = View.VISIBLE
            beforeGrid.visibility = View.VISIBLE
            teacherPeopleGrid.visibility  = View.GONE
            teacherNoUser.visibility = View.GONE

            RealtimeDatabaseUtils.floorUserExist(floor, "先生") { isExist ->
                val locationScrollView = root.findViewById<FrameLayout>(R.id.locationScrollView)
                locationScrollView.scrollTo(0, 0)
                thread {
                    handler.post {
                        progress.visibility = View.GONE
                        if (isExist) {
                            beforeGrid.visibility = View.GONE
                            teacherPeopleGrid.visibility = View.VISIBLE
                        } else {
                            teacherNoUser.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        teacherFirstLoad(1)

        // 生徒が読み込み中または同じ階層に居ない時の処理
        val studentPeopleGrid = root.findViewById<RecyclerView>(R.id.studentPeopleGrid)
        val studentNoUser = root.findViewById<TextView>(R.id.studentNoUser)

        fun studentFirstLoad(floor: Int) {
            val beforeGrid = root.findViewById<FrameLayout>(R.id.studentBeforeGrid)
            val progress = root.findViewById<ProgressBar>(R.id.studentProgress)
            progress.visibility = View.VISIBLE
            beforeGrid.visibility = View.VISIBLE
            studentPeopleGrid.visibility  = View.GONE
            studentNoUser.visibility = View.GONE

            RealtimeDatabaseUtils.floorUserExist(floor, "生徒") { isExist ->
                thread {
                    handler.post {
                        progress.visibility = View.GONE
                        if (isExist) {
                            beforeGrid.visibility = View.GONE
                            studentPeopleGrid.visibility = View.VISIBLE
                        } else {
                            studentNoUser.visibility = View.VISIBLE
                        }
                        val locationScrollView = root.findViewById<FrameLayout>(R.id.locationScrollView)
                        locationScrollView.scrollTo(0, 0)
                    }
                }
            }
        }
        studentFirstLoad(1)

        // 校内図
        val floorMap = root.findViewById<FrameLayout>(R.id.floorMap)
        val floorTab = root.findViewById<RadioGroup>(R.id.floorTab)
        val floorMapImage = root.findViewById<ImageView>(R.id.floorMapImage)

        // ユーザーピンを追加
        val mapPinLayout = root.findViewById<FrameLayout>(R.id.mapPinLayout)
        val userPin = UserPin(requireContext(), inflater, floorMapImage, mapPinLayout)
        var floorMapHeight1F = 0
        floorMapImage.afterMeasured {
            floorMap.layoutParams.height = floorMapImage.height
            floorMapHeight1F = floorMapImage.height
        }

        // 同じ階にいる先生、生徒の表示
        val teacherGrid = PeopleGrid(requireContext(), teacherPeopleGrid)
        val studentGrid = PeopleGrid(requireContext(), studentPeopleGrid)
        fun setUserLocationUpdateListener(floor: Int) = RealtimeDatabaseUtils.userLocationUpdateListener(floor) { dataSnapshot, state ->
            when (state) {
                "USER_ADDED" -> {
                    val name = dataSnapshot.child("name").value.toString()
                    val position = dataSnapshot.child("position").value.toString()
                    val location = dataSnapshot.child("location").value.toString()
                    val timestamp = dataSnapshot.child("timestamp").value.toString()
                    val uid = dataSnapshot.key.toString()

                    val add = { grid: PeopleGrid, peopleGrid: RecyclerView, noUser: TextView ->
                        if (grid.count() == 0) thread {
                            handler.post {
                                peopleGrid.visibility = View.VISIBLE
                                noUser.visibility = View.GONE
                            }
                        }
                        grid.add(R.drawable.user, uid, name, location, timestamp)
                    }

                    when (position) {
                        "先生" -> add(teacherGrid, teacherPeopleGrid, teacherNoUser)
                        "生徒" -> add(studentGrid, studentPeopleGrid, studentNoUser)
                    }

                    userPin.add(FloorMapLocation.P_1F.map, uid, location)
                }
                "USER_CHANGED" -> {
                    val position = dataSnapshot.child("position").value.toString()
                    val location = dataSnapshot.child("location").value.toString()
                    val timestamp = dataSnapshot.child("timestamp").value.toString()
                    val uid = dataSnapshot.key.toString()

                    when (position) {
                        "先生" -> teacherGrid.update(uid, location, timestamp)
                        "生徒" -> studentGrid.update(uid, location, timestamp)
                    }
                }
                "USER_REMOVED" -> {
                    val position = dataSnapshot.child("position").value.toString()
                    val uid = dataSnapshot.key.toString()

                    val remove = { grid: PeopleGrid, peopleGrid: RecyclerView, noUser: TextView ->
                        grid.remove(uid)
                        if (grid.count() == 0) thread {
                            handler.post {
                                peopleGrid.visibility = View.GONE
                                noUser.visibility = View.VISIBLE
                            }
                        }
                    }

                    when (position) {
                        "先生" -> remove(teacherGrid, teacherPeopleGrid, teacherNoUser)
                        "生徒" -> remove(studentGrid, studentPeopleGrid, studentNoUser)
                    }
                }
            }
        }

        setUserLocationUpdateListener(1)

        // 校内図のタブ切り替え
        floorTab.setOnCheckedChangeListener { _, checkedId ->
            // 切り替え前のデータを削除
            userPin.removeAll()
            studentGrid.removeAll()
            teacherGrid.removeAll()
            RealtimeDatabaseUtils.removeAllUserLocationUpdateListener()

            // 初回ロード＆画像の切り替え
            val imageResource = when (checkedId) {
                R.id.floorTab1F -> {
                    teacherFirstLoad(1)
                    studentFirstLoad(1)
                    setUserLocationUpdateListener(1)
                    R.drawable.school_map_1f
                }
                R.id.floorTab2F -> {
                    teacherFirstLoad(2)
                    studentFirstLoad(2)
                    setUserLocationUpdateListener(2)
                    R.drawable.school_map_2f
                }
                R.id.floorTab3F -> R.drawable.school_map_3f
                R.id.floorTab4F -> R.drawable.school_map_4f
                R.id.floorTab5F -> R.drawable.school_map_5f
                else -> null
            }
            imageResource?.let { floorMapImage.setImageResource(it) }

            // 画像の高さを動的に設定
            floorMap.layoutParams.height = when (checkedId) {
                R.id.floorTab1F -> floorMapHeight1F
                else -> floorMapHeight1F * 0.94F
            }.toInt()
        }

        // タッチ座標をログに書き出し
//        FloorMapLocationTest.logTouchPosition(floorMapImage)

//        teacherGrid.onClickListener(object : PeopleGrid.OnClickListener {
//            override fun onClickItem(tappedView: View, name: String, location: String) {
//                Log.d("teacherGrid", "name:$name, location:$location")
//            }
//        })
//
//        studentGrid.onClickListener(object : PeopleGrid.OnClickListener {
//            override fun onClickItem(tappedView: View, name: String, location: String) {
//                Log.d("studentGrid", "name:$name, location:$location")
//            }
//        })

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