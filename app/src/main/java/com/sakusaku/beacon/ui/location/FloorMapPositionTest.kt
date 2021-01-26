package com.sakusaku.beacon.ui.location

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView

object FloorMapPositionTest {
    @SuppressLint("ClickableViewAccessibility")
    fun onTouchListener(
            floorMapImage: ImageView,
            ACTION_DOWN: (event: MotionEvent) -> Unit = {},
            ACTION_UP: (event: MotionEvent) -> Unit = {}
    ) {
        floorMapImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> ACTION_DOWN(event)
                MotionEvent.ACTION_UP -> ACTION_UP(event)
            }
            true
        }
    }

    fun logTouchPosition(floorMapImage: ImageView) {
        onTouchListener(floorMapImage, ACTION_DOWN = { event ->
            Log.d("onTouchPosition", "${event.x}F, ${event.y}F")
        })
    }
}