package com.example.planalog.ui.search

sealed class SearchItem {
    data class SearchResult(val resultText: String, val userData: Map<String, Any>) : SearchItem()
    data class SearchHistory(val historyText: String) : SearchItem()
}
