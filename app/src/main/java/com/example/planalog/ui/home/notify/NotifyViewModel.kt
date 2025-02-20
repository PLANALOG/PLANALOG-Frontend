import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.planalog.network.notice.NotificationItem

class NotifyViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        @Volatile
        private var INSTANCE: NotifyViewModel? = null

        fun getInstance(application: Application): NotifyViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotifyViewModel(application).also { INSTANCE = it }
            }
        }
    }

    private val _notifications = MutableLiveData<List<NotificationItem>>(emptyList())
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val deletedNotificationIds = mutableSetOf<String>() // ✅ 삭제된 알림 ID를 저장

    fun setNotifications(notifications: List<NotificationItem>) {
        // ✅ 삭제된 알림은 다시 추가되지 않도록 필터링
        _notifications.value = notifications.filter { it.id.toString() !in deletedNotificationIds }
    }

    fun removeNotification(notification: NotificationItem) {
        // ✅ 삭제된 알림을 저장하고 LiveData에서 제거
        deletedNotificationIds.add(notification.id.toString())
        _notifications.value = _notifications.value?.filter { it.id != notification.id }

        Log.d("NotifyViewModel", "✅ 삭제된 알림 제거됨: ${notification.id}, 현재 개수: ${_notifications.value?.size}")
    }

    fun refreshNotifications(notices: List<NotificationItem>) {
        // ✅ 삭제된 알림을 제외하고 새로운 데이터 적용
        val filteredNotices = notices.filter { it.id.toString() !in deletedNotificationIds }
        _notifications.value = filteredNotices

        Log.d("NotifyViewModel", "📌 서버에서 동기화된 데이터 반영 완료: ${_notifications.value?.size}개")
    }
}