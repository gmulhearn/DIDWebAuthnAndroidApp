package com.gmulhearn.didwebauthn

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

fun Fragment.showAlertDialog(
    title: String,
    message: String,
    @StringRes positiveButtonResId: Int,
    onPositive: (() -> Unit) ? = null,
    @StringRes negativeButtonResId: Int,
    onNegative: (() -> Unit) ? = null
) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonResId) { _, _ -> onPositive?.invoke() }
        .setNegativeButton(negativeButtonResId) { _, _ -> onNegative?.invoke() }
        .show()
}

fun Fragment.showAlertDialog(
    title: String,
    message: String,
    @StringRes positiveButtonResId: Int,
    onPositive: (() -> Unit) ? = null
) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonResId) { _, _ -> onPositive?.invoke() }
        .show()
}