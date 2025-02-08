package com.example.planalog.ui.search

sealed class SearchItem {
    data class SearchResult(val resultText: String) : SearchItem()
    data class SearchHistory(val historyText: String) : SearchItem()
}
