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
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.rijckaert.tim.animatedvector.FloatingMusicActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sakusaku.beacon.BeaconService
import com.sakusaku.beacon.R

class LocationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_location, container, false)

        // 校内図
        val floorTab = root.findViewById<RadioGroup>(R.id.floorTab)
        floorTab.setOnCheckedChangeListener { _, checkedId ->
            val floorMapImage = root.findViewById<ImageView>(R.id.floorMapImage)
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
                customFab.changeMode(FloatingMusicActionButton.Mode.PAUSE_TO_PLAY)
                fab.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.red)))
            }
        }

        fab.setOnMusicFabClickListener(object : FloatingMusicActionButton.OnMusicFabClickListener {
            override fun onClick(view: View) {
                when (customFab.getOppositeMode()) {
                    // ビーコン取得開始
                    FloatingMusicActionButton.Mode.PAUSE_TO_PLAY -> {
                        fab.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.red)))
                        // デバイスのBLE対応チェック
                        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                            Toast.makeText(activity, "このデバイスはBLE未対応です", Toast.LENGTH_LONG).show()
                        } else {
                            requireActivity().startForegroundService(Intent(activity, BeaconService::class.java))
                        }
                    }
                    // ビーコン取得停止
                    FloatingMusicActionButton.Mode.PLAY_TO_PAUSE -> {
                        fab.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.blue_500)))
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
}