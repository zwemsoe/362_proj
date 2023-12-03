package com.example.travelassistant.utils

import androidx.fragment.app.Fragment

/**
 * https://stackoverflow.com/a/72160387/5895675
 */
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}