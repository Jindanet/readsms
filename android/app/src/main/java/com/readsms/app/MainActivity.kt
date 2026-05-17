package com.readsms.app

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.readsms.app.data.SettingsStore
import com.readsms.app.data.SmsFileQueue
import com.readsms.app.data.SmsReader
import com.readsms.app.model.SmsRow
import com.readsms.app.net.ApiClient
import com.readsms.app.sync.CollectorForegroundService
import com.readsms.app.sync.SyncScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

private val lightAppColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFF8FAFC),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF151922),
    surface = Color(0xFFFCFDFF),
    onSurface = Color(0xFF151922),
    surfaceVariant = Color(0xFFE9EEF6),
    onSurfaceVariant = Color(0xFF586172),
    outline = Color(0xFFD5DBE6),
    error = Color(0xFFB3261E),
)

private val darkAppColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4FF),
    onPrimary = Color(0xFF06214B),
    background = Color(0xFF101318),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF171B22),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF232934),
    onSurfaceVariant = Color(0xFFAAB3C2),
    outline = Color(0xFF394252),
    error = Color(0xFFFFB4AB),
)

private val readSmsTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 27.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
)

private data class UiPalette(
    val page: Color,
    val surface: Color,
    val chip: Color,
    val blue: Color,
    val blueSoft: Color,
    val blueLine: Color,
    val text: Color,
    val muted: Color,
    val line: Color,
    val warn: Color,
    val warnSoft: Color,
    val ok: Color,
    val okSoft: Color,
    val bubbleIn: Color,
    val bubbleUnread: Color,
    val bubbleDevice: Color,
)

private val lightUiPalette = UiPalette(
    page = Color(0xFFF7F9FC),
    surface = Color(0xFFFCFDFF),
    chip = Color(0xFFEFF3F8),
    blue = Color(0xFF2563EB),
    blueSoft = Color(0xFFE7F0FF),
    blueLine = Color(0xFF9BC0FF),
    text = Color(0xFF151922),
    muted = Color(0xFF586172),
    line = Color(0xFFDCE3EE),
    warn = Color(0xFFB45309),
    warnSoft = Color(0xFFFFF3D9),
    ok = Color(0xFF15803D),
    okSoft = Color(0xFFE7F7EE),
    bubbleIn = Color(0xFFEFF3F8),
    bubbleUnread = Color(0xFFDCEBFF),
    bubbleDevice = Color(0xFFE7F0FF),
)

private val darkUiPalette = UiPalette(
    page = Color(0xFF101318),
    surface = Color(0xFF171B22),
    chip = Color(0xFF232934),
    blue = Color(0xFF8AB4FF),
    blueSoft = Color(0xFF17345E),
    blueLine = Color(0xFF3F69A8),
    text = Color(0xFFE6EAF2),
    muted = Color(0xFFAAB3C2),
    line = Color(0xFF2B3341),
    warn = Color(0xFFFFC36A),
    warnSoft = Color(0xFF3B2A12),
    ok = Color(0xFF76D394),
    okSoft = Color(0xFF153621),
    bubbleIn = Color(0xFF232934),
    bubbleUnread = Color(0xFF17345E),
    bubbleDevice = Color(0xFF17345E),
)

private val LocalUiPalette = staticCompositionLocalOf { lightUiPalette }
private val LocalAppText = staticCompositionLocalOf { AppText.English }

private object Ui {
    val page: Color
        @Composable get() = LocalUiPalette.current.page
    val surface: Color
        @Composable get() = LocalUiPalette.current.surface
    val chip: Color
        @Composable get() = LocalUiPalette.current.chip
    val blue: Color
        @Composable get() = LocalUiPalette.current.blue
    val blueSoft: Color
        @Composable get() = LocalUiPalette.current.blueSoft
    val blueLine: Color
        @Composable get() = LocalUiPalette.current.blueLine
    val text: Color
        @Composable get() = LocalUiPalette.current.text
    val muted: Color
        @Composable get() = LocalUiPalette.current.muted
    val line: Color
        @Composable get() = LocalUiPalette.current.line
    val warn: Color
        @Composable get() = LocalUiPalette.current.warn
    val warnSoft: Color
        @Composable get() = LocalUiPalette.current.warnSoft
    val ok: Color
        @Composable get() = LocalUiPalette.current.ok
    val okSoft: Color
        @Composable get() = LocalUiPalette.current.okSoft
    val bubbleIn: Color
        @Composable get() = LocalUiPalette.current.bubbleIn
    val bubbleUnread: Color
        @Composable get() = LocalUiPalette.current.bubbleUnread
    val bubbleDevice: Color
        @Composable get() = LocalUiPalette.current.bubbleDevice
}

private data class SmsThread(
    val key: String,
    val sender: String,
    val deviceId: String,
    val latestBody: String,
    val latestAt: String,
    val latestAtMs: Long,
    val count: Int,
    val unreadCount: Int,
    val messages: List<SmsRow>,
)

private enum class SetupState {
    Missing,
    Checking,
    Valid,
    Invalid,
}

private const val OWNER_SMS_CHANNEL_ID = "owner_sms_realtime_silent"
private const val OWNER_SMS_GROUP = "owner_sms_group"
private const val FRESH_NOTIFICATION_WINDOW_MS = 24L * 60L * 60L * 1000L
private const val OWNER_POLL_INTERVAL_MS = 5_000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadSmsTheme {
                ReadSmsApp()
            }
        }
    }
}

@Composable
private fun ReadSmsTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val uiPalette = if (darkTheme) darkUiPalette else lightUiPalette
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            applySystemBarColors(window, uiPalette)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) darkAppColorScheme else lightAppColorScheme,
        typography = readSmsTypography,
    ) {
        CompositionLocalProvider(LocalUiPalette provides uiPalette) {
            content()
        }
    }
}

@Suppress("DEPRECATION")
private fun applySystemBarColors(window: Window, uiPalette: UiPalette) {
    window.statusBarColor = uiPalette.page.toArgb()
    window.navigationBarColor = uiPalette.page.toArgb()
}

@Composable
fun ReadSmsApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val settings = remember { SettingsStore(context) }
    val queue = remember { SmsFileQueue(context) }
    val json = remember { Json { ignoreUnknownKeys = true } }

    var baseUrl by remember { mutableStateOf(settings.baseUrl) }
    var apiToken by remember { mutableStateOf(settings.apiToken) }
    var deviceId by remember { mutableStateOf(settings.deviceId) }
    var deviceName by remember { mutableStateOf(settings.deviceName) }
    var role by remember { mutableStateOf(settings.role) }
    var language by remember { mutableStateOf(settings.language) }
    val text = remember(language) { AppText.from(language) }
    var status by remember { mutableStateOf(text.ready) }
    var pendingCount by remember { mutableStateOf(queue.countPending()) }
    var messages by remember { mutableStateOf<List<SmsRow>>(emptyList()) }
    var liveConnected by remember { mutableStateOf(false) }
    var ownerHttpOnline by remember { mutableStateOf(false) }
    var tokenVisible by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(baseUrl.isBlank() || apiToken.isBlank()) }
    var batteryUnrestricted by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedThreadKey by remember { mutableStateOf<String?>(null) }
    var reconnectNonce by remember { mutableIntStateOf(0) }
    var setupState by remember { mutableStateOf(if (baseUrl.isBlank() || apiToken.isBlank()) SetupState.Missing else SetupState.Checking) }
    var setupError by remember { mutableStateOf<String?>(null) }
    var notificationPermissionGranted by remember { mutableStateOf(canPostNotifications(context)) }
    var ownerLoadedOnce by remember { mutableStateOf(false) }
    var ownerRefreshInFlight by remember { mutableStateOf(false) }
    var ownerReadInitialized by remember { mutableStateOf(settings.ownerReadInitialized) }
    var readMessageKeys by remember { mutableStateOf(settings.ownerReadMessageKeys) }
    var collectorKeepAliveStarted by remember { mutableStateOf(false) }

    val connectionReady = baseUrl.isNotBlank() && apiToken.isNotBlank()
    val setupValid = setupState == SetupState.Valid
    val threads = remember(messages, searchQuery, readMessageKeys, text.unknownSender) {
        buildThreads(messages, searchQuery, readMessageKeys, text.unknownSender)
    }
    val selectedThread = threads.firstOrNull { it.key == selectedThreadKey }

    LaunchedEffect(language) {
        settings.language = language
        status = text.ready
    }

    fun persistSettings() {
        baseUrl = normalizeBaseUrl(baseUrl)
        settings.baseUrl = baseUrl
        settings.apiToken = apiToken
        settings.deviceId = deviceId
        settings.deviceName = deviceName
        settings.role = role
        settings.language = language
    }

    fun validateSetup() {
        if (!connectionReady) {
            setupState = SetupState.Missing
            setupError = text.serverTokenRequired
            showSettings = true
            liveConnected = false
            ownerHttpOnline = false
            status = text.completeSetup
            return
        }

        persistSettings()
        setupState = SetupState.Checking
        setupError = null
        status = text.checkingServer
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { ApiClient(settings).validate() }
            }.onSuccess {
                setupState = SetupState.Valid
                setupError = null
                showSettings = false
                ownerHttpOnline = true
                status = text.connected
                reconnectNonce += 1
            }.onFailure {
                setupState = SetupState.Invalid
                setupError = it.message ?: text.cannotConnectServer
                showSettings = true
                liveConnected = false
                ownerHttpOnline = false
                status = text.fixConnectionSetup
            }
        }
    }

    fun markSetupDirty() {
        setupState = SetupState.Missing
        setupError = text.saveToVerify
        showSettings = true
        liveConnected = false
        ownerHttpOnline = false
    }

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun persistReadState(keys: Set<String>, initialized: Boolean = ownerReadInitialized) {
        readMessageKeys = keys
        ownerReadInitialized = initialized
        settings.ownerReadMessageKeys = keys
        settings.ownerReadInitialized = initialized
    }

    fun markThreadRead(thread: SmsThread?) {
        if (thread == null) return
        val keys = thread.messages.map { messageKey(it) }.toSet()
        persistReadState(readMessageKeys + keys, initialized = true)
    }

    fun refreshOwner(notifyNew: Boolean = false) {
        if (!connectionReady) {
            status = text.addServerToken
            showSettings = true
            return
        }
        if (ownerRefreshInFlight) return
        persistSettings()
        ownerRefreshInFlight = true
        scope.launch {
            if (!notifyNew) {
                status = text.syncingInbox
            }
            runCatching {
                withContext(Dispatchers.IO) { ApiClient(settings).recent(limit = 500) }
            }.onSuccess {
                ownerHttpOnline = true
                val known = messages.map { row -> "${row.deviceId}|${row.smsId}" }.toSet()
                val newRows = it.messages.filter { row -> "${row.deviceId}|${row.smsId}" !in known }
                messages = it.messages
                val currentKeys = it.messages.map { row -> messageKey(row) }.toSet()
                if (!ownerReadInitialized) {
                    persistReadState(currentKeys, initialized = true)
                } else {
                    val prunedReadKeys = readMessageKeys.intersect(currentKeys)
                    if (prunedReadKeys.size != readMessageKeys.size) {
                        persistReadState(prunedReadKeys)
                    }
                }
                if (notifyNew && ownerLoadedOnce && newRows.isNotEmpty()) {
                    notifyFreshOwnerMessages(context, newRows)
                }
                ownerLoadedOnce = true
                status = text.updated(it.count, newRows.size)
            }.onFailure {
                ownerHttpOnline = false
                status = it.message ?: text.refreshFailed
            }.also {
                ownerRefreshInFlight = false
            }
        }
    }

    fun pullRecentSms() {
        if (!connectionReady || !setupValid) {
            status = text.fixServerFirst
            showSettings = true
            return
        }
        if (!hasSmsPermission()) {
            status = text.allowSmsFirst
            return
        }

        persistSettings()
        scope.launch {
            status = text.readingLocalSms
            runCatching {
                withContext(Dispatchers.IO) {
                    val since = Instant.now().minusSeconds(24L * 60L * 60L).toEpochMilli()
                    val rows = SmsReader.readInboxSince(context, since)
                    val added = queue.append(rows)
                    SyncScheduler.enqueueNow(context)
                    Triple(rows.size, added, queue.countPending())
                }
            }.onSuccess { (readCount, addedCount, pending) ->
                pendingCount = pending
                status = text.readQueued(readCount, addedCount)
            }.onFailure {
                status = it.message ?: text.cannotReadLocalSms
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.all { it }) {
            status = text.smsPermissionGranted
            if (role == "collector" && setupValid) {
                pullRecentSms()
            }
        } else {
            status = text.smsPermissionMissing
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationPermissionGranted = granted
        if (!granted && role == "owner") {
            status = text.notificationPermissionMissing
        }
    }

    LaunchedEffect(Unit) {
        SyncScheduler.schedulePeriodic(context)
        SyncScheduler.scheduleCollectorWatchdog(context)
        validateSetup()
    }

    LaunchedEffect(role, setupState) {
        if (role == "owner" && setupValid) {
            ensureOwnerNotificationChannel(context)
            notificationPermissionGranted = canPostNotifications(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else if (role == "collector" && setupValid && hasSmsPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            persistSettings()
            CollectorForegroundService.start(context)
            collectorKeepAliveStarted = true
            pullRecentSms()
        } else if (role != "collector") {
            CollectorForegroundService.stop(context)
            collectorKeepAliveStarted = false
        }
    }

    LaunchedEffect(role, reconnectNonce, setupState) {
        if (role == "owner" && setupValid) {
            refreshOwner()
        }
    }

    LaunchedEffect(role, setupState) {
        if (role == "owner" && setupValid) {
            while (true) {
                delay(OWNER_POLL_INTERVAL_MS)
                refreshOwner(notifyNew = true)
            }
        }
    }

    DisposableEffect(role, reconnectNonce, setupState) {
        if (role != "owner" || !setupValid) {
            liveConnected = false
            if (role == "owner") {
                ownerHttpOnline = false
            }
            onDispose { }
        } else {
            persistSettings()
            status = text.connectingRealtime
            val webSocket = ApiClient(settings).viewerSocket(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        scope.launch {
                            liveConnected = true
                            ownerHttpOnline = true
                            status = text.realtimeReady
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, messageText: String) {
                        runCatching {
                            val root = json.parseToJsonElement(messageText).jsonObject
                            if (root["type"]?.jsonPrimitive?.content == "sms.inserted") {
                                val rows = root["messages"]?.jsonArray.orEmpty().map {
                                    json.decodeFromJsonElement(SmsRow.serializer(), it)
                                }
                                scope.launch {
                                    val known = messages.map { row -> "${row.deviceId}|${row.smsId}" }.toSet()
                                    val newRows = rows.filter { row -> "${row.deviceId}|${row.smsId}" !in known }
                                    messages = mergeMessages(rows + messages).take(500)
                                    ownerLoadedOnce = true
                                    status = text.newMessageReceived
                                    notifyFreshOwnerMessages(context, newRows)
                                }
                            }
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        scope.launch {
                            liveConnected = false
                            status = t.message ?: text.realtimeDisconnectedRetry
                            delay(3_000L)
                            if (role == "owner" && setupValid) {
                                reconnectNonce += 1
                            }
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        scope.launch {
                            liveConnected = false
                            status = text.realtimeClosed
                        }
                    }
                },
            )
            onDispose {
                webSocket.close(1000, "Screen changed")
                liveConnected = false
            }
        }
    }

    DisposableEffect(lifecycleOwner, role, setupState, baseUrl, apiToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryUnrestricted = isIgnoringBatteryOptimizations(context)
                if (role == "owner" && setupValid) {
                    refreshOwner()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CompositionLocalProvider(LocalAppText provides text) {
    Surface(modifier = Modifier.fillMaxSize(), color = Ui.page) {
        if (role == "owner" && selectedThread != null) {
            ThreadScreen(
                thread = selectedThread,
                readMessageKeys = readMessageKeys,
                onBack = {
                    markThreadRead(selectedThread)
                    selectedThreadKey = null
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    AppTopBar(
                        title = if (role == "owner") text.messagesTitle else text.collectorTitle,
                        subtitle = if (role == "owner") {
                            when {
                                setupState == SetupState.Valid && liveConnected -> text.realtimeReady
                                setupState == SetupState.Valid && ownerHttpOnline -> text.pollingOnline
                                setupState == SetupState.Valid -> text.connectingRealtime
                                setupState == SetupState.Checking -> text.checkingSetup
                                else -> text.setupRequired
                            }
                        } else {
                            if (setupValid) text.backgroundSmsSync else text.setupRequired
                        },
                        live = role == "owner" && (liveConnected || ownerHttpOnline),
                        onSettings = { showSettings = if (!setupValid) true else !showSettings },
                        onRefresh = {
                            if (role == "owner") {
                                if (setupValid) {
                                    reconnectNonce += 1
                                    refreshOwner()
                                } else {
                                    validateSetup()
                                }
                            } else {
                                SyncScheduler.enqueueNow(context)
                                pendingCount = queue.countPending()
                                status = text.syncQueued
                            }
                        },
                    )
                }

                item {
                    RoleSegment(
                        role = role,
                        onRoleChange = {
                            role = it
                            settings.role = it
                            selectedThreadKey = null
                            ownerLoadedOnce = false
                            ownerHttpOnline = false
                            if (it != "collector") {
                                CollectorForegroundService.stop(context)
                                collectorKeepAliveStarted = false
                            }
                            showSettings = baseUrl.isBlank() || apiToken.isBlank()
                        },
                    )
                }

                if (showSettings || !setupValid) {
                    item {
                        SettingsPanel(
                            baseUrl = baseUrl,
                            apiToken = apiToken,
                            deviceId = deviceId,
                            deviceName = deviceName,
                            language = language,
                            tokenVisible = tokenVisible,
                            onBaseUrlChange = {
                                baseUrl = it.trim()
                                markSetupDirty()
                            },
                            onApiTokenChange = {
                                apiToken = it.trim()
                                markSetupDirty()
                            },
                            onDeviceIdChange = {
                                deviceId = it.trim()
                                markSetupDirty()
                            },
                            onDeviceNameChange = { deviceName = it },
                            onLanguageChange = {
                                language = it
                                settings.language = it
                            },
                            onToggleToken = { tokenVisible = !tokenVisible },
                            setupState = setupState,
                            setupError = setupError,
                            onSave = {
                                validateSetup()
                            },
                        )
                    }
                }

                if (!setupValid) {
                    item {
                        EmptyState(
                            title = when (setupState) {
                                SetupState.Checking -> text.setupCheckingConnection
                                SetupState.Missing -> text.setupServerFirst
                                SetupState.Invalid -> text.setupConnectionInvalid
                                SetupState.Valid -> text.setupConnected
                            },
                            body = setupError ?: text.setupBody,
                            actionLabel = text.editSetup,
                            onAction = { showSettings = true },
                        )
                    }
                } else if (role == "collector") {
                    item {
                        CollectorHome(
                            pendingCount = pendingCount,
                            hasSmsPermission = hasSmsPermission(),
                            batteryUnrestricted = batteryUnrestricted,
                            keepAliveStarted = collectorKeepAliveStarted,
                            connectionReady = connectionReady,
                            status = status,
                            onRequestPermission = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_SMS,
                                        Manifest.permission.RECEIVE_SMS,
                                    ),
                                )
                            },
                            onOpenAppSettings = {
                                openAppSettings(context)
                                status = text.openSmsPermissionPath
                            },
                            onRequestBatteryAccess = {
                                requestIgnoreBatteryOptimizations(context)
                                batteryUnrestricted = isIgnoringBatteryOptimizations(context)
                                status = text.allowBackgroundSync
                            },
                            onStartKeepAlive = {
                                role = "collector"
                                settings.role = "collector"
                                persistSettings()
                                CollectorForegroundService.start(context)
                                collectorKeepAliveStarted = true
                                status = text.startKeepAliveSync
                            },
                            onBackfill = {
                                pullRecentSms()
                            },
                            onSync = {
                                role = "collector"
                                settings.role = "collector"
                                persistSettings()
                                CollectorForegroundService.start(context)
                                collectorKeepAliveStarted = true
                                SyncScheduler.enqueueNow(context)
                                pendingCount = queue.countPending()
                                status = text.syncQueued
                            },
                        )
                    }
                } else {
                    item {
                        OwnerSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            status = status,
                            liveConnected = liveConnected,
                            ownerHttpOnline = ownerHttpOnline,
                            messageCount = messages.size,
                        )
                    }

                    if (threads.isEmpty()) {
                        item {
                            EmptyState(
                                title = if (searchQuery.isBlank()) text.noMessages else text.noMatchingMessages,
                                body = if (searchQuery.isBlank()) text.noMessagesBody else text.noMatchingBody,
                                actionLabel = text.refresh,
                                onAction = { reconnectNonce += 1 },
                            )
                        }
                    } else {
                        items(threads, key = { it.key }) { thread ->
                            ConversationRow(
                                thread = thread,
                                onClick = { selectedThreadKey = thread.key },
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    subtitle: String,
    live: Boolean,
    onSettings: () -> Unit,
    onRefresh: () -> Unit,
) {
    val text = LocalAppText.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = Ui.text,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (live) Ui.ok else Ui.warn),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ui.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = text.refresh, tint = Ui.muted)
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Default.MoreVert, contentDescription = text.settings, tint = Ui.muted)
        }
    }
}

@Composable
private fun OwnerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    status: String,
    liveConnected: Boolean,
    ownerHttpOnline: Boolean,
    messageCount: Int,
) {
    val text = LocalAppText.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Ui.chip,
            shape = RoundedCornerShape(28.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Ui.muted) },
                placeholder = { Text(text.searchMessages, color = Ui.muted) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = Ui.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            StatusChip(
                label = when {
                    liveConnected -> text.realtime
                    ownerHttpOnline -> text.polling
                    else -> text.offline
                },
                positive = liveConnected || ownerHttpOnline,
            )
            Spacer(Modifier.width(8.dp))
            Text(text.messageCount(messageCount), style = MaterialTheme.typography.bodySmall, color = Ui.muted)
        }
    }
}

@Composable
private fun ConversationRow(thread: SmsThread, onClick: () -> Unit) {
    val hasUnread = thread.unreadCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (hasUnread) Ui.blueSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(text = thread.sender)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = thread.sender,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = Ui.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatThreadTime(thread.latestAtMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasUnread) Ui.blue else Ui.muted,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DeviceTag(thread.deviceId)
                Spacer(Modifier.width(7.dp))
                Text(
                    text = thread.latestBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasUnread) Ui.text else Ui.muted,
                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (hasUnread) {
                    Spacer(Modifier.width(8.dp))
                    UnreadBadge(count = thread.unreadCount)
                }
            }
        }
    }
}

@Composable
private fun ThreadScreen(thread: SmsThread, readMessageKeys: Set<String>, onBack: () -> Unit) {
    val text = LocalAppText.current
    val sortedMessages = thread.messages.sortedBy { it.receivedAtMs }
    val firstUnreadIndex = sortedMessages.indexOfFirst { messageKey(it) !in readMessageKeys }
    Column(modifier = Modifier.fillMaxSize().background(Ui.page)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = text.back, tint = Ui.text)
            }
            Avatar(text = thread.sender, size = 38.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.sender,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Ui.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = text.threadSubtitle(thread.deviceId, thread.unreadCount, thread.count),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ui.muted,
                    maxLines = 1,
                )
            }
        }
        HorizontalDivider(color = Ui.line)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(sortedMessages, key = { _, item -> item.id }) { index, message ->
                if (index == firstUnreadIndex) {
                    NewMessagesDivider(count = thread.unreadCount)
                }
                MessageBubble(message, isUnread = messageKey(message) !in readMessageKeys)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: SmsRow, isUnread: Boolean) {
    val text = LocalAppText.current
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Surface(
            color = if (isUnread) Ui.bubbleUnread else Ui.bubbleIn,
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp),
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (isUnread) {
                    Text(
                        text = text.newLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Ui.blue,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ui.text,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = formatTimestamp(message.receivedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ui.muted,
                )
            }
        }
    }
}

@Composable
private fun CollectorHome(
    pendingCount: Int,
    hasSmsPermission: Boolean,
    batteryUnrestricted: Boolean,
    keepAliveStarted: Boolean,
    connectionReady: Boolean,
    status: String,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onRequestBatteryAccess: () -> Unit,
    onStartKeepAlive: () -> Unit,
    onBackfill: () -> Unit,
    onSync: () -> Unit,
) {
    val text = LocalAppText.current
    Panel {
        Text(
            text = text.syncStatus,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Ui.text,
        )
        Text(status, style = MaterialTheme.typography.bodyMedium, color = Ui.muted)
        Spacer(Modifier.height(16.dp))

        StatusLine(text.server, if (connectionReady) text.readyValue else text.missingValue, connectionReady)
        StatusLine(text.smsAccess, if (hasSmsPermission) text.allowed else text.needsPermission, hasSmsPermission)
        StatusLine(text.battery, if (batteryUnrestricted) text.unrestricted else text.restricted, batteryUnrestricted)
        StatusLine(text.keepAlive, if (keepAliveStarted) text.on else text.off, keepAliveStarted)
        StatusLine(text.queue, text.pendingCount(pendingCount), pendingCount == 0)

        Spacer(Modifier.height(18.dp))
        if (!hasSmsPermission) {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
            ) {
                Icon(Icons.Default.Sms, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text.allowSmsAccess)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenAppSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text.openAppPermissions)
            }
            Spacer(Modifier.height(10.dp))
        }
        if (!batteryUnrestricted) {
            OutlinedButton(
                onClick = onRequestBatteryAccess,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text.allowBackgroundSync)
            }
            Spacer(Modifier.height(10.dp))
        }
        if (!keepAliveStarted) {
            OutlinedButton(
                onClick = onStartKeepAlive,
                enabled = hasSmsPermission && connectionReady,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
            ) {
                Icon(Icons.Default.Wifi, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text.startKeepAliveSync)
            }
            Spacer(Modifier.height(10.dp))
        }
        Button(
            onClick = onBackfill,
            enabled = hasSmsPermission && connectionReady,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text.pullLastDay)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onSync,
            enabled = connectionReady,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text.syncNow)
        }
    }
}

@Composable
private fun SettingsPanel(
    baseUrl: String,
    apiToken: String,
    deviceId: String,
    deviceName: String,
    language: String,
    tokenVisible: Boolean,
    onBaseUrlChange: (String) -> Unit,
    onApiTokenChange: (String) -> Unit,
    onDeviceIdChange: (String) -> Unit,
    onDeviceNameChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onToggleToken: () -> Unit,
    setupState: SetupState,
    setupError: String?,
    onSave: () -> Unit,
) {
    val text = LocalAppText.current
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Ui.blue)
            Spacer(Modifier.width(8.dp))
            Text(
                text = text.connectionSetup,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Ui.text,
            )
        }
        Spacer(Modifier.height(12.dp))
        SetupCheckPanel(setupState = setupState, setupError = setupError)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            label = { Text(text.serverUrl) },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = apiToken,
            onValueChange = onApiTokenChange,
            label = { Text(text.apiToken) },
            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onToggleToken) {
                    Icon(
                        imageVector = if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (tokenVisible) text.hideApiToken else text.showApiToken,
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = deviceName,
            onValueChange = onDeviceNameChange,
            label = { Text(text.deviceName) },
            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = deviceId,
            onValueChange = onDeviceIdChange,
            label = { Text(text.deviceId) },
            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = text.languageTitle,
            color = Ui.text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        LanguageSegment(
            language = language,
            onLanguageChange = onLanguageChange,
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
        ) {
            Text(if (setupState == SetupState.Checking) text.checking else text.checkAndSave)
        }
    }
}

@Composable
private fun SetupCheckPanel(setupState: SetupState, setupError: String?) {
    val text = LocalAppText.current
    val positive = setupState == SetupState.Valid
    val checking = setupState == SetupState.Checking
    val title = when (setupState) {
        SetupState.Missing -> text.setupPanelMissing
        SetupState.Checking -> text.setupPanelChecking
        SetupState.Valid -> text.setupPanelValid
        SetupState.Invalid -> text.setupPanelInvalid
    }
    val body = when (setupState) {
        SetupState.Missing -> text.setupPanelMissingBody
        SetupState.Checking -> text.setupPanelCheckingBody
        SetupState.Valid -> text.setupPanelValidBody
        SetupState.Invalid -> setupError ?: text.setupPanelInvalidBody
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = when {
            positive -> Ui.okSoft
            checking -> Ui.blueSoft
            else -> Ui.warnSoft
        },
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (positive) Icons.Default.CheckCircle else if (checking) Icons.Default.Wifi else Icons.Default.Warning,
                contentDescription = null,
                tint = if (positive) Ui.ok else if (checking) Ui.blue else Ui.warn,
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Ui.text, fontWeight = FontWeight.SemiBold)
                Text(body, color = Ui.muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun RoleSegment(role: String, onRoleChange: (String) -> Unit) {
    val text = LocalAppText.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Ui.chip)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegmentChoice(
            label = text.ownerMode,
            selected = role == "owner",
            modifier = Modifier.weight(1f),
            onClick = { onRoleChange("owner") },
        )
        SegmentChoice(
            label = text.collectorMode,
            selected = role == "collector",
            modifier = Modifier.weight(1f),
            onClick = { onRoleChange("collector") },
        )
    }
}

@Composable
private fun LanguageSegment(language: String, onLanguageChange: (String) -> Unit) {
    val text = LocalAppText.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Ui.chip)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegmentChoice(
            label = text.languageEnglish,
            selected = language != "th",
            modifier = Modifier.weight(1f),
            onClick = { onLanguageChange("en") },
        )
        SegmentChoice(
            label = text.languageThai,
            selected = language == "th",
            modifier = Modifier.weight(1f),
            onClick = { onLanguageChange("th") },
        )
    }
}

@Composable
private fun SegmentChoice(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Ui.surface else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Ui.blue else Ui.muted,
            )
        }
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Ui.surface),
        border = BorderStroke(1.dp, Ui.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun StatusLine(label: String, value: String, positive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (positive) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (positive) Ui.ok else Ui.warn,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(label, color = Ui.text, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text(value, color = Ui.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Surface(shape = RoundedCornerShape(999.dp), color = Ui.blue) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = Ui.surface,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NewMessagesDivider(count: Int) {
    val text = LocalAppText.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Ui.blueLine)
        Surface(
            modifier = Modifier.padding(horizontal = 10.dp),
            shape = RoundedCornerShape(999.dp),
            color = Ui.blueSoft,
        ) {
            Text(
                text = text.newMessages(count),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = Ui.blue,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = Ui.blueLine)
    }
}

@Composable
private fun EmptyState(title: String, body: String, actionLabel: String, onAction: () -> Unit) {
    Panel {
        Icon(Icons.Default.Sms, contentDescription = null, tint = Ui.blue, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Ui.text)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Ui.muted)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onAction, shape = RoundedCornerShape(22.dp)) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun Avatar(text: String, size: androidx.compose.ui.unit.Dp = 48.dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = Ui.blueSoft,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = avatarText(text),
                color = Ui.blue,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun DeviceTag(deviceId: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = Ui.bubbleDevice) {
        Text(
            text = deviceId,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            color = Ui.blue,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusChip(label: String, positive: Boolean) {
    Surface(shape = RoundedCornerShape(999.dp), color = if (positive) Ui.okSoft else Ui.warnSoft) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (positive) Ui.ok else Ui.warn),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (positive) Ui.ok else Ui.warn,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun buildThreads(
    messages: List<SmsRow>,
    query: String,
    readMessageKeys: Set<String>,
    unknownSender: String,
): List<SmsThread> {
    val normalizedQuery = query.trim().casefold()
    val filtered = if (normalizedQuery.isBlank()) {
        messages
    } else {
        messages.filter { row ->
            listOf(row.sender.orEmpty(), row.body, row.deviceId).any {
                it.casefold().contains(normalizedQuery)
            }
        }
    }

    return filtered
        .groupBy { "${it.deviceId}|${it.sender.orEmpty()}" }
        .map { (key, rows) ->
            val sorted = rows.sortedByDescending { it.receivedAtMs }
            val latest = sorted.first()
            val unreadCount = rows.count { messageKey(it) !in readMessageKeys }
            SmsThread(
                key = key,
                sender = latest.sender?.takeIf { it.isNotBlank() } ?: unknownSender,
                deviceId = latest.deviceId,
                latestBody = latest.body,
                latestAt = latest.receivedAt,
                latestAtMs = latest.receivedAtMs,
                count = rows.size,
                unreadCount = unreadCount,
                messages = sorted,
            )
        }
        .sortedByDescending { it.latestAtMs }
}

private fun mergeMessages(rows: List<SmsRow>): List<SmsRow> {
    return rows
        .distinctBy { "${it.deviceId}|${it.smsId}" }
        .sortedByDescending { it.receivedAtMs }
}

private fun messageKey(row: SmsRow): String = "${row.deviceId}|${row.smsId}"

private fun String.casefold(): String = lowercase()

private fun avatarText(value: String): String {
    return value.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

private fun normalizeBaseUrl(value: String): String {
    val trimmed = value.trim().trimEnd('/')
    if (trimmed.isBlank()) return trimmed
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "http://$trimmed"
    }
}

private fun formatThreadTime(value: Long): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(value).atZone(zone)
    val today = LocalDate.now(zone)
    return if (dateTime.toLocalDate() == today) {
        DateTimeFormatter.ofPattern("HH:mm").format(dateTime)
    } else {
        DateTimeFormatter.ofPattern("dd MMM").format(dateTime)
    }
}

private fun formatTimestamp(value: String): String {
    return runCatching {
        val dateTime = OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").format(dateTime)
    }.getOrDefault(value)
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun canPostNotifications(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

private fun ensureOwnerNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val text = AppText.from(SettingsStore(context).language)
    val manager = context.getSystemService(NotificationManager::class.java)
    val channel = NotificationChannel(
        OWNER_SMS_CHANNEL_ID,
        text.ownerSmsChannelName,
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = text.ownerSmsChannelDescription
        setSound(null, null)
        enableVibration(false)
        setShowBadge(true)
    }
    manager.createNotificationChannel(channel)
}

private fun notifyFreshOwnerMessages(context: Context, rows: List<SmsRow>) {
    if (!canPostNotifications(context)) return
    ensureOwnerNotificationChannel(context)
    val text = AppText.from(SettingsStore(context).language)

    val now = System.currentTimeMillis()
    val freshRows = rows
        .filter {
            val age = now - it.receivedAtMs
            age >= -60_000L && age <= FRESH_NOTIFICATION_WINDOW_MS
        }
        .take(5)
    if (freshRows.isEmpty()) return

    val openIntent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val manager = NotificationManagerCompat.from(context)

    freshRows.forEach { row ->
        val sender = row.sender?.takeIf { it.isNotBlank() } ?: text.unknownSender
        val title = sender
        val details = "${text.fromDevicePrefix} ${row.deviceId}"
        val notification = NotificationCompat.Builder(context, OWNER_SMS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sms_notification)
            .setContentTitle(title)
            .setContentText(row.body)
            .setSubText(details)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("$sender • ${row.deviceId}")
                    .setSummaryText(details)
                    .bigText(row.body),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(false)
            .setGroup(OWNER_SMS_GROUP)
            .build()

        runCatching {
            manager.notify(10_000 + row.id, notification)
        }
    }

    if (freshRows.size > 1) {
        val summary = freshRows.fold(NotificationCompat.InboxStyle()) { style, row ->
            val sender = row.sender?.takeIf { it.isNotBlank() } ?: text.unknownSender
            style.addLine("$sender: ${row.body}")
        }
        val notification = NotificationCompat.Builder(context, OWNER_SMS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sms_notification)
            .setContentTitle(text.newSmsTitle(freshRows.size))
            .setContentText(text.readSmsReceived)
            .setStyle(summary)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setGroup(OWNER_SMS_GROUP)
            .setGroupSummary(true)
            .build()

        runCatching {
            manager.notify(9_999, notification)
        }
    }
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    val packageUri = Uri.parse("package:${context.packageName}")
    val requestIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri)
    val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
    val intent = if (requestIntent.resolveActivity(context.packageManager) != null) {
        requestIntent
    } else {
        fallbackIntent
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:${context.packageName}"),
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
