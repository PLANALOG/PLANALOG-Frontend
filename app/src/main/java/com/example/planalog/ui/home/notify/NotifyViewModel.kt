import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.planalog.ui.home.notify.NotificationItem

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

    fun addNotification(notification: NotificationItem) {
        val updatedList = _notifications.value.orEmpty().toMutableList()
        updatedList.add(0, notification)
        _notifications.postValue(updatedList)

        Log.d("NotifyViewModel", "📌 알림 추가됨: ${notification.userId}, 메시지: ${notification.message}")
        Log.d("NotifyViewModel", "📌 변경 후 현재 알림 개수: ${_notifications.value?.size ?: 0}")
    }
}