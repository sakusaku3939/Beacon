package com.sakusaku.beacon

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.jgabrielfreitas.core.BlurImageView
import com.sakusaku.beacon.firebase.FirebaseAuthUtils
import com.sakusaku.beacon.firebase.FirestoreUtils
import com.sakusaku.beacon.onBoarding.GetStartedFragment
import com.sakusaku.beacon.onBoarding.NameEntryFragment
import com.sakusaku.beacon.onBoarding.PermissionFragment

class OnBoardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding)

        intent.data?.let {
            FirebaseAuthUtils.verifySignInLink(this, intent) { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val blurImage: BlurImageView = findViewById(R.id.BlurImage)
                    if (result?.additionalUserInfo?.isNewUser == true) {
                        FragmentUtil.delayBlur(blurImage)
                        FragmentUtil.replaceFragment(this, NameEntryFragment())
                    } else {
                        FragmentUtil.existsUserData(this)
                    }
                } else {
                    Toast.makeText(this, "マジックリンクが無効です", Toast.LENGTH_LONG).show()
                }
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

object FragmentUtil {
    fun replaceFragment(activity: FragmentActivity, fragment: Fragment) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fragment_close_enter, R.anim.fragment_close_exit)
        transaction.addToBackStack(null)
        transaction.replace(R.id.onboarding_fragment, fragment)
        transaction.commit()
    }

    fun existsUserData(activity: FragmentActivity) {
        FirestoreUtils.existsUserData { isExist ->
            delayBlur(activity.findViewById(R.id.BlurImage))
            replaceFragment(activity, if (isExist) PermissionFragment() else NameEntryFragment())
        }
    }

    fun delayBlur(blurImage: BlurImageView) = Handler().postDelayed({ blurImage.setBlur(5) }, 100)
}