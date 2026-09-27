package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.AppDatabase
import com.example.data.repository.DatingRepository
import com.example.ui.components.MatchCelebrationDialog
import com.example.ui.components.ProposeDateDialog
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.CompatibilityScreen
import com.example.ui.screens.DatesScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.ProfileDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DatingScreen
import com.example.ui.viewmodel.DatingViewModel
import com.example.ui.viewmodel.DatingViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = DatingRepository(database.datingDao())
        val factory = DatingViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[DatingViewModel::class.java]

        setContent {
            MyApplicationTheme {
                CitaRealApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CitaRealApp(viewModel: DatingViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val feedProfiles by viewModel.filteredFeedProfiles.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val matchedProfiles by viewModel.matchedProfiles.collectAsStateWithLifecycle()
    val realDates by viewModel.realDates.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val activeChatProfile by viewModel.activeChatProfile.collectAsStateWithLifecycle()
    val selectedProfileDetail by viewModel.selectedProfileForDetail.collectAsStateWithLifecycle()
    val matchCelebrationProfile by viewModel.matchCelebrationProfile.collectAsStateWithLifecycle()
    val proposeDateForProfile by viewModel.proposeDateForProfile.collectAsStateWithLifecycle()

    // Back button handling
    BackHandler(enabled = selectedProfileDetail != null || activeChatProfile != null || currentScreen != DatingScreen.FEED) {
        when {
            selectedProfileDetail != null -> viewModel.selectProfileForDetail(null)
            activeChatProfile != null -> viewModel.closeChat()
            currentScreen != DatingScreen.FEED -> viewModel.setScreen(DatingScreen.FEED)
        }
    }

    // Modal dialogs
    matchCelebrationProfile?.let { matched ->
        MatchCelebrationDialog(
            profile = matched,
            onChatClick = {
                viewModel.dismissMatchCelebration()
                viewModel.openChatWithProfile(matched)
            },
            onDateClick = {
                viewModel.dismissMatchCelebration()
                viewModel.startProposingDate(matched)
            },
            onDismiss = {
                viewModel.dismissMatchCelebration()
            }
        )
    }

    proposeDateForProfile?.let { profile ->
        ProposeDateDialog(
            profile = profile,
            onConfirm = { title, location, dateTime, category, notes ->
                viewModel.confirmProposeDate(title, location, dateTime, category, notes)
            },
            onDismiss = {
                viewModel.dismissProposeDate()
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (selectedProfileDetail == null && activeChatProfile == null) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == DatingScreen.FEED,
                        onClick = { viewModel.setScreen(DatingScreen.FEED) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == DatingScreen.FEED) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Descubrir Citas"
                            )
                        },
                        label = { Text("Descubrir", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_feed")
                    )

                    NavigationBarItem(
                        selected = currentScreen == DatingScreen.DATES,
                        onClick = { viewModel.setScreen(DatingScreen.DATES) },
                        icon = {
                            if (realDates.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${realDates.size}") } }) {
                                    Icon(
                                        imageVector = if (currentScreen == DatingScreen.DATES) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                        contentDescription = "Mis Citas Reales"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (currentScreen == DatingScreen.DATES) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                    contentDescription = "Mis Citas Reales"
                                )
                            }
                        },
                        label = { Text("Mis Citas", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_dates")
                    )

                    NavigationBarItem(
                        selected = currentScreen == DatingScreen.CHATS,
                        onClick = { viewModel.setScreen(DatingScreen.CHATS) },
                        icon = {
                            if (matchedProfiles.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${matchedProfiles.size}") } }) {
                                    Icon(
                                        imageVector = if (currentScreen == DatingScreen.CHATS) Icons.Default.Chat else Icons.Outlined.Chat,
                                        contentDescription = "Mensajes"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (currentScreen == DatingScreen.CHATS) Icons.Default.Chat else Icons.Outlined.Chat,
                                    contentDescription = "Mensajes"
                                )
                            }
                        },
                        label = { Text("Mensajes", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_chats")
                    )

                    NavigationBarItem(
                        selected = currentScreen == DatingScreen.COMPATIBILITY,
                        onClick = { viewModel.setScreen(DatingScreen.COMPATIBILITY) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == DatingScreen.COMPATIBILITY) Icons.Default.Psychology else Icons.Outlined.Psychology,
                                contentDescription = "Afinidad"
                            )
                        },
                        label = { Text("Afinidad", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_compatibility")
                    )

                    NavigationBarItem(
                        selected = currentScreen == DatingScreen.PROFILE,
                        onClick = { viewModel.setScreen(DatingScreen.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == DatingScreen.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                                contentDescription = "Mi Perfil"
                            )
                        },
                        label = { Text("Perfil", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedProfileDetail != null) {
                ProfileDetailScreen(
                    profile = selectedProfileDetail!!,
                    onBack = { viewModel.selectProfileForDetail(null) },
                    onProposeDate = {
                        val target = selectedProfileDetail!!
                        viewModel.selectProfileForDetail(null)
                        viewModel.startProposingDate(target)
                    },
                    onOpenChat = {
                        val target = selectedProfileDetail!!
                        viewModel.selectProfileForDetail(null)
                        viewModel.likeProfile(target)
                        viewModel.openChatWithProfile(target)
                    }
                )
            } else {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        DatingScreen.FEED -> FeedScreen(
                            profiles = feedProfiles,
                            selectedCategory = categoryFilter,
                            onCategorySelected = { viewModel.setCategoryFilter(it) },
                            onLikeProfile = { viewModel.likeProfile(it) },
                            onPassProfile = { viewModel.passProfile(it) },
                            onSelectProfile = { viewModel.selectProfileForDetail(it) },
                            onResetFeed = { viewModel.resetFeed() }
                        )

                        DatingScreen.DATES -> DatesScreen(
                            dates = realDates,
                            onExploreClick = { viewModel.setScreen(DatingScreen.FEED) },
                            onCancelDate = { viewModel.cancelDate(it) },
                            onToggleSafety = { date, isShared -> viewModel.updateSafetyProtocol(date, isShared) }
                        )

                        DatingScreen.CHATS -> ChatsScreen(
                            matches = matchedProfiles,
                            activeProfile = activeChatProfile,
                            messages = chatMessages,
                            onSelectMatch = { viewModel.openChatWithProfile(it) },
                            onCloseChat = { viewModel.closeChat() },
                            onSendMessage = { viewModel.sendMessage(it) },
                            onProposeDate = { viewModel.startProposingDate(it) },
                            onSendClosure = { viewModel.sendRespectfulClosure() },
                            onExploreClick = { viewModel.setScreen(DatingScreen.FEED) }
                        )

                        DatingScreen.COMPATIBILITY -> CompatibilityScreen(
                            onSubmitQuiz = { viewModel.submitQuiz(it) }
                        )

                        DatingScreen.PROFILE -> ProfileScreen(
                            userProfile = userProfile,
                            onSaveProfile = { name, bio, idealDate, nonNegotiables ->
                                viewModel.saveUserProfile(name, bio, idealDate, nonNegotiables)
                            }
                        )
                    }
                }
            }
        }
    }
}
