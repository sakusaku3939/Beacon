package com.sakusaku.beacon

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
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
    suspend fun uploadProfileImage(bitmap: Bitmap): Boolean {
        return suspendCoroutine { continuation ->
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
            return@suspendCoroutine
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