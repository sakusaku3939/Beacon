package com.sakusaku.beacon

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object RealtimeDatabaseUtils {
    private const val TAG: String = "RealtimeDatabase"
    private val removeListenerList = mutableListOf<() -> Unit>()

    fun floorUserExist(floor: Int, position: String, callback: (isExist: Boolean) -> (Unit)) {
        FirestoreUtils.getUserData {
            GlobalScope.launch(Dispatchers.IO) {
                val isExistPublic = async { asyncFloorUserExist(floor, "public", position) }
                if (it["position"] == "生徒") {
                    val isExistOnly = async { asyncFloorUserExist(floor, "students_only", position) }
                    callback(isExistPublic.await() || isExistOnly.await())
                } else {
                    callback(isExistPublic.await())
                }
            }
        }
    }

    private suspend fun asyncFloorUserExist(floor: Int, range: String, position: String): Boolean {
        return suspendCoroutine { continuation ->
            val reference = Firebase.database.reference.child("${floor}F").child(range).orderByChild("position").equalTo(position)
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "GetFloorUserData successfully ${dataSnapshot.value}")
                    reference.removeEventListener(this)
                    continuation.resume(dataSnapshot.value != null)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "onCancelled", databaseError.toException())
                    reference.removeEventListener(this)
                    continuation.resume(false)
                }
            }
            reference.addValueEventListener(listener)
            return@suspendCoroutine
        }
    }

    fun userLocationUpdateListener(floor: Int, callback: (dataSnapshot: DataSnapshot, state: String) -> Unit) {
        fun setListener(f: (dataSnapshot: DataSnapshot, state: String) -> Unit, range: String): () -> Unit {
            val reference = Firebase.database.reference.child("${floor}F").child(range)
            val listener = object : ChildEventListener {
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
            }
            reference.addChildEventListener(listener)
            return { reference.removeEventListener(listener) }
        }

        removeListenerList.add(setListener(callback, "public"))
        FirestoreUtils.getUserData {
            if (it["position"] == "生徒") removeListenerList.add(setListener(callback, "students_only"))
        }
    }

    fun removeAllUserLocationUpdateListener() {
        removeListenerList.forEach { it() }
    }
}