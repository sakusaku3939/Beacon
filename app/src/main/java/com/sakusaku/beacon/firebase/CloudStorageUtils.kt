package com.sakusaku.beacon.firebase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
     * @param context Context
     * @param imageView 反映させたい画像データ
     * @param uid 取得したいプロフィール画像のUID (省略可)
     */
    fun setProfileImage(context: Context, imageView: ImageView, uid: String? = FirebaseAuthUtils.uid) = uid?.let {
        val profileRef = Firebase.storage.reference.child("users/${it}/profile_picture.jpg")
        GlideApp.with(context)
                .load(profileRef)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView)
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