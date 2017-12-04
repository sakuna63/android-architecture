/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.statistics

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.NavUtils
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View

import com.example.android.architecture.blueprints.todoapp.Injection
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ViewModelHolder
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils

/**
 * Show statistics for tasks.
 */
class StatisticsActivity : AppCompatActivity() {

    private var mDrawerLayout: DrawerLayout? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.statistics_act)

        setupToolbar()

        setupNavigationDrawer()

        val statisticsFragment = findOrCreateViewFragment()

        val statisticsViewModel = findOrCreateViewModel()

        // Link View and ViewModel
        statisticsFragment.setViewModel(statisticsViewModel)
    }

    private fun findOrCreateViewModel(): StatisticsViewModel {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        val retainedViewModel = supportFragmentManager
            .findFragmentByTag(STATS_VIEWMODEL_TAG) as? ViewModelHolder<StatisticsViewModel>

        if (retainedViewModel?.viewmodel != null) {
            // If the model was retained, return it.
            return retainedViewModel.viewmodel!!
        } else {
            // There is no ViewModel yet, create it.
            val viewModel = StatisticsViewModel(applicationContext,
                Injection.provideTasksRepository(applicationContext))

            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                supportFragmentManager,
                ViewModelHolder.createContainer(viewModel),
                STATS_VIEWMODEL_TAG)
            return viewModel
        }
    }

    private fun findOrCreateViewFragment(): StatisticsFragment {
        var statisticsFragment: StatisticsFragment? = supportFragmentManager
            .findFragmentById(R.id.contentFrame) as StatisticsFragment
        if (statisticsFragment == null) {
            statisticsFragment = StatisticsFragment.newInstance()
            ActivityUtils.addFragmentToActivity(supportFragmentManager,
                statisticsFragment!!, R.id.contentFrame)
        }
        return statisticsFragment
    }

    private fun setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout!!.setStatusBarBackground(R.color.colorPrimaryDark)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        if (navigationView != null) {
            setupDrawerContent(navigationView)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab!!.setTitle(R.string.statistics_title)
        ab.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.list_navigation_menu_item -> NavUtils.navigateUpFromSameTask(this@StatisticsActivity)
                R.id.statistics_navigation_menu_item -> {
                }
                else -> {
                }
            }// Do nothing, we're already on that screen
            // Close the navigation drawer when an item is selected.
            menuItem.isChecked = true
            mDrawerLayout!!.closeDrawers()
            true
        }
    }

    companion object {

        val STATS_VIEWMODEL_TAG = "ADD_EDIT_VIEWMODEL_TAG"
    }
}
