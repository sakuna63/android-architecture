/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.taskdetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import com.example.android.architecture.blueprints.todoapp.Injection
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ViewModelHolder
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity.Companion.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment.Companion.REQUEST_EDIT_TASK
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils

/**
 * Displays task details screen.
 */
class TaskDetailActivity : AppCompatActivity(), TaskDetailNavigator {

    private var mTaskViewModel: TaskDetailViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.taskdetail_act)

        setupToolbar()

        val taskDetailFragment = findOrCreateViewFragment()

        mTaskViewModel = findOrCreateViewModel()
        mTaskViewModel!!.setNavigator(this)

        // Link View and ViewModel
        taskDetailFragment.setViewModel(mTaskViewModel!!)
    }

    override fun onDestroy() {
        mTaskViewModel!!.onActivityDestroyed()
        super.onDestroy()
    }

    private fun findOrCreateViewModel(): TaskDetailViewModel {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        val retainedViewModel = supportFragmentManager
            .findFragmentByTag(TASKDETAIL_VIEWMODEL_TAG) as? ViewModelHolder<TaskDetailViewModel>

        if (retainedViewModel?.viewmodel != null) {
            // If the model was retained, return it.
            return retainedViewModel.viewmodel!!
        } else {
            // There is no ViewModel yet, create it.
            val viewModel = TaskDetailViewModel(
                applicationContext,
                Injection.provideTasksRepository(applicationContext))

            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                supportFragmentManager,
                ViewModelHolder.createContainer(viewModel),
                TASKDETAIL_VIEWMODEL_TAG)
            return viewModel
        }
    }

    private fun findOrCreateViewFragment(): TaskDetailFragment {
        // Get the requested task id
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)

        var taskDetailFragment = supportFragmentManager
            .findFragmentById(R.id.contentFrame) as? TaskDetailFragment

        if (taskDetailFragment == null) {
            taskDetailFragment = TaskDetailFragment.newInstance(taskId)

            ActivityUtils.addFragmentToActivity(supportFragmentManager,
                taskDetailFragment, R.id.contentFrame)
        }
        return taskDetailFragment
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab!!.setDisplayHomeAsUpEnabled(true)
        ab.setDisplayShowHomeEnabled(true)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == ADD_EDIT_RESULT_OK) {
                // If the result comes from the add/edit screen, it's an edit.
                setResult(EDIT_RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onTaskDeleted() {
        setResult(DELETE_RESULT_OK)
        // If the task was deleted successfully, go back to the list.
        finish()
    }

    override fun onStartEditTask() {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        val intent = Intent(this, AddEditTaskActivity::class.java)
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
        startActivityForResult(intent, REQUEST_EDIT_TASK)
    }

    companion object {

        val EXTRA_TASK_ID = "TASK_ID"

        val TASKDETAIL_VIEWMODEL_TAG = "TASKDETAIL_VIEWMODEL_TAG"

        val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2

        val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
    }
}
