package com.example.planalog.ui.home.ctgy

import com.example.planalog.ui.home.memo.ChecklistItem

data class Category(
    var id: Int,
    var title: String,
    val checklists: MutableList<ChecklistItem>,
    var color: Int,
    var isEditable: Boolean = true,
    var isSelected: Boolean = false,
    var isPartiallySelected: Boolean = false,
    var isDeleteMode: Boolean = false,
)
