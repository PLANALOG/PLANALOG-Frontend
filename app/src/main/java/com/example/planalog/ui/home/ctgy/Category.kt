package com.example.planalog.ui.home.ctgy

import com.example.planalog.ui.home.memo.ChecklistItem

data class Category(
    var title: String,
    val checklists: MutableList<ChecklistItem>,
    val color: Int,
    var isEditable: Boolean = true,
)
