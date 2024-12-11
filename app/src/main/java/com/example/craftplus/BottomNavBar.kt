package com.example.craftplus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


// THIS CLASS CAN BE REMOVED NOW
@Composable
fun BottomNavBar(navController: NavController, defaultItem: Int, modifier: Modifier = Modifier) {
    // State to track the selected item
    var selectedNavItem by remember { mutableStateOf(defaultItem) } // Default selected is "Home"

    // List of icons and labels for the navigation bar
    val items = listOf(
        R.drawable.ic_builds,       // Builds
        R.drawable.ic_inventory, // Materials
        R.drawable.ic_home,       // Home
        R.drawable.ic_person,   // Profile
        R.drawable.ic_group,    // Friends
    )

    NavigationBar(
        modifier = Modifier
            .height(150.dp),
        containerColor = Color.Transparent
    ) {
        items.forEachIndexed { index, iconResId ->
            NavigationBarItem(
                icon = {
                    // Add square outline to each icon
                    Box(
                        modifier = Modifier
                            .size(44.dp) // Adjust icon box size
                            .clip(RoundedCornerShape(4.dp)) // Square shape with rounded corners
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (selectedNavItem == index) Color.Blue else Color.Gray
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp), // Icon size within the square
                            tint = if (selectedNavItem == index) Color.Blue else Color.Gray
                        )
                    }
                },
                selected = selectedNavItem == index,
                onClick = {
                    selectedNavItem = index
                    // Perform navigation
                    when (index) {
                        0 -> navController.navigate("builds")
                        1 -> navController.navigate("materials")
                        2 -> navController.navigate("home")
                        3 -> navController.navigate("profile")
                        4 -> navController.navigate("friends")
                    }
                },
                // Remove padding around the item
                alwaysShowLabel = false // Hide labels
            )
        }
    }
}