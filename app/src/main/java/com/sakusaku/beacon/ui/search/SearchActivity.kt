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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val region = intent.getStringExtra("region") ?: ""
        val subject = intent.getStringExtra("subject") ?: ""

        FirestoreUtils.searchUser("", region, subject)

//        val db = FirebaseFirestore.getInstance().collection("users")
//        val conditionalDB = when {
//            region.isNotEmpty() && subject.isNotEmpty() ->
//                db.whereEqualTo("region", region).whereEqualTo("subject", subject)
//            region.isNotEmpty() ->
//                db.whereEqualTo("region", region)
//            subject.isNotEmpty() ->
//                db.whereEqualTo("subject", subject)
//            else -> db
//        }
//
//        // .orderBy("name").startAt(name).endAt(name + "\uf8ff")
//        conditionalDB.get().addOnSuccessListener { documents ->
//            if (!documents.isEmpty) for (document in documents) {
//                Log.d("firestore_test", document.data.toString())
//            } else {
//                Log.d("firestore_test", "None")
//            }
//        }.addOnFailureListener { exception ->
//            Log.d("test", "get failed with ", exception)
//        }

//        val ref = Firebase.database.reference.child("floor")
//        val listener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (i in 1..5) {
//                    Log.d("database_test", "${dataSnapshot.child("${i}F").child("public").hasChild("jEazVdPDhqec0tnEOG7vM5wbDyU1")}")
//                }
//                Log.d("database_test", "${dataSnapshot.child("1F").child("public").hasChild("jEazVdPDhqec0tnEOG7vM5wbDyU1")}")
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w("database_test", "onCancelled", databaseError.toException())
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