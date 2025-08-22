package com.example.memoryvisualizer.ui.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import android.view.View

object UiMessageHelper {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(context: Context, @StringRes messageResId: Int) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackbar(view: View, @StringRes messageResId: Int) {
        Snackbar.make(view, messageResId, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackbarWithAction(
        view: View,
        message: String,
        actionLabel: String,
        action: () -> Unit
    ) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(actionLabel) { action() }
            .show()
    }
}
