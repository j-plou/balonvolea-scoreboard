package com.volley.scoreboard

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas
import kotlin.random.Random

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.volley.scoreboard.ui.theme.ColorWhite
import com.volley.scoreboard.ui.theme.OrangePrimary
import com.volley.scoreboard.ui.theme.PurplePrimary
import com.volley.scoreboard.ui.theme.ScoreboardTheme
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.math.max

class MainActivity : ComponentActivity() {
    private val viewModel: ScoreboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel.initSoundPlayer(this)
        
        setContent {
            ScoreboardTheme {
                ScoreboardApp(viewModel = viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (viewModel.isVolumeControlActive()) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    viewModel.incrementPoints(isLocal = true)
                    return true
                }

                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    viewModel.incrementPoints(isLocal = false)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (viewModel.isVolumeControlActive() &&
            (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        ) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}

private fun getOrdinalText(number: Int): String {
    return when (number) {
        1 -> "Primer"
        2 -> "Segundo"
        3 -> "Tercer"
        4 -> "Cuarto"
        5 -> "Quinto"
        else -> "${number}º"
    }
}

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val color: Color,
    val size: Float
)

@Composable
fun CustomConfettiEffect(
    isActive: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val particles = remember { mutableStateOf(listOf<ConfettiParticle>()) }
    val startTime = remember { mutableStateOf(0L) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            startTime.value = System.currentTimeMillis()
            // Crear partículas iniciales - explosión masiva
            val newParticles = mutableListOf<ConfettiParticle>()
            val colors = listOf(primaryColor, Color.White, primaryColor.copy(alpha = 0.7f), Color(0xFFFFD700))
            
            repeat(300) {
                val angle = Random.nextFloat() * 2 * Math.PI
                val speed = Random.nextFloat() * 20f + 8f
                newParticles.add(
                    ConfettiParticle(
                        x = 0.5f,
                        y = 1.0f,
                        velocityX = (Math.cos(angle) * speed).toFloat(),
                        velocityY = -(Math.sin(angle) * speed).toFloat() - 15f,
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = Random.nextFloat() * 15f - 7.5f,
                        color = colors.random(),
                        size = Random.nextFloat() * 10f + 5f
                    )
                )
            }
            particles.value = newParticles
            
            // Animación
            while (isActive) {
                delay(16) // ~60fps
                val elapsed = (System.currentTimeMillis() - startTime.value) / 1000f
                
                particles.value = particles.value.map { p ->
                    p.copy(
                        x = p.x + p.velocityX * 0.016f,
                        y = p.y + p.velocityY * 0.016f,
                        velocityY = p.velocityY + 0.5f, // gravedad
                        rotation = p.rotation + p.rotationSpeed
                    )
                }.filter { it.y < 1.5f } // Eliminar partículas que salieron de pantalla
                
                // Añadir muchas más partículas desde arriba (lluvia intensa)
                if (elapsed < 10f && Random.nextFloat() < 0.8f) {
                    repeat(3) {
                        val newParticle = ConfettiParticle(
                            x = Random.nextFloat(),
                            y = -0.1f,
                            velocityX = Random.nextFloat() * 3f - 1.5f,
                            velocityY = Random.nextFloat() * 3f + 2f,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 15f - 7.5f,
                            color = colors.random(),
                            size = Random.nextFloat() * 10f + 5f
                        )
                        particles.value = particles.value + newParticle
                    }
                }
            }
        } else {
            particles.value = emptyList()
        }
    }
    
    Canvas(modifier = modifier) {
        particles.value.forEach { particle ->
            val px = particle.x * size.width
            val py = particle.y * size.height
            
            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(px, py),
                alpha = 0.9f
            )
        }
    }
}

class CelebrationSoundPlayer(private val context: Context) {
    private var popPlayer: MediaPlayer? = null
    private var celebrationPlayer: MediaPlayer? = null
    
    fun play() {
        stop()
        try {
            // 1. Pop de cartoon - inmediato
            popPlayer = MediaPlayer.create(context, R.raw.cartoon_pop)
            popPlayer?.setOnCompletionListener { 
                it.release()
                popPlayer = null
            }
            popPlayer?.start()
            
            // 2. Celebración femenina - después de 100ms
            celebrationPlayer = MediaPlayer.create(context, R.raw.female_celebration)
            celebrationPlayer?.setOnCompletionListener { 
                it.release()
                celebrationPlayer = null
            }
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                celebrationPlayer?.start()
            }, 100)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stop() {
        popPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        popPlayer = null
        
        celebrationPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        celebrationPlayer = null
    }
}

@Composable
fun PointBubble(
    number: Int,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = color,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(1f, 1f),
                    blurRadius = 2f
                )
            )
        )
    }
}

@Composable
fun ScoreboardApp(viewModel: ScoreboardViewModel) {
    val state = viewModel.uiState
    
    LaunchedEffect(state.showTimelineHistory, state.timelineOpenedBySetWin) {
        if (state.showTimelineHistory && 
            state.timelineOpenedBySetWin && 
            state.isMatchFinished()) {
            viewModel.playCelebrationSound()
        }
    }
    
    ScoreboardScreen(
        state = state,
        onIncrementPoint = { viewModel.incrementPoints(it) },
        onDecrementPoint = { viewModel.decrementPoints(it) },
        onResetAll = { viewModel.resetAll() },
        onOpenTeamEditor = { viewModel.openTeamEditor(it) },
        onCloseTeamEditor = { viewModel.closeTeamEditor() },
        onUpdateTeam = { side, name, color -> viewModel.updateTeam(side, name, color) },
        onToggleSettings = { viewModel.toggleSettings(it) },
        onToggleVolumeControl = { viewModel.setVolumeControl(it) },
        onToggleCelebrationSound = { viewModel.setCelebrationSound(it) },
        onSelectTotalSets = { viewModel.updateTotalSets(it) },
        onContinueAfterSetWin = { viewModel.continueAfterSetWin() },
        onToggleTimelineHistory = { viewModel.toggleTimelineHistory(it) }
    )
}

@Composable
fun ScoreboardScreen(
    state: ScoreboardState,
    onIncrementPoint: (Boolean) -> Unit,
    onDecrementPoint: (Boolean) -> Unit,
    onResetAll: () -> Unit,
    onOpenTeamEditor: (Boolean) -> Unit,
    onCloseTeamEditor: () -> Unit,
    onUpdateTeam: (Boolean, String, Color) -> Unit,
    onToggleSettings: (Boolean) -> Unit,
    onToggleVolumeControl: (Boolean) -> Unit,
    onToggleCelebrationSound: (Boolean) -> Unit,
    onSelectTotalSets: (Int) -> Unit,
    onContinueAfterSetWin: () -> Unit,
    onToggleTimelineHistory: (Boolean) -> Unit
) {
    val matchFinished = state.isMatchFinished()
    val currentSetNumber = state.local.sets + state.visitor.sets + 1
    val localSetPoint = isSetPoint(state.local.points, state.visitor.points, currentSetNumber, state.totalSets)
    val visitorSetPoint = isSetPoint(state.visitor.points, state.local.points, currentSetNumber, state.totalSets)
    val localMatchPoint = localSetPoint && state.local.sets == setsToWin(state.totalSets) - 1
    val visitorMatchPoint = visitorSetPoint && state.visitor.sets == setsToWin(state.totalSets) - 1

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            TeamHalf(
                team = state.local,
                matchPoint = localMatchPoint,
                matchFinished = matchFinished,
                isLocal = true,
                onEditTeam = { onOpenTeamEditor(true) },
                onSwipeUp = { onIncrementPoint(true) },
                onSwipeDown = { onDecrementPoint(true) },
                modifier = Modifier.weight(1f)
            )
            TeamHalf(
                team = state.visitor,
                matchPoint = visitorMatchPoint,
                matchFinished = matchFinished,
                isLocal = false,
                onEditTeam = { onOpenTeamEditor(false) },
                onSwipeUp = { onIncrementPoint(false) },
                onSwipeDown = { onDecrementPoint(false) },
                modifier = Modifier.weight(1f)
            )
        }

        SetsBar(
            localSets = state.local.sets,
            visitorSets = state.visitor.sets,
            setHistory = state.setHistory,
            onOpenTimeline = { onToggleTimelineHistory(true) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        TopActions(
            onResetAll = onResetAll,
            onOpenSettings = { onToggleSettings(true) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
        )
    }

    if (state.isSettingsOpen) {
        SettingsDialog(
            volumeControlEnabled = state.volumeControlEnabled,
            celebrationSoundEnabled = state.celebrationSoundEnabled,
            totalSets = state.totalSets,
            hasMatchInProgress = state.local.points > 0 || 
                                 state.visitor.points > 0 || 
                                 state.local.sets > 0 || 
                                 state.visitor.sets > 0 ||
                                 state.setHistory.isNotEmpty(),
            onDismiss = { onToggleSettings(false) },
            onVolumeControlChange = onToggleVolumeControl,
            onCelebrationSoundChange = onToggleCelebrationSound,
            onTotalSetsChange = onSelectTotalSets
        )
    }

    state.editingTeamSide?.let { side ->
        val team = if (side) state.local else state.visitor
        TeamEditDialog(
            initialName = team.name,
            initialColor = team.color,
            onDismiss = onCloseTeamEditor,
            onSave = { name, color -> onUpdateTeam(side, name, color) }
        )
    }

    if (state.showTimelineHistory) {
        SetHistoryTimelineDialog(
            state = state,
            onDismiss = onContinueAfterSetWin
        )
    }
}

@Composable
fun TopActions(
    onResetAll: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(32.dp)
                .padding(bottom = 4.dp)
        ) {
            IconButton(
                onClick = onResetAll,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset general",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.size(32.dp)
        ) {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TeamHalf(
    team: TeamState,
    matchPoint: Boolean,
    matchFinished: Boolean,
    isLocal: Boolean,
    onEditTeam: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableStateOf(0f) }
    var gestureTriggered by remember { mutableStateOf(false) }
    val draggableState = rememberDraggableState { delta ->
        if (gestureTriggered) return@rememberDraggableState
        dragOffset += delta
        if (dragOffset <= -40f) {
            onSwipeUp()
            gestureTriggered = true
        } else if (dragOffset >= 40f) {
            onSwipeDown()
            gestureTriggered = true
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(team.color)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                enabled = !matchFinished,
                onDragStopped = {
                    dragOffset = 0f
                    gestureTriggered = false
                }
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp)
                .zIndex(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onEditTeam
                )
        ) {
            Text(
                text = team.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.75f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = team.color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            FlipCounter(
                value = team.points,
                background = team.color,
                matchPoint = matchPoint,
                streak = team.streak,
                isLocal = isLocal,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StreakIndicator(
    streak: Int,
    isLeftAligned: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = streak >= 3,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.5f),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.5f),
        modifier = modifier
    ) {
        val shakeDuration = when {
            streak >= 10 -> 150
            streak >= 8 -> 250
            streak >= 6 -> 350
            streak >= 4 -> 500
            else -> 650
        }
        
        val shakeAmplitude = when {
            streak >= 8 -> 5f
            streak >= 5 -> 4f
            else -> 3f
        }
        
        val infiniteTransition = rememberInfiniteTransition(label = "shake_$streak")
        val shakeRotation by infiniteTransition.animateFloat(
            initialValue = -shakeAmplitude,
            targetValue = shakeAmplitude,
            animationSpec = infiniteRepeatable(
                animation = tween(shakeDuration / 2, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake_rotation"
        )
        
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(shakeDuration, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .scale(scale)
                .rotate(shakeRotation)
                .background(
                    color = Color(0xFFEF4444).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+$streak",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun FlipCounter(
    value: Int,
    background: Color,
    matchPoint: Boolean,
    streak: Int = 0,
    isLocal: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColor = Color(0xFFF7F7F7)
    Surface(
        color = cardColor,
        contentColor = Color.Black,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth(fraction = 0.95f)
            .aspectRatio(1.05f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            HorizontalDivider(
                color = Color.Black.copy(alpha = 0.16f),
                thickness = 1.5.dp,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .align(Alignment.Center)
            )
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    val direction = if (targetState > initialState) -1 else 1
                    (slideInVertically(animationSpec = tween(180)) { fullHeight -> fullHeight / 2 * direction } + fadeIn(animationSpec = tween(180))) togetherWith
                            (slideOutVertically(animationSpec = tween(180)) { fullHeight -> -fullHeight / 2 * direction } + fadeOut(animationSpec = tween(180)))
                },
                label = "counter"
            ) { target ->
                val textColor = background
                val (rotation, scale) = animateShake(matchPoint)
                val animatedRotation by rotation
                val animatedScale by scale
                Text(
                    text = target.toString().padStart(2, '0'),
                    modifier = Modifier
                        .scale(animatedScale)
                        .rotate(animatedRotation),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 220.sp,
                        letterSpacing = 1.5.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.75f),
                            offset = Offset(3f, 3f),
                            blurRadius = 5f
                        )
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
            
            StreakIndicator(
                streak = streak,
                isLeftAligned = isLocal,
                modifier = Modifier
                    .align(if (isLocal) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun animateShake(active: Boolean): Pair<androidx.compose.runtime.State<Float>, androidx.compose.runtime.State<Float>> {
    val shakeDuration = 250
    val shakeAmplitude = 5f
    
    val transition = rememberInfiniteTransition(label = "shakeTransition")
    
    val rotation = transition.animateFloat(
        initialValue = if (active) -shakeAmplitude else 0f,
        targetValue = if (active) shakeAmplitude else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shakeDuration / 2, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_rotation"
    )
    
    val scale = transition.animateFloat(
        initialValue = 1f,
        targetValue = if (active) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shakeDuration, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_scale"
    )
    
    return Pair(rotation, scale)
}

@Composable
fun SetsBar(
    localSets: Int,
    visitorSets: Int,
    setHistory: List<SetResult>,
    onOpenTimeline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ColorWhite,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .wrapContentWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOpenTimeline
            )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = localSets.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color.LightGray.copy(alpha = 0.8f))
                )
                Text(
                    text = visitorSets.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            
            if (setHistory.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(1.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    setHistory.forEach { result ->
                        Row(
                            modifier = Modifier.width(90.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.localPoints.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                modifier = Modifier.weight(1f),
                                color = Color.Gray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "-",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                                color = Color.LightGray,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            Text(
                                text = result.visitorPoints.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                modifier = Modifier.weight(1f),
                                color = Color.Gray,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetTimelineItem(
    setResult: SetResult,
    localColor: Color,
    visitorColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${getOrdinalText(setResult.setNumber)} set: ${setResult.localPoints} - ${setResult.visitorPoints}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val totalPoints = setResult.pointSequence.size
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        var localPointCount = 0
                        for (i in 0 until totalPoints) {
                            if (i > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (setResult.pointSequence[i] == true) {
                                localPointCount++
                                PointBubble(
                                    number = localPointCount,
                                    color = localColor
                                )
                            } else {
                                Spacer(modifier = Modifier.width(28.dp))
                            }
                        }
                    }
                    
                    Row {
                        var visitorPointCount = 0
                        for (i in 0 until totalPoints) {
                            if (i > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (setResult.pointSequence[i] == false) {
                                visitorPointCount++
                                PointBubble(
                                    number = visitorPointCount,
                                    color = visitorColor
                                )
                            } else {
                                Spacer(modifier = Modifier.width(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentSetTimelineItem(
    setNumber: Int,
    localPoints: Int,
    visitorPoints: Int,
    sequence: List<Boolean>,
    localColor: Color,
    visitorColor: Color
) {
    val scrollState = rememberScrollState()
    
    LaunchedEffect(sequence.size) {
        if (sequence.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5),
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${getOrdinalText(setNumber)} set en juego: $localPoints - $visitorPoints",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val totalPoints = sequence.size
                
                if (totalPoints > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            var localPointCount = 0
                            for (i in 0 until totalPoints) {
                                if (i > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (sequence[i] == true) {
                                    localPointCount++
                                    PointBubble(
                                        number = localPointCount,
                                        color = localColor
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(28.dp))
                                }
                            }
                        }
                        
                        Row {
                            var visitorPointCount = 0
                            for (i in 0 until totalPoints) {
                                if (i > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (sequence[i] == false) {
                                    visitorPointCount++
                                    PointBubble(
                                        number = visitorPointCount,
                                        color = visitorColor
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(28.dp))
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Sin puntos aún",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SetHistoryTimelineDialog(
    state: ScoreboardState,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val totalItems = state.setHistory.size + if (state.local.points > 0 || state.visitor.points > 0) 1 else 0
    val isMatchFinished = state.isMatchFinished()
    
    val winnerTeam = if (state.lastSetWinner == true) state.local else state.visitor
    
    LaunchedEffect(Unit) {
        if (totalItems > 0) {
            listState.scrollToItem(totalItems - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = if (isMatchFinished) onDismiss else onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 12.dp)
                ) {
                    if (state.timelineOpenedBySetWin) {
                        val winnerIsLocal = state.lastSetWinner ?: true
                        val winnerTeam = if (winnerIsLocal) state.local else state.visitor
                        val setNumber = state.setHistory.size
                        
                        val message = if (isMatchFinished) {
                            "🎉 Victoria para ${winnerTeam.name}"
                        } else {
                            "🏆 ${getOrdinalText(setNumber)} set para ${winnerTeam.name}"
                        }
                        
                        Text(
                            text = message,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = winnerTeam.color
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(state.setHistory) { index, setResult ->
                            SetTimelineItem(
                                setResult = setResult,
                                localColor = state.local.color,
                                visitorColor = state.visitor.color
                            )
                        }
                        
                        if (!state.timelineOpenedBySetWin && !isMatchFinished && (state.local.points > 0 || state.visitor.points > 0)) {
                            item {
                                CurrentSetTimelineItem(
                                    setNumber = state.local.sets + state.visitor.sets + 1,
                                    localPoints = state.local.points,
                                    visitorPoints = state.visitor.points,
                                    sequence = state.currentSetSequence,
                                    localColor = state.local.color,
                                    visitorColor = state.visitor.color
                                )
                            }
                        }
                    }
                }
                
                // Botón de cierre flotante
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        
        if (isMatchFinished && state.timelineOpenedBySetWin) {
            CustomConfettiEffect(
                isActive = true,
                primaryColor = winnerTeam.color,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(999f)
            )
        }
    }
}

@Composable
fun TeamEditDialog(
    initialName: String,
    initialColor: Color,
    onDismiss: () -> Unit,
    onSave: (String, Color) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    val basePalette = listOf(
        OrangePrimary,
        PurplePrimary,
        Color(0xFFE8E8E8), // light gray (softer white)
        Color(0xFF2B2B2B), // dark gray (softer black)
        Color(0xFF9CA3AF), // gray
        Color(0xFFEF4444), // red
        Color(0xFFEC4899), // pink
        Color(0xFFFFD54F), // yellow
        Color(0xFF22C55E), // green
        Color(0xFFA3E635), // light green
        Color(0xFF0EA5E9), // blue
        Color(0xFF2DD4BF), // light blue
        Color(0xFF1E3A8A), // navy
        Color(0xFFFF6B6B)  // coral
    )
    val palette = if (basePalette.any { it == initialColor }) {
        basePalette
    } else {
        basePalette.dropLast(1) + initialColor
    }
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar equipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 15) name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Text(text = "Color", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    palette.chunked(6).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { swatch ->
                                val isSelected = swatch == selectedColor
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(swatch)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { selectedColor = swatch }
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(selectedColor)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.ifBlank { initialName }, selectedColor); onDismiss() }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SettingsDialog(
    volumeControlEnabled: Boolean,
    celebrationSoundEnabled: Boolean,
    totalSets: Int,
    hasMatchInProgress: Boolean,
    onDismiss: () -> Unit,
    onVolumeControlChange: (Boolean) -> Unit,
    onCelebrationSoundChange: (Boolean) -> Unit,
    onTotalSetsChange: (Int) -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingTotalSets by remember { mutableStateOf<Int?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Capturar volumen")
                        Text(
                            text = "Usar botones de volumen para sumar puntos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = volumeControlEnabled,
                        onCheckedChange = onVolumeControlChange
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sonido de celebración")
                    Switch(
                        checked = celebrationSoundEnabled,
                        onCheckedChange = onCelebrationSoundChange
                    )
                }
                Text("Número de sets")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 5).forEach { option ->
                        val selected = option == totalSets
                        Button(
                            onClick = { 
                                if (hasMatchInProgress && option != totalSets) {
                                    pendingTotalSets = option
                                    showConfirmationDialog = true
                                } else {
                                    onTotalSetsChange(option)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = "$option",
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
    
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { 
                showConfirmationDialog = false
                pendingTotalSets = null
            },
            title = { Text("Cambiar número de sets") },
            text = { 
                Text("Esto reiniciará el partido actual y se perderán todos los datos (puntos, sets e historial). ¿Deseas continuar?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingTotalSets?.let { onTotalSetsChange(it) }
                        showConfirmationDialog = false
                        pendingTotalSets = null
                    }
                ) {
                    Text("Reiniciar Partido")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        pendingTotalSets = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

data class TeamState(
    val name: String,
    val color: Color,
    val points: Int = 0,
    val sets: Int = 0,
    val streak: Int = 0
)

data class SetResult(
    val setNumber: Int,
    val localPoints: Int,
    val visitorPoints: Int,
    val pointSequence: List<Boolean> = emptyList()
)

data class ScoreboardState(
    val local: TeamState = TeamState("Equipo 1", OrangePrimary),
    val visitor: TeamState = TeamState("Equipo 2", PurplePrimary),
    val totalSets: Int = 5,
    val volumeControlEnabled: Boolean = true,
    val celebrationSoundEnabled: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val editingTeamSide: Boolean? = null,
    val setHistory: List<SetResult> = emptyList(),
    val lastSetWinner: Boolean? = null,
    val currentSetSequence: List<Boolean> = emptyList(),
    val showTimelineHistory: Boolean = false,
    val timelineOpenedBySetWin: Boolean = false
) {
    fun isMatchFinished(): Boolean {
        val targetSets = setsToWin(totalSets)
        return local.sets >= targetSets || visitor.sets >= targetSets
    }
}

class ScoreboardViewModel : androidx.lifecycle.ViewModel() {
    var uiState by mutableStateOf(ScoreboardState())
        private set
    
    private var soundPlayer: CelebrationSoundPlayer? = null
    
    fun initSoundPlayer(context: Context) {
        soundPlayer = CelebrationSoundPlayer(context)
    }
    
    fun playCelebrationSound() {
        if (uiState.celebrationSoundEnabled) {
            soundPlayer?.play()
        }
    }
    
    fun stopCelebrationSound() {
        soundPlayer?.stop()
    }
    
    fun setCelebrationSound(enabled: Boolean) {
        uiState = uiState.copy(celebrationSoundEnabled = enabled)
    }

    fun incrementPoints(isLocal: Boolean) {
        val state = uiState
        if (state.isMatchFinished() || state.showTimelineHistory) return
        val setNumber = state.local.sets + state.visitor.sets + 1
        val target = setTarget(setNumber, state.totalSets)
        val teams = if (isLocal) state.local to state.visitor else state.visitor to state.local
        val updatedPoints = teams.first.points + 1
        val updatedSequence = state.currentSetSequence + isLocal
        if (wouldWinSet(updatedPoints, teams.second.points, target)) {
            val updatedWinner = teams.first.copy(points = updatedPoints, sets = teams.first.sets + 1, streak = 0)
            val updatedLoser = teams.second.copy(streak = 0)
            val newSetResult = SetResult(
                setNumber = setNumber,
                localPoints = if (isLocal) updatedPoints else teams.second.points,
                visitorPoints = if (isLocal) teams.second.points else updatedPoints,
                pointSequence = updatedSequence
            )
            val newState = if (isLocal) {
                state.copy(
                    local = updatedWinner, 
                    visitor = updatedLoser,
                    setHistory = state.setHistory + newSetResult,
                    lastSetWinner = true
                )
            } else {
                state.copy(
                    local = updatedLoser, 
                    visitor = updatedWinner,
                    setHistory = state.setHistory + newSetResult,
                    lastSetWinner = false
                )
            }
            
            uiState = newState.copy(showTimelineHistory = true, timelineOpenedBySetWin = true)
        } else {
            val updatedWinner = teams.first.copy(points = updatedPoints, streak = teams.first.streak + 1)
            val updatedLoser = teams.second.copy(streak = 0)
            uiState = if (isLocal) {
                state.copy(local = updatedWinner, visitor = updatedLoser, currentSetSequence = updatedSequence)
            } else {
                state.copy(local = updatedLoser, visitor = updatedWinner, currentSetSequence = updatedSequence)
            }
        }
    }

    fun decrementPoints(isLocal: Boolean) {
        val state = uiState
        val team = if (isLocal) state.local else state.visitor
        if (team.points > 0) {
            val newPoints = team.points - 1
            val newStreak = max(0, team.streak - 1)
            val updated = team.copy(points = newPoints, streak = newStreak)
            
            val newSequence = if (state.currentSetSequence.isNotEmpty()) {
                val lastIndexOfTeam = state.currentSetSequence.indexOfLast { it == isLocal }
                if (lastIndexOfTeam >= 0) {
                    state.currentSetSequence.filterIndexed { index, _ -> index != lastIndexOfTeam }
                } else {
                    state.currentSetSequence
                }
            } else {
                state.currentSetSequence
            }
            
            uiState = if (isLocal) {
                state.copy(local = updated, currentSetSequence = newSequence)
            } else {
                state.copy(visitor = updated, currentSetSequence = newSequence)
            }
        }
    }

    fun continueAfterSetWin() {
        stopCelebrationSound()
        
        val state = uiState
        
        if (!state.timelineOpenedBySetWin) {
            uiState = state.copy(showTimelineHistory = false)
            return
        }
        
        val matchFinished = state.isMatchFinished()
        uiState = if (matchFinished) {
            state.copy(
                showTimelineHistory = false,
                lastSetWinner = null,
                timelineOpenedBySetWin = false
            )
        } else {
            state.copy(
                local = state.local.copy(points = 0, streak = 0),
                visitor = state.visitor.copy(points = 0, streak = 0),
                showTimelineHistory = false,
                lastSetWinner = null,
                currentSetSequence = emptyList(),
                timelineOpenedBySetWin = false
            )
        }
    }

    fun incrementSets(isLocal: Boolean) {
        val state = uiState
        if (state.isMatchFinished()) return
        val limit = setsToWin(state.totalSets)
        val team = if (isLocal) state.local else state.visitor
        val updated = team.copy(sets = (team.sets + 1).coerceAtMost(limit))
        uiState = if (isLocal) state.copy(local = updated) else state.copy(visitor = updated)
    }

    fun decrementSets(isLocal: Boolean) {
        val state = uiState
        val team = if (isLocal) state.local else state.visitor
        val updated = team.copy(sets = max(0, team.sets - 1))
        uiState = if (isLocal) state.copy(local = updated) else state.copy(visitor = updated)
    }

    fun resetPoints() {
        val state = uiState
        uiState = state.copy(
            local = state.local.copy(points = 0, streak = 0),
            visitor = state.visitor.copy(points = 0, streak = 0)
        )
    }

    fun resetAll() {
        val state = uiState
        uiState = state.copy(
            local = state.local.copy(points = 0, sets = 0, streak = 0),
            visitor = state.visitor.copy(points = 0, sets = 0, streak = 0),
            isSettingsOpen = false,
            editingTeamSide = null,
            setHistory = emptyList(),
            lastSetWinner = null,
            currentSetSequence = emptyList(),
            showTimelineHistory = false,
            timelineOpenedBySetWin = false
        )
    }

    fun openTeamEditor(isLocal: Boolean) {
        uiState = uiState.copy(editingTeamSide = isLocal)
    }

    fun closeTeamEditor() {
        uiState = uiState.copy(editingTeamSide = null)
    }

    fun updateTeam(isLocal: Boolean, name: String, color: Color) {
        val state = uiState
        val updatedTeam = if (isLocal) state.local else state.visitor
        val fixedName = name.ifBlank { updatedTeam.name }
        val newTeam = updatedTeam.copy(name = fixedName, color = color)
        uiState = if (isLocal) state.copy(local = newTeam) else state.copy(visitor = newTeam)
    }

    fun toggleSettings(open: Boolean) {
        uiState = uiState.copy(isSettingsOpen = open)
    }

    fun setVolumeControl(enabled: Boolean) {
        uiState = uiState.copy(volumeControlEnabled = enabled)
    }

    fun updateTotalSets(total: Int) {
        val sanitized = if (total in listOf(1, 3, 5)) total else 5
        uiState = uiState.copy(
            totalSets = sanitized,
            local = uiState.local.copy(points = 0, sets = 0, streak = 0),
            visitor = uiState.visitor.copy(points = 0, sets = 0, streak = 0),
            setHistory = emptyList(),
            currentSetSequence = emptyList(),
            lastSetWinner = null,
            showTimelineHistory = false,
            timelineOpenedBySetWin = false
        )
    }

    fun isVolumeControlActive(): Boolean = uiState.volumeControlEnabled
    
    fun toggleTimelineHistory(show: Boolean) {
        uiState = uiState.copy(showTimelineHistory = show, timelineOpenedBySetWin = false)
    }
    
    override fun onCleared() {
        super.onCleared()
        soundPlayer?.stop()
    }
}

private fun wouldWinSet(points: Int, opponentPoints: Int, target: Int): Boolean {
    return points >= target && points - opponentPoints >= 2
}

private fun isSetPoint(points: Int, opponentPoints: Int, setNumber: Int, totalSets: Int): Boolean {
    val target = setTarget(setNumber, totalSets)
    return wouldWinSet(points + 1, opponentPoints, target)
}

private fun setTarget(setNumber: Int, totalSets: Int): Int {
    if (totalSets == 1) return 25
    return if (setNumber == totalSets) 15 else 25
}

private fun setsToWin(totalSets: Int): Int = totalSets / 2 + 1
