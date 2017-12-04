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

package com.example.android.architecture.blueprints.todoapp.addedittask

import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.test.espresso.IdlingResource
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import com.example.android.architecture.blueprints.todoapp.Injection
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ViewModelHolder
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

/**
 * Displays an add or edit task screen.
 */
class AddEditTaskActivity : AppCompatActivity(), AddEditTaskNavigator {

    private var mViewModel: AddEditTaskViewModel? = null

    val countingIdlingResource: IdlingResource
        @VisibleForTesting
        get() = EspressoIdlingResource.idlingResource

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onTaskSaved() {
        setResult(ADD_EDIT_RESULT_OK)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)

        // Set up the toolbar.
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        val addEditTaskFragment = findOrCreateViewFragment()

        mViewModel = findOrCreateViewModel()

        // Link View and ViewModel
        addEditTaskFragment.setViewModel(mViewModel!!)

        mViewModel!!.onActivityCreated(this)
    }

    override fun onDestroy() {
        mViewModel!!.onActivityDestroyed()
        super.onDestroy()
    }

    private fun findOrCreateViewFragment(): AddEditTaskFragment {
        // View Fragment
        var addEditTaskFragment: AddEditTaskFragment? = supportFragmentManager
            .findFragmentById(R.id.contentFrame) as AddEditTaskFragment

        if (addEditTaskFragment == null) {
            addEditTaskFragment = AddEditTaskFragment.newInstance()

            // Send the task ID to the fragment
            val bundle = Bundle()
            bundle.putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID,
                intent.getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID))
            addEditTaskFragment!!.arguments = bundle

            ActivityUtils.addFragmentToActivity(supportFragmentManager,
                addEditTaskFragment, R.id.contentFrame)
        }
        return addEditTaskFragment
    }

    private fun findOrCreateViewModel(): AddEditTaskViewModel? {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        val retainedViewModel = supportFragmentManager
            .findFragmentByTag(ADD_EDIT_VIEWMODEL_TAG) as ViewModelHolder<AddEditTaskViewModel>

        if (retainedViewModel != null && retainedViewModel.viewmodel != null) {
            // If the model was retained, return it.
            return retainedViewModel.viewmodel
        } else {
            // There is no ViewModel yet, create it.
            val viewModel = AddEditTaskViewModel(
                applicationContext,
                Injection.provideTasksRepository(applicationContext))

            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                supportFragmentManager,
                ViewModelHolder.createContainer(viewModel),
                ADD_EDIT_VIEWMODEL_TAG)
            return viewModel
        }
    }

    companion object {

        val REQUEST_CODE = 1

        val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1

        val ADD_EDIT_VIEWMODEL_TAG = "ADD_EDIT_VIEWMODEL_TAG"
    }
}
