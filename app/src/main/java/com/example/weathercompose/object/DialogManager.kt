package com.example.weathercompose.`object`

import android.app.AlertDialog
import android.content.Context
import com.example.weathercompose.R

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.dialog_manager_title)
        dialog.setMessage(context.getString(R.string.dialog_manager_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.dialog_manager_ok)) { _, _ ->
            listener.onClick()
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.dialog_manager_cancel)) { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }
    interface Listener {
        fun onClick()
    }
}