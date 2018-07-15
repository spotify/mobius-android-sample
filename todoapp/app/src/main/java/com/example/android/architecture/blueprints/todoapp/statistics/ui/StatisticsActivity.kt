/*
 * Copyright 2016, The Android Open Source Project
 * Copyright (c) 2017-2018 Spotify AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.statistics.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.NavUtils
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils

/** Show statistics for tasks.  */
class StatisticsActivity : AppCompatActivity() {

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.statistics_act)

        // Set up the toolbar.
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.run {
            setTitle(R.string.statistics_title)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            setDisplayHomeAsUpEnabled(true)
        }

        // Set up the navigation drawer.
        drawerLayout.setStatusBarBackground(R.color.colorPrimaryDark)
        setupDrawerContent(findViewById(R.id.nav_view))

        if (savedInstanceState == null) {
            ActivityUtils.addFragmentToActivity(supportFragmentManager, StatisticsFragment(), R.id.contentFrame)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            // Open the navigation drawer when the home icon is selected from the toolbar.
            drawerLayout.openDrawer(GravityCompat.START)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.list_navigation_menu_item) {
                NavUtils.navigateUpFromSameTask(this@StatisticsActivity)
            }

            // Close the navigation drawer when an item is selected.
            it.isChecked = true
            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }
}
