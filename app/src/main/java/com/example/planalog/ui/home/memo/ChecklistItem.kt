package com.example.planalog.ui.home.memo

data class ChecklistItem(
    var task : String,
    var isEditable: Boolean = true,
    //var isChecked: Boolean = false,
    var isSelected: Boolean = false,
    var isDeleteMode: Boolean = false,
)
