package frb.axeron.manager.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import frb.axeron.api.Axeron
import frb.axeron.api.core.AxeronSettings
import frb.axeron.manager.ui.theme.basePrimaryDefault
import frb.axeron.manager.ui.theme.toHexString
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
//    private val prefs = AxeronSettings.getPreferences()

    val themeOptions = listOf("Follow System", "Dark Theme", "Light Theme")

    var isIgniteWhenRelogEnabled by mutableStateOf(
        AxeronSettings.getEnableIgniteRelog()
    )
        private set

    var isActivateOnBootEnabled by mutableStateOf(
        AxeronSettings.getStartOnBoot()
    )
        private set

    var isPreferSystemBinariesEnabled by mutableStateOf(
        AxeronSettings.getPreferSystemBinaries()
    )
        private set

    var isTcpModeEnabled by mutableStateOf(
        AxeronSettings.getTcpMode()
    )
        private set

    var tcpPortInt: Int? by mutableStateOf(
        AxeronSettings.getTcpPort()
    )
        private set

    var isDynamicColorEnabled by mutableStateOf(
        AxeronSettings.getEnableDynamicColor()
    )
        private set

    var getAppThemeId by mutableIntStateOf(
        AxeronSettings.getAppThemeId()
    )
        private set

    var isDeveloperModeEnabled by mutableStateOf(
        AxeronSettings.getEnableDeveloperOptions()
    )
        private set

    var isWebDebuggingEnabled by mutableStateOf(
        AxeronSettings.getEnableWebDebugging()
    )
        private set

    // fungsi toggle / set manual

    fun setIgniteWhenRelog(enabled: Boolean) {
        viewModelScope.launch {
            isIgniteWhenRelogEnabled = enabled
            AxeronSettings.setEnableIgniteRelog(enabled)
        }
    }

    fun setActivateOnBoot(enabled: Boolean) {
        viewModelScope.launch {
            isActivateOnBootEnabled = enabled
            AxeronSettings.setStartOnBoot(enabled)
        }
    }

    fun setPreferSystemBinaries(enabled: Boolean) {
        viewModelScope.launch {
            isPreferSystemBinariesEnabled = enabled
            AxeronSettings.setPreferSystemBinaries(enabled)
            if (Axeron.pingBinder()) {
                Axeron.setPreferSystemBinaries(enabled)
            }
        }
    }

    fun setTcpMode(enabled: Boolean) {
        viewModelScope.launch {
            isTcpModeEnabled = enabled
            AxeronSettings.setTcpMode(enabled)
        }
    }

    fun setTcpPort(port: Int?) {
        viewModelScope.launch {
            tcpPortInt = port
            AxeronSettings.setTcpPort(port)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            isDynamicColorEnabled = enabled
            AxeronSettings.setEnableDynamicColor(enabled)
        }
    }

    fun setAppTheme(themeId: Int) {
        viewModelScope.launch {
            getAppThemeId = themeId
            AxeronSettings.setAppThemeId(themeId)
        }
    }

    fun setDeveloperOptions(enabled: Boolean) {
        viewModelScope.launch {
            isDeveloperModeEnabled = enabled
            AxeronSettings.setEnableDeveloperOptions(enabled)
        }
    }

    fun setWebDebugging(enabled: Boolean) {
        viewModelScope.launch {
            isWebDebuggingEnabled = enabled
            AxeronSettings.setEnableWebDebugging(enabled)
        }
    }

    var customPrimaryColorHex by mutableStateOf(
        AxeronSettings.getCustomPrimaryColor() ?: basePrimaryDefault.toHexString()
    )
        private set

    fun setCustomPrimaryColor(hex: String) {
        viewModelScope.launch {
            customPrimaryColorHex = hex
            AxeronSettings.setPrimaryColor(hex)
        }
    }

    fun removeCustomPrimaryColor() {
        viewModelScope.launch {
            customPrimaryColorHex = basePrimaryDefault.toHexString()
            AxeronSettings.removePrimaryColor()
        }
    }

    // ── Liquid Glass 底栏 ────────────────────────────────────────────────────────────
    // Kept in the app's own SharedPreferences rather than AxeronSettings: that class lives
    // in the api submodule, and this port deliberately does not modify the library's source.

    private val glassPrefs =
        application.getSharedPreferences("glass", Application.MODE_PRIVATE)

    /** 0.0 = fully transparent glass, 1.0 = fully opaque. Default sits low, as asked. */
    var glassBarOpacity by mutableFloatStateOf(
        glassPrefs.getFloat(KEY_GLASS_OPACITY, DEFAULT_GLASS_OPACITY)
    )
        private set

    fun setGlassOpacity(value: Float) {
        val clamped = value.coerceIn(MIN_GLASS_OPACITY, MAX_GLASS_OPACITY)
        glassBarOpacity = clamped
        glassPrefs.edit().putFloat(KEY_GLASS_OPACITY, clamped).apply()
    }

    companion object {
        private const val KEY_GLASS_OPACITY = "glass_opacity"

        /** Translucent by default: enough tint to read the icons, thin enough to see through. */
        const val DEFAULT_GLASS_OPACITY = 0.18f
        const val MIN_GLASS_OPACITY = 0.0f
        const val MAX_GLASS_OPACITY = 1.0f
    }

}