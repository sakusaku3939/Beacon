package com.sakusaku.beacon.ui.account

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.sakusaku.beacon.*
import com.sakusaku.beacon.firebase.CloudStorageUtils
import com.sakusaku.beacon.firebase.FirebaseAuthUtils
import com.sakusaku.beacon.firebase.FirestoreUtils
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.concurrent.thread


class AccountSettingActivity : AppCompatActivity() {
    companion object {
        private const val READ_REQUEST_CODE: Int = 42
    }

    private var newProfileBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler()

        FirestoreUtils.getUserData { user ->
            setContentView(R.layout.account_setting_activity)

            val toolbar = findViewById<Toolbar>(R.id.accountSettingToolbar)
            toolbar.setNavigationOnClickListener { onBackPressed() }

            val image = findViewById<CardView>(R.id.cardView)
            image.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
                startActivityForResult(intent, READ_REQUEST_CODE)
            }

            val name = findViewById<EditText>(R.id.accountNameEdit)
            name.setText(FirebaseAuthUtils.name.toString())
            NameRestriction.add(name)

            val regionSpinner = findViewById<Spinner>(R.id.accountRegionSpinner)
            val subjectSpinner = findViewById<Spinner>(R.id.accountSubjectSpinner)
            if (isPositionTeacher(user)) {
                val regionAdapter = ArrayAdapter.createFromResource(this, R.array.array_region, android.R.layout.simple_spinner_dropdown_item)
                regionSpinner.apply {
                    adapter = regionAdapter
                    setSelection(regionAdapter.getPosition(user["region"]))
                }

                val subjectAdapter = ArrayAdapter.createFromResource(this, R.array.array_subject, android.R.layout.simple_spinner_dropdown_item)
                subjectSpinner.apply {
                    adapter = subjectAdapter
                    setSelection(subjectAdapter.getPosition(user["subject"]))
                }
            } else {
                regionSpinner.isEnabled = false
                subjectSpinner.isEnabled = false
            }

            val saveButton = findViewById<Button>(R.id.accountSaveButton)
            saveButton.setOnClickListener {
                if (name.text.isNotEmpty()) {
                    if (isInputFieldChange(user)) {
                        val region = if (isPositionTeacher(user)) regionSpinner.selectedItem.toString() else null
                        val subject = if (isPositionTeacher(user)) subjectSpinner.selectedItem.toString() else null

                        val progress = findViewById<ProgressBar>(R.id.accountSaveProgress)
                        progress.visibility = View.VISIBLE
                        saveButton.text = ""

                        GlobalScope.launch {
                            val uploadProfileImage = async { CloudStorageUtils.uploadProfileImage(newProfileBitmap) }
                            val updateUser = async { FirestoreUtils.asyncUpdateUserData(region = region, subject = subject) }
                            val updateProfile = async { FirebaseAuthUtils.asyncUpdateProfile(name = name.text.toString()) }

                            val resultToast = if (updateUser.await() && updateProfile.await() && uploadProfileImage.await()) "プロフィールを更新しました" else "プロフィールの更新に失敗しました"
                            thread {
                                handler.post {
                                    progress.visibility = View.GONE
                                    saveButton.text = resources.getString(R.string.account_setting_save)
                                    Toast.makeText(applicationContext, resultToast, Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        }
                    }
                } else {
                    name.error = "文字を入力してください"
                }
            }
        }
    }

    override fun onBackPressed() {
        FirestoreUtils.getUserData { user ->
            if (isInputFieldChange(user)) AlertDialog.Builder(this)
                    .setMessage("変更が破棄されますがよろしいですか？")
                    .setPositiveButton("破棄") { _, _ -> super.onBackPressed() }
                    .setNegativeButton("キャンセル") { _, _ -> }
                    .show()
            else super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            READ_REQUEST_CODE -> resultData?.data?.also { uri ->
                try {
                    val tmpFileName = UUID.randomUUID().toString() + ".jpg"
                    File.createTempFile(tmpFileName, null, cacheDir)
                    val tmpFileUri = Uri.fromFile(File(cacheDir, tmpFileName))

                    val options = UCrop.Options()
                    options.setToolbarTitle("画像の切り抜き")
                    options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
                    val uCrop = UCrop.of(uri, tmpFileUri).withAspectRatio(1F, 1F)
                    uCrop.withOptions(options)
                    uCrop.start(this)
                } catch (e: Exception) {
                    Toast.makeText(this, "画像読み込み時にエラーが発生しました", Toast.LENGTH_LONG).show()
                    Log.w("imageLoadError", e)
                }
            }
            UCrop.REQUEST_CROP -> resultData?.also { data ->
                if (resultCode == UCrop.RESULT_ERROR) {
                    Toast.makeText(this, "画像の切り抜きに失敗しました", Toast.LENGTH_LONG).show()
                    Log.e("uCropError", UCrop.getError(data).toString())
                    return
                }

                val afterResizeBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(UCrop.getOutput(data)!!.encodedPath), 400, 400, true)
                val profilePhoto = findViewById<ImageView>(R.id.profilePhoto)
                profilePhoto.setImageBitmap(afterResizeBitmap)

                newProfileBitmap = afterResizeBitmap
            }
        }
    }

    private fun isInputFieldChange(user: Map<String, String>): Boolean {
        val name = findViewById<EditText>(R.id.accountNameEdit)
        val region = findViewById<Spinner>(R.id.accountRegionSpinner)
        val subject = findViewById<Spinner>(R.id.accountSubjectSpinner)
        return when {
            newProfileBitmap != null -> true
            name.text.toString() != FirebaseAuthUtils.name.toString() -> true
            isPositionTeacher(user) &&
                    (region.selectedItem.toString() != user["region"] || subject.selectedItem.toString() != user["subject"]) -> true
            else -> false
        }
    }

    private fun isPositionTeacher(user: Map<String, String>): Boolean {
        return user["position"] == "先生"
    }
}