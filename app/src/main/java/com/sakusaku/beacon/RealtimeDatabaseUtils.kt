package com.sakusaku.beacon

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object RealtimeDatabaseUtils {
    private const val TAG: String = "RealtimeDatabase"

    fun getFloorMapData(floor: Int, callback: (data: Map<String, String>?) -> (Unit) = {}) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.child("public")
                Log.d(TAG, "DocumentSnapshot successfully $data")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException())
            }
        }
        val postReference = Firebase.database.reference
                .child("${floor}F")
        postReference.addValueEventListener(postListener)
    }

    fun userLocationUpdateListener(listener: ChildEventListener) {
        Firebase.database.reference.child("1F").child("public")/*.orderByChild("location").equalTo("経営企画室")*/.addChildEventListener(listener)
    }
}