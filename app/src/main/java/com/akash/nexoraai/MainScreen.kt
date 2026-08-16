package com.akash.nexoraai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.core.content.ContextCompat
import com.akash.nexoraai.utils.VoiceToTextManager
import com.akash.nexoraai.utils.VoiceToTextState
import java.util.Locale
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.akash.nexoraai.ui.theme.*
import com.akash.nexoraai.presentation.chat.ChatViewModel
import com.akash.nexoraai.presentation.user.UserViewModel
import com.akash.nexoraai.presentation.user.Notification
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Voice : Screen("voice")
    object Chat : Screen("chat")
    object LearningHub : Screen("learning_hub")
    object CodeAssistant : Screen("code_assistant")
    object InterviewMode : Screen("interview_mode")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Onboarding.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen {
                navController.navigate(Screen.Dashboard.route)
            }
        }
        composable(Screen.Dashboard.route) {
            val chatViewModel: ChatViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = chatViewModel,
                userViewModel = userViewModel,
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onVoiceClick = { navController.navigate(Screen.Voice.route) },
                onLearningClick = { navController.navigate(Screen.LearningHub.route) },
                onCodeClick = { navController.navigate(Screen.CodeAssistant.route) },
                onInterviewClick = { navController.navigate(Screen.InterviewMode.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                navController = navController
            )
        }
        composable(Screen.Voice.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            VoiceInteractionScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Chat.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            ChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.LearningHub.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            LearningHubScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCategoryClick = { category ->
                    viewModel.clearChat()
                    viewModel.sendMessage("", "Act as a technical tutor. I want to learn about $category. Start by giving me a high-level overview and ask what I want to dive into first.")
                    navController.navigate(Screen.Chat.route)
                }
            )
        }
        composable(Screen.CodeAssistant.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            CodeAssistantScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onActionTriggered = { action, code ->
                    viewModel.clearChat()
                    val prompt = when(action) {
                        "Explain Code" -> "Explain this code in detail and tell me how it works step by step:\n\n$code"
                        "Debug" -> "Find any bugs or potential issues in this code and suggest fixes:\n\n$code"
                        "Optimize" -> "Rewrite this code to be more efficient/performant while maintaining the same functionality:\n\n$code"
                        "Convert Language" -> "Convert this code to another popular programming language (choose the most appropriate one) and explain the changes:\n\n$code"
                        "Generate Tests" -> "Generate comprehensive unit tests for this code using a popular testing framework:\n\n$code"
                        else -> "Analyze this code:\n\n$code"
                    }
                    viewModel.sendMessage("", prompt)
                }
            )
        }
        composable(Screen.InterviewMode.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            InterviewModeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onStartInterview = { tech ->
                    viewModel.clearChat()
                    viewModel.sendMessage("", "Act as a professional technical interviewer for a $tech position. Start the interview by introducing yourself briefly and then ask me the first question. Wait for my response before asking the next one.")
                }
            )
        }
        composable(Screen.History.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onItemClick = { navController.navigate(Screen.Chat.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = userViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = userViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Notifications.route) {
            NotificationScreen(viewModel = userViewModel, onBack = { navController.popBackStack() })
        }
        composable("topic_explorer/{topicName}") { backStackEntry ->
            val topicName = backStackEntry.arguments?.getString("topicName") ?: ""
            val viewModel: ChatViewModel = hiltViewModel()
            TopicExplorerScreen(
                topicName = topicName,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// --- 2. Onboarding Screen (Screen 1) ---
@Composable
fun OnboardingScreen(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Animated Header Text
            AnimatedOnboardingText()

            // Robot & Speech Bubble
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Animated Speech Bubble Button
                val bubbleTransition = rememberInfiniteTransition(label = "")
                val bubbleScale by bubbleTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = ""
                )

                Surface(
                    color = AccentPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .scale(bubbleScale)
                ) {
                    Text(
                        "Need our help\nnow?",
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                }

                ProfessionalAnimatedRobot()
            }

            // Custom "Slide to Start" Button
            SlideToStartButton(onStartClick)
        }
    }
}

@Composable
fun AnimatedOnboardingText() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = ""
    )
    
    Text(
        text = buildAnnotatedString {
            append("Meet the\n")
            withStyle(style = SpanStyle(color = AccentPurple)) {
                append("Echo Mind!")
            }
        },
        fontSize = 44.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = alpha),
        lineHeight = 52.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
            .graphicsLayer(translationY = (1f - alpha) * 20f)
    )
}

@Composable
fun ProfessionalAnimatedRobot() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    var isJumping by remember { mutableStateOf(false) }
    var forwardOffset by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    
    // Smooth transition for forward movement
    val moveForward by animateFloatAsState(
        targetValue = forwardOffset,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = ""
    )

    // Jump animation
    val jumpOffset by animateFloatAsState(
        targetValue = if (isJumping) -40f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        finishedListener = { if (it == -40f) isJumping = false },
        label = ""
    )

    // Floating movement
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = ""
    )

    // Orbital rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), label = ""
    )

    Box(
        modifier = Modifier
            .size(260.dp)
            .graphicsLayer(
                translationY = floatOffset + jumpOffset,
                translationX = moveForward
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isJumping = true },
                    onDoubleTap = {
                        forwardOffset = 100f
                        scope.launch {
                            delay(2000)
                            forwardOffset = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Orbital Rings
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(AccentCyan, Color.Transparent, AccentPurple, Color.Transparent)),
                    radius = size.width / 2.2f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                    alpha = 0.5f
                )
            }
        }

        // Humanoid Body Robot Representation
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Head
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = GlassWhite,
                border = androidx.compose.foundation.BorderStroke(2.dp, AccentCyan.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentCyan))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentCyan))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Body
            Surface(
                modifier = Modifier.size(width = 80.dp, height = 100.dp),
                shape = RoundedCornerShape(20.dp, 20.dp, 10.dp, 10.dp),
                color = GlassWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Adb, // Android body base
                        contentDescription = null,
                        tint = AccentPurple.copy(alpha = 0.8f),
                        modifier = Modifier.size(50.dp)
                    )
                    // Cyber heart
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AccentCyan.copy(alpha = 0.8f)))
                }
            }
            
            // Legs
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(width = 12.dp, height = 40.dp).clip(RoundedCornerShape(6.dp)).background(GlassWhite))
                Box(modifier = Modifier.size(width = 12.dp, height = 40.dp).clip(RoundedCornerShape(6.dp)).background(GlassWhite))
            }
        }
    }
}

@Composable
fun SlideToStartButton(onStartClick: () -> Unit) {
    val density = LocalDensity.current
    var buttonWidth by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    val maxOffset = remember(buttonWidth) { buttonWidth - with(density) { 80.dp.toPx() } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(bottom = 20.dp)
            .onGloballyPositioned { buttonWidth = it.size.width.toFloat() }
            .clip(RoundedCornerShape(50.dp))
            .background(GlassWhite)
    ) {
        // Background Hint Text
        Text(
            text = "Slide to Get Started",
            color = TextGray.copy(alpha = 0.4f),
            modifier = Modifier.align(Alignment.Center),
            fontSize = 16.sp
        )

        // The Slidable Part
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .size(72.dp)
                .padding(4.dp)
                .clip(CircleShape)
                .background(AccentPurple)
                .pointerInput(maxOffset) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX >= maxOffset * 0.8f) {
                                offsetX = maxOffset
                                onStartClick()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX + dragAmount).coerceIn(0f, maxOffset)
                            offsetX = newOffset
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
        
        // Final text indicator
        if (offsetX < maxOffset * 0.5f) {
            Text(
                ">>>",
                color = TextGray.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
                fontSize = 20.sp
            )
        }
    }
}

// --- 3. Dashboard Screen (Screen 2) ---
@Composable
fun DashboardScreen(
    viewModel: ChatViewModel,
    userViewModel: UserViewModel,
    onChatClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onLearningClick: () -> Unit,
    onCodeClick: () -> Unit,
    onInterviewClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    navController: androidx.navigation.NavController
) {
    val recentConversations by viewModel.recentConversations.collectAsState()
    val userName by userViewModel.userName.collectAsState()
    val profileImage by userViewModel.profileImage.collectAsState()
    val appBackground by userViewModel.appBackground.collectAsState()

    val backgroundBrush = remember(appBackground) {
        if (appBackground == "Gradient") {
            Brush.verticalGradient(listOf(PrimaryBackground, SecondaryBackground))
        } else {
            Brush.linearGradient(listOf(PrimaryBackground, PrimaryBackground))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onProfileClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profileImage != null) {
                    AsyncImage(
                        model = profileImage,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentPurple)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Hello,", color = TextGray, fontSize = 12.sp)
                    Text(userName, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onNotificationClick, modifier = Modifier.background(GlassWhite, CircleShape)) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main "Ask Nexora" Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable { onChatClick() },
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Column {
                    Text(
                        "Ask Nexora\nanything",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Powered by Echo Mind System", color = TextGray, fontSize = 14.sp)
                }
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = SoftHighlight,
                    modifier = Modifier.size(48.dp).align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Actions Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardActionCard("Code\nAssistant", Icons.Default.Code, Modifier.weight(1f), onCodeClick)
            DashboardActionCard("Interview\nMode", Icons.Default.Psychology, Modifier.weight(1f), onInterviewClick)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardActionCard("Learning\nHub", Icons.Default.School, Modifier.weight(1f), onLearningClick)
            DashboardActionCard("Voice\nAssistant", Icons.Default.Mic, Modifier.weight(1f), onVoiceClick)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Topics
        SectionHeader("Topics", onHistoryClick)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TopicItem("DSA", Icons.Default.AccountTree) {
                navController.navigate("topic_explorer/DSA")
            }
            TopicItem("Android", Icons.Default.Android) {
                navController.navigate("topic_explorer/Android")
            }
            TopicItem("System", Icons.Default.Dns) {
                navController.navigate("topic_explorer/System Design")
            }
            TopicItem("SQL", Icons.Default.Storage) {
                navController.navigate("topic_explorer/SQL")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TopicItem("Kotlin", Icons.Default.Code) {
                navController.navigate("topic_explorer/Kotlin")
            }
            TopicItem("Java", Icons.Default.Coffee) {
                navController.navigate("topic_explorer/Java")
            }
            TopicItem("Cloud", Icons.Default.Cloud) {
                navController.navigate("topic_explorer/Cloud")
            }
            TopicItem("Security", Icons.Default.Security) {
                navController.navigate("topic_explorer/Security")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recent Conversations
        SectionHeader("Recent Conversations", onHistoryClick)
        if (recentConversations.isEmpty()) {
            Text("No recent chats", color = TextGray, fontSize = 14.sp)
        } else {
            recentConversations.take(3).forEach { session ->
                HistoryItem(session.title, session.date, 
                    if(session.iconName == "Mic") Icons.Default.Mic else Icons.AutoMirrored.Filled.Chat,
                    modifier = Modifier.clickable { 
                        viewModel.clearChat()
                        viewModel.sendMessage("", "Continue our conversation about: ${session.title}")
                        onChatClick()
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardActionCard(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(160.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "View All",
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onViewAll() }
        )
    }
}

@Composable
fun TopicItem(name: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(GlassWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, color = TextGray, fontSize = 12.sp)
    }
}

@Composable
fun HistoryItem(title: String, date: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontSize = 14.sp)
            Text(date, color = TextGray, fontSize = 12.sp)
        }
    }
}

// --- 5. Chat Screen (Screen 4) ---
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatScreen(viewModel: ChatViewModel, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val chatMessages = viewModel.messages
    val isLoading by viewModel.isLoading.collectAsState()
    
    val context = LocalContext.current
    val voiceManager = remember { VoiceToTextManager(context) }
    val voiceState by voiceManager.state.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceManager.startListening()
        }
    }
    
    LaunchedEffect(voiceState.spokenText) {
        if (voiceState.spokenText.isNotBlank()) {
            viewModel.sendMessage(voiceState.spokenText)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text(
                    "Nexora",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                }
            }
        },
        bottomBar = {
            ChatInputBar(
                text = if (voiceState.isListening) "Listening..." else messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                onMicClick = {
                    if (voiceState.isListening) {
                        voiceManager.stopListening()
                    } else {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            voiceManager.startListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(GlassWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(35.dp))
                    }
                }
            }

            items(chatMessages) { message ->
                ChatBubble(message)
            }

            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        repeat(3) { index ->
            val delay = index * 200
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ), label = ""
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) AccentPurple else SecondaryBackground.copy(alpha = 0.8f)
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = bgColor,
                shape = shape,
                border = if (!message.isUser) androidx.compose.foundation.BorderStroke(0.5.dp, GlassWhite) else null,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                SelectionContainer {
                    val styledText = formatAIText(message.text)
                    Text(
                        text = styledText,
                        color = Color.White,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            
            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (!message.isUser) {
            Row(
                modifier = Modifier.padding(start = 48.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy, 
                    "Copy", 
                    tint = TextGray.copy(alpha = 0.6f), 
                    modifier = Modifier.size(18.dp).clickable { 
                        clipboardManager.setText(AnnotatedString(message.text))
                    }
                )
                Icon(
                    Icons.Default.Refresh, 
                    "Regenerate", 
                    tint = TextGray.copy(alpha = 0.6f), 
                    modifier = Modifier.size(18.dp).clickable { /* Regenerate logic */ }
                )
                Icon(
                    Icons.Default.ThumbUpOffAlt, 
                    "Like", 
                    tint = TextGray.copy(alpha = 0.6f), 
                    modifier = Modifier.size(18.dp).clickable { /* Like */ }
                )
                Icon(
                    Icons.Default.Share, 
                    "Share", 
                    tint = TextGray.copy(alpha = 0.6f), 
                    modifier = Modifier.size(18.dp).clickable { 
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, message.text)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                )
            }
        }
    }
}

/**
 * A sophisticated text formatter for AI responses.
 * Handles Headings (#), Bold (**), and Bullet Points (-).
 */
@Composable
fun formatAIText(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            when {
                // Heading Handling (# Heading)
                line.startsWith("#") -> {
                    val headingText = line.replace("#", "").trim()
                    withStyle(style = SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftHighlight
                    )) {
                        append(headingText)
                    }
                }
                // Bullet Point Handling (- Item)
                line.trim().startsWith("-") || line.trim().startsWith("*") -> {
                    val bulletText = line.trim().substring(1).trim()
                    append("  •  ") // Unique bullet style
                    parseInlineStyles(bulletText, this)
                }
                // Standard Paragraph
                else -> {
                    parseInlineStyles(line, this)
                }
            }
            if (index < lines.size - 1) append("\n")
        }
    }
}

/**
 * Internal helper to handle bold (**text**) and italic (*text*) within a line.
 */
fun parseInlineStyles(text: String, builder: androidx.compose.ui.text.AnnotatedString.Builder) {
    var currentText = text
    
    // Simple Bold Pattern Matching (**bold**)
    val parts = currentText.split("**")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) { // It's a bold part
            builder.withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PureWhite)) {
                append(part)
            }
        } else {
            // Handle Italics within non-bold part (*italic*)
            val italicParts = part.split("*")
            italicParts.forEachIndexed { i, iPart ->
                if (i % 2 == 1) {
                    builder.withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = TextGray)) {
                        append(iPart)
                    }
                } else {
                    builder.append(iPart)
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(text: String, onValueChange: (String) -> Unit, onSend: () -> Unit, onMicClick: () -> Unit = {}, onAttachClick: () -> Unit = {}) {
    Surface(
        color = SurfaceDark,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        shape = RoundedCornerShape(30.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhite)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachClick) {
                Icon(Icons.Default.Add, contentDescription = "Attach", tint = TextGray, modifier = Modifier.size(22.dp))
            }
            
            TextField(
                value = text,
                onValueChange = onValueChange,
                placeholder = { Text("Ask anything...", color = TextGray) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AccentPurple,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(onClick = onMicClick) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = TextGray, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AccentPurple)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// --- 6. Voice Screen (Screen 3) ---
enum class VoiceState { IDLE, LISTENING, THINKING, SPEAKING }

@Composable
fun VoiceInteractionScreen(viewModel: ChatViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceToTextManager(context) }
    val voiceState by voiceManager.state.collectAsState()
    var appVoiceState by remember { mutableStateOf(VoiceState.IDLE) }
    val chatMessages = viewModel.messages
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Initialize TTS
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tts?.stop() // Stop AI if speaking when user wants to speak
            voiceManager.startListening()
            appVoiceState = VoiceState.LISTENING
        }
    }

    // Handle user voice input
    LaunchedEffect(voiceState.spokenText) {
        if (voiceState.spokenText.isNotBlank()) {
            val textToProcess = voiceState.spokenText
            voiceManager.resetSpokenText() 
            appVoiceState = VoiceState.THINKING
            viewModel.sendMessage(textToProcess)
        }
    }

    // Synchronize appVoiceState with ViewModel's loading state
    LaunchedEffect(isLoading) {
        if (isLoading) {
            appVoiceState = VoiceState.THINKING
        }
    }

    // Handle AI response and speak it
    LaunchedEffect(chatMessages.size) {
        val lastMessage = chatMessages.lastOrNull()
        if (lastMessage != null && !lastMessage.isUser && (appVoiceState == VoiceState.THINKING || appVoiceState == VoiceState.IDLE)) {
            if (System.currentTimeMillis() - lastMessage.timestamp < 5000) {
                appVoiceState = VoiceState.SPEAKING
                
                val cleanSpeechText = lastMessage.text
                    .replace(Regex("[#*`_~]"), "")
                    .replace(Regex("\\(.*?\\)"), "")
                    .replace(Regex("\\[.*?\\]"), "")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .take(400) 
                
                tts?.speak(cleanSpeechText, TextToSpeech.QUEUE_FLUSH, null, null)
                
                val wordCount = cleanSpeechText.split(" ").size
                val estimatedSpeechMillis = (wordCount * 500L).coerceIn(2000L, 10000L)
                delay(estimatedSpeechMillis)
                
                appVoiceState = VoiceState.IDLE
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.background(GlassWhite, CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                }
                Text(
                    "Voice Assistant",
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // New Close/Cancel Button
                IconButton(onClick = {
                    tts?.stop()
                    onBack()
                }, modifier = Modifier.background(GlassWhite, CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = when {
                    voiceState.error != null -> voiceState.error!!
                    voiceState.isListening -> "Go ahead, I'm listening..."
                    isLoading -> "Nexora is thinking..."
                    appVoiceState == VoiceState.SPEAKING -> "Nexora is speaking..."
                    voiceState.spokenText.isNotBlank() -> "You: ${voiceState.spokenText}"
                    else -> "Tap to start speaking"
                },
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Premium Animated Orb
            VoiceOrb(if (voiceState.isListening) VoiceState.LISTENING else if (isLoading) VoiceState.THINKING else appVoiceState)

            Spacer(modifier = Modifier.weight(1f))
            
            // Show the last AI message text briefly
            chatMessages.lastOrNull { !it.isUser }?.let { msg ->
                if (appVoiceState == VoiceState.SPEAKING) {
                    Text(
                        msg.text.take(100) + (if(msg.text.length > 100) "..." else ""),
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Bottom Mic Button
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(GlassWhite)
                    .clickable {
                        if (voiceState.isListening) {
                            voiceManager.stopListening()
                        } else {
                            tts?.stop() // Interrupt AI if it was speaking
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                voiceManager.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(if (voiceState.isListening) AccentCyan else AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (voiceState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun VoiceOrb(state: VoiceState) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = ""
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(if (state == VoiceState.THINKING) 2000 else 8000, easing = LinearEasing), RepeatMode.Restart), label = ""
    )

    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Glows
        repeat(3) { i ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f + (i * 0.1f),
                targetValue = 1.3f + (i * 0.1f),
                animationSpec = infiniteRepeatable(tween(2000 + (i * 500)), RepeatMode.Reverse), label = ""
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                (if (state == VoiceState.LISTENING) AccentCyan else AccentPurple).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Rotating Rings
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(AccentPurple, AccentCyan, AccentPurple)),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                    alpha = 0.6f
                )
            }
            rotate(-rotation * 0.7f) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(AccentCyan, AccentPurple, AccentCyan)),
                    radius = size.width / 2.5f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                    alpha = 0.4f
                )
            }
        }

        // Center Orb
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(if (state == VoiceState.SPEAKING) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (state == VoiceState.LISTENING) listOf(AccentCyan, SoftHighlight) else listOf(AccentPurple, AccentCyan)
                    )
                )
        )
    }
}

// --- 7. Learning Hub Screen ---
@Composable
fun LearningHubScreen(viewModel: ChatViewModel, onBack: () -> Unit, onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        "DSA" to 0.7f, "Java" to 0.4f, "Kotlin" to 0.9f,
        "Android" to 0.8f, "SQL" to 0.3f, ".NET" to 0.2f,
        "System Design" to 0.5f, "Interview Prep" to 0.6f
    )
    val chatMessages = viewModel.messages
    val isLoading by viewModel.isLoading.collectAsState()
    val isSessionActive = chatMessages.size > 1

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(PrimaryBackground).padding(16.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = if (isSessionActive) { { viewModel.clearChat() } } else onBack) { 
                    Icon(if (isSessionActive) Icons.Default.Close else Icons.Default.ArrowBack, "", tint = Color.White) 
                }
                Text(if (isSessionActive) "Tutor Session" else "Learning Hub", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = PrimaryBackground
    ) { padding ->
        if (isSessionActive) {
            // Tutor Chat View
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(chatMessages) { message -> ChatBubble(message) }
                    if (isLoading) { item { TypingIndicator() } }
                }
                
                var messageText by remember { mutableStateOf("") }
                ChatInputBar(
                    text = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    }
                )
            }
        } else {
            // Category List View
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(categories) { (title, progress) ->
                    LearningCategoryCard(title, progress) { onCategoryClick(title) }
                }
            }
        }
    }
}

@Composable
fun LearningCategoryCard(title: String, progress: Float, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = SecondaryBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}%", color = SoftHighlight, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = AccentPurple,
                trackColor = GlassWhite
            )
        }
    }
}

// --- 8. Code Assistant Screen ---
@Composable
fun CodeAssistantScreen(viewModel: ChatViewModel, onBack: () -> Unit, onActionTriggered: (String, String) -> Unit) {
    var codeText by remember { mutableStateOf("fun main() {\n    println(\"Hello Nexora AI\")\n}") }
    val chatMessages = viewModel.messages
    val isLoading by viewModel.isLoading.collectAsState()
    val lastAiResponse = chatMessages.lastOrNull { !it.isUser && it.text != "Hi! I'm Nexora.\nHow can I help you today?" }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(PrimaryBackground).padding(16.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "", tint = Color.White) }
                Text("Code Assistant", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = PrimaryBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            Text("AI-Powered Coding", color = SoftHighlight, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Code Input Block
            Text("Your Code:", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 300.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhite)
            ) {
                TextField(
                    value = codeText,
                    onValueChange = { codeText = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color(0xFFCE9178),
                        unfocusedTextColor = Color(0xFFCE9178)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Actions
            val actions = listOf("Explain Code", "Debug", "Optimize", "Convert Language", "Generate Tests")
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    actions.take(3).forEach { action ->
                        Button(
                            onClick = { onActionTriggered(action, codeText) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(action, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    actions.drop(3).forEach { action ->
                        Button(
                            onClick = { onActionTriggered(action, codeText) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(action, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                    // Mock Screenshot button
                    Button(
                        onClick = { /* Pick image logic */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryBackground),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, "", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Screenshot", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Result Section
            if (isLoading || lastAiResponse != null) {
                Text("Result:", color = SoftHighlight, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = SecondaryBackground,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isLoading) {
                            TypingIndicator()
                        } else if (lastAiResponse != null) {
                            SelectionContainer {
                                Text(
                                    text = lastAiResponse.text,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            IconButton(
                                onClick = { /* Logic to copy result */ },
                                modifier = Modifier.align(Alignment.End).background(GlassWhite, CircleShape)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 9. Interview Mode Screen ---
// --- 9. Interview Mode Screen ---
@Composable
fun InterviewModeScreen(viewModel: ChatViewModel, onBack: () -> Unit, onStartInterview: (String) -> Unit) {
    var step by remember { mutableStateOf(0) } // 0: Config, 1: Interview
    var selectedTech by remember { mutableStateOf("") }
    
    if (step == 0) {
        InterviewConfig(onStart = { tech ->
            selectedTech = tech
            onStartInterview(tech)
            step = 1
        })
    } else {
        ActiveInterviewScreen(viewModel, selectedTech, onBack)
    }
}

@Composable
fun ActiveInterviewScreen(viewModel: ChatViewModel, tech: String, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val chatMessages = viewModel.messages
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(PrimaryBackground).padding(16.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "", tint = Color.White) }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Interview: $tech", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Live Session", color = SoftHighlight, fontSize = 12.sp)
                }
                TextButton(
                    onClick = { 
                        viewModel.sendMessage("", "The interview is now complete. Please provide a detailed evaluation of my performance, covering: 1. Technical Knowledge, 2. Communication, 3. Confidence, 4. Correctness, and 5. Suggestions for improvement.")
                    }
                ) {
                    Text("End & Evaluate", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            ChatInputBar(
                text = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                }
            )
        },
        containerColor = PrimaryBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message)
            }
            if (isLoading) {
                item { TypingIndicator() }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: ChatViewModel, onBack: () -> Unit, onItemClick: () -> Unit) {
    val recentConversations by viewModel.recentConversations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(PrimaryBackground).statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "", tint = Color.White) }
                    Text("History", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                
                // Search Bar
                Surface(
                    color = GlassWhite,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search chats...", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, "", tint = TextGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        containerColor = PrimaryBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (recentConversations.isEmpty()) {
                item {
                    Text(
                        if (searchQuery.isEmpty()) "No history yet" else "No matches found",
                        color = TextGray,
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(recentConversations) { session ->
                    HistoryItem(
                        session.title, 
                        session.date, 
                        if(session.iconName == "Mic") Icons.Default.Mic else Icons.AutoMirrored.Filled.Chat,
                        modifier = Modifier.clickable { 
                            viewModel.clearChat()
                            viewModel.sendMessage("", "Continue our conversation about: ${session.title}")
                            onItemClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationScreen(viewModel: UserViewModel, onBack: () -> Unit) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBackground)
                    .padding(16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    "Notifications",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.clearNotifications() }) {
                    Text("Clear All", color = SoftHighlight)
                }
            }
        },
        containerColor = PrimaryBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications", color = TextGray)
                    }
                }
            } else {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Surface(
        color = SecondaryBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(notification.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(notification.content, color = TextGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ProfileScreen(viewModel: UserViewModel, onBack: () -> Unit) {
    val userName by viewModel.userName.collectAsState()
    val profileImage by viewModel.profileImage.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "", tint = Color.White) }
            Text("Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                if (isEditing) {
                    viewModel.updateUserName(editedName)
                }
                isEditing = !isEditing
            }) {
                Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, "", tint = SoftHighlight)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AccentPurple)
                .clickable {
                    // Logic to pick image could go here, for now we set a mock URL
                    viewModel.updateProfileImage("https://picsum.photos/200")
                },
            contentAlignment = Alignment.Center
        ) {
            if (profileImage != null) {
                AsyncImage(
                    model = profileImage,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditing) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Name", color = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentPurple
                )
            )
        } else {
            Text(userName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))

        ProfileMenuItem("Subscription", Icons.Default.CardMembership)
        ProfileMenuItem("Achievements", Icons.Default.EmojiEvents)
        ProfileMenuItem("Connected Devices", Icons.Default.Devices)
        ProfileMenuItem("Logout", Icons.Default.Logout, Color.Red)
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, color: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(RoundedCornerShape(12.dp)).background(GlassWhite).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, "", tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, "", tint = TextGray)
    }
}

@Composable
fun SettingsScreen(viewModel: UserViewModel, onBack: () -> Unit) {
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val voiceResponses by viewModel.voiceResponses.collectAsState()
    val dataSaving by viewModel.dataSaving.collectAsState()
    val appBackground by viewModel.appBackground.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "", tint = Color.White) }
            Text("Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsToggle("Haptic Feedback", hapticFeedback) { viewModel.updateHaptic(it) }
        SettingsToggle("Voice Responses", voiceResponses) { viewModel.updateVoice(it) }
        SettingsToggle("Data Saving", dataSaving) { viewModel.updateDataSaving(it) }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Personalization", color = SoftHighlight, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("App Background", color = Color.White)
            Row {
                Button(
                    onClick = { viewModel.updateBackground("Default") },
                    colors = ButtonDefaults.buttonColors(containerColor = if(appBackground != "Gradient") AccentPurple else GlassWhite),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Default")
                }
                Button(
                    onClick = { viewModel.updateBackground("Gradient") },
                    colors = ButtonDefaults.buttonColors(containerColor = if(appBackground == "Gradient") AccentPurple else GlassWhite)
                ) {
                    Text("Gradient")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("AI Model Preference", color = SoftHighlight, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Nexora currently uses a hybrid Groq + Gemini router for best performance.",
            color = TextGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SettingsToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(PrimaryBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onBack) { Text("Back") }
        }
    }
}

@Composable
fun TopicExplorerScreen(topicName: String, viewModel: ChatViewModel, onBack: () -> Unit) {
    val modules = when (topicName) {
        "Android" -> listOf(
            "Architecture (MVVM/Clean)", "Jetpack Compose State", "Side Effects & Recomposition",
            "Coroutines & Flow", "WorkManager & Background", "Hilt Dependency Injection",
            "Retrofit & Ktor", "Room DB & Migrations", "Performance Profiling",
            "UI Testing (Espresso)", "MotionLayout Animations", "Biometric Security",
            "Custom Views & Canvas", "Modularization Strategies", "Bluetooth & BLE",
            "Material 3 Components", "Memory Leak Detection", "Navigation Component",
            "Activities & Fragments", "Android View System", "Intents & Intent Filters",
            "Broadcast Receivers", "Content Providers", "Data Binding & View Binding",
            "Paging Library", "App Startup Library", "CameraX API", "Google Maps Integration",
            "Firebase Integration", "Google Sign-In", "In-App Purchases", "Push Notifications",
            "ProGuard & R8", "App Bundle & Dynamic Delivery", "Accessibility in Android",
            "Internationalization (i18n)", "Android Jetpack Glance", "WorkManager (Advanced)",
            "Hilt Multi-module DI", "Ktor vs Retrofit", "SharedFlow & StateFlow",
            "Android TV & WearOS", "Automotive & Android Auto"
        )
        "DSA" -> listOf(
            "Advanced Graph Algorithms", "Dynamic Programming (DP)", "Segment Trees & Fenwick",
            "Bitmasking Techniques", "Trie & Suffix Trees", "Sliding Window Patterns",
            "Backtracking & Recursion", "Heap & Priority Queues", "KMP String Matching",
            "Disjoint Set Union (DSU)", "Topological Sorting", "Binary Search (Hard)",
            "Heavy-Light Decomposition", "Max Flow / Min Cut", "Geometry Algorithms",
            "Sqrt Decomposition", "B+ Trees & Red-Black", "Rabin-Karp Hashing"
        )
        "System Design" -> listOf(
            "Microservices Architecture", "Distributed Caching (Redis)", "Load Balancing (L7/L4)",
            "Database Sharding", "Kafka & Message Queues", "CAP & PACELC Theorem",
            "API Gateway Design", "CDN & Edge Computing", "OAuth2 & JWT Security",
            "Observability (Grafana)", "Rate Limiting Patterns", "Global Scalability",
            "Consistency Models", "Gossip Protocol", "Vector Clocks",
            "Consistent Hashing", "Bloom Filters", "Service Discovery"
        )
        "SQL" -> listOf(
            "Query Optimization", "Window Functions (Rank)", "CTEs & Recursive SQL",
            "ACID & Transactions", "Deadlock Prevention", "Indexing (B-Tree/Hash)",
            "SQL Injection Defense", "Stored Procedures", "Partitioning Strategies",
            "JSON Data in SQL", "Full Text Search", "Materialized Views",
            "Query Execution Plans", "Database Replication", "Triggers & Hooks",
            "Hierarchical Queries", "Database Normalization", "Isolation Levels"
        )
        "Kotlin" -> listOf(
            "Coroutines & Flow", "Sealed Classes", "Extension Functions", 
            "Delegated Properties", "Inline/Value Classes", "DSL Construction",
            "Multiplatform (KMP)", "Reflection", "Ktor Framework",
            "Kotlin Metaprogramming", "KSP (Symbol Processing)", "Contracts API",
            "Custom Type-Safe Builders", "Scope Functions Deep Dive", "Generics & Reified"
        )
        "Java" -> listOf(
            "Generics", "Streams API", "Optional", "Concurrency (Locks/Phasers)",
            "Reflection API", "JVM Internals", "Garbage Collection", "Maven/Gradle",
            "Java Memory Model", "JIT Compilation", "Project Loom (Virtual Threads)",
            "GraalVM", "NIO & Selectors", "Annotation Processing", "Lambda Internals"
        )
        "Cloud" -> listOf(
            "Docker & K8s", "Serverless (AWS Lambda)", "CI/CD Pipelines",
            "Cloud Storage", "IaC (Terraform)", "VPC & Networking",
            "Service Mesh (Istio)", "Auto-scaling Policies", "Cost Optimization",
            "Disaster Recovery Patterns", "Hybrid Cloud Setup", "Cloud Security Groups"
        )
        "Security" -> listOf(
            "Encryption (AES/RSA)", "OAuth2 & OIDC", "Penetration Testing",
            "OWASP Top 10", "Network Security", "Biometrics",
            "Zero Trust Architecture", "TLS/SSL Handshake", "API Security Best Practices",
            "Threat Modeling", "Content Security Policy", "SQLi & XSS Defense"
        )
        else -> emptyList()
    }

    val chatMessages = viewModel.messages
    val isLoading by viewModel.isLoading.collectAsState()
    val lastResponse = chatMessages.lastOrNull { !it.isUser && it.text != "Hi! I'm Nexora.\nHow can I help you today?" }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(PrimaryBackground).padding(16.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "", tint = Color.White) }
                Column {
                    Text(topicName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Advanced Syllabus & Q&A", color = SoftHighlight, fontSize = 12.sp)
                }
            }
        },
        containerColor = PrimaryBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Master $topicName - Select a Module:", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 16.dp))
            
            // Grid/List of modules
            modules.chunked(2).forEach { rowModules ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowModules.forEach { module ->
                        Surface(
                            onClick = { 
                                viewModel.clearChat()
                                viewModel.sendMessage("", """
                                    Perform a deep dive into '$module' for $topicName. 
                                    Format your response strictly as follows:
                                    # $module
                                    ## Core Concept & Overview
                                    ## Advanced Important Sub-topics
                                    ## Deep Technical Explanation
                                    ## Interview Q&A Section (Top 5 complex questions with detailed answers)
                                """.trimIndent())
                            },
                            color = SecondaryBackground,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhite),
                            modifier = Modifier.weight(1f).padding(vertical = 6.dp)
                        ) {
                            Text(
                                module, 
                                color = Color.White, 
                                modifier = Modifier.padding(16.dp), 
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                minLines = 2
                            )
                        }
                    }
                    if (rowModules.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Inline Result Section
            if (isLoading || lastResponse != null) {
                Text("Study Material & Q&A:", color = SoftHighlight, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = SecondaryBackground,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (isLoading) {
                            TypingIndicator()
                        } else if (lastResponse != null) {
                            SelectionContainer {
                                Text(
                                    text = formatAIText(lastResponse.text),
                                    color = Color.White,
                                    lineHeight = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Select text to copy details", color = TextGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { /* Copy already handled by selection */ },
                                    modifier = Modifier.background(GlassWhite, CircleShape)
                                ) {
                                    Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewConfig(onStart: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(PrimaryBackground).padding(24.dp).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Interview Mode", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(40.dp))
        
        Text("Select Technology", color = TextGray)
        Spacer(modifier = Modifier.height(16.dp))
        val techs = listOf("Java", "Android", "Kotlin", "DSA", "SQL")
        techs.forEach { tech ->
            Button(
                onClick = { onStart(tech) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryBackground)
            ) {
                Text(tech, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text("Choose Difficulty", color = TextGray)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {}) { Text("Easy") }
            Button(onClick = {}) { Text("Medium") }
            Button(onClick = {}) { Text("Hard") }
        }
    }
}
