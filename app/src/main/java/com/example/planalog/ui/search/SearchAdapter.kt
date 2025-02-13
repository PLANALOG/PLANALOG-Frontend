import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemSearchBinding
import com.example.planalog.databinding.ItemSearchHistoryBinding
import com.example.planalog.ui.search.SearchItem
import com.google.gson.Gson

class SearchAdapter(
    private val items: MutableList<SearchItem>,
    private val onHistoryClick: (String) -> Unit,
    private val onResultClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HISTORY -> {
                val binding = ItemSearchHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SearchHistoryViewHolder(binding)
            }
            else -> {
                val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SearchResultViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SearchItem.SearchHistory -> (holder as SearchHistoryViewHolder).bind(item.historyText)
            is SearchItem.SearchResult -> (holder as SearchResultViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchItem.SearchHistory -> VIEW_TYPE_HISTORY
            is SearchItem.SearchResult -> VIEW_TYPE_RESULT
        }
    }

    override fun getItemCount(): Int = items.size

    /** 검색 결과 업데이트 */
    fun updateSearchResults(newResults: List<Map<String, Any>>) {
        items.clear()

        // 새로운 검색 결과 추가
        val searchResults = newResults.map { user ->
            // 닉네임을 문자열로 변환 후 비어 있는지 확인
            val nickname = user["nickname"] as? String
            val name = user["name"] as? String ?: "이름 없음"

            // 닉네임이 비어 있지 않으면 닉네임, 그렇지 않으면 이름을 표시
            val displayText = if (!nickname.isNullOrEmpty()) {
                nickname
            } else {
                name
            }

            SearchItem.SearchResult(displayText, user)
        }
        items.addAll(searchResults)

        notifyDataSetChanged()
    }


    /** 최종 검색어만 검색 기록에 추가 후 스크롤 이동 */
    fun addSearchHistory(newHistory: String?, recyclerView: RecyclerView) {
        if (newHistory.isNullOrBlank()) {
            Log.e("SearchAdapter", "❌ 빈 검색 기록은 추가할 수 없습니다.")
            return
        }

        val existingIndex = items.indexOfFirst {
            it is SearchItem.SearchHistory && it.historyText == newHistory
        }

        if (existingIndex != -1) {
            items.removeAt(existingIndex) // 기존 중복 항목 제거
            notifyItemRemoved(existingIndex)
        }

        items.add(0, SearchItem.SearchHistory(newHistory)) // 최신 검색 기록을 최상단에 추가
        notifyItemInserted(0)

        recyclerView.scrollToPosition(0)

        Log.d("SearchAdapter", "✅ 추가된 검색 기록 (최신순): $newHistory")
    }


    inner class SearchHistoryViewHolder(private val binding: ItemSearchHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(historyText: String) {
            Log.d("SearchHistoryViewHolder", "바인딩된 검색 기록: $historyText")
            binding.searchRecentWord.text = historyText
            binding.root.setOnClickListener {
                Log.d("SearchHistoryViewHolder", "클릭된 검색 기록: $historyText")
                onHistoryClick(historyText)
            }
        }
    }

    inner class SearchResultViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem.SearchResult) {
            binding.searchPfTv.text = item.resultText

            // 클릭 시 전체 사용자 데이터를 JSON으로 변환해 콜백 전달
            binding.root.setOnClickListener {
                val userJson = Gson().toJson(item.userData)
                Log.d("SearchAdapter", "전달되는 JSON 데이터: $userJson")
                onResultClick(userJson)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HISTORY = 0
        private const val VIEW_TYPE_RESULT = 1
    }
}
