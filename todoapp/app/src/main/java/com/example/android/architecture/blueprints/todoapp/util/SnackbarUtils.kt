package com.example.android.architecture.blueprints.todoapp.util

import android.support.design.widget.Snackbar
import android.view.View

/**
 * Provides a method to show a Snackbar.
 */
object SnackbarUtils {

    fun showSnackbar(v: View?, snackbarText: String?) {
        if (v == null || snackbarText == null) {
            return
        }
        Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG).show()
    }
}
