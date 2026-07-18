package ru.games.platform.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ru.games.platform.data.api.CaptchaChallenge

@Composable
fun AuthScreen(
    state: AuthUiState,
    onLoadCaptcha: () -> Unit,
    onToggleMode: (Boolean) -> Unit,
    onCaptchaAnswer: (Int) -> Unit,
    onSubmit: (login: String, password: String) -> Unit,
    onChangeServer: () -> Unit,
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { onLoadCaptcha() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (state.registerMode) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading,
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading,
        )
        Spacer(Modifier.height(16.dp))
        CaptchaBlock(
            captcha = state.captcha,
            answer = state.captchaAnswer,
            onAnswer = onCaptchaAnswer,
            onRefresh = onLoadCaptcha,
        )
        if (!state.error.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(state.error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onSubmit(login, password) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text(if (state.registerMode) "Создать аккаунт" else "Войти")
        }
        TextButton(onClick = { onToggleMode(!state.registerMode) }) {
            Text(
                if (state.registerMode) "Уже есть аккаунт? Войти"
                else "Зарегистрироваться",
            )
        }
        OutlinedButton(onClick = onChangeServer, modifier = Modifier.fillMaxWidth()) {
            Text("Сменить сервер")
        }
    }
}

@Composable
private fun CaptchaBlock(
    captcha: CaptchaChallenge?,
    answer: Int,
    onAnswer: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Сдвиньте фигурку", style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = onRefresh) { Text("↻") }
        }
        if (captcha == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return
        }
        val bg = remember(captcha.background) { decodeDataUri(captcha.background) }
        val piece = remember(captcha.piece) { decodeDataUri(captcha.piece) }
        val maxSlide = (captcha.trackWidth - captcha.pieceWidth).coerceAtLeast(1)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(captcha.imageHeight.dp),
        ) {
            if (bg != null) {
                Image(
                    bitmap = bg.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (piece != null) {
                Image(
                    bitmap = piece.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = answer.dp, y = captcha.pieceY.dp)
                        .width(captcha.pieceWidth.dp)
                        .height(captcha.pieceWidth.dp),
                )
            }
        }
        Slider(
            value = answer.toFloat(),
            onValueChange = { onAnswer(it.toInt()) },
            valueRange = 0f..maxSlide.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun decodeDataUri(uri: String): android.graphics.Bitmap? {
    val marker = "base64,"
    val idx = uri.indexOf(marker)
    if (idx < 0) return null
    val bytes = Base64.decode(uri.substring(idx + marker.length), Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
