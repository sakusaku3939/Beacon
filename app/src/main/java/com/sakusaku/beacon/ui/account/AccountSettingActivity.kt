package com.sakusaku.beacon.ui.account

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.NameRestriction
import com.sakusaku.beacon.R


class AccountSettingActivity : AppCompatActivity() {
    companion object {
        private const val READ_REQUEST_CODE: Int = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            name.setText(FirebaseAuthUtils.getUserProfile()["name"].toString())
            NameRestriction.add(name)

            Log.d("test", FirebaseAuthUtils.getUserProfile()["photoUrl"].toString())

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
                    val region = if (isPositionTeacher(user)) regionSpinner.selectedItem.toString() else null
                    val subject = if (isPositionTeacher(user)) subjectSpinner.selectedItem.toString() else null

                    if (isInputFieldChange(user)) FirestoreUtils.updateUserData(region = region, subject = subject) { isSuccess ->
                        if (isSuccess) {
                            FirebaseAuthUtils.updateProfile(name.text.toString()) { isSuccess2 ->
                                if (isSuccess2) {
                                    Toast.makeText(this, "プロフィールを更新しました", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "プロフィールの更新に失敗しました", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "プロフィールの更新に失敗しました", Toast.LENGTH_SHORT).show()
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
            READ_REQUEST_CODE -> {
                try {
                    resultData?.data?.also { uri ->
                        val inputStream = contentResolver?.openInputStream(uri)
                        val image = BitmapFactory.decodeStream(inputStream)

                        val width = (image.width * 0.7f).toInt()
                        val height = (image.height * 0.7f).toInt()
                        val startX = (image.width - width) / 2
                        val startY = (image.height - height) / 2
                        val afterResizeImage = Bitmap.createBitmap(image, startX, startY, width, height, null, true);

                        val profilePhoto = findViewById<ImageView>(R.id.profilePhoto)
                        profilePhoto.setImageBitmap(afterResizeImage)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "画像読み込み時にエラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun isInputFieldChange(user: Map<String, String>): Boolean {
        val name = findViewById<EditText>(R.id.accountNameEdit)
        val region = findViewById<Spinner>(R.id.accountRegionSpinner)
        val subject = findViewById<Spinner>(R.id.accountSubjectSpinner)
        return when {
            name.text.toString() != FirebaseAuthUtils.getUserProfile()["name"].toString() -> true
            isPositionTeacher(user) &&
                    (region.selectedItem.toString() != user["region"] || subject.selectedItem.toString() != user["subject"]) -> true
            else -> false
        }
    }

    private fun isPositionTeacher(user: Map<String, String>): Boolean {
        return user["position"] == "先生"
    }
}