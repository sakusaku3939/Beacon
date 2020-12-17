package com.sakusaku.beacon

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object FirebaseUtils {
    private const val TAG: String = "Firebase"
    fun buildActionCodeSettings(): ActionCodeSettings {
        return actionCodeSettings {
            setAndroidPackageName("com.sakusaku.beacon", true, null).handleCodeInApp = true
            url = "https://tkg-beacon.firebaseapp.com"
        }
    }

    fun sendSignInLink(email: String, activity: Activity, actionCodeSettings: ActionCodeSettings) {
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                    } else {
                        Log.d(TAG, "Email sending failed")
                        Toast.makeText(activity, "メールの送信に失敗しました", Toast.LENGTH_LONG).show()
                    }
                }
    }

    fun verifySignInLink(context: Context, intent: Intent, afterSignIn: (Task<AuthResult>) -> (Unit)) {
        val auth = Firebase.auth
        val emailLink = intent.data.toString()

        if (auth.isSignInWithEmailLink(emailLink)) {
            val email = PreferenceManager.getDefaultSharedPreferences(context).getString("email", "").toString()

            auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener { task ->
                        afterSignIn(task)
                        if (task.isSuccessful) {
                            Log.d(TAG, "Successfully signed in with email link!")
                        } else {
                            Log.e(TAG, "Error signing in with email link", task.exception)
                        }
                    }
        }
    }
}