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

import android.databinding.Observable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.databinding.TaskdetailFragBinding
import com.example.android.architecture.blueprints.todoapp.util.SnackbarUtils


/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : Fragment() {

    private var mViewModel: TaskDetailViewModel? = null
    private var mSnackbarCallback: Observable.OnPropertyChangedCallback? = null

    fun setViewModel(taskViewModel: TaskDetailViewModel) {
        mViewModel = taskViewModel
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupFab()

        setupSnackbar()
    }

    override fun onDestroy() {
        if (mSnackbarCallback != null) {
            mViewModel!!.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback)
        }
        super.onDestroy()
    }

    private fun setupSnackbar() {
        mSnackbarCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                SnackbarUtils.showSnackbar(view, mViewModel!!.getSnackbarText())
            }
        }
        mViewModel!!.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback)
    }

    private fun setupFab() {
        val fab = activity.findViewById<View>(R.id.fab_edit_task) as FloatingActionButton

        fab.setOnClickListener { mViewModel!!.startEditTask() }
    }

    override fun onResume() {
        super.onResume()
        mViewModel!!.start(arguments.getString(ARGUMENT_TASK_ID))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.taskdetail_frag, container, false)

        val viewDataBinding = TaskdetailFragBinding.bind(view)
        viewDataBinding.viewmodel = mViewModel

        setHasOptionsMenu(true)

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_delete -> {
                mViewModel!!.deleteTask()
                return true
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.taskdetail_fragment_menu, menu)
    }

    companion object {

        val ARGUMENT_TASK_ID = "TASK_ID"

        val REQUEST_EDIT_TASK = 1

        fun newInstance(taskId: String): TaskDetailFragment {
            val arguments = Bundle()
            arguments.putString(ARGUMENT_TASK_ID, taskId)
            val fragment = TaskDetailFragment()
            fragment.arguments = arguments
            return fragment
        }
    }
}
