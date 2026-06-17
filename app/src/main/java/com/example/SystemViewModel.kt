package com.gemileith.launcher

import android.app.Application
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

data class NotificationItem(
    val title: String,
    val description: String,
    val time: String,
    val isRead: Boolean
)

data class SystemState(
    val memoryTotalGb: Float = 0f,
    val memoryUsedGb: Float = 0f,
    val memoryUsagePercent: Float = 0f,
    val batteryTempC: Float = 0f,
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val installedApps: List<AppItem> = emptyList(),
    val isLoadingApps: Boolean = true,
    val searchQuery: String = "",
    val volumeLevel: Float = 0.5f,
    val isWifiEnabled: Boolean = true,
    val isBluetoothEnabled: Boolean = false,
    val isDarkMode: Boolean = true,
    val diskUsedGb: Float = 0f,
    val diskTotalGb: Float = 0f,
    val diskUsagePercent: Float = 0f,
    val refreshRateMs: Long = 2000L,
    val notifications: List<NotificationItem> = emptyList(),
    val ramHistory: List<Float> = emptyList(),
    val cpuHistory: List<Float> = emptyList()
)

class SystemViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SystemState())
    val uiState: StateFlow<SystemState> = _uiState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            
            val batteryPct = if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else 0
            
            _uiState.update { 
                it.copy(
                    batteryTempC = temp,
                    batteryLevel = batteryPct,
                    isCharging = isCharging
                )
            }
        }
    }

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)
        
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val startVolPct = if (maxVol > 0) currentVol.toFloat() / maxVol else 0.5f
        
        val initRam = List(20) { 0.4f + Math.random().toFloat() * 0.2f }
        val initCpu = List(20) { 0.2f + Math.random().toFloat() * 0.5f }
        
        val initialNotifications = listOf(
            NotificationItem("System Initialized", "Gemileith OS booted successfully.", "Just now", false),
            NotificationItem("Update Available", "A new system tune-up patch is ready.", "1h ago", true)
        )

        _uiState.update { 
            it.copy(
                volumeLevel = startVolPct,
                ramHistory = initRam,
                cpuHistory = initCpu,
                notifications = initialNotifications
            ) 
        }

        updateMemory()
        loadApps()
    }

    fun updateMemory() {
        val actManager = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val totalGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availGb = memInfo.availMem / (1024f * 1024f * 1024f)
        val usedGb = totalGb - availGb
        val pct = if (totalGb > 0) usedGb / totalGb else 0f

        val statFs = StatFs(Environment.getDataDirectory().path)
        val diskTotal = statFs.totalBytes / (1024f * 1024f * 1024f)
        val diskFree = statFs.availableBytes / (1024f * 1024f * 1024f)
        val diskUsed = diskTotal - diskFree
        val diskPct = if (diskTotal > 0) diskUsed / diskTotal else 0f

        _uiState.update { state ->
            val newRamList = (state.ramHistory.drop(1) + pct).takeLast(20)
            val newCpuUsg = (state.cpuHistory.last() + (Math.random().toFloat() - 0.5f) * 0.3f).coerceIn(0.1f, 0.98f)
            val newCpuList = (state.cpuHistory.drop(1) + newCpuUsg).takeLast(20)

            state.copy(
                memoryTotalGb = totalGb,
                memoryUsedGb = usedGb,
                memoryUsagePercent = pct,
                diskTotalGb = diskTotal,
                diskUsedGb = diskUsed,
                diskUsagePercent = diskPct,
                ramHistory = newRamList,
                cpuHistory = newCpuList
            )
        }
    }

    fun setVolume(percent: Float) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val vol = (percent * maxVol).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
        _uiState.update { it.copy(volumeLevel = percent) }
    }

    fun toggleWifi() {
        _uiState.update { it.copy(isWifiEnabled = !it.isWifiEnabled) }
    }

    fun toggleBluetooth() {
        _uiState.update { it.copy(isBluetoothEnabled = !it.isBluetoothEnabled) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleRefreshRate() {
        _uiState.update { 
            val nextRate = when(it.refreshRateMs) {
                1000L -> 2000L
                2000L -> 5000L
                else -> 1000L
            }
            it.copy(refreshRateMs = nextRate)
        }
    }

    fun dismissNotifications() {
        _uiState.update { it.copy(notifications = it.notifications.map { n -> n.copy(isRead = true) }) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList = pm.queryIntentActivities(intent, 0)
            
            val apps = resolveInfoList.map { resolveInfo ->
                AppItem(
                    name = resolveInfo.loadLabel(pm).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = resolveInfo.loadIcon(pm)
                )
            }.sortedBy { it.name }

            _uiState.update { it.copy(installedApps = apps, isLoadingApps = false) }
        }
    }
    
    fun launchApp(packageName: String) {
        val pm = getApplication<Application>().packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(batteryReceiver)
    }
}
