package com.sakusaku.beacon.firebase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FirebaseAuthUtils {
    private const val TAG: String = "Firebase"

    val name: String? get() { return Firebase.auth.currentUser?.displayName }
    val email: String? get() { return Firebase.auth.currentUser?.email }
    val emailVerified: Boolean? get() { return Firebase.auth.currentUser?.isEmailVerified }
    val photoUrl: Uri? get() { return Firebase.auth.currentUser?.photoUrl }
    val uid: String? get() { return Firebase.auth.currentUser?.uid }

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

    /**
     *  サインイン用メールを送信するメソッド
     *
     *  @param email
     *  @param actionCodeSettings
     *  @param callback: (Task<Void>) -> (Unit)
     */
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

    /**
     * メールリンクが正しいかチェックするメソッド
     *
     * @param context
     * @param intent
     * @param callback: (Task<AuthResult>) -> (Unit)
     */
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

    /**
     * プロフィールを更新するメソッド
     *
     * @param name 名前
     * @param photoUri プロフィール画像のURI
     * @param callback: (isSuccessful: Boolean) -> (Unit)
     */
    fun updateProfile(name: String? = null, photoUri: String? = null, callback: (isSuccessful: Boolean) -> (Unit) = {}) {
        updateProfileUtils(name, photoUri, callback)
    }

    /**
     * プロフィールを更新するメソッド (async)
     *
     * @param name 名前
     * @param photoUri プロフィール画像のURI
     * @return isSuccessful 更新に成功したかを返す
     */
    suspend fun asyncUpdateProfile(name: String? = null, photoUri: String? = null, callback: (isSuccessful: Boolean) -> (Unit) = {}): Boolean {
        return suspendCoroutine { continuation ->
            updateProfileUtils(name, photoUri) { continuation.resume(it) }
        }
    }

    private fun updateProfileUtils(name: String?, uri: String?, callback: (isSuccessful: Boolean) -> (Unit)) {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            when {
                !name.isNullOrEmpty() && !uri.isNullOrEmpty() -> {
                    displayName = name
                    photoUri = Uri.parse(uri)
                }
                !name.isNullOrEmpty() -> displayName = name
                !uri.isNullOrEmpty() -> photoUri = Uri.parse(uri)
                else -> return
            }
        }

        user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User profile updated.")
                    }
                    callback(task.isSuccessful)
                }

        name?.let { FirestoreUtils.addName(name) }
    }
}