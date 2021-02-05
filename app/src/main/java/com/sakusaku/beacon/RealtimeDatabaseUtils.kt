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

    fun floorUserDataExist(floor: Int, callback: (isPublicExist: Boolean, isStudentsOnlyExist: Boolean) -> (Unit)) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "GetFloorUserData successfully $dataSnapshot")
                callback(dataSnapshot.child("public").exists(),
                        dataSnapshot.child("students_only").exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException())
                callback(false, false)
            }
        }
        val postReference = Firebase.database.reference.child("${floor}F")
        postReference.addValueEventListener(postListener)
    }

    fun userLocationUpdateListener(floor: Int, callback: (dataSnapshot: DataSnapshot, state: String) -> Unit) {
        val listener = {f: (dataSnapshot: DataSnapshot, state: String) -> Unit, childName: String ->
            Firebase.database.reference.child("${floor}F").child(childName).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("RealtimeDatabase", "DataSnapshot added $dataSnapshot")
                    f(dataSnapshot, "USER_ADDED")
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("RealtimeDatabase", "DataSnapshot changed $dataSnapshot")
                    f(dataSnapshot, "USER_CHANGED")
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    Log.d("RealtimeDatabase", "DataSnapshot removed $dataSnapshot")
                    f(dataSnapshot, "USER_REMOVED")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "onCancelled", error.toException())
                }
            })
        }

        listener(callback, "public")
        FirestoreUtils.getUserData {
            if (it["position"] == "生徒") listener(callback, "students_only")
        }
    }
}