package com.studiomogli.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studiomogli.app.ui.components.StudioMogliTopRightControls
import com.studiomogli.app.ui.components.StudioMogliWelcomePopup
import com.studiomogli.app.ui.credits.CreditsScreen
import com.studiomogli.app.ui.details.DetailsScreen
import com.studiomogli.app.ui.home.HomeScreen
import com.studiomogli.app.ui.home.HomeViewModel
import com.studiomogli.app.ui.player.VideoPlayerScreen
import com.studiomogli.app.ui.theme.NetflixBlack
import com.studiomogli.app.ui.theme.NetflixRed
import com.studiomogli.app.ui.theme.NetflixSurface
import com.studiomogli.app.ui.theme.StudioMogliTheme
import com.studiomogli.app.ui.trust.TrustScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudioMogliTheme {
                StudioMogliApp()
            }
        }
    }
}

@Composable
fun StudioMogliApp() {
    val navController = rememberNavController()
    val viewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    val updateInfo by viewModel.updateInfo.collectAsState()

    // First-time launch popup check via SharedPreferences
    val prefs = remember { context.getSharedPreferences("studio_mogli_prefs", Context.MODE_PRIVATE) }
    var showWelcomePopup by remember {
        mutableStateOf(!prefs.getBoolean("has_seen_welcome_popup", false))
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val isFullScreenPlayer = currentRoute == "player"

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = NetflixBlack,
            bottomBar = {
                if (!isFullScreenPlayer) {
                    NavigationBar(containerColor = NetflixSurface) {
                        NavigationBarItem(
                            selected = currentRoute == "home" || currentRoute == "details",
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NetflixRed,
                                selectedTextColor = NetflixRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "trust",
                            onClick = { navController.navigate("trust") },
                            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Trust") },
                            label = { Text("Trust") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NetflixRed,
                                selectedTextColor = NetflixRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "credits",
                            onClick = { navController.navigate("credits") },
                            icon = { Icon(Icons.Default.Info, contentDescription = "Credits") },
                            label = { Text("Credits") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NetflixRed,
                                selectedTextColor = NetflixRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = if (isFullScreenPlayer) Modifier.fillMaxSize() else Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onAnimeClick = { anime ->
                            viewModel.selectAnime(anime)
                            navController.navigate("details")
                        }
                    )
                }
                composable("details") {
                    DetailsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onPlayEpisode = { episode, category, langCode ->
                            viewModel.playEpisode(episode, category, langCode) {
                                navController.navigate("player")
                            }
                        }
                    )
                }
                composable("player") {
                    VideoPlayerScreen(viewModel = viewModel)
                }
                composable("trust") {
                    TrustScreen()
                }
                composable("credits") {
                    CreditsScreen()
                }
            }
        }

        // Persistent Top-Right Studio Mogli Button + Auto-Hidden GitHub Update Button
        // Automatically hidden during fullscreen anime playback
        StudioMogliTopRightControls(
            isFullScreenPlayer = isFullScreenPlayer,
            updateInfo = updateInfo,
            onOpenPopup = { showWelcomePopup = true },
            modifier = Modifier.align(Alignment.TopEnd)
        )

        if (showWelcomePopup && !isFullScreenPlayer) {
            StudioMogliWelcomePopup(
                onDismiss = {
                    prefs.edit().putBoolean("has_seen_welcome_popup", true).apply()
                    showWelcomePopup = false
                },
                onOpenTrustPage = { navController.navigate("trust") },
                onOpenCreditsPage = { navController.navigate("credits") }
            )
        }
    }
}
