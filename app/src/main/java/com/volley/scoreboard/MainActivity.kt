package com.volley.scoreboard

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        onIncrementSet = { viewModel.incrementSets(it) },
        onDecrementSet = { viewModel.decrementSets(it) },
        onResetPoints = { viewModel.resetPoints() },
        onResetAll = { viewModel.resetAll() },
        onOpenTeamEditor = { viewModel.openTeamEditor(it) },
        onCloseTeamEditor = { viewModel.closeTeamEditor() },
        onUpdateTeam = { side, name, color -> viewModel.updateTeam(side, name, color) },
        onToggleSettings = { viewModel.toggleSettings(it) },
        onToggleVolumeControl = { viewModel.setVolumeControl(it) },
        onSelectTotalSets = { viewModel.updateTotalSets(it) }
    )
}

@Composable
fun ScoreboardScreen(
    state: ScoreboardState,
    onIncrementPoint: (Boolean) -> Unit,
    onDecrementPoint: (Boolean) -> Unit,
    onIncrementSet: (Boolean) -> Unit,
    onDecrementSet: (Boolean) -> Unit,
    onResetPoints: () -> Unit,
    onResetAll: () -> Unit,
    onOpenTeamEditor: (Boolean) -> Unit,
    onCloseTeamEditor: () -> Unit,
    onUpdateTeam: (Boolean, String, Color) -> Unit,
    onToggleSettings: (Boolean) -> Unit,
    onToggleVolumeControl: (Boolean) -> Unit,
    onSelectTotalSets: (Int) -> Unit
) {
    val matchFinished = state.isMatchFinished()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        TopBar(
            onResetPoints = onResetPoints,
            onResetAll = onResetAll,
            onOpenSettings = { onToggleSettings(true) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TeamPanel(
                title = "Local",
                team = state.local,
                opponent = state.visitor,
                totalSets = state.totalSets,
                matchFinished = matchFinished,
                modifier = Modifier.weight(1f),
                onEditTeam = { onOpenTeamEditor(true) },
                onIncrementPoint = { onIncrementPoint(true) },
                onDecrementPoint = { onDecrementPoint(true) },
                onIncrementSet = { onIncrementSet(true) },
                onDecrementSet = { onDecrementSet(true) }
            )
            TeamPanel(
                title = "Visitante",
                team = state.visitor,
                opponent = state.local,
                totalSets = state.totalSets,
                matchFinished = matchFinished,
                modifier = Modifier.weight(1f),
                onEditTeam = { onOpenTeamEditor(false) },
                onIncrementPoint = { onIncrementPoint(false) },
                onDecrementPoint = { onDecrementPoint(false) },
                onIncrementSet = { onIncrementSet(false) },
                onDecrementSet = { onDecrementSet(false) }
            )
        }
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
fun TopBar(
    onResetPoints: () -> Unit,
    onResetAll: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onResetPoints) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset puntos")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onResetAll) {
                Text("Reset general")
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Configuración")
            }
        }
    }
}

@Composable
fun TeamPanel(
    title: String,
    team: TeamState,
    opponent: TeamState,
    totalSets: Int,
    matchFinished: Boolean,
    modifier: Modifier = Modifier,
    onEditTeam: () -> Unit,
    onIncrementPoint: () -> Unit,
    onDecrementPoint: () -> Unit,
    onIncrementSet: () -> Unit,
    onDecrementSet: () -> Unit
) {
    val setNumber = team.sets + opponent.sets + 1
    val setPoint = isSetPoint(team.points, opponent.points, setNumber, totalSets)
    val matchPoint = setPoint && team.sets == setsToWin(totalSets) - 1
    val indicatorColor = if (setPoint) Color(0xFFE53935) else team.color

    Card(
        modifier = modifier
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TeamHeader(
                title = title,
                team = team,
                indicatorColor = indicatorColor,
                onEditTeam = onEditTeam
            )
            ScoreDisplay(
                color = indicatorColor,
                points = team.points,
                setPoint = setPoint,
                matchPoint = matchPoint,
                modifier = Modifier.weight(1f)
            )
            ControlsRow(
                enabled = !matchFinished,
                onIncrement = onIncrementPoint,
                onDecrement = onDecrementPoint
            )
            SetRow(
                sets = team.sets,
                totalSets = totalSets,
                matchFinished = matchFinished,
                onIncrementSet = onIncrementSet,
                onDecrementSet = onDecrementSet
            )
        }
    }
}

@Composable
fun TeamHeader(
    title: String,
    team: TeamState,
    indicatorColor: Color,
    onEditTeam: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onEditTeam
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Text(
                text = team.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(indicatorColor)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
        )
    }
}

@Composable
fun ScoreDisplay(
    color: Color,
    points: Int,
    setPoint: Boolean,
    matchPoint: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FlipCounter(
            value = points,
            background = color,
            setPoint = setPoint,
            matchPoint = matchPoint
        )
    }
}

@Composable
fun FlipCounter(
    value: Int,
    background: Color,
    setPoint: Boolean,
    matchPoint: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.12f)
                            )
                        )
                    )
            )
        }
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(animationSpec = tween(120)) togetherWith
                        fadeOut(animationSpec = tween(120))
            },
            label = "counter"
        ) { target ->
            val textColor = when {
                matchPoint -> Color(0xFFFFEBEE)
                setPoint -> ColorWhite
                else -> ColorWhite
            }
            val animatedScale by animateHeartbeat(matchPoint)
            Text(
                text = target.toString().padStart(2, '0'),
                modifier = Modifier.scale(animatedScale),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 80.sp,
                    letterSpacing = 2.sp
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
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
fun ControlsRow(
    enabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreButton(text = "-", enabled = enabled) { onDecrement() }
        ScoreButton(text = "+", enabled = enabled) { onIncrement() }
    }
}

@Composable
fun ScoreButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .width(100.dp)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun SetRow(
    sets: Int,
    totalSets: Int,
    matchFinished: Boolean,
    onIncrementSet: () -> Unit,
    onDecrementSet: () -> Unit
) {
    val setsToWin = setsToWin(totalSets)
    val canIncrease = !matchFinished && sets < setsToWin
    val canDecrease = !matchFinished && sets > 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Sets", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Text(
                text = "$sets / $setsToWin",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onDecrementSet() }, enabled = canDecrease) {
                Text("-")
            }
            OutlinedButton(onClick = { onIncrementSet() }, enabled = canIncrease) {
                Text("+")
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
    var red by remember { mutableStateOf((initialColor.red * 255).toInt()) }
    var green by remember { mutableStateOf((initialColor.green * 255).toInt()) }
    var blue by remember { mutableStateOf((initialColor.blue * 255).toInt()) }

    val color = Color(red / 255f, green / 255f, blue / 255f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar equipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                ColorSlider(label = "Rojo", value = red) { red = it }
                ColorSlider(label = "Verde", value = green) { green = it }
                ColorSlider(label = "Azul", value = blue) { blue = it }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.ifBlank { initialName }, color); onDismiss() }) {
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
fun ColorSlider(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value.toString())
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f
        )
    }
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
                            enabled = !selected
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

data class TeamState(
    val name: String,
    val color: Color,
    val points: Int = 0,
    val sets: Int = 0
)

data class ScoreboardState(
    val local: TeamState = TeamState("Local", OrangePrimary),
    val visitor: TeamState = TeamState("Visitante", PurplePrimary),
    val totalSets: Int = 5,
    val volumeControlEnabled: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val editingTeamSide: Boolean? = null
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
        uiState = ScoreboardState()
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
