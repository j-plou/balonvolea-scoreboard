package com.volley.scoreboard

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow

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
import kotlin.math.max

class MainActivity : ComponentActivity() {
    private val viewModel: ScoreboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun ScoreboardApp(viewModel: ScoreboardViewModel) {
    val state = viewModel.uiState
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
        onSelectTotalSets = { viewModel.updateTotalSets(it) },
        onOpenSetSelector = { viewModel.openSetSelector(it) },
        onCloseSetSelector = { viewModel.closeSetSelector() },
        onSelectSetValue = { side, value -> viewModel.setSetsValue(side, value) }
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
    onSelectTotalSets: (Int) -> Unit,
    onOpenSetSelector: (Boolean) -> Unit,
    onCloseSetSelector: () -> Unit,
    onSelectSetValue: (Boolean, Int) -> Unit
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
                onEditTeam = { onOpenTeamEditor(true) },
                onSwipeUp = { onIncrementPoint(true) },
                onSwipeDown = { onDecrementPoint(true) },
                modifier = Modifier.weight(1f)
            )
            TeamHalf(
                team = state.visitor,
                matchPoint = visitorMatchPoint,
                matchFinished = matchFinished,
                onEditTeam = { onOpenTeamEditor(false) },
                onSwipeUp = { onIncrementPoint(false) },
                onSwipeDown = { onDecrementPoint(false) },
                modifier = Modifier.weight(1f)
            )
        }

        SetsBar(
            localSets = state.local.sets,
            visitorSets = state.visitor.sets,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            onTapLocal = { onOpenSetSelector(true) },
            onTapVisitor = { onOpenSetSelector(false) }
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
            totalSets = state.totalSets,
            onDismiss = { onToggleSettings(false) },
            onVolumeControlChange = onToggleVolumeControl,
            onTotalSetsChange = onSelectTotalSets
        )
    }

    state.editingSetsSide?.let { side ->
        val sets = if (side) state.local.sets else state.visitor.sets
        SetSelectionDialog(
            sideLabel = if (side) "Local" else "Visitante",
            current = sets,
            totalSets = state.totalSets,
            onDismiss = onCloseSetSelector,
            onSelect = { value -> onSelectSetValue(side, value) }
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
                modifier = Modifier
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun FlipCounter(
    value: Int,
    background: Color,
    matchPoint: Boolean,
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
                val animatedScale by animateHeartbeat(matchPoint)
                Text(
                    text = target.toString().padStart(2, '0'),
                    modifier = Modifier.scale(animatedScale),
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
        }
    }
}

@Composable
fun animateHeartbeat(active: Boolean): androidx.compose.runtime.State<Float> {
    if (!active) return remember { mutableStateOf(1f) }
    val transition = rememberInfiniteTransition(label = "heartbeatTransition")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )
}

@Composable
fun SetsBar(
    localSets: Int,
    visitorSets: Int,
    modifier: Modifier = Modifier,
    onTapLocal: () -> Unit,
    onTapVisitor: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ColorWhite,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 92.dp)
            .wrapContentWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp, vertical = 12.dp)
                .wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sets",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
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
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTapLocal
                        )
                        .padding(horizontal = 12.dp)
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
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTapVisitor
                        )
                        .padding(horizontal = 12.dp)
                )
            }
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
    totalSets: Int,
    onDismiss: () -> Unit,
    onVolumeControlChange: (Boolean) -> Unit,
    onTotalSetsChange: (Int) -> Unit
) {
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
                Text("Número de sets")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 5).forEach { option ->
                        val selected = option == totalSets
                        Button(
                            onClick = { onTotalSetsChange(option) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("$option")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun SetSelectionDialog(
    sideLabel: String,
    current: Int,
    totalSets: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = (0..setsToWin(totalSets)).toList()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar sets $sideLabel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Selecciona el marcador de sets (0-${setsToWin(totalSets)})")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { value ->
                        val selected = value == current
                        Button(
                            onClick = { onSelect(value) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(value.toString())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

data class TeamState(
    val name: String,
    val color: Color,
    val points: Int = 0,
    val sets: Int = 0
)

data class ScoreboardState(
    val local: TeamState = TeamState("Equipo 1", OrangePrimary),
    val visitor: TeamState = TeamState("Equipo 2", PurplePrimary),
    val totalSets: Int = 5,
    val volumeControlEnabled: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val editingTeamSide: Boolean? = null,
    val editingSetsSide: Boolean? = null
) {
    fun isMatchFinished(): Boolean {
        val targetSets = setsToWin(totalSets)
        return local.sets >= targetSets || visitor.sets >= targetSets
    }
}

class ScoreboardViewModel : androidx.lifecycle.ViewModel() {
    var uiState by mutableStateOf(ScoreboardState())
        private set

    fun incrementPoints(isLocal: Boolean) {
        val state = uiState
        if (state.isMatchFinished()) return
        val setNumber = state.local.sets + state.visitor.sets + 1
        val target = setTarget(setNumber, state.totalSets)
        val teams = if (isLocal) state.local to state.visitor else state.visitor to state.local
        val updatedPoints = teams.first.points + 1
        if (wouldWinSet(updatedPoints, teams.second.points, target)) {
            val updatedWinner = teams.first.copy(points = 0, sets = teams.first.sets + 1)
            val updatedLoser = teams.second.copy(points = 0)
            uiState = if (isLocal) {
                state.copy(local = updatedWinner, visitor = updatedLoser)
            } else {
                state.copy(local = updatedLoser, visitor = updatedWinner)
            }
        } else {
            val updatedWinner = teams.first.copy(points = updatedPoints)
            uiState = if (isLocal) {
                state.copy(local = updatedWinner)
            } else {
                state.copy(visitor = updatedWinner)
            }
        }
    }

    fun decrementPoints(isLocal: Boolean) {
        val state = uiState
        val team = if (isLocal) state.local else state.visitor
        val updated = team.copy(points = max(0, team.points - 1))
        uiState = if (isLocal) {
            state.copy(local = updated)
        } else {
            state.copy(visitor = updated)
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
            local = state.local.copy(points = 0),
            visitor = state.visitor.copy(points = 0)
        )
    }

    fun resetAll() {
        val state = uiState
        uiState = state.copy(
            local = state.local.copy(points = 0, sets = 0),
            visitor = state.visitor.copy(points = 0, sets = 0),
            isSettingsOpen = false,
            editingTeamSide = null,
            editingSetsSide = null
        )
    }

    fun openSetSelector(isLocal: Boolean) {
        uiState = uiState.copy(editingSetsSide = isLocal)
    }

    fun closeSetSelector() {
        uiState = uiState.copy(editingSetsSide = null)
    }

    fun setSetsValue(isLocal: Boolean, value: Int) {
        val state = uiState
        val limit = setsToWin(state.totalSets)
        val clamped = value.coerceIn(0, limit)
        val team = if (isLocal) state.local else state.visitor
        val updatedTeam = team.copy(sets = clamped)
        uiState = if (isLocal) {
            state.copy(local = updatedTeam, editingSetsSide = null)
        } else {
            state.copy(visitor = updatedTeam, editingSetsSide = null)
        }
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
        val resetState = uiState.copy(
            totalSets = sanitized,
            local = uiState.local.copy(points = 0, sets = 0),
            visitor = uiState.visitor.copy(points = 0, sets = 0)
        )
        uiState = resetState
    }

    fun isVolumeControlActive(): Boolean = uiState.volumeControlEnabled
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
