package com.sakusaku.beacon.ui.location

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sakusaku.beacon.BeaconService
import com.sakusaku.beacon.R

class LocationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_location, container, false)
        val fab: FloatingActionButton = root.findViewById(R.id.fab)
        val fabPause: FloatingActionButton = root.findViewById(R.id.fab_pauce)

        // フォアグラウンド実行中か
        val manager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (serviceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BeaconService::class.java.name == serviceInfo.service.className) {
                fabPause.visibility = View.VISIBLE
                fab.visibility = View.GONE
            }
        }
        fab.setOnClickListener {
            // デバイスのBLE対応チェック
            if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(activity, "このデバイスはBLE未対応です", Toast.LENGTH_LONG).show()
            } else {
                fabPause.visibility = View.VISIBLE
                fab.visibility = View.GONE
                requireActivity().startForegroundService(Intent(activity, BeaconService::class.java))
            }
        }
        fabPause.setOnClickListener {
            fab.visibility = View.VISIBLE
            fabPause.visibility = View.GONE
            requireActivity().stopService(Intent(activity, BeaconService::class.java))
        }
        val textView = root.findViewById<TextView?>(R.id.text_location)
        locationViewModel.getText()?.observe(viewLifecycleOwner) { s -> textView.text = s }
        return root
    }
}