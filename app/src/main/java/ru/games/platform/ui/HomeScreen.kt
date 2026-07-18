package ru.games.platform.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.games.platform.data.api.ChallengeStatus
import ru.games.platform.data.api.GameCatalogItem
import ru.games.platform.data.api.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    state: HomeUiState,
    onRefresh: () -> Unit,
    onOpenGame: (GameCatalogItem) -> Unit,
    onLogout: () -> Unit,
    onServer: () -> Unit,
) {
    LaunchedEffect(user.id) { onRefresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Привет, ${user.login}")
                        val grade = user.grade
                        Text(
                            if (grade != null) "$grade класс" else "Класс не назначен",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                    IconButton(onClick = onServer) {
                        Icon(Icons.Default.Settings, contentDescription = "Сервер")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Выйти")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading && state.games.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { CircularProgressIndicator() }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!state.error.isNullOrBlank()) {
                        item {
                            Text(state.error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    state.challenge?.let { ch ->
                        item { ChallengeCard(ch) }
                    }
                    item {
                        Text(
                            "Игры",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    items(state.games, key = { it.id }) { game ->
                        GameCard(game = game, onClick = { onOpenGame(game) })
                    }
                    if (state.games.isEmpty() && !state.loading) {
                        item {
                            Text(
                                "Нет доступных игр. Попросите администратора назначить класс.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(status: ChallengeStatus) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Вызов дня", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Пройдено ${status.completed} из ${status.total}")
            if (status.total > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { status.completed.toFloat() / status.total },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            status.week?.let { week ->
                Spacer(Modifier.height(8.dp))
                Text("Серия: ${week.wins} дн. — ${week.praise.orEmpty()}")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.days.forEach { day ->
                        Text(
                            if (day.done) "●" else "○",
                            color = if (day.done) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        )
                    }
                }
            }
            if (status.allDone) {
                Spacer(Modifier.height(8.dp))
                Text("Все задания на сегодня выполнены!", fontWeight = FontWeight.SemiBold)
                status.reward?.let { r ->
                    Text("Награда: ${r.planetName ?: r.planet}, код ${r.code}")
                }
            }
            if (status.games.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                status.games.forEach { g ->
                    Text("${if (g.done) "✓" else "○"} ${g.title ?: g.gameId}")
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: GameCatalogItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = game.available, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (game.available) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(game.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            if (!game.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    game.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }
            if (!game.available) {
                Spacer(Modifier.height(6.dp))
                Text("Недоступно для вашего класса", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
