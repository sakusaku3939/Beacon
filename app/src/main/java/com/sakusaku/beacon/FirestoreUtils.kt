package com.sakusaku.beacon

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FirestoreUtils {
    private const val TAG: String = "Firestore"
    private var user: Map<String, String>? = null

    /**
     * ユーザー情報を上書きするメソッド
     *
     * @param position 位置情報
     * @param region 領域
     * @param region 教科
     * @param callback: (isSuccess: Boolean) -> (Unit) 成功したかを返すコールバック関数
     */
    fun writeUserData(position: String? = null, region: String? = null, subject: String? = null, callback: (isSuccess: Boolean) -> (Unit) = {}) {
        userDataUtils("written", position, region, subject) { callback(it) }
    }

    /**
     * ユーザー情報を更新するメソッド
     *
     * @param position 位置情報
     * @param region 領域
     * @param region 教科
     * @param callback: (isSuccess: Boolean) -> (Unit) 成功したかを返すコールバック関数
     */
    fun updateUserData(position: String? = null, region: String? = null, subject: String? = null, callback: (isSuccess: Boolean) -> (Unit) = {}) {
        userDataUtils("update", position, region, subject) { callback(it) }
    }

    /**
     * 名前を追加するメソッド
     *
     * @param name 名前
     */
    fun addName(name: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf("name" to name)

        FirebaseAuthUtils.uid?.let {
            db.collection("users").document(it).set(user, SetOptions.merge())
        }
    }

    /**
     * ユーザー情報を取得するメソッド
     * ※一度読み込んだことがある場合はそのユーザー情報を返す
     *
     * @param callback: (data: Map<String, String>) -> (Unit) ユーザー情報を返すコールバック関数
     */
    fun getUserData(callback: (data: Map<String, String>) -> (Unit)) {
        user?.let {
            callback(it)
        } ?: loadUserData { data ->
            data?.let { callback(it) }
        }
    }

    /**
     * ユーザー情報を読み込むメソッド
     *
     * @param callback: (data: Map<String, String>) -> (Unit) ユーザー情報を返すコールバック関数
     */
    fun loadUserData(callback: (data: Map<String, String>?) -> (Unit) = {}) {
        val db = FirebaseFirestore.getInstance()
        FirebaseAuthUtils.uid?.let { uid ->
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                            val documentMapData = document.data?.map { d -> d.key to d.value.toString() }?.toMap()
                            user = documentMapData
                            callback(documentMapData)
                        } else {
                            Log.d(TAG, "No such document")
                            callback(null)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                        callback(null)
                    }
        } ?: callback(null)
    }

    /**
     * ユーザーを検索するメソッド
     *
     * @param name 名前
     * @param region 領域
     * @param subject 教科
     * @param callback: callback: (onlineUserMap: List<Map<String, Any?>>, offlineUserMap: List<Map<String, Any?>>) -> (Unit)
     *                  オンラインのユーザー、オフラインのユーザーを連想配列で返すコールバック関数
     */
    fun searchUser(name: String, region: String, subject: String,
                   callback: (onlineUserMap: List<Map<String, Any?>>, offlineUserMap: List<Map<String, Any?>>) -> Unit) {
        val users = FirebaseFirestore.getInstance().collection("users")
        val conditionRole = when {
            region.isNotEmpty() && subject.isNotEmpty() ->
                users.whereEqualTo("region", region).whereEqualTo("subject", subject)
            region.isNotEmpty() ->
                users.whereEqualTo("region", region)
            subject.isNotEmpty() ->
                users.whereEqualTo("subject", subject)
            else -> users
        }
        val conditionName = when {
            name.isNotEmpty() ->
                conditionRole.orderBy("name").startAt(name).endAt(name + "\uf8ff")
            else -> conditionRole
        }

        GlobalScope.launch(Dispatchers.IO) {
            val deferredFloorData = async { RealtimeDatabaseUtils.asyncFloorData() }
            val deferredUserData = async { asyncConditionUserData(conditionName) }
            val floorData = deferredFloorData.await()
            val userData = deferredUserData.await()
            if (userData != null) {
                // Firestoreのユーザーデータを連想配列化
                val userMap: List<Map<String, String?>> = userData.map { user -> user.data.keys.zip(user.data.values).associateBy({ it.first }, { it.second?.toString() }) }

                // 連想配列にIDを挿入
                val idList = userData.map { it.id }
                val userMapWithID = userMap.mapIndexed { i, map -> map.plus("id" to idList[i]) }

                // FirebaseのユーザーデータとRealtime Databaseの位置情報データを結合
                val getFloorDataChild = { range: String -> (1..5).map { i -> floorData?.child("${i}F")?.child(range) } }
                val getOnlineUserMap = { range: String ->
                    (0..4).map { i ->
                        userMapWithID.filter {
                            getFloorDataChild(range)[i]?.hasChild(it["id"] ?: "") ?: false
                        }.map {
                            it.plus("location" to (getFloorDataChild(range)[i]?.child(it["id"]
                                    ?: "")?.child("location")?.value ?: "なし"))
                        }.toMutableList()
                    }
                }

                // オンライン・オフラインでリスト分け
                val mutableOnlineUserMap = getOnlineUserMap("public")
                if (user?.get("position") == "生徒") (0..4).forEach { i -> mutableOnlineUserMap[i].addAll(getOnlineUserMap("students_only")[i]) }

                val onlineUserMap = mutableOnlineUserMap.flatten()
                val offlineUserMap = (userMapWithID + (0..4).map { i -> mutableOnlineUserMap[i] }.flatten()).groupBy { it["id"] }.filter { it.value.size == 1 }.flatMap { it.value }

                callback(onlineUserMap, offlineUserMap)
            } else {
                callback(listOf(), listOf())
            }
        }
    }

    private fun userDataUtils(updateMode: String, position: String?, region: String?, subject: String?, callback: (isSuccess: Boolean) -> (Unit) = {}) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
                "position" to position,
                "region" to region,
                "subject" to subject
        ).filter { it.value != null }

        FirebaseAuthUtils.uid?.takeIf { user.isNotEmpty() }?.let { uid ->
            val document = db.collection("users").document(uid)
            val task = when (updateMode) {
                "written" -> document.set(user)
                "update" -> document.set(user, SetOptions.merge())
                else -> document.set(user, SetOptions.merge())
            }
            task.addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully $updateMode!")
                this.user = null
                callback(true)
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error $updateMode document", e)
                callback(false)
            }
        } ?: callback(true)
    }

    private suspend fun asyncConditionUserData(conditionDB: Query): QuerySnapshot? {
        return suspendCoroutine { continuation ->
            conditionDB.get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Log.d(TAG, "ConditionUserData successfully")
                    continuation.resume(documents)
                } else {
                    Log.d(TAG, "None")
                    continuation.resume(null)
                }
            }.addOnFailureListener { exception ->
                Log.d("test", "get failed with ", exception)
                continuation.resume(null)
            }
            return@suspendCoroutine
        }
    }
}