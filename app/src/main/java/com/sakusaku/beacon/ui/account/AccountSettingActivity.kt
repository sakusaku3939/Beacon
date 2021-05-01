package com.sakusaku.beacon.ui.account

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sakusaku.beacon.FirebaseAuthUtils
import com.sakusaku.beacon.FirestoreUtils
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

            val region = findViewById<Spinner>(R.id.accountRegionSpinner)
            val subject = findViewById<Spinner>(R.id.accountSubjectSpinner)
            if (user["position"] == "先生") {
                val regionAdapter = ArrayAdapter.createFromResource(this, R.array.array_region, android.R.layout.simple_spinner_dropdown_item)
                region.apply {
                    adapter = regionAdapter
                    setSelection(regionAdapter.getPosition(user["region"]))
                }

                val subjectAdapter = ArrayAdapter.createFromResource(this, R.array.array_subject, android.R.layout.simple_spinner_dropdown_item)
                subject.apply {
                    adapter = subjectAdapter
                    setSelection(subjectAdapter.getPosition(user["subject"]))
                }
            } else {
                region.isEnabled = false
                subject.isEnabled = false
            }
        }
    }

    override fun onBackPressed() {
        FirestoreUtils.getUserData { user ->
            val name = findViewById<EditText>(R.id.accountNameEdit)
            val region = findViewById<Spinner>(R.id.accountRegionSpinner)
            val subject = findViewById<Spinner>(R.id.accountSubjectSpinner)
            val showDialog = {
                AlertDialog.Builder(this)
                        .setMessage("変更が破棄されますがよろしいですか？")
                        .setPositiveButton("破棄") { _, _ -> super.onBackPressed() }
                        .setNegativeButton("キャンセル") { _, _ -> }
                        .show()
            }

            when {
                name.text.toString() != FirebaseAuthUtils.getUserProfile()["name"].toString() -> showDialog()
                user["position"] == "先生" &&
                        (region.selectedItem != user["region"] || subject.selectedItem != user["subject"]) -> showDialog()
                else -> super.onBackPressed()
            }
        }
    }
}