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

import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView

import com.example.android.architecture.blueprints.todoapp.Injection
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ScrollChildSwipeRefreshLayout
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.databinding.TaskItemBinding
import com.example.android.architecture.blueprints.todoapp.databinding.TasksFragBinding
import com.example.android.architecture.blueprints.todoapp.util.SnackbarUtils

import java.util.ArrayList

/**
 * Display a grid of [Task]s. User can choose to view all, active or completed tasks.
 */
class TasksFragment : Fragment() {

    private var mTasksViewModel: TasksViewModel? = null

    private var mTasksFragBinding: TasksFragBinding? = null

    private var mListAdapter: TasksAdapter? = null

    private var mSnackbarCallback: Observable.OnPropertyChangedCallback? = null

    override fun onResume() {
        super.onResume()
        mTasksViewModel!!.start()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mTasksFragBinding = TasksFragBinding.inflate(inflater, container, false)

        mTasksFragBinding!!.view = this

        mTasksFragBinding!!.viewmodel = mTasksViewModel

        setHasOptionsMenu(true)

        return mTasksFragBinding!!.root
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_clear -> mTasksViewModel!!.clearCompletedTasks()
            R.id.menu_filter -> showFilteringPopUpMenu()
            R.id.menu_refresh -> mTasksViewModel!!.loadTasks(true)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.tasks_fragment_menu, menu)
    }

    fun setViewModel(viewModel: TasksViewModel) {
        mTasksViewModel = viewModel
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupSnackbar()

        setupFab()

        setupListAdapter()

        setupRefreshLayout()
    }

    override fun onDestroy() {
        mListAdapter!!.onDestroy()
        if (mSnackbarCallback != null) {
            mTasksViewModel!!.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback)
        }
        super.onDestroy()
    }

    private fun setupSnackbar() {
        mSnackbarCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                SnackbarUtils.showSnackbar(view, mTasksViewModel!!.getSnackbarText())
            }
        }
        mTasksViewModel!!.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback)
    }

    private fun showFilteringPopUpMenu() {
        val popup = PopupMenu(context, activity.findViewById(R.id.menu_filter))
        popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.active -> mTasksViewModel!!.setFiltering(TasksFilterType.ACTIVE_TASKS)
                R.id.completed -> mTasksViewModel!!.setFiltering(TasksFilterType.COMPLETED_TASKS)
                else -> mTasksViewModel!!.setFiltering(TasksFilterType.ALL_TASKS)
            }
            mTasksViewModel!!.loadTasks(false)
            true
        }

        popup.show()
    }

    private fun setupFab() {
        val fab = activity.findViewById<View>(R.id.fab_add_task) as FloatingActionButton

        fab.setImageResource(R.drawable.ic_add)
        fab.setOnClickListener { mTasksViewModel!!.addNewTask() }
    }

    private fun setupListAdapter() {
        val listView = mTasksFragBinding!!.tasksList

        mListAdapter = TasksAdapter(
            ArrayList(0),
            activity as TasksActivity,
            Injection.provideTasksRepository(context.applicationContext),
            mTasksViewModel)
        listView.adapter = mListAdapter
    }

    private fun setupRefreshLayout() {
        val listView = mTasksFragBinding!!.tasksList
        val swipeRefreshLayout = mTasksFragBinding!!.refreshLayout
        swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(activity, R.color.colorPrimary),
            ContextCompat.getColor(activity, R.color.colorAccent),
            ContextCompat.getColor(activity, R.color.colorPrimaryDark)
        )
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView)
    }

    class TasksAdapter(tasks: List<Task>, taskItemNavigator: TasksActivity,
                       private val mTasksRepository: TasksRepository,
                       private val mTasksViewModel: TasksViewModel) : BaseAdapter() {

        private var mTaskItemNavigator: TaskItemNavigator? = null

        private var mTasks: List<Task>? = null

        init {
            mTaskItemNavigator = taskItemNavigator
            setList(tasks)

        }

        fun onDestroy() {
            mTaskItemNavigator = null
        }

        fun replaceData(tasks: List<Task>) {
            setList(tasks)
        }

        override fun getCount(): Int {
            return if (mTasks != null) mTasks!!.size else 0
        }

        override fun getItem(i: Int): Task {
            return mTasks!![i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val task = getItem(i)
            val binding: TaskItemBinding
            if (view == null) {
                // Inflate
                val inflater = LayoutInflater.from(viewGroup.context)

                // Create the binding
                binding = TaskItemBinding.inflate(inflater, viewGroup, false)
            } else {
                // Recycling view
                binding = DataBindingUtil.getBinding(view)
            }

            val viewmodel = TaskItemViewModel(
                viewGroup.context.applicationContext,
                mTasksRepository
            )

            viewmodel.setNavigator(mTaskItemNavigator)

            binding.viewmodel = viewmodel
            // To save on PropertyChangedCallbacks, wire the item's snackbar text observable to the
            // fragment's.
            viewmodel.snackbarText.addOnPropertyChangedCallback(
                object : Observable.OnPropertyChangedCallback() {
                    override fun onPropertyChanged(observable: Observable, i: Int) {
                        mTasksViewModel.snackbarText.set(viewmodel.getSnackbarText())
                    }
                })
            viewmodel.setTask(task)

            return binding.root
        }


        private fun setList(tasks: List<Task>) {
            mTasks = tasks
            notifyDataSetChanged()
        }
    }

    companion object {

        fun newInstance(): TasksFragment {
            return TasksFragment()
        }
    }
}// Requires empty public constructor
