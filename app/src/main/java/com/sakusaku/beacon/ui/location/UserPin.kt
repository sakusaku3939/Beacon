package com.sakusaku.beacon.ui.location

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.sakusaku.beacon.R
import com.skyfishjy.library.RippleBackground

class UserPin(private val context: Context, private val inflater: LayoutInflater,
              private val floorMapImage: ImageView, private val mapPinLayout: FrameLayout) {
    fun add(positionMap: Map<String, Position>, positionName: String) {
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val positionX = (positionMap[positionName]?.x ?: -10F) / 100
        val positionY = (positionMap[positionName]?.y ?: -10F) / 100
        params.marginStart = (floorMapImage.width * positionX).toInt() - 32F.dp()
        params.topMargin = (floorMapImage.height * positionY).toInt() - 32F.dp()

        val view = inflater.inflate(R.layout.map_pin, FrameLayout(context))
        val mapPinRipple = view.findViewById<RippleBackground>(R.id.mapPinRipple)
        mapPinRipple.startRippleAnimation()

        mapPinLayout.addView(view, params)
    }

    fun removeAll() = mapPinLayout.removeAllViews()

    private fun Float.dp() = (this * context.resources.displayMetrics.density).toInt()
}