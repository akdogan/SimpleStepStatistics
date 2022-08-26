package com.akdogan.simplestepstatistics.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.akdogan.simplestepstatistics.R
import com.akdogan.simplestepstatistics.ui.main.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .setReorderingAllowed(true)
                .addToBackStack(MainFragment::class.java.simpleName)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> navigateToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToSettings(): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingsFragment.newInstance())
            .setReorderingAllowed(true)
            .addToBackStack(SettingsFragment::class.java.simpleName)
            .commit()
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) super.onBackPressed()//supportFragmentManager.popBackStack()
        else finish()
    }
}

