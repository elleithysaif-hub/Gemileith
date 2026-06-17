package com.gemileith.launcher

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gemileith.core.LauncherState
import com.gemileith.launcher.LauncherScreen
import com.gemileith.launcher.LauncherViewModel
import com.gemileith.launcher.ui.theme.MyApplicationTheme
import com.gemileith.launcher.ui.theme.SemanticCyan
import com.gemileith.launcher.ui.theme.SemanticMagenta
import com.gemileith.launcher.ui.theme.SemanticAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.material.icons.outlined.Notifications

import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.filled.Notifications

@Composable
fun Greeting(name: String) {
  Text(
    text = "Hello $name!",
    color = MaterialTheme.colorScheme.onBackground,
    fontSize = 24.sp,
    fontWeight = FontWeight.Medium
  )
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel = viewModel<SystemViewModel>()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val launcherViewModel = remember { LauncherViewModel() }
      val launcherState by launcherViewModel.uiState
      var currentTab by remember { mutableStateOf("HOME") }

      MyApplicationTheme(darkTheme = uiState.isDarkMode) {
        val context = LocalContext.current
        
        LaunchedEffect(uiState.memoryUsagePercent, uiState.cpuHistory.lastOrNull()) {
            val cpu = uiState.cpuHistory.lastOrNull() ?: 0f
            val ram = uiState.memoryUsagePercent
            if (cpu > 0.9f || ram > 0.9f) {
                android.widget.Toast.makeText(context, "Warning: System usage spike detected (>90%)", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        LaunchedEffect(uiState.refreshRateMs) {
            while(true) {
                delay(uiState.refreshRateMs)
                viewModel.updateMemory()
            }
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = { BottomNavigation(currentTab) { currentTab = it } }
        ) { innerPadding ->
          Column(
            modifier = Modifier
              .padding(innerPadding)
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
          ) {
            TopHeader(
                uiState = uiState, 
                onDismissNotifications = { viewModel.dismissNotifications() },
                onToggleRefreshRate = { 
                    viewModel.toggleRefreshRate() 
                    android.widget.Toast.makeText(context, "Refresh rate set", android.widget.Toast.LENGTH_SHORT).show()
                },
                onExportPdf = {
                    android.widget.Toast.makeText(context, "Dashboard exported to PDF", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val duration = 400
                    fadeIn(animationSpec = tween(duration, easing = FastOutSlowInEasing)) + slideInHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { it / 4 } togetherWith
                    fadeOut(animationSpec = tween(duration, easing = FastOutSlowInEasing)) + slideOutHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { -it / 4 }
                },
                label = "tab_transition",
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                when (targetTab) {
                    "HOME" -> SystemControlScreen(uiState, onVolumeChange = { viewModel.setVolume(it) }, modifier = Modifier.fillMaxSize())
                    "TOOLS" -> LauncherScreen(
                        state = launcherState,
                        onQueryChanged = { launcherViewModel.onQueryChanged(it) },
                        onAppSelected = { launcherViewModel.launchApp(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                    "CORE" -> CoreSettingsScreen(
                        uiState, 
                        onToggleWifi = { 
                            viewModel.toggleWifi()
                            android.widget.Toast.makeText(context, "Wi-Fi settings updated", android.widget.Toast.LENGTH_SHORT).show()
                        }, 
                        onToggleBluetooth = { 
                            viewModel.toggleBluetooth()
                            android.widget.Toast.makeText(context, "Bluetooth settings updated", android.widget.Toast.LENGTH_SHORT).show()
                        }, 
                        onToggleDarkMode = { 
                            viewModel.toggleDarkMode()
                            android.widget.Toast.makeText(context, "Theme updated", android.widget.Toast.LENGTH_SHORT).show()
                        }, 
                        modifier = Modifier.fillMaxSize()
                    )
                    "STATS" -> SystemMonitorScreen(uiState, modifier = Modifier.fillMaxSize())
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Coming Soon in Gemileith OS", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AppDrawerScreen(uiState: SystemState, onLaunch: (String) -> Unit, onSearch: (String) -> Unit, modifier: Modifier = Modifier) {
    if (uiState.isLoadingApps) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search Apps...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )
            
            val filteredApps = uiState.installedApps.filter { 
                it.name.contains(uiState.searchQuery, ignoreCase = true) 
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(filteredApps) { app ->
                    LiquidTouchRippleBox(
                        onClick = { onLaunch(app.packageName) },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(app.icon)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = app.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = app.name,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemControlScreen(uiState: SystemState, onVolumeChange: (Float) -> Unit, modifier: Modifier = Modifier) {
  val scrollState = rememberScrollState()
  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(top = 8.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    OptimumCard(uiState.memoryUsagePercent)
    VolumeControlSlider(uiState.volumeLevel, onVolumeChange)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      val cardModifier = Modifier.width(160.dp)
      LiquidTouchRippleBox(onClick = { }, modifier = cardModifier) {
          CrescentWidget(
            title = "RAM",
            value = String.format("%.1f", uiState.memoryUsedGb),
            unit = "GB",
            progress = uiState.memoryUsagePercent,
            icon = Icons.Outlined.Memory,
            color = SemanticCyan
          )
      }
      LiquidTouchRippleBox(onClick = { }, modifier = cardModifier) {
          CrescentWidget(
            title = "CPU",
            value = "${(uiState.cpuHistory.lastOrNull()?.times(100) ?: 0f).toInt()}",
            unit = "%",
            progress = uiState.cpuHistory.lastOrNull() ?: 0f,
            icon = Icons.Outlined.Thermostat,
            color = SemanticAmber
          )
      }
      LiquidTouchRippleBox(onClick = { }, modifier = cardModifier) {
          CrescentWidget(
            title = "Disk",
            value = String.format("%.1f", uiState.diskUsedGb),
            unit = "GB",
            progress = uiState.diskUsagePercent,
            icon = Icons.Outlined.Folder,
            color = SemanticCyan
          )
      }
      LiquidTouchRippleBox(onClick = { }, modifier = cardModifier) {
          CrescentWidget(
            title = if (uiState.isCharging) "Charging" else "Battery",
            value = "${uiState.batteryLevel}",
            unit = "%",
            progress = uiState.batteryLevel / 100f,
            icon = if (uiState.isCharging) Icons.Outlined.Bolt else Icons.Outlined.BatterySaver,
            color = if (uiState.isCharging) SemanticCyan else SemanticMagenta
          )
      }
    }
    SystemMonitorChart(uiState.ramHistory, uiState.cpuHistory)
    PerformanceProfiles(uiState.isCharging)
  }
}

@Composable
fun TopHeader(uiState: SystemState, onDismissNotifications: () -> Unit, onToggleRefreshRate: () -> Unit, onExportPdf: () -> Unit) {
  val breathingOpacity = rememberBreathingEngineOpacity()
  var currentTime by remember { mutableStateOf("") }
  var currentDate by remember { mutableStateOf("") }
  var expandedNotifications by remember { mutableStateOf(false) }
  
  LaunchedEffect(Unit) {
      val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
      val dateFormat = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
      while(true) {
          val now = java.util.Date()
          currentTime = timeFormat.format(now)
          currentDate = dateFormat.format(now)
          delay(1000)
      }
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background.copy(alpha = breathingOpacity))
      .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "GEMILEITH OS",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 2.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "System Control",
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        IconButton(onClick = onExportPdf) {
            Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Export PDF", tint = MaterialTheme.colorScheme.onBackground)
        }
        
        TextButton(onClick = onToggleRefreshRate) {
            Text("${uiState.refreshRateMs / 1000}s", color = SemanticCyan, fontWeight = FontWeight.Bold)
        }

        Box {
            val unreadCount = uiState.notifications.count { !it.isRead }
            IconButton(onClick = { expandedNotifications = true }) {
                DynamicIcon(
                    icon = if (unreadCount > 0) Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications,
                    hasNotification = unreadCount > 0,
                    tint = if (unreadCount > 0) SemanticCyan else MaterialTheme.colorScheme.onBackground
                )
            }
            DropdownMenu(
                expanded = expandedNotifications,
                onDismissRequest = { 
                    expandedNotifications = false
                    onDismissNotifications()
                },
                modifier = Modifier
                    .width(300.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Text("Recent Events", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                if (uiState.notifications.isEmpty()) {
                    Text("No new events", fontSize = 12.sp, modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    uiState.notifications.forEach { note ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(note.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (note.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                                    Text(note.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(note.time, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            },
                            onClick = { 
                                expandedNotifications = false 
                                onDismissNotifications()
                            }
                        )
                    }
                }
            }
        }

        NetworkIndicator(uiState.isWifiEnabled)
        
        Column(horizontalAlignment = Alignment.End) {
            Text(currentTime, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(currentDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
  }
}

@Composable
fun NetworkIndicator(isWifiEnabled: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isWifiEnabled) {
            Icon(Icons.Outlined.Wifi, contentDescription = "Wi-Fi", tint = SemanticCyan, modifier = Modifier.size(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(12.dp)) {
                repeat(4) { 
                    Box(modifier = Modifier.width(3.dp).height(((it + 1) * 3).dp).background(SemanticCyan.copy(alpha = if (it < 3) 1f else 0.3f), RoundedCornerShape(1.dp)))
                }
            }
        } else {
            Icon(Icons.Filled.Settings, contentDescription = "Ethernet", tint = SemanticMagenta, modifier = Modifier.size(16.dp))
            Text("ETH", fontSize = 10.sp, color = SemanticMagenta, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun rememberBreathingEngineOpacity(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_engine")
    val opacity by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_opacity"
    )
    return opacity
}

@Composable
fun DynamicIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, hasNotification: Boolean, tint: Color, modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "dynamic_icon")
  val scale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (hasNotification) 1.25f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "icon_scale"
  )
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.scale(scale)
    )
  }
}

@Composable
fun CrescentWidget(
    title: String,
    value: String,
    unit: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "crescent"
    )

    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(1000),
        label = "crescent_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(targetState = icon, label = "icon_anim") { targetIcon -> 
                Icon(targetIcon, contentDescription = null, tint = animatedColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedContent(targetState = title, label = "title_anim") { targetTitle ->
                Text(targetTitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
            val outlineVariant = MaterialTheme.colorScheme.outlineVariant
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = outlineVariant,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = animatedColor,
                    startAngle = 135f,
                    sweepAngle = (270f * animatedProgress).coerceAtLeast(1f),
                    useCenter = false,
                    style = stroke
                )
            }
            AnimatedContent(
                targetState = value to unit,
                transitionSpec = {
                    val duration = 400
                    fadeIn(animationSpec = tween(duration)) + slideInVertically(animationSpec = tween(duration)) { height -> height/2 } togetherWith
                            fadeOut(animationSpec = tween(duration)) + slideOutVertically(animationSpec = tween(duration)) { height -> -height/2 }
                },
                label = "value_animation"
            ) { target ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(target.first, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(target.second, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun LiquidTouchRippleBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var rippleCenter by remember { mutableStateOf<Offset?>(null) }
    var rippleColor by remember { mutableStateOf(SemanticCyan) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pressure = down.pressure
                    val color = if (pressure > 0.6f) SemanticMagenta else SemanticCyan
                    
                    if (pressure > 0.6f) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    rippleCenter = down.position
                    rippleColor = color
                    
                    scope.launch {
                        rippleAlpha.snapTo(0.6f)
                        rippleRadius.snapTo(0f)
                        launch { rippleRadius.animateTo(1000f, tween(600, easing = LinearOutSlowInEasing)) }
                        launch { rippleAlpha.animateTo(0f, tween(600)) }
                    }
                    
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        onClick()
                    }
                }
            }
            .clipToBounds()
    ) {
        content()
        if (rippleCenter != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = rippleColor.copy(alpha = rippleAlpha.value),
                    radius = rippleRadius.value,
                    center = rippleCenter!!
                )
            }
        }
    }
}

@Composable
fun OptimumCard(memUsage: Float) {
  val optScore = 1f - (memUsage * 0.5f) // Simple fake score formulation based on RAM 
  
  val statusColor = when {
    optScore > 0.7f -> SemanticCyan
    optScore > 0.4f -> SemanticMagenta
    else -> SemanticAmber
  }
  
  val statusText = when {
    optScore > 0.7f -> "STABLE"
    optScore > 0.4f -> "INTENSE"
    else -> "WARNING"
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
      .padding(24.dp)
  ) {
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .background(statusColor, RoundedCornerShape(16.dp))
        .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
      Text(
        text = statusText,
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.sp
      )
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier.size(144.dp),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          progress = { 1f },
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.outlineVariant,
          strokeWidth = 8.dp,
          strokeCap = StrokeCap.Round
        )
        CircularProgressIndicator(
          progress = { optScore },
          modifier = Modifier.fillMaxSize().rotate(180f),
          color = statusColor,
          strokeWidth = 8.dp,
          strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "${(optScore * 100).toInt()}%", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
          Text(
            text = "HEALTH",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      LiquidTouchRippleBox(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
      ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("RUN SYSTEM TUNE-UP", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
          }
      }
    }
  }
}

@Composable
fun PerformanceProfiles(isCharging: Boolean) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Performance Profiles", fontSize = 14.sp, fontWeight = FontWeight.Medium)
      Text("System Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    
    Column(modifier = Modifier.padding(8.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (isCharging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent, RoundedCornerShape(16.dp))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Outlined.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Text("Extreme Performance", fontSize = 14.sp)
        }
        Switch(
          checked = isCharging, 
          onCheckedChange = null,
          colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.secondary)
        )
      }
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (!isCharging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent, RoundedCornerShape(16.dp))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Outlined.BatterySaver, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Text("Battery Saver Plus", fontSize = 14.sp)
        }
        Switch(
          checked = !isCharging, 
          onCheckedChange = null,
          colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White, 
              checkedTrackColor = MaterialTheme.colorScheme.secondary,
              uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, 
              uncheckedTrackColor = MaterialTheme.colorScheme.outline
          )
        )
      }
    }
  }
}

@Composable
fun BottomNavigation(currentTab: String, onTabSelect: (String) -> Unit) {
  Column {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      NavItem(
        icon = Icons.Filled.Home, 
        label = "HOME", 
        isSelected = currentTab == "HOME",
        onClick = { onTabSelect("HOME") }
      )
      NavItem(
        icon = Icons.Outlined.QueryStats, 
        label = "STATS", 
        isSelected = currentTab == "STATS",
        onClick = { onTabSelect("STATS") }
      )
      NavItem(
        icon = Icons.Outlined.Build, 
        label = "TOOLS", 
        isSelected = currentTab == "TOOLS",
        onClick = { onTabSelect("TOOLS") }
      )
      NavItem(
        icon = Icons.Filled.Settings, 
        label = "CORE", 
        isSelected = currentTab == "CORE",
        onClick = { onTabSelect("CORE") }
      )
    }
  }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    label: String, 
    isSelected: Boolean,
    onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(56.dp).clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .height(32.dp)
        .width(56.dp)
        .background(
          color = if (isSelected) Color(0xFF004A77) else Color.Transparent,
          shape = RoundedCornerShape(16.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      letterSpacing = (-0.5).sp
    )
  }
}

@Composable
fun VolumeControlSlider(volumeLevel: Float, onVolumeChange: (Float) -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
      .padding(20.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("System Volume", fontSize = 14.sp)
      }
      Text("${(volumeLevel * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
    Spacer(modifier = Modifier.height(16.dp))
    Slider(
      value = volumeLevel,
      onValueChange = onVolumeChange,
      modifier = Modifier.fillMaxWidth(),
      colors = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
      )
    )
  }
}

@Composable
fun SystemMonitorChart(ramHistory: List<Float>, cpuHistory: List<Float>) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
      .padding(top = 20.dp, bottom = 24.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Real-time Monitor", fontSize = 14.sp, fontWeight = FontWeight.Medium)
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(6.dp).background(primaryColor, CircleShape))
          Spacer(modifier = Modifier.width(4.dp))
          Text("RAM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(6.dp).background(secondaryColor, CircleShape))
          Spacer(modifier = Modifier.width(4.dp))
          Text("CPU", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .padding(horizontal = 20.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        fun drawLineChart(data: List<Float>, color: Color) {
          if (data.isEmpty()) return
          val path = Path()
          val stepX = width / (data.size - 1).coerceAtLeast(1)
          
          data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value * height)
            if (index == 0) {
              path.moveTo(x, y)
            } else {
              path.lineTo(x, y)
            }
          }
          drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
          )
        }
        
        drawLineChart(ramHistory, primaryColor)
        drawLineChart(cpuHistory, secondaryColor)
      }
    }
  }
}

@Composable
fun CoreSettingsScreen(uiState: SystemState, onToggleWifi: () -> Unit, onToggleBluetooth: () -> Unit, onToggleDarkMode: () -> Unit, modifier: Modifier = Modifier) {
  val scrollState = rememberScrollState()
  val context = LocalContext.current
  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(top = 8.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    NetworkSettingsCard(
        isWifiEnabled = uiState.isWifiEnabled,
        isBluetoothEnabled = uiState.isBluetoothEnabled,
        isDarkMode = uiState.isDarkMode,
        onToggleWifi = { 
            onToggleWifi()
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { context.startActivity(intent) } catch (e: Exception) { }
        },
        onToggleBluetooth = { 
            onToggleBluetooth()
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { context.startActivity(intent) } catch (e: Exception) { }
        },
        onToggleDarkMode = onToggleDarkMode
    )
  }
}

@Composable
fun NetworkSettingsCard(isWifiEnabled: Boolean, isBluetoothEnabled: Boolean, isDarkMode: Boolean, onToggleWifi: () -> Unit, onToggleBluetooth: () -> Unit, onToggleDarkMode: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("System Configuration", fontSize = 14.sp, fontWeight = FontWeight.Medium)
      Text("Active", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    
    Column(modifier = Modifier.padding(8.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (isWifiEnabled) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent, RoundedCornerShape(16.dp))
          .clickable { onToggleWifi() }
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Outlined.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Wi-Fi", fontSize = 14.sp)
            Text(if (isWifiEnabled) "Connected" else "Off", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isWifiEnabled) {
                // Connection strength indicator (mock dots)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(end = 12.dp)) {
                    repeat(3) { Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape)) }
                    Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.outline, CircleShape))
                }
            }
            Switch(
            checked = isWifiEnabled, 
            onCheckedChange = { onToggleWifi() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        }
      }
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (isBluetoothEnabled) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent, RoundedCornerShape(16.dp))
          .clickable { onToggleBluetooth() }
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Outlined.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Bluetooth", fontSize = 14.sp)
            Text(if (isBluetoothEnabled) "Discoverable" else "Off", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        Switch(
          checked = isBluetoothEnabled, 
          onCheckedChange = { onToggleBluetooth() },
          colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
      }
      
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent, RoundedCornerShape(16.dp))
          .clickable { onToggleDarkMode() }
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Dark Mode", fontSize = 14.sp)
            Text("System Theme", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        Switch(
          checked = isDarkMode, 
          onCheckedChange = { onToggleDarkMode() },
          colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
      }
    }
  }
}

@Composable
fun SystemMonitorScreen(uiState: SystemState, modifier: Modifier = Modifier) {
  val scrollState = rememberScrollState()
  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(top = 8.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    SystemMonitorChart(uiState.ramHistory, uiState.cpuHistory)
    
    // Storage Info Box
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        .padding(20.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Outlined.Folder, contentDescription = "Storage", tint = SemanticCyan)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Internal Storage", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Text(String.format("%.1f GB / %.1f GB", uiState.diskUsedGb, uiState.diskTotalGb), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Spacer(modifier = Modifier.height(16.dp))
      Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(4.dp))) {
          Box(modifier = Modifier.fillMaxWidth(uiState.diskUsagePercent.coerceIn(0f, 1f)).height(8.dp).background(SemanticCyan, RoundedCornerShape(4.dp)))
      }
    }
  }
}
