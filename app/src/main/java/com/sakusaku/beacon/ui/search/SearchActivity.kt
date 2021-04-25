package com.sakusaku.beacon.ui.search

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.sakusaku.beacon.FirestoreUtils
import com.sakusaku.beacon.R
import com.sakusaku.beacon.RealtimeDatabaseUtils
import kotlin.coroutines.resume

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val region: String? = intent.getStringExtra("region")
        val subject: String? = intent.getStringExtra("subject")

        val db = FirebaseFirestore.getInstance().collection("users")
        val conditionalDB = when {
            !region.isNullOrEmpty() && !subject.isNullOrEmpty() ->
                db.whereEqualTo("region", region).whereEqualTo("subject", subject)
            !region.isNullOrEmpty() ->
                db.whereEqualTo("region", region)
            !subject.isNullOrEmpty() ->
                db.whereEqualTo("subject", subject)
            else -> db
        }

        conditionalDB.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) for (document in documents) {
                Log.d("test", document.data.toString())
            } else {
                Log.d("test", "None")
            }
        }.addOnFailureListener { exception ->
            Log.d("test", "get failed with ", exception)
        }

//        val queryText = "ET"
////        Log.d("test", "ok")
//        val ref = Firebase.database.reference.child("search")
//                .orderByKey()
//                .startAt(queryText)
//                .endAt(queryText + "\uf8ff")
//        val listener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
////                Log.d("test", "${dataSnapshot.value}")
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w("test", "onCancelled", databaseError.toException())
//            }
//        }
//        ref.addListenerForSingleValueEvent(listener)


        // 戻る
        val searchBackButton = findViewById<ImageButton>(R.id.searchBackButton)
        searchBackButton.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
        finish()
    }
}