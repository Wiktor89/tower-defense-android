package ru.games.platform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.games.platform.ui.AppViewModel
import ru.games.platform.ui.AuthScreen
import ru.games.platform.ui.GameWebViewScreen
import ru.games.platform.ui.GamesTheme
import ru.games.platform.ui.HomeScreen
import ru.games.platform.ui.ServerScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GamesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GamesApp()
                }
            }
        }
    }
}

private object Routes {
    const val Server = "server"
    const val Auth = "auth"
    const val Home = "home"
    const val Game = "game/{title}/{path}"
}

@Composable
private fun GamesApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val baseUrl by vm.baseUrl.collectAsState()
    val user by vm.user.collectAsState()
    val home by vm.home.collectAsState()
    val auth by vm.auth.collectAsState()
    val serverError by vm.serverError.collectAsState()
    val busy by vm.busy.collectAsState()
    val ready by vm.ready.collectAsState()

    if (!ready) {
        return
    }

    val start = when {
        baseUrl.isBlank() -> Routes.Server
        user == null -> Routes.Auth
        else -> Routes.Home
    }

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.Server) {
            ServerScreen(
                initialUrl = baseUrl,
                busy = busy,
                error = serverError,
                onSave = { url ->
                    vm.saveServer(url) {
                        if (user == null) nav.navigate(Routes.Auth) {
                            popUpTo(Routes.Server) { inclusive = true }
                        } else nav.navigate(Routes.Home) {
                            popUpTo(Routes.Server) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Routes.Auth) {
            AuthScreen(
                state = auth,
                onLoadCaptcha = vm::loadCaptcha,
                onToggleMode = vm::setRegisterMode,
                onCaptchaAnswer = vm::setCaptchaAnswer,
                onSubmit = { login, password ->
                    vm.submitAuth(login, password) {
                        nav.navigate(Routes.Home) {
                            popUpTo(Routes.Auth) { inclusive = true }
                        }
                    }
                },
                onChangeServer = {
                    nav.navigate(Routes.Server)
                },
            )
        }
        composable(Routes.Home) {
            val u = user
            if (u == null) {
                nav.navigate(Routes.Auth) { popUpTo(Routes.Home) { inclusive = true } }
            } else {
                HomeScreen(
                    user = u,
                    state = home,
                    onRefresh = vm::refreshHome,
                    onOpenGame = { game ->
                        val title = URLEncoder.encode(game.title, StandardCharsets.UTF_8)
                        val path = URLEncoder.encode(game.url, StandardCharsets.UTF_8)
                        nav.navigate("game/$title/$path")
                    },
                    onLogout = {
                        vm.logout {
                            nav.navigate(Routes.Auth) {
                                popUpTo(Routes.Home) { inclusive = true }
                            }
                        }
                    },
                    onServer = { nav.navigate(Routes.Server) },
                )
            }
        }
        composable(
            Routes.Game,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("path") { type = NavType.StringType },
            ),
        ) { entry ->
            val title = URLDecoder.decode(entry.arguments?.getString("title").orEmpty(), StandardCharsets.UTF_8)
            val path = URLDecoder.decode(entry.arguments?.getString("path").orEmpty(), StandardCharsets.UTF_8)
            val u = user
            if (u == null || baseUrl.isBlank()) {
                nav.popBackStack()
            } else {
                GameWebViewScreen(
                    title = title,
                    baseUrl = baseUrl,
                    gamePath = path,
                    user = u,
                    onBack = {
                        nav.popBackStack()
                        vm.refreshHome()
                    },
                )
            }
        }
    }
}
