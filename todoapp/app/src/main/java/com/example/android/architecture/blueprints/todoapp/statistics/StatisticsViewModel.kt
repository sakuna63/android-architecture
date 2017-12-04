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

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.annotation.VisibleForTesting

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

/**
 * Exposes the data to be used in the statistics screen.
 *
 *
 * This ViewModel uses both [ObservableField]s ([ObservableBoolean]s in this case) and
 * [Bindable] getters. The values in [ObservableField]s are used directly in the layout,
 * whereas the [Bindable] getters allow us to add some logic to it. This is
 * preferable to having logic in the XML layout.
 */
class StatisticsViewModel(private val mContext: Context, private val mTasksRepository: TasksRepository) : BaseObservable() {

    val dataLoading = ObservableBoolean(false)

    internal val error = ObservableBoolean(false)

    @VisibleForTesting
    internal var mNumberOfActiveTasks = 0

    @VisibleForTesting
    internal var mNumberOfCompletedTasks = 0
    /**
     * Returns a String showing the number of active tasks.
     */
    val numberOfActiveTasks: String
        @Bindable
        get() = mContext.getString(R.string.statistics_active_tasks, mNumberOfActiveTasks)

    /**
     * Returns a String showing the number of completed tasks.
     */
    val numberOfCompletedTasks: String
        @Bindable
        get() = mContext.getString(R.string.statistics_completed_tasks, mNumberOfCompletedTasks)

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    val isEmpty: Boolean
        @Bindable
        get() = mNumberOfActiveTasks + mNumberOfCompletedTasks == 0

    fun start() {
        loadStatistics()
    }

    fun loadStatistics() {
        dataLoading.set(true)

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment() // App is busy until further notice

        mTasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.idlingResource.isIdleNow()) {
                    EspressoIdlingResource.decrement() // Set app as idle.
                }

                computeStats(tasks)
            }

            override fun onDataNotAvailable() {
                error.set(true)
            }
        })
    }

    /**
     * Called when new data is ready.
     */
    private fun computeStats(tasks: List<Task>) {
        var completed = 0
        var active = 0

        for (task in tasks) {
            if (task.isCompleted) {
                completed += 1
            } else {
                active += 1
            }
        }
        mNumberOfActiveTasks = active
        mNumberOfCompletedTasks = completed

        // There are multiple @Bindable fields in this ViewModel, calling notifyChange() will
        // update all the UI elements that depend on them.
        notifyChange()

        // To update just one of them and avoid unnecessary UI updates,
        // use notifyPropertyChanged(BR.field)

        // Observable fields don't need to be notified. set() will trigger an update.
        dataLoading.set(false)
        error.set(false)
    }
}
