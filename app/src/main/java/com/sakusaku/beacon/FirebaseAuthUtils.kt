package com.sakusaku.beacon

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

object FirebaseAuthUtils {
    private const val TAG: String = "Firebase"

    fun isSignIn(): Boolean = Firebase.auth.currentUser != null

    fun signOut() {
        Firebase.auth.signOut()
    }

    fun buildActionCodeSettings(): ActionCodeSettings {
        return actionCodeSettings {
            setAndroidPackageName("com.sakusaku.beacon", true, null).handleCodeInApp = true
            url = "https://tkg-beacon.firebaseapp.com"
        }
    }

    fun sendSignInLink(email: String, actionCodeSettings: ActionCodeSettings, callback: (Task<Void>) -> (Unit) = {}) {
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener { task ->
                    callback(task)
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                    } else {
                        Log.d(TAG, "Email sending failed")
                    }
                }
    }

    fun verifySignInLink(context: Context, intent: Intent, callback: (Task<AuthResult>) -> (Unit) = {}) {
        val auth = Firebase.auth
        val emailLink = intent.data.toString()

        if (auth.isSignInWithEmailLink(emailLink)) {
            val email = PreferenceManager.getDefaultSharedPreferences(context).getString("email", "").toString()

            auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener { task ->
                        callback(task)
                        if (task.isSuccessful) {
                            Log.d(TAG, "Successfully signed in with email link!")
                        } else {
                            Log.e(TAG, "Error signing in with email link", task.exception)
                        }
                    }
        }
    }

    fun updateProfile(name: String?, callback: (Task<Void>) -> (Unit) = {}) {
        val user = Firebase.auth.currentUser

        name?.let {
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }

            user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { task ->
                        callback(task)
                        if (task.isSuccessful) {
                            Log.d(TAG, "User profile updated.")
                        }
                    }

            FirestoreUtils.addName(name)
        }
    }

    fun getUserProfile(): Map<String, Any?> {
        val user = Firebase.auth.currentUser
        return mapOf(
                "name" to user?.displayName,
                "email" to user?.email,
                "emailVerified" to user?.isEmailVerified,
                "uid" to user?.uid)
    }

}