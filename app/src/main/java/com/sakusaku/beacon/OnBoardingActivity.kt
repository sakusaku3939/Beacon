package com.sakusaku.beacon

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.sakusaku.beacon.onBoarding.GetStartedFragment
import com.sakusaku.beacon.onBoarding.NameEntryFragment

class OnBoardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding)
        if (intent.data != null) FirebaseUtils.verifySignInLink(this, intent) { task ->
            if (task.isSuccessful) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit)
                transaction.addToBackStack(null)
                transaction.replace(R.id.onboarding_fragment, NameEntryFragment())
                transaction.commit()
            } else {
                Toast.makeText(this, "メールリンクが無効です", Toast.LENGTH_LONG).show()
            }
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.onboarding_fragment, GetStartedFragment())
        transaction.commit()
    }

    override fun onBackPressed() {
        supportFragmentManager.popBackStack()
    }
}