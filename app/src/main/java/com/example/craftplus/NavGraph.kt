package com.example.craftplus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph (navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route // TODO Deve ser depois alterado para login! Só está assim para efeitos de teste
    )
    {
        composable(route = Screens.Login.route) {
            DiceWithButtonAndImage(
                navController = navController,
                viewModel = diceViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }

        composable(route = Screens.Register.route) {
            DiceResult(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                resultShow = resultShow)
        }

        composable(route = Screens.Home.route) {
            BusinessCard(navController = navController) // DO NOT CHANGE MODIFIER
        }

        composable(route = Screens.Builds.route) {
            AllPagesButtons(navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
        }

    }
}