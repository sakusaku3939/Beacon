package com.sakusaku.beacon.ui.account

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.NameRestriction
import com.sakusaku.beacon.R

class AccountSettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirestoreUtils.getUserData { user ->
            setContentView(R.layout.account_setting_activity)

            val toolbar = findViewById<Toolbar>(R.id.accountSettingToolbar)
            toolbar.setNavigationOnClickListener { onBackPressed() }

            val name = findViewById<EditText>(R.id.accountNameEdit)
            name.setText(FirebaseAuthUtils.getUserProfile()["name"].toString())
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
                val region = if (isPositionTeacher(user)) regionSpinner.selectedItem.toString() else null
                val subject = if (isPositionTeacher(user)) subjectSpinner.selectedItem.toString() else null
                if (isInputFieldChange(user)) FirestoreUtils.updateUserData(region = region, subject = subject) { isSuccess ->
                    if (isSuccess) {
                        FirebaseAuthUtils.updateProfile(name.text.toString())
                        Toast.makeText(this, "プロフィールを更新しました", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "プロフィールの更新に失敗しました", Toast.LENGTH_SHORT).show()
                    }
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