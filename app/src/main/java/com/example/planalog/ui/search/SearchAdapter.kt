import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.planalog.databinding.ItemSearchBinding
import com.example.planalog.databinding.ItemSearchHistoryBinding
import com.example.planalog.ui.search.SearchItem

class SearchAdapter(
    private val items: MutableList<SearchItem>,
    private val onItemClick: (Any) -> Unit
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
            is SearchItem.SearchResult -> (holder as SearchResultViewHolder).bind(item.resultText)
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
    fun updateSearchResults(newResults: List<String>) {
        // 기존 검색 결과 제거
        items.removeAll { it is SearchItem.SearchResult }

        // 새로운 검색 결과를 맨 앞에 추가
        val searchResults = newResults.map { SearchItem.SearchResult(it) }
        items.addAll(0, searchResults)

        // 데이터 변경 알림 및 맨 위로 스크롤
        notifyDataSetChanged()
    }



    /** 최종 검색어만 검색 기록에 추가 후 스크롤 이동 */
    fun addSearchHistory(newHistory: String, recyclerView: RecyclerView) {
        val existingIndex = items.indexOfFirst { it is SearchItem.SearchHistory && (it as SearchItem.SearchHistory).historyText == newHistory }
        if (existingIndex != -1) {
            items.removeAt(existingIndex)  // 중복된 항목 제거
            notifyItemRemoved(existingIndex)
        }

        items.add(0, SearchItem.SearchHistory(newHistory))
        notifyItemInserted(0)

        // 스크롤을 맨 위로 이동
        recyclerView.scrollToPosition(0)
    }

    inner class SearchHistoryViewHolder(private val binding: ItemSearchHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(historyText: String) {
            binding.searchRecentWord.text = historyText
            binding.root.setOnClickListener { onItemClick(historyText) }
        }
    }

    inner class SearchResultViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultText: String) {
            binding.searchPfTv.text = resultText

            // 클릭 시 friendId 추출 후 액티비티로 전달
            binding.root.setOnClickListener {
                val friendId = extractFriendId(resultText)
                if (friendId != -1) {
                    onItemClick(friendId.toString())
                    Log.d("friendId", "${friendId}")
                } else {
                    Toast.makeText(binding.root.context, "유효하지 않은 데이터입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun extractFriendId(resultText: String): Int {
            // 예: "Alice:123"에서 "123"을 추출
            val parts = resultText.split(":")
            return if (parts.size == 2 && parts[1].isNotEmpty()) {
                parts[1].toIntOrNull() ?: -1  // 숫자 변환 실패 시 -1 반환
            } else {
                -1  // 잘못된 형식일 경우
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HISTORY = 0
        private const val VIEW_TYPE_RESULT = 1
    }
}
