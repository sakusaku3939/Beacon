package com.sakusaku.beacon

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtils {
    private const val TAG: String = "Firestore"

    fun updateUser(position: String? = null, region: String? = null, subject: String? = null, callback: (isSuccess: Boolean) -> (Unit) = {}) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
                "position" to position,
                "region" to region,
                "subject" to subject
        ).filter { it.value != null }

        val uid = FirebaseAuthUtils.getUserProfile()["uid"] as String?
        uid?.takeIf { user.isNotEmpty() }?.let {
            db.collection("users").document(it)
                    .set(user)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        callback(true)
                    }
                    .addOnFailureListener {
                        e -> Log.w(TAG, "Error writing document", e)
                        callback(false)
                    }
        } ?: callback(false)
    }

    fun getUserData(callback: (data: Map<String, Any>?) -> (Unit)) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuthUtils.getUserProfile()["uid"] as String?
        uid?.let {
            db.collection("users").document(it)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                            callback(document.data)
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