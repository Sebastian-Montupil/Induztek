package com.example.induztek.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.induztek.ui.screens.InicioScreen
import com.example.induztek.ui.screens.NuevaVisitaScreen
import com.example.induztek.ui.screens.VisitasScreen

object Screen {
    const val INICIO = "inicio"
    const val VISITAS = "visitas"
    const val NUEVA_VISITA = "nueva_visita"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.INICIO,
        modifier = modifier
    ) {
        composable(Screen.INICIO) {
            InicioScreen(
                onNavigateToVisitas = {
                    navController.navigate(Screen.VISITAS)
                }
            )
        }
        composable(Screen.VISITAS) {
            VisitasScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToNuevaVisita = {
                    navController.navigate(Screen.NUEVA_VISITA)
                }
            )
        }
        composable(Screen.NUEVA_VISITA) {
            NuevaVisitaScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
