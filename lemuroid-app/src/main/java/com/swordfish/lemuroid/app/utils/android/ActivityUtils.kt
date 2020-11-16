package com.swordfish.lemuroid.app.utils.android

import android.app.Activity
import androidx.appcompat.app.AlertDialog

fun Activity.displayErrorDialog(messageId: Int, actionLabelId: Int, action: () -> Unit) {
    displayErrorDialog(resources.getString(messageId), resources.getString(actionLabelId), action)
}

fun Activity.displayErrorDialog(message: String, actionLabel: String, action: () -> Unit) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(actionLabel) { _, _ -> action() }
        .setCancelable(false)
        .show()
}

fun Activity.displayConfirmationDialog(
    message: Int,
    positiveActionLabel: Int,
    negativeActionLabel: Int,
    positiveAction: () -> Unit,
    negativeAction: () -> Unit
) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(positiveActionLabel) { _, _ -> positiveAction() }
        .setNegativeButton(negativeActionLabel) { _, _ -> negativeAction() }
        .setCancelable(false)
        .show()
}
