package com.example.induztek.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.induztek.data.repository.SesionRepository
import com.example.induztek.ui.screens.EquiposScreen
import com.example.induztek.ui.screens.IngresoScreen
import com.example.induztek.ui.screens.InicioScreen
import com.example.induztek.ui.screens.NuevaVisitaScreen
import com.example.induztek.ui.screens.VisitasScreen

object Screen {
    const val INGRESO = "ingreso"
    const val INICIO = "inicio"
    const val VISITAS = "visitas"
    const val NUEVA_VISITA = "nueva_visita"
    const val EQUIPOS = "equipos"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.INGRESO,
        modifier = modifier
    ) {
        composable(Screen.INGRESO) {
            IngresoScreen(
                onIngresoExitoso = {
                    navController.navigate(Screen.INICIO) {
                        popUpTo(Screen.INGRESO) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.INICIO) {
            InicioScreen(
                onNavigateToVisitas = {
                    navController.navigate(Screen.VISITAS)
                },
                onNavigateToEquipos = {
                    navController.navigate(Screen.EQUIPOS)
                },
                onCerrarSesion = {
                    SesionRepository.cerrarSesion()
                    navController.navigate(Screen.INGRESO) {
                        popUpTo(Screen.INICIO) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
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
        composable(Screen.EQUIPOS) {
            EquiposScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
