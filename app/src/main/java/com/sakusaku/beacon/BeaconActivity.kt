package com.sakusaku.beacon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class BeaconActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon)
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putBoolean("isForegroundFabChange", false).apply()
        val navView = findViewById<BottomNavigationView?>(R.id.nav_view)
        val navController = getNavController()
        NavigationUI.setupWithNavController(navView, navController)
    }

    private fun getNavController(): NavController {
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        check(fragment is NavHostFragment) {
            ("Activity " + this
                    + " does not have a NavHostFragment")
        }
        return fragment.navController
    }
}