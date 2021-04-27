package com.sakusaku.beacon

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object RealtimeDatabaseUtils {
    private const val TAG: String = "RealtimeDatabase"
    private val removeListenerList = mutableListOf<() -> Unit>()
    private var currentFloor: Int = 0
    private val floorRef = Firebase.database.reference.child("floor")

    data class UserLocation(
            val name: String = "",
            val location: String = "",
            val position: String = "",
            val timestamp: Map<String, String> = ServerValue.TIMESTAMP
    )

    /**
     * 現在の位置情報を書き込むメソッド
     *
     * @param context Context (PreferenceManagerから取得するために使用)
     * @param floor 階層
     * @param location 位置情報
     */
    fun writeUserLocation(context: Context, floor: Int, location: String) {
        val disclosureRange = getDisclosureRange(context)
        disclosureRange.takeIf { it.isNotEmpty() }?.let { range ->
            val uid = FirebaseAuthUtils.getUserProfile()["uid"] as String?
            uid?.let {
                FirestoreUtils.getUserData { user ->
                    val ref = floorRef.child("${floor}F").child(range).child(it)
                    val data = UserLocation(user["name"]!!, location, user["position"]!!)
                    ref.setValue(data)

                    if (currentFloor != 0 && currentFloor != floor) deleteUserLocation(context)
                    currentFloor = floor
                }
            }
        }
    }

    /**
     * 現在の位置情報を削除するメソッド
     *
     * @param context Context (PreferenceManagerから取得するために使用)
     */
    fun deleteUserLocation(context: Context) {
        val disclosureRange = getDisclosureRange(context)
        disclosureRange.takeIf { it.isNotEmpty() }?.let { range ->
            val uid = FirebaseAuthUtils.getUserProfile()["uid"] as String?
            uid?.let {
                val ref = floorRef.child("${currentFloor}F").child(range).child(it)
                ref.removeValue()
                currentFloor = 0
            }
        }
    }

    /**
     * 指定された階に人がいるか調べるメソッド
     *
     * @param floor 階層
     * @param position 位置情報
     * @param callback: (isExist: Boolean) -> (Unit) ユーザーがいるかを返すコールバック関数
     */
    fun isFloorUserExist(floor: Int, position: String, callback: (isExist: Boolean) -> (Unit)) {
        FirestoreUtils.getUserData { user ->
            GlobalScope.launch(Dispatchers.IO) {
                val isExistPublic = async { asyncIsFloorUserExist(floor, "public", position) }
                if (user["position"] == "生徒") {
                    val isExistOnly = async { asyncIsFloorUserExist(floor, "students_only", position) }
                    callback(isExistPublic.await() || isExistOnly.await())
                } else {
                    callback(isExistPublic.await())
                }
            }
        }
    }

    /**
     * floorノードの変更を監視するリスナーを返すメソッド
     *
     * state の中身: "USER_ADDED" or "USER_ADDED" or "USER_REMOVED"
     *
     * @param floor 階層
     * @param callback: (dataSnapshot: DataSnapshot, state: String) -> (Unit) JSONツリーを返すコールバック関数
     */
    fun userLocationUpdateListener(floor: Int, callback: (dataSnapshot: DataSnapshot, state: String) -> Unit) {
        fun setListener(f: (dataSnapshot: DataSnapshot, state: String) -> Unit, range: String): () -> Unit {
            val ref = floorRef.child("${floor}F").child(range)
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
            ref.addChildEventListener(listener)
            return { ref.removeEventListener(listener) }
        }

        removeListenerList.add(setListener(callback, "public"))
        FirestoreUtils.getUserData { user ->
            if (user["position"] == "生徒") removeListenerList.add(setListener(callback, "students_only"))
        }
    }

    /**
     *  全てのfloorノードのリスナーを削除するメソッド
     */
    fun removeAllUserLocationUpdateListener() {
        removeListenerList.forEach { it() }
    }

    private fun getDisclosureRange(context: Context): String {
        return when (PreferenceManager.getDefaultSharedPreferences(context)
                .getString("preference_disclosure_range", "全体に公開")) {
            "全体に公開" -> "public"
            "生徒にのみ公開" -> "students_only"
            else -> ""
        }
    }

    private suspend fun asyncIsFloorUserExist(floor: Int, range: String, position: String): Boolean {
        return suspendCoroutine { continuation ->
            val ref = floorRef.child("${floor}F").child(range).orderByChild("position").equalTo(position)
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "GetFloorUserData successfully ${dataSnapshot.value}")
                    continuation.resume(dataSnapshot.value != null)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "onCancelled", databaseError.toException())
                    continuation.resume(false)
                }
            }
            ref.addListenerForSingleValueEvent(listener)
            return@suspendCoroutine
        }
    }
}