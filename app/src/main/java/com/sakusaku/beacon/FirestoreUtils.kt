package com.sakusaku.beacon

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirestoreUtils {
    private const val TAG: String = "Firestore"
    private var user: Map<String, String>? = null

    fun writeUserData(position: String? = null, region: String? = null, subject: String? = null, callback: (isSuccess: Boolean) -> (Unit) = {}) {
        userDataUtils("written", position, region, subject) { callback(it) }
    }

    fun updateUserData(position: String? = null, region: String? = null, subject: String? = null, callback: (isSuccess: Boolean) -> (Unit) = {}) {
        userDataUtils("update", position, region, subject) { callback(it) }
    }

    private fun userDataUtils(updateMode: String, position: String?, region: String?, subject: String?, callback: (isSuccess: Boolean) -> (Unit) = {}) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
                "position" to position,
                "region" to region,
                "subject" to subject
        ).filter { it.value != null }

        val uid = FirebaseAuthUtils.getUserProfile()["uid"] as String?
        uid?.takeIf { user.isNotEmpty() }?.let {
            val document = db.collection("users").document(it)
            val task = when (updateMode) {
                "written" -> document.set(user)
                "update" -> document.set(user, SetOptions.merge())
                else -> document.set(user, SetOptions.merge())
            }
            task.addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully $updateMode!")
                callback(true)
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error $updateMode document", e)
                callback(false)
            }
        } ?: callback(true)
    }

    fun addName(name: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf("name" to name)

        val uid = FirebaseAuthUtils.getUserProfile()["uid"] as String?
        uid?.let { db.collection("users").document(it).set(user, SetOptions.merge()) }
    }

    fun getUserData(callback: (data: Map<String, String>) -> (Unit)) {
        user?.let {
            callback(it)
        } ?: loadUserData { data ->
            data?.let { callback(it) }
        }
    }

    fun loadUserData(callback: (data: Map<String, String>?) -> (Unit) = {}) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuthUtils.getUserProfile()["uid"] as String?
        uid?.let {
            db.collection("users").document(it)
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
}