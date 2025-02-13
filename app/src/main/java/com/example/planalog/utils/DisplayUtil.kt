package com.example.planalog.utils

import android.content.Context

object DisplayUtil {
    fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

}
