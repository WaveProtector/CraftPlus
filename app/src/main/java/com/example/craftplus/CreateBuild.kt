package com.example.craftplus

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.craftplus.network.BuildObject
import com.example.craftplus.network.BuildViewModel

@Composable
fun CreateBuildScreen(buildViewModel: BuildViewModel, navController: NavController, modifier: Modifier = Modifier) {

    //IR BUSCAR A BD
    val friends = listOf("Herobrine", "Steve", "Slender") // Lista de amigos
    var selectedFriend by remember { mutableStateOf("") }
    var buildTitle by remember { mutableStateOf("") } // Armazena o título do Build
    val build = BuildObject(
        id = "mockId123",
        title = buildTitle,
        starter = "John Doe",
        friend = selectedFriend,
        builder = "Builder Bot",
        recorder = "Camera Bot",
        blocks = 150, // Exemplo de número de blocos
        video = "https://example.com/mock_video.mp4", // URL de um vídeo fictício
        steps = 5 // Exemplo de número de passos
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TopBar(navController, "Create Build")

        // Campo de título
        //GUARDAR NA BD
        TitleInput(onTitleChanged = { newTitle ->
            buildTitle = newTitle // Atualiza o título na variável
        })

        Spacer(modifier = Modifier.height(48.dp))

        //GUARDAR NA BD
        // Texto "Choose a Friend"
        Text(
            text = "Choose a Friend",
            color = Color.Black,
            fontSize = 28.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo de pesquisa
        FriendSelectionDropdown(
            friends = friends,
            selectedFriend = selectedFriend,
            onFriendSelected = { friend ->
                selectedFriend = friend // Atualiza o amigo selecionado
            }
        )

        Spacer(modifier = Modifier.height(100.dp))

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Adicione padding, se necessário
        ) {
            // Botões Cancelar e Iniciar
            Row(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    Log.d("SAVE", build.toString())
                    buildViewModel.saveCurrentBuild(build); navController.navigate(Screens.Roles.route) },
                    modifier = Modifier.wrapContentSize().padding(horizontal = 8.dp))
                {
                    Text("Start")
                }
                Button(onClick = { navController.navigate(Screens.Home.route) },
                    modifier = Modifier.wrapContentSize().padding(horizontal = 8.dp)) {
                    Text("Cancel")
                }
            }
        }
        //Spacer(modifier = Modifier.height(250.dp))
    }
}


// guardar o titulo
@Composable
fun TitleInput(onTitleChanged: (String) -> Unit) {

    var title by remember { mutableStateOf("") } // Estado para armazenar o título

    // Campo de título
    OutlinedTextField(
        value = title,
        onValueChange = {
            title = it // Atualiza o estado do título
            onTitleChanged(it) // Envia o título para a função callback
        },
        label = { Text("Title") },
        placeholder = { Text("Your title build here...") },
        modifier = Modifier.fillMaxWidth()
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSelectionDropdown(
    friends: List<String>,
    selectedFriend: String,
    onFriendSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // Controla se o menu está expandido
    var searchQuery by remember { mutableStateOf(selectedFriend) } // Controla o texto pesquisado
    var dropdownHeight by remember { mutableStateOf(0) } // Para controlar a altura do menu

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded } // Alterna entre expandido/fechado
    ) {
        // Campo de texto para pesquisa
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Choose a Friend") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor() // Conecta o menu ao campo
        )

        // Filtro de amigos baseado no texto pesquisado
        val filteredFriends = friends.filter {
            it.contains(searchQuery, ignoreCase = true)
        }

        // Menu suspenso
        Box(modifier = Modifier.zIndex(1f)) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                filteredFriends.forEach { friend ->
                    //NAO CONSEGUI POR ISTO IMEDIATAMENTE ABAIXO DO MENU...
                    DropdownMenuItem(
                        onClick = {
                            searchQuery = friend // Define o amigo selecionado
                            onFriendSelected(friend)
                            expanded = false // Fecha o menu
                        },
                        text = { Text(text = friend) }
                    )
                }
            }
        }

    }
}

