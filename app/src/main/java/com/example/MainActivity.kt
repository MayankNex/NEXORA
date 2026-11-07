package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.NexoraDatabase
import com.example.data.repository.ChatRepository
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.PersonaStudioSheet
import com.example.ui.screens.ProviderSettingsSheet
import com.example.ui.screens.SidebarDrawer
import com.example.ui.screens.TokenAnalyticsDialog
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraTheme
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class NexoraViewModelFactory(
    private val repository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NexoraDatabase.getDatabase(applicationContext)
        val repository = ChatRepository(database)
        val factory = NexoraViewModelFactory(repository)

        setContent {
            NexoraTheme {
                NexoraApp(factory = factory)
            }
        }
    }
}

@Composable
fun NexoraApp(factory: NexoraViewModelFactory) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val sessions by chatViewModel.allSessions.collectAsStateWithLifecycle()
    val tokenStats by chatViewModel.totalTokenStats.collectAsStateWithLifecycle()

    var showProvidersSheet by remember { mutableStateOf(false) }
    var showPersonasSheet by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }

    // Back button handling
    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NexoraSurface
            ) {
                SidebarDrawer(
                    sessions = sessions,
                    currentSessionId = chatUiState.currentSessionId,
                    activeModelId = chatUiState.activeModelId,
                    tokenStats = tokenStats,
                    onSelectSession = { sessionId ->
                        chatViewModel.selectSession(sessionId)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        chatViewModel.startNewSession()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onDeleteSession = { sessionId ->
                        chatViewModel.deleteSession(sessionId)
                    },
                    onTogglePin = { sessionId, isPinned ->
                        chatViewModel.togglePinSession(sessionId, isPinned)
                    },
                    onOpenProviders = {
                        coroutineScope.launch { drawerState.close() }
                        showProvidersSheet = true
                    },
                    onOpenPersonas = {
                        coroutineScope.launch { drawerState.close() }
                        showPersonasSheet = true
                    },
                    onOpenAnalytics = {
                        coroutineScope.launch { drawerState.close() }
                        showAnalyticsDialog = true
                    }
                )
            }
        }
    ) {
        ChatScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onOpenDrawer = {
                coroutineScope.launch { drawerState.open() }
            },
            onOpenSettings = {
                showProvidersSheet = true
            },
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Sheets & Dialogs
        if (showProvidersSheet) {
            ProviderSettingsSheet(
                viewModel = settingsViewModel,
                uiState = settingsUiState,
                onDismiss = { showProvidersSheet = false }
            )
        }

        if (showPersonasSheet) {
            PersonaStudioSheet(
                viewModel = settingsViewModel,
                activePersonaId = chatUiState.activePersona?.id,
                onSelectPersona = { persona ->
                    chatViewModel.selectPersona(persona)
                    showPersonasSheet = false
                },
                onDismiss = { showPersonasSheet = false }
            )
        }

        if (showAnalyticsDialog) {
            TokenAnalyticsDialog(
                currentSession = chatUiState.currentSession,
                tokenStats = tokenStats,
                viewModel = settingsViewModel,
                onExportMarkdown = {
                    chatViewModel.exportCurrentSession(context)
                },
                onDismiss = { showAnalyticsDialog = false }
            )
        }
    }
}
