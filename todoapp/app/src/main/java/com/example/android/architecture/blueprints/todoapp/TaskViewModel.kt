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

package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.Observable
import android.databinding.ObservableField

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository


/**
 * Abstract class for View Models that expose a single [Task].
 */
abstract class TaskViewModel(context: Context, private val mTasksRepository: TasksRepository) : BaseObservable(), TasksDataSource.GetTaskCallback {

    val snackbarText = ObservableField<String>()

    val title = ObservableField<String>()

    val description = ObservableField<String>()

    private val mTaskObservable = ObservableField<Task>()

    private val mContext: Context

    @get:Bindable
    var isDataLoading: Boolean = false
        private set

    // "completed" is two-way bound, so in order to intercept the new value, use a @Bindable
    // annotation and process it in the setter.
    // Notify repository and user
    var completed: Boolean
        @Bindable
        get() {
            val task = mTaskObservable.get()
            return task != null && task.isCompleted
        }
        set(completed) {
            if (isDataLoading) {
                return
            }
            val task = mTaskObservable.get()
            if (completed) {
                mTasksRepository.completeTask(task)
                snackbarText.set(mContext.resources.getString(R.string.task_marked_complete))
            } else {
                mTasksRepository.activateTask(task)
                snackbarText.set(mContext.resources.getString(R.string.task_marked_active))
            }
        }

    val isDataAvailable: Boolean
        @Bindable
        get() = mTaskObservable.get() != null

    // This could be an observable, but we save a call to Task.getTitleForList() if not needed.
    val titleForList: String?
        @Bindable
        get() = if (mTaskObservable.get() == null) {
            "No data"
        } else mTaskObservable.get().titleForList

    protected val taskId: String?
        get() = mTaskObservable.get().id

    init {
        mContext = context.applicationContext // Force use of Application Context.

        // Exposed observables depend on the mTaskObservable observable:
        mTaskObservable.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                val task = mTaskObservable.get()
                if (task != null) {
                    title.set(task.title)
                    description.set(task.description)
                } else {
                    title.set(mContext.getString(R.string.no_data))
                    description.set(mContext.getString(R.string.no_data_description))
                }
            }
        })
    }

    fun start(taskId: String?) {
        if (taskId != null) {
            isDataLoading = true
            mTasksRepository.getTask(taskId, this)
        }
    }

    fun setTask(task: Task) {
        mTaskObservable.set(task)
    }

    override fun onTaskLoaded(task: Task) {
        mTaskObservable.set(task)
        isDataLoading = false
        notifyChange() // For the @Bindable properties
    }

    override fun onDataNotAvailable() {
        mTaskObservable.set(null)
        isDataLoading = false
    }

    open fun deleteTask() {
        if (mTaskObservable.get() != null) {
            mTasksRepository.deleteTask(mTaskObservable.get().id)
        }
    }

    fun onRefresh() {
        if (mTaskObservable.get() != null) {
            start(mTaskObservable.get().id)
        }
    }

    fun getSnackbarText(): String {
        return snackbarText.get()
    }
}
