package com.sakusaku.beacon.firebase

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import coil.load
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sakusaku.beacon.R
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object CloudStorageUtils {
    private const val TAG: String = "CloudStorage"
    private val ref = Firebase.storage.reference.child("users/${FirebaseAuthUtils.uid}/profile_picture.jpg")

    /**
     * プロフィール画像をアップロードするメソッド (async)
     *
     * @param bitmap 画像データ
     * @return isSuccess アップロードに成功したかを返す
     */
    suspend fun uploadProfileImage(bitmap: Bitmap?): Boolean {
        return suspendCoroutine { continuation ->
            bitmap?.also {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                val uploadTask = ref.putBytes(stream.toByteArray())
                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Upload is done")
                        continuation.resume(true)
                    } else {
                        Log.w(TAG, "Task did not succeed:", task.exception)
                        continuation.resume(false)
                    }
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Upload error:", e)
                    continuation.resume(false)
                }
            } ?: continuation.resume(true)
            return@suspendCoroutine
        }
    }

    /**
     * プロフィール画像をImageViewに反映するメソッド
     *
     * @param imageView 反映させたい画像データ
     * @param url プロフィール画像のURL (省略可)
     */
    fun setProfileImage(imageView: ImageView, url: String = "", uid: String = "") {
        val loadImage = { u: String -> imageView.load(u) { error(R.drawable.user) } }

        when {
            url.isNotEmpty() -> loadImage(url)
            uid.isNotEmpty() -> FirestoreUtils.getUserData(uid) { user ->
                loadImage(user["photoUri"] ?: "")
            }
            else -> FirestoreUtils.getUserData { user ->
                loadImage(user["photoUri"] ?: "")
            }
        }
    }

    /**
     * プロフィール画像のダウンロード用URLを返すメソッド (async)
     *
     * @return Uri
     */
    suspend fun getDownloadUrl(): Uri? {
        return suspendCoroutine { continuation ->
            ref.downloadUrl.addOnSuccessListener { uri ->
                Log.d(TAG, "get downloadUrl succeed $uri")
                continuation.resume(uri)
            }.addOnFailureListener { e ->
                Log.w(TAG, "get downloadUrl failed:", e)
                continuation.resume(null)
            }
        }
    }
}