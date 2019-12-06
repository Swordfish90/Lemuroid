package com.swordfish.lemuroid.lib.ui

import android.view.View

fun View.updateVisibility(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}
