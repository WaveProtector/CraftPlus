package com.example.craftplus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PrimaryKey
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresListDataFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun BuilderScreen(
    buildId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isRecordingStopped by remember { mutableStateOf(false) }
    val supabaseClient = remember {
        createSupabaseClient(
            supabaseUrl = "https://utbdioxirmblbdwagasi.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV0YmRpb3hpcm1ibGJkd2FnYXNpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY3Mjg5NTUsImV4cCI6MjA1MjMwNDk1NX0.W2lCPBmDcFUUyql22kK1NtUabHZ6f_EwWzuwBbeIaLU"
        )
        {
            install(Postgrest)
            install(io.github.jan.supabase.realtime.Realtime) // Realtime plugin
        }
    }

    // Simulação de escuta de mudanças no estado de gravação
    LaunchedEffect(Unit) {
        scope.launch {
            listenForRecordingStop(supabaseClient, buildId) { stopped ->
                if (stopped) {
                    isRecordingStopped = true
                }
            }
        }
    }

    // Redireciona automaticamente quando a gravação for parada
    if (isRecordingStopped) {
        LaunchedEffect(Unit) {
            navController.navigate(Screens.Home.route)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Start Building!")

        Spacer(modifier = Modifier.height(16.dp))

        // Exibe uma mensagem enquanto espera o "Recorder" parar a gravação
        Text("Waiting for the Recorder to stop the recording...")
    }
}

/**
 * Escuta o status de gravação no Supabase usando Realtime.
 */
fun listenForRecordingStop(
    supabaseClient: SupabaseClient,
    buildId: String,
    onRecordingStopped: (Boolean) -> Unit
) {
    // Criar um canal Realtime
    val channel = supabaseClient.realtime.channel("recording_status")

    // Usar postgresListDataFlow para observar mudanças
    channel.postgresListDataFlow(
        table = "recordings",
        filter = FilterOperation(column = "build_id", operator = FilterOperator.EQ, value = buildId), // Filtrar pelo buildId
        primaryKey = PrimaryKey(columnName = "id", producer = { data -> (data as Map<*, *>)["id"].toString() } )
    ).onEach { dataList ->
        // Verificar o status de cada gravação no fluxo
        val isStopped = dataList.any { data ->
            val status = (data as Map<*, *>)["status"]?.toString()
            status == "stopped"
        }
        if (isStopped) {
            onRecordingStopped(true)
        }
    }//.launchIn(rememberCoroutineScope()) // Garantir que o fluxo está em execução
}
