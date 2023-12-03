package com.example.travelassistant.utils

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView


private const val MIN_LINES = 2
private const val MAX_LINES = 3

/**
 * https://stackoverflow.com/a/59779604/5895675
 */
private fun EditText.multilineIme(action: Int) {
    imeOptions = action
    inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
    setHorizontallyScrolling(false)
    minLines = MIN_LINES
    maxLines = MAX_LINES
}

/**
 * https://stackoverflow.com/a/59779604/5895675
 */
fun EditText.multilineDone(callback: ((String) -> Unit)? = null) {
    val action = EditorInfo.IME_ACTION_DONE
    multilineIme(action)
    setOnEditorActionListener { editTextView, actionId, _ ->
        if (action == actionId) {
            callback?.invoke(editTextView.text.toString())
            true
        }
        false
    }
}