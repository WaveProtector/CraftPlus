package com.example.craftplus

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.craftplus.ui.theme.CraftPlusTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    // Firebase instance variables
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CraftPlusTheme {
                val navController = rememberNavController()

                // Screens that don't show the bottom navigation
                val hideBottomNavigationScreens = listOf("login", "register")

                //Bottom Navigation bar
                val items = listOf(
                    BottomNavigationItem(
                        title = Screens.Home.route,
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,
                    ),
                    BottomNavigationItem(
                        title = "Build",
                        selectedIcon = Icons.Filled.Build,
                        unselectedIcon = Icons.Outlined.Build,
                        hasNews = false
                    ),
                    BottomNavigationItem(
                        title = "Search",
                        selectedIcon = Icons.Filled.Search,
                        unselectedIcon = Icons.Outlined.Search,
                        hasNews = false
                    ),
                    BottomNavigationItem(
                        //Alterar
                        title = Screens.Home.route,
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        hasNews = true,
                    ),
                )

                var selectedItemIndex by rememberSaveable {
                    mutableStateOf(0)
                }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            bottomBar = {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route
                                // Verifica se o Bottom Navigation deve ser exibido
                                if (currentRoute !in hideBottomNavigationScreens) {
                                    NavigationBar {
                                        items.forEachIndexed { index, item ->
                                            NavigationBarItem(
                                                selected = selectedItemIndex == index,
                                                onClick = {
                                                    selectedItemIndex = index
                                                    navController.navigate(item.title)
                                                },
                                                label = {
                                                    Text(text = item.title)
                                                },
                                                alwaysShowLabel = false,
                                                icon = {
                                                    BadgedBox(
                                                        badge = {
                                                            if (item.badgeCount != null) {
                                                                Badge {
                                                                    Text(text = item.badgeCount.toString())
                                                                }
                                                            } else if (item.hasNews) {
                                                                Badge()
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = if (index == selectedItemIndex) {
                                                                item.selectedIcon
                                                            } else item.unselectedIcon,
                                                            contentDescription = item.title
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                NavGraph(navController = navController)
                            }
                        }
                    }
            }
        }

        // Initialize Realtime Database
        db = Firebase.firestore

        // Apenas adicionado para debug purposes.. Devido a um erro ao carregar nos botões de builder ou recorder
        FirebaseApp.initializeApp(this)

        val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        val activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                // Handle Permission granted/rejected
                var permissionGranted = true
                permissions.entries.forEach {
                    if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                        permissionGranted = false
                }
                if (!permissionGranted) {
                    Toast.makeText(baseContext,
                        "Permission request denied. Please grant permissions in order to use important functionalities.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        activityResultLauncher.launch(REQUIRED_PERMISSIONS) // ASKS FOR PERMISSIONS
    }

//    private fun signOut() {
//        AuthUI.getInstance().signOut(this)
//        startActivity(Intent(this, SignInActivity::class.java))
//        finish()
//    }

    data class BottomNavigationItem(
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
        val hasNews: Boolean,
        val badgeCount: Int? = null
    )

}