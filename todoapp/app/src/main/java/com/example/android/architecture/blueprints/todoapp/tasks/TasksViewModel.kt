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

package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableList
import android.graphics.drawable.Drawable

import com.example.android.architecture.blueprints.todoapp.BR
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

import java.util.ArrayList

/**
 * Exposes the data to be used in the task list screen.
 *
 *
 * [BaseObservable] implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a [Bindable] annotation to the property's
 * getter method.
 */
class TasksViewModel(
    private val mTasksRepository: TasksRepository,
    context: Context) : BaseObservable() {

    // These observable fields will update Views automatically
    val items: ObservableList<Task> = ObservableArrayList()

    val dataLoading = ObservableBoolean(false)

    val currentFilteringLabel = ObservableField<String>()

    val noTasksLabel = ObservableField<String>()

    val noTaskIconRes = ObservableField<Drawable>()

    val tasksAddViewVisible = ObservableBoolean()

    internal val snackbarText = ObservableField<String>()

    private var mCurrentFiltering = TasksFilterType.ALL_TASKS

    private val mIsDataLoadingError = ObservableBoolean(false)

    private val mContext: Context // To avoid leaks, this must be an Application Context.

    private var mNavigator: TasksNavigator? = null

    val isEmpty: Boolean
        @Bindable
        get() = items.isEmpty()

    init {
        mContext = context.applicationContext // Force use of Application Context.

        // Set initial state
        setFiltering(TasksFilterType.ALL_TASKS)
    }

    internal fun setNavigator(navigator: TasksNavigator) {
        mNavigator = navigator
    }

    internal fun onActivityDestroyed() {
        // Clear references to avoid potential memory leaks.
        mNavigator = null
    }

    fun start() {
        loadTasks(false)
    }

    fun loadTasks(forceUpdate: Boolean) {
        loadTasks(forceUpdate, true)
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be [TasksFilterType.ALL_TASKS],
     * [TasksFilterType.COMPLETED_TASKS], or
     * [TasksFilterType.ACTIVE_TASKS]
     */
    fun setFiltering(requestType: TasksFilterType) {
        mCurrentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            TasksFilterType.ALL_TASKS -> {
                currentFilteringLabel.set(mContext.getString(R.string.label_all))
                noTasksLabel.set(mContext.resources.getString(R.string.no_tasks_all))
                noTaskIconRes.set(mContext.resources.getDrawable(
                    R.drawable.ic_assignment_turned_in_24dp))
                tasksAddViewVisible.set(true)
            }
            TasksFilterType.ACTIVE_TASKS -> {
                currentFilteringLabel.set(mContext.getString(R.string.label_active))
                noTasksLabel.set(mContext.resources.getString(R.string.no_tasks_active))
                noTaskIconRes.set(mContext.resources.getDrawable(
                    R.drawable.ic_check_circle_24dp))
                tasksAddViewVisible.set(false)
            }
            TasksFilterType.COMPLETED_TASKS -> {
                currentFilteringLabel.set(mContext.getString(R.string.label_completed))
                noTasksLabel.set(mContext.resources.getString(R.string.no_tasks_completed))
                noTaskIconRes.set(mContext.resources.getDrawable(
                    R.drawable.ic_verified_user_24dp))
                tasksAddViewVisible.set(false)
            }
        }
    }

    fun clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks()
        snackbarText.set(mContext.getString(R.string.completed_tasks_cleared))
        loadTasks(false, false)
    }

    fun getSnackbarText(): String {
        return snackbarText.get()
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewTask() {
        if (mNavigator != null) {
            mNavigator!!.addNewTask()
        }
    }

    internal fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            when (resultCode) {
                TaskDetailActivity.EDIT_RESULT_OK -> snackbarText.set(
                    mContext.getString(R.string.successfully_saved_task_message))
                AddEditTaskActivity.ADD_EDIT_RESULT_OK -> snackbarText.set(
                    mContext.getString(R.string.successfully_added_task_message))
                TaskDetailActivity.DELETE_RESULT_OK -> snackbarText.set(
                    mContext.getString(R.string.successfully_deleted_task_message))
            }
        }
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the [TasksDataSource]
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private fun loadTasks(forceUpdate: Boolean, showLoadingUI: Boolean) {
        if (showLoadingUI) {
            dataLoading.set(true)
        }
        if (forceUpdate) {

            mTasksRepository.refreshTasks()
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment() // App is busy until further notice

        mTasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                val tasksToShow = ArrayList<Task>()

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.idlingResource.isIdleNow()) {
                    EspressoIdlingResource.decrement() // Set app as idle.
                }

                // We filter the tasks based on the requestType
                for (task in tasks) {
                    when (mCurrentFiltering) {
                        TasksFilterType.ALL_TASKS -> tasksToShow.add(task)
                        TasksFilterType.ACTIVE_TASKS -> if (task.isActive) {
                            tasksToShow.add(task)
                        }
                        TasksFilterType.COMPLETED_TASKS -> if (task.isCompleted) {
                            tasksToShow.add(task)
                        }
                        else -> tasksToShow.add(task)
                    }
                }
                if (showLoadingUI) {
                    dataLoading.set(false)
                }
                mIsDataLoadingError.set(false)

                items.clear()
                items.addAll(tasksToShow)
                notifyPropertyChanged(BR.empty) // It's a @Bindable so update manually
            }

            override fun onDataNotAvailable() {
                mIsDataLoadingError.set(true)
            }
        })
    }

}
