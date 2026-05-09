@file:OptIn(ExperimentalMaterial3Api::class)

package com.companion.gokeys.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.companion.gokeys.R
import com.companion.gokeys.ui.screens.AutomationsScreen
import com.companion.gokeys.ui.screens.ConnectionScreen
import com.companion.gokeys.ui.screens.HelpScreen
import com.companion.gokeys.ui.screens.LoopMixScreen
import com.companion.gokeys.ui.screens.MacrosScreen
import com.companion.gokeys.ui.screens.MonitorScreen
import com.companion.gokeys.ui.screens.PatchesScreen
import com.companion.gokeys.ui.screens.PerformanceScreen
import com.companion.gokeys.ui.screens.ProfilesScreen
import com.companion.gokeys.ui.theme.Destructive
import com.companion.gokeys.ui.theme.Success
import com.companion.gokeys.viewmodel.CompanionViewModel

private data class NavItem(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val NAV_ITEMS = listOf(
    NavItem("connection", R.string.nav_connection, Icons.Default.Cable),
    NavItem("performance", R.string.nav_performance, Icons.Default.MusicNote),
    NavItem("loopmix", R.string.nav_loopmix, Icons.Default.Loop),
    NavItem("profiles", R.string.nav_profiles, Icons.Default.Person),
    NavItem("macros", R.string.nav_macros, Icons.Default.PlayArrow),
    NavItem("automations", R.string.nav_automations, Icons.Default.AccountTree),
    NavItem("monitor", R.string.nav_monitor, Icons.Default.Timeline),
)

@Composable
fun App(vm: CompanionViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val connected by vm.service.connected.collectAsState()
    val mainColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_app), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { nav.navigate("help") }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = stringResource(R.string.cd_help))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                NAV_ITEMS.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val isConnect = item.route == "connection"
                    val itemColor = when {
                        isConnect && connected -> Success
                        isConnect -> Destructive
                        else -> mainColor
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) itemColor.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isSelected) itemColor else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable {
                                nav.navigate(item.route) {
                                    launchSingleTop = true
                                    popUpTo(nav.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(item.labelRes),
                            color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurface,
                            fontSize = 9.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        },
    ) { padding: PaddingValues ->
        NavHost(
            navController = nav,
            startDestination = "connection",
            modifier = Modifier.padding(padding),
        ) {
            composable("connection") { ConnectionScreen(vm) }
            composable("performance") { PerformanceScreen(vm) }
            composable(
                route = "patches/{part}",
                arguments = listOf(navArgument("part") { type = NavType.IntType }),
            ) { entry ->
                val part = entry.arguments?.getInt("part") ?: 0
                PatchesScreen(vm, partIndex = part, onPicked = { nav.popBackStack() })
            }
            composable("loopmix") { LoopMixScreen(vm) }
            composable("profiles") { ProfilesScreen(vm) }
            composable("macros") { MacrosScreen(vm) }
            composable("automations") { AutomationsScreen(vm) }
            composable("monitor") { MonitorScreen(vm) }
            composable("help") { HelpScreen() }
        }
    }
}
