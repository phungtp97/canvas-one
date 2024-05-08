package com.joshta.canvasone

import Screen
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joshta.canvasone.ui.MainScreen
import com.joshta.canvasone.ui.gallery.GalleryScreen
import com.joshta.canvasone.ui.theme.AppTheme
import com.joshta.canvasone.ui.theme.CanvasOneTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasOneTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = AppTheme.color.background)
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.MainScreen.route
                    ) {
                        composable(Screen.MainScreen.route) {
                            val uri: Uri? =
                                navController.previousBackStackEntry?.savedStateHandle?.get("newUri") // new
                            MainScreen()
                        }
                        composable(Screen.GalleryScreen.route) {
                            GalleryScreen(onImageSelected = { uri ->
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "newUri", uri
                                )
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}