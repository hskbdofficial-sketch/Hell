package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Screen Enumeration
enum class AppScreen {
    BOOT, AUTH, MARKETPLACE, JOBS, WALLET, KYC, ADMIN, PROFILE
}

@Composable
fun MainLayout(viewModel: StoreViewModel) {
    val currentEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val eventMessage by viewModel.uiEventMessage.collectAsState(initial = "")

    var currentScreen by remember { mutableStateOf(AppScreen.BOOT) }
    var activeTab by remember { mutableStateOf("MARKETPLACE") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Listen to event messages
    LaunchedEffect(eventMessage) {
        if (eventMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(eventMessage)
        }
    }

    // Handle screen routing on user auth state changes
    LaunchedEffect(currentEmail, user) {
        if (currentScreen == AppScreen.BOOT) return@LaunchedEffect
        if (currentEmail == null) {
            currentScreen = AppScreen.AUTH
        } else {
            if (currentScreen == AppScreen.AUTH) {
                currentScreen = AppScreen.MARKETPLACE
                activeTab = "MARKETPLACE"
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = CyberBlack,
        topBar = {
            if (currentScreen != AppScreen.BOOT && currentScreen != AppScreen.AUTH) {
                CyberTopBar(
                    user = user,
                    onNavigate = { screen ->
                        currentScreen = screen
                        if (screen == AppScreen.MARKETPLACE) activeTab = "MARKETPLACE"
                        if (screen == AppScreen.JOBS) activeTab = "JOBS"
                        if (screen == AppScreen.WALLET) activeTab = "WALLET"
                        if (screen == AppScreen.PROFILE) activeTab = "PROFILE"
                        if (screen == AppScreen.ADMIN) activeTab = "ADMIN"
                    },
                    currentScreen = currentScreen
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.BOOT && currentScreen != AppScreen.AUTH) {
                CyberBottomNav(
                    activeTab = activeTab,
                    isAdmin = user?.role == "Admin",
                    onTabSelected = { tab ->
                        activeTab = tab
                        currentScreen = when (tab) {
                            "MARKETPLACE" -> AppScreen.MARKETPLACE
                            "JOBS" -> AppScreen.JOBS
                            "WALLET" -> AppScreen.WALLET
                            "PROFILE" -> AppScreen.PROFILE
                            "ADMIN" -> AppScreen.ADMIN
                            else -> AppScreen.MARKETPLACE
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .align(Alignment.TopCenter)
                ) {
                    when (screen) {
                        AppScreen.BOOT -> BootLoaderScreen(onFinished = {
                            currentScreen = if (currentEmail == null) AppScreen.AUTH else AppScreen.MARKETPLACE
                        })
                        AppScreen.AUTH -> AuthGateScreen(viewModel)
                        AppScreen.MARKETPLACE -> MarketplaceScreen(viewModel)
                        AppScreen.JOBS -> JobsScreen(viewModel)
                        AppScreen.WALLET -> WalletScreen(viewModel)
                        AppScreen.KYC -> KYCScreen(viewModel, onBack = { currentScreen = AppScreen.PROFILE })
                        AppScreen.PROFILE -> ProfileScreen(viewModel, onNavigateKYC = { currentScreen = AppScreen.KYC })
                        AppScreen.ADMIN -> AdminDashboardScreen(viewModel)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. BOOT LOADER SCREEN (Splash)
// ==========================================
@Composable
fun BootLoaderScreen(onFinished: () -> Unit) {
    val terminalLogs = remember { mutableStateListOf<String>() }
    var bootProgress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val bootSteps = listOf(
            "CONNECTING TO SECURE FIREBASE AUTH SERVER...",
            "INITIALIZING SUPABASE POSTGRESQL CLUSTER...",
            "VERIFYING HELLSEC STORAGE BACKEND...",
            "SYNCING ENCRYPTED ESCROW SMART CONTRACTS...",
            "VALIDATING LOCAL AUDIT LOGGER SERVICE...",
            "ESTABLISHING CRYPTO GRAPHIC HANDSHAKE...",
            "HELLSEC MARKETPLACE CLIENT VERIFIED: COMPLIANT"
        )

        for (step in bootSteps) {
            terminalLogs.add(">> $step")
            delay(400)
            bootProgress += 1f / bootSteps.size
        }
        delay(600)
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonGreen.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            radius = size.width * 0.9f
                        )
                    }
                    .border(1.dp, NeonGreen, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "Hellsec Shield",
                    tint = NeonGreen,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HELLSEC STORE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = NeonGreen,
                letterSpacing = 2.sp
            )
            Text(
                text = "SECURE CYBER MARKETPLACE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = CyberCyan,
                letterSpacing = 1.sp
            )
        }

        // Terminal Output Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .background(CyberDarkSurface)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) {
                items(terminalLogs.reversed()) { log ->
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        color = if (log.contains("VERIFIED")) NeonGreen else TextLight,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { bootProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = NeonGreen,
                trackColor = CyberMediumSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "ENCRYPTED SSL SESSION v3.5",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

// ==========================================
// 2. AUTH SCREEN GATE
// ==========================================
@Composable
fun AuthGateScreen(viewModel: StoreViewModel) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Register details
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Bangladesh") }
    var dateOfBirth by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("User") } // User or Freelancer

    var errorMessage by remember { mutableStateOf("") }
    var showRecoverDialog by remember { mutableStateOf(false) }
    var recoverEmail by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("What was the name of your first pet?") }
    var securityAnswer by remember { mutableStateOf("") }
    var recoverAnswer by remember { mutableStateOf("") }
    var recoverNewPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(CyberBlack, CyberDarkSurface, CyberMediumSurface)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            
            // Glowing Lock & Shield Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(NeonGreen.copy(alpha = 0.1f), CircleShape)
                    .border(BorderStroke(1.5.dp, NeonGreen), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Terminal,
                    contentDescription = "Secure Key",
                    tint = NeonGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isLoginMode) "SECURE LOG IN" else "CREATE ACCOUNT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = androidx.compose.ui.graphics.Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "HELLSEC STORE SECURED GATEWAY",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // SECURE GATEWAY INDICATOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberMediumSurface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = "Secured Connection",
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HARDENED MILITARY-GRADE AES-GCM SECURITY ACTIVE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (errorMessage.isNotEmpty()) {
                Surface(
                    color = NeonPink.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonPink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Error, "Error", tint = NeonPink, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage, color = TextLight, fontSize = 13.sp)
                    }
                }
            }



            if (isLoginMode) {
                // Email Field
                CyberTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "EMAIL ENCRYPTED ADDRESS",
                    icon = Icons.Outlined.Email,
                    tag = "login_email_input"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                CyberTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "ACCOUNT ACCESS KEY (PASSWORD)",
                    icon = Icons.Outlined.Lock,
                    isPassword = true,
                    tag = "login_password_input"
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Recover Password",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { showRecoverDialog = true }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = ""
                        if (email.isEmpty() || password.isEmpty()) {
                            errorMessage = "Please enter email and security access key."
                            return@Button
                        }
                        viewModel.loginUser(email, password) { success, msg ->
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "ENTER SECURED MARKETPLACE",
                        color = CyberBlack,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // Register fields
                CyberTextField(value = firstName, onValueChange = { firstName = it }, label = "FIRST NAME", icon = Icons.Outlined.Person, tag = "first_name")
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(value = lastName, onValueChange = { lastName = it }, label = "LAST NAME", icon = Icons.Outlined.Person, tag = "last_name")
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(value = username, onValueChange = { username = it }, label = "SECURE USERNAME", icon = Icons.Outlined.AlternateEmail, tag = "username")
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(value = email, onValueChange = { email = it }, label = "EMAIL ADDRESS", icon = Icons.Outlined.Email, tag = "email")
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(value = password, onValueChange = { password = it }, label = "ACCESS PASSWORD", icon = Icons.Outlined.Lock, isPassword = true, tag = "password")
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = "MOBILE NUMBER", icon = Icons.Outlined.Phone, keyboardType = KeyboardType.Phone, tag = "phone")
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(value = dateOfBirth, onValueChange = { dateOfBirth = it }, label = "DATE OF BIRTH (YYYY-MM-DD)", icon = Icons.Outlined.CalendarToday, tag = "dob")
                Spacer(modifier = Modifier.height(12.dp))

                // Security Recovery Fields
                CyberTextField(
                    value = securityQuestion,
                    onValueChange = { securityQuestion = it },
                    label = "SECURITY QUESTION (FOR RECOVERY)",
                    icon = Icons.Outlined.Help,
                    tag = "security_question"
                )
                Spacer(modifier = Modifier.height(12.dp))
                CyberTextField(
                    value = securityAnswer,
                    onValueChange = { securityAnswer = it },
                    label = "SECURITY ANSWER",
                    icon = Icons.Outlined.QuestionAnswer,
                    tag = "security_answer"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Role Selector
                Text(
                    "CHOOSE SECURE NETWORK ROLE:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "User" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRole == "User") CyberLightSurface else CyberDarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedRole == "User") NeonGreen else CyberMediumSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.ShoppingCart, "User", tint = if (selectedRole == "User") NeonGreen else TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("STANDARD BUYER", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextLight)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "Freelancer" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRole == "Freelancer") CyberLightSurface else CyberDarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedRole == "Freelancer") CyberCyan else CyberMediumSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Code, "Freelancer", tint = if (selectedRole == "Freelancer") CyberCyan else TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("FREELANCER", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextLight)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                 Button(
                    onClick = {
                        errorMessage = ""
                        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || mobileNumber.isEmpty() || dateOfBirth.isEmpty() || securityAnswer.isEmpty()) {
                            errorMessage = "All credentials and security recovery settings are required."
                            return@Button
                        }
                        viewModel.registerUser(
                            firstName = firstName,
                            lastName = lastName,
                            username = username,
                            email = email,
                            passwordHash = password,
                            mobileNumber = mobileNumber,
                            country = country,
                            dateOfBirth = dateOfBirth,
                            role = selectedRole,
                            securityQuestion = securityQuestion,
                            securityAnswer = securityAnswer
                        ) { success, msg ->
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "SUBMIT ENCRYPTED SIGNUP (+10 SKULL)",
                        color = CyberBlack,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isLoginMode) "New Operator? " else "Already authorized? ",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Text(
                    text = if (isLoginMode) "Initialize Account" else "Proceed to Log In",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        isLoginMode = !isLoginMode
                        errorMessage = ""
                    }
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Recover Dialog
    if (showRecoverDialog) {
        var recoverAnswerLocal by remember { mutableStateOf("") }
        var recoverNewPasswordLocal by remember { mutableStateOf("") }
        var recoverError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecoverDialog = false },
            containerColor = CyberDarkSurface,
            title = {
                Text(
                    "SECURE PROTOCOL RECOVERY",
                    fontFamily = FontFamily.Monospace,
                    color = CyberCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Verify your security response credentials below to reset your secure account password key.",
                        color = TextLight,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (recoverError.isNotEmpty()) {
                        Text(
                            text = "❌ $recoverError",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    CyberTextField(
                        value = recoverEmail,
                        onValueChange = { recoverEmail = it },
                        label = "REGISTERED EMAIL",
                        icon = Icons.Outlined.Email,
                        tag = "recover_email"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CyberTextField(
                        value = recoverAnswerLocal,
                        onValueChange = { recoverAnswerLocal = it },
                        label = "SECURITY ANSWER",
                        icon = Icons.Outlined.QuestionAnswer,
                        tag = "recover_answer"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CyberTextField(
                        value = recoverNewPasswordLocal,
                        onValueChange = { recoverNewPasswordLocal = it },
                        label = "NEW SECURE ACCESS PASSWORD",
                        icon = Icons.Outlined.Lock,
                        isPassword = true,
                        tag = "recover_new_password"
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showRecoverDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMediumSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", color = TextLight, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            recoverError = ""
                            if (recoverEmail.isEmpty() || recoverAnswerLocal.isEmpty() || recoverNewPasswordLocal.isEmpty()) {
                                recoverError = "All fields are required for recovery verification."
                                return@Button
                            }
                            viewModel.recoverPassword(recoverEmail, recoverAnswerLocal, recoverNewPasswordLocal) { success, msg ->
                                if (success) {
                                    viewModel.triggerMessage(msg)
                                    showRecoverDialog = false
                                    // Reset fields
                                    recoverAnswerLocal = ""
                                    recoverNewPasswordLocal = ""
                                } else {
                                    recoverError = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RECOVER", color = CyberBlack, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

// ==========================================
// 3. SECURE CYBER TOP BAR
// ==========================================
@Composable
fun CyberTopBar(
    user: UserEntity?,
    onNavigate: (AppScreen) -> Unit,
    currentScreen: AppScreen
) {
    Surface(
        color = CyberDarkSurface,
        border = BorderStroke(0.dp, Color.Transparent),
        modifier = Modifier.drawBehind {
            drawLine(
                color = NeonGreen.copy(alpha = 0.4f),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigate(AppScreen.PROFILE) }
            ) {
                // Avatar Frame
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CyberMediumSurface)
                        .border(1.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.profilePicUri?.isNotEmpty() == true) {
                        AsyncImage(
                            model = user.profilePicUri,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "No pic",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = user?.username ?: "SEC_OP",
                        color = TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (user?.kycStatus == "APPROVED") NeonGreen else NeonPink)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (user?.role == "Admin") "ADMIN" else "KYC: ${user?.kycStatus}",
                            fontSize = 9.sp,
                            color = if (user?.kycStatus == "APPROVED") NeonGreen else TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Wallet Skull balance pill
            Surface(
                color = CyberBlack,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f)),
                modifier = Modifier.clickable { onNavigate(AppScreen.WALLET) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalActivity,
                        contentDescription = "Skull Currency",
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${user?.skullBalance ?: 0} SKULL",
                        color = NeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. BOTTOM NAVIGATION
// ==========================================
@Composable
fun CyberBottomNav(
    activeTab: String,
    isAdmin: Boolean,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = CyberDarkSurface,
        modifier = Modifier.drawBehind {
            drawLine(
                color = CyberCyan.copy(alpha = 0.4f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2f
            )
        }
    ) {
        NavigationBarItem(
            selected = activeTab == "MARKETPLACE",
            onClick = { onTabSelected("MARKETPLACE") },
            icon = { Icon(Icons.Filled.Storefront, "Market") },
            label = { Text("Market", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = CyberMediumSurface,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = activeTab == "JOBS",
            onClick = { onTabSelected("JOBS") },
            icon = { Icon(Icons.Filled.Work, "Jobs") },
            label = { Text("Escrow", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = CyberMediumSurface,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = activeTab == "WALLET",
            onClick = { onTabSelected("WALLET") },
            icon = { Icon(Icons.Filled.Wallet, "Wallet") },
            label = { Text("Wallet", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = CyberMediumSurface,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = activeTab == "PROFILE",
            onClick = { onTabSelected("PROFILE") },
            icon = { Icon(Icons.Filled.ManageAccounts, "Profile") },
            label = { Text("Profile", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = CyberMediumSurface,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        if (isAdmin) {
            NavigationBarItem(
                selected = activeTab == "ADMIN",
                onClick = { onTabSelected("ADMIN") },
                icon = { Icon(Icons.Filled.AdminPanelSettings, "Admin") },
                label = { Text("Admin", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberYellow,
                    selectedTextColor = CyberYellow,
                    indicatorColor = CyberMediumSurface,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

// ==========================================
// 5. DIGITAL PRODUCT MARKETPLACE SCREEN
// ==========================================
@Composable
fun MarketplaceScreen(viewModel: StoreViewModel) {
    val products by viewModel.approvedProducts.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf(
        "ALL", "Security Tools", "AI Tools", "Python Tools",
        "Android Apps", "Desktop Software", "Source Code",
        "APIs", "Templates", "Digital Assets"
    )

    val filteredProducts = products.filter {
        (selectedCategory == "ALL" || it.category == selectedCategory) &&
                (it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.tags.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Category Headers
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDarkSurface)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_bar"),
                placeholder = { Text("Search decentralized nodes...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = CyberCyan) },
                textStyle = LocalTextStyle.current.copy(color = TextLight),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberLightSurface,
                    focusedContainerColor = CyberBlack,
                    unfocusedContainerColor = CyberBlack
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categories horizontal bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonGreen else CyberMediumSurface)
                            .border(
                                1.dp,
                                if (isSelected) NeonGreen else CyberLightSurface,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) CyberBlack else TextLight,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Add Product FAB Trigger for verified users/freelancers
        Box(modifier = Modifier.fillMaxSize()) {
            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.SentimentVeryDissatisfied,
                    title = "NO NODES LOCATED",
                    desc = "No cybersecurity products fit current filters. Post your own!"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts) { prod ->
                        ProductCard(
                            product = prod,
                            onDetailClick = { selectedProduct = prod }
                        )
                    }
                }
            }

            // Add Product FAB
            if (currentUser != null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = NeonGreen,
                    contentColor = CyberBlack,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("add_product_fab")
                ) {
                    Icon(Icons.Filled.Add, "Add Product")
                }
            }
        }
    }

    // Modal Details Screen for Product Selection
    if (selectedProduct != null) {
        ProductDetailsDialog(
            product = selectedProduct!!,
            viewModel = viewModel,
            onDismiss = { selectedProduct = null }
        )
    }

    // Publish New Product Dialog
    if (showAddDialog) {
        AddProductDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun ProductCard(product: ProductEntity, onDetailClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberLightSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image with fallback loader
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberMediumSurface)
            ) {
                AsyncImage(
                    model = product.thumbnailUri,
                    contentDescription = "Thumb",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Rating overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(CyberBlack.copy(alpha = 0.8f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        "★ ${String.format("%.1f", product.avgRating)}",
                        color = CyberYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(CyberMediumSurface, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(product.category, color = CyberCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }

                    Text(
                        "${product.price} SKULL",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ==========================================
// 6. MICRO ESCROW JOB MARKETPLACE
// ==========================================
@Composable
fun JobsScreen(viewModel: StoreViewModel) {
    val openJobs by viewModel.openJobs.collectAsStateWithLifecycle()
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showPostJobDialog by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<JobEntity?>(null) }
    var isMyJobsTab by remember { mutableStateOf(false) }

    val displayedJobs = if (isMyJobsTab && currentUser != null) {
        allJobs.filter {
            it.clientEmail == currentUser?.email || it.freelancerEmail == currentUser?.email
        }
    } else {
        openJobs
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle tabs for browsing vs client/contract list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDarkSurface)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { isMyJobsTab = false }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "OPEN SECURITY CONTRACTS",
                    color = if (!isMyJobsTab) NeonGreen else TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { isMyJobsTab = true }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "MY CONSOLE CONTRACTS",
                    color = if (isMyJobsTab) NeonGreen else TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (displayedJobs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.Terminal,
                    title = "NO CYBER DIRECTIVES FOUND",
                    desc = if (isMyJobsTab) "You have no active escrow contracts. Go apply or list!" else "No open contracts. System standby."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedJobs) { job ->
                        JobCard(job = job, onClick = { selectedJob = job })
                    }
                }
            }

            // Post Job FAB (buyer profile)
            if (currentUser != null) {
                FloatingActionButton(
                    onClick = { showPostJobDialog = true },
                    containerColor = NeonGreen,
                    contentColor = CyberBlack,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("post_job_fab")
                ) {
                    Icon(Icons.Filled.Add, "List Job")
                }
            }
        }
    }

    if (selectedJob != null) {
        JobDetailsDialog(
            job = selectedJob!!,
            viewModel = viewModel,
            onDismiss = { selectedJob = null }
        )
    }

    if (showPostJobDialog) {
        PostJobDialog(viewModel = viewModel, onDismiss = { showPostJobDialog = false })
    }
}

@Composable
fun JobCard(job: JobEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("job_card_${job.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberLightSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "ESCROW CONSOLE DIRECTIVE #${job.id}",
                    fontFamily = FontFamily.Monospace,
                    color = CyberCyan,
                    fontSize = 10.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            when (job.status) {
                                "OPEN" -> NeonGreen.copy(alpha = 0.1f)
                                "IN_PROGRESS" -> CyberCyan.copy(alpha = 0.1f)
                                "DISPUTED" -> NeonPink.copy(alpha = 0.1f)
                                else -> CyberMediumSurface
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = job.status,
                        color = when (job.status) {
                            "OPEN" -> NeonGreen
                            "IN_PROGRESS" -> CyberCyan
                            "DISPUTED" -> NeonPink
                            else -> TextLight
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.title,
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = job.description,
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, "Time", tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deadline: ${job.deadline}", color = TextMuted, fontSize = 11.sp)
                }

                Text(
                    "${job.budget} SKULL",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ==========================================
// 7. BKASH SKULL WALLET NODE
// ==========================================
@Composable
fun WalletScreen(viewModel: StoreViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val topups by viewModel.allTopups.collectAsStateWithLifecycle()
    val withdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var walletTab by remember { mutableStateOf("DEPOSITS") }

    val myTopups = topups.filter { it.userEmail == user?.email }
    val myWithdrawals = withdrawals.filter { it.userEmail == user?.email }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Balance Display Screen
        Surface(
            color = CyberDarkSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, NeonGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = NeonGreen,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "SECURE WALLET CORES BALANCE",
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    "${user?.skullBalance ?: 0} SKULL",
                    fontFamily = FontFamily.Monospace,
                    color = NeonGreen,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "Exchange Rate Locked: 1 SKULL = 1 BDT",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showDepositDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("deposit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("DEPOSIT (bKash)", color = CyberBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("withdraw_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("WITHDRAW", color = CyberBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History logs
        Text(
            "TRANSACTION HISTORY LEDGER",
            fontFamily = FontFamily.Monospace,
            color = TextLight,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle deposits vs withdrawals
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { walletTab = "DEPOSITS" },
                colors = ButtonDefaults.buttonColors(containerColor = if (walletTab == "DEPOSITS") CyberMediumSurface else Color.Transparent),
                modifier = Modifier.weight(1f)
            ) {
                Text("bKash Deposits", color = if (walletTab == "DEPOSITS") NeonGreen else TextMuted, fontSize = 11.sp)
            }
            Button(
                onClick = { walletTab = "WITHDRAWALS" },
                colors = ButtonDefaults.buttonColors(containerColor = if (walletTab == "WITHDRAWALS") CyberMediumSurface else Color.Transparent),
                modifier = Modifier.weight(1f)
            ) {
                Text("bKash Withdrawals", color = if (walletTab == "WITHDRAWALS") NeonGreen else TextMuted, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ledger Column
        if (walletTab == "DEPOSITS") {
            if (myTopups.isEmpty()) {
                Text("No top-up entries logged.", color = TextMuted, modifier = Modifier.padding(16.dp), fontSize = 12.sp)
            } else {
                myTopups.forEach { tp ->
                    LedgerItem(
                        title = "bKash Top-up Request",
                        amount = "+${tp.amount} SKULL",
                        status = tp.status,
                        subText = "Txn ID: ${tp.transactionId} | Sender: ${tp.senderNumber}",
                        timestamp = tp.timestamp
                    )
                }
            }
        } else {
            if (myWithdrawals.isEmpty()) {
                Text("No withdrawal entries logged.", color = TextMuted, modifier = Modifier.padding(16.dp), fontSize = 12.sp)
            } else {
                myWithdrawals.forEach { wd ->
                    LedgerItem(
                        title = "bKash Withdrawal",
                        amount = "-${wd.amount + wd.fee} SKULL",
                        status = wd.status,
                        subText = "Target Phone: ${wd.receiverNumber} (Fee: ${wd.fee} SKULL)",
                        timestamp = wd.timestamp
                    )
                }
            }
        }
    }

    if (showDepositDialog) {
        DepositBKashDialog(viewModel = viewModel, onDismiss = { showDepositDialog = false })
    }

    if (showWithdrawDialog) {
        WithdrawBKashDialog(viewModel = viewModel, onDismiss = { showWithdrawDialog = false })
    }
}

@Composable
fun LedgerItem(title: String, amount: String, status: String, subText: String, timestamp: Long) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberLightSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subText, color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    color = if (amount.startsWith("+")) NeonGreen else NeonPink,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(
                    text = status,
                    color = when (status) {
                        "APPROVED" -> NeonGreen
                        "PENDING" -> CyberYellow
                        else -> NeonPink
                    },
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 8. PROFILE & ENCRYPTED KYC SCREEN
// ==========================================
@Composable
fun ProfileScreen(viewModel: StoreViewModel, onNavigateKYC: () -> Unit) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportUserEmail by remember { mutableStateOf("") }
    var reportReason by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar Frame
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(CyberMediumSurface)
                .border(2.dp, NeonGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (user?.profilePicUri?.isNotEmpty() == true) {
                AsyncImage(
                    model = user?.profilePicUri,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Profile Pic",
                    tint = NeonGreen,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            user?.username ?: "AGENT_OP",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextLight
        )

        Text(
            user?.email ?: "",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Role & KYC status cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, CyberLightSurface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SYSTEM ROLE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        user?.role?.uppercase() ?: "USER",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateKYC() },
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, if (user?.kycStatus == "APPROVED") NeonGreen else CyberCyan)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("KYC VERIFICATION", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        user?.kycStatus ?: "NOT_STARTED",
                        color = if (user?.kycStatus == "APPROVED") NeonGreen else CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Details Fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
            border = BorderStroke(1.dp, CyberLightSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "DATABASE REGISTRY INFO",
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileInfoRow("First Name", user?.firstName ?: "")
                ProfileInfoRow("Last Name", user?.lastName ?: "")
                ProfileInfoRow("Mobile", user?.mobileNumber ?: "")
                ProfileInfoRow("Country", user?.country ?: "")
                ProfileInfoRow("Date of Birth", user?.dateOfBirth ?: "")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Options list
        Button(
            onClick = { showReportDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyberMediumSurface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.Report, "Report", tint = NeonPink)
            Spacer(modifier = Modifier.width(8.dp))
            Text("REPORT SUSPICIOUS OPERATOR", color = NeonPink, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.logoutUser() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.ExitToApp, "Logout", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOGOUT SYSTEM SESSION", color = Color.White, fontFamily = FontFamily.Monospace)
        }
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = CyberDarkSurface,
            title = { Text("REPORT OPERATOR DISCREPANCY", fontFamily = FontFamily.Monospace, color = NeonPink) },
            text = {
                Column {
                    OutlinedTextField(
                        value = reportUserEmail,
                        onValueChange = { reportUserEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = TextLight),
                        label = { Text("Operator Email to Report") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPink)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = TextLight),
                        label = { Text("Violation / Security Reason") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPink)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = TextLight),
                        label = { Text("Full Violation Details") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPink)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportUserEmail.isNotEmpty() && reportReason.isNotEmpty()) {
                            viewModel.submitReport(reportUserEmail, reportReason, reportDesc, "")
                        }
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text("SUBMIT EVIDENCE", color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// ==========================================
// 9. ENCRYPTED KYC CAM SCANNER
// ==========================================
@Composable
fun KYCScreen(viewModel: StoreViewModel, onBack: () -> Unit) {
    var nidNumber by remember { mutableStateOf("") }
    var selectedStep by remember { mutableIntStateOf(1) } // 1: NID front, 2: NID back, 3: selfie
    var frontPhoto by remember { mutableStateOf("") }
    var backPhoto by remember { mutableStateOf("") }
    var selfiePhoto by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextLight)
            }
            Text("KYC DATABASE REGISTRY", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = CyberCyan)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KYCProgressStep(1, "NID Front", selectedStep)
            KYCProgressStep(2, "NID Back", selectedStep)
            KYCProgressStep(3, "Selfie", selectedStep)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fake HUD Camera preview framing!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .border(2.dp, NeonGreen, RoundedCornerShape(12.dp))
                .background(CyberDarkSurface),
            contentAlignment = Alignment.Center
        ) {
            // Draw futuristic scanning lines overlays
            Canvas(modifier = Modifier.fillMaxSize()) {
                val h = size.height
                val w = size.width
                // Corners
                drawLine(NeonGreen, Offset(10f, 10f), Offset(40f, 10f), strokeWidth = 4f)
                drawLine(NeonGreen, Offset(10f, 10f), Offset(10f, 40f), strokeWidth = 4f)

                drawLine(NeonGreen, Offset(w - 10f, 10f), Offset(w - 40f, 10f), strokeWidth = 4f)
                drawLine(NeonGreen, Offset(w - 10f, 10f), Offset(w - 10f, 40f), strokeWidth = 4f)

                drawLine(NeonGreen, Offset(10f, h - 10f), Offset(40f, h - 10f), strokeWidth = 4f)
                drawLine(NeonGreen, Offset(10f, h - 10f), Offset(10f, h - 40f), strokeWidth = 4f)

                drawLine(NeonGreen, Offset(w - 10f, h - 10f), Offset(w - 40f, h - 10f), strokeWidth = 4f)
                drawLine(NeonGreen, Offset(w - 10f, h - 10f), Offset(w - 10f, h - 40f), strokeWidth = 4f)
            }

            val curPhoto = when (selectedStep) {
                1 -> frontPhoto
                2 -> backPhoto
                else -> selfiePhoto
            }

            if (curPhoto.isNotEmpty()) {
                AsyncImage(
                    model = curPhoto,
                    contentDescription = "Doc Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedStep == 3) Icons.Filled.Face else Icons.Filled.DocumentScanner,
                        contentDescription = "Scan icon",
                        tint = CyberCyan,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ALIGN DOCUMENT WITHIN FRAME",
                        color = TextLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Click capture to scan securely",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Generate a realistic, gorgeous sample placeholder matching document style
                val generatedUrl = when (selectedStep) {
                    1 -> "https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?auto=format&fit=crop&q=80&w=300"
                    2 -> "https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&q=80&w=300"
                    else -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=300"
                }
                if (selectedStep == 1) frontPhoto = generatedUrl
                if (selectedStep == 2) backPhoto = generatedUrl
                if (selectedStep == 3) selfiePhoto = generatedUrl
                viewModel.triggerMessage("Document captured successfully.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Camera, "Capture", tint = CyberBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SECURE CAPTURE SCAN", color = CyberBlack, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedStep < 3) {
            Button(
                onClick = {
                    val curPhoto = if (selectedStep == 1) frontPhoto else backPhoto
                    if (curPhoto.isEmpty()) {
                        viewModel.triggerMessage("Capture photo before proceeding to next node.")
                    } else {
                        selectedStep += 1
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberMediumSurface)
            ) {
                Text("PROCEED NEXT STEP", color = TextLight, fontFamily = FontFamily.Monospace)
            }
        } else {
            // Last step: Add NID Number and Submit!
            OutlinedTextField(
                value = nidNumber,
                onValueChange = { nidNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("NATIONAL IDENTITY NUMBER (NID)") },
                textStyle = LocalTextStyle.current.copy(color = TextLight),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nidNumber.isEmpty() || frontPhoto.isEmpty() || backPhoto.isEmpty() || selfiePhoto.isEmpty()) {
                        viewModel.triggerMessage("All scan artifacts and NID number required.")
                    } else {
                        viewModel.submitKYC(nidNumber, frontPhoto, backPhoto, selfiePhoto)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("SUBMIT COMPLIANCE PROFILE", color = CyberBlack, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun RowScope.KYCProgressStep(step: Int, label: String, activeStep: Int) {
    val isActive = step == activeStep
    val isCompleted = step < activeStep
    Box(
        modifier = Modifier
            .weight(1f)
            .height(35.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) CyberCyan else if (isCompleted) NeonGreen else CyberMediumSurface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isActive || isCompleted) CyberBlack else TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ==========================================
// 10. ADMIN DASHBOARD SCREEN
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: StoreViewModel) {
    val kycs by viewModel.allKYCs.collectAsStateWithLifecycle()
    val topups by viewModel.allTopups.collectAsStateWithLifecycle()
    val withdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val jobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val disputes by viewModel.allDisputes.collectAsStateWithLifecycle()
    val reports by viewModel.allReports.collectAsStateWithLifecycle()

    var activeAdminTab by remember { mutableStateOf("PENDING_PRODUCTS") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontally scrolling admin tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDarkSurface)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "PENDING_PRODUCTS" to "Products (${products.count { it.status == "PENDING_REVIEW" }})",
                "PENDING_JOBS" to "Jobs (${jobs.count { it.status == "PENDING_APPROVAL" }})",
                "KYC" to "KYC (${kycs.count { it.status == "PENDING" }})",
                "DEPOSITS" to "Deposits (${topups.count { it.status == "PENDING" }})",
                "WITHDRAWALS" to "Withdrawals (${withdrawals.count { it.status == "PENDING" }})",
                "DISPUTES" to "Disputes (${disputes.count { it.status == "PENDING" }})",
                "REPORTS" to "User Reports (${reports.count { it.status == "PENDING" }})",
                "CONFIG" to "bKash Config"
            )
            items(tabs) { (id, label) ->
                val isSelected = activeAdminTab == id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyberYellow else CyberMediumSurface)
                        .clickable { activeAdminTab = id }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        label,
                        color = if (isSelected) CyberBlack else TextLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Selected Admin tab list view
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (activeAdminTab) {
                "PENDING_PRODUCTS" -> {
                    val pending = products.filter { it.status == "PENDING_REVIEW" }
                    if (pending.isEmpty()) {
                        Text("No products waiting review.", color = TextMuted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pending) { prod ->
                                AdminApproveCard(
                                    title = prod.title,
                                    desc = prod.description,
                                    subText = "Seller: ${prod.sellerEmail} | Category: ${prod.category} | Price: ${prod.price} SKULL",
                                    onApprove = { viewModel.adminApproveProduct(prod.id) },
                                    onReject = { viewModel.adminRejectProduct(prod.id) }
                                )
                            }
                        }
                    }
                }
                "PENDING_JOBS" -> {
                    val pending = jobs.filter { it.status == "PENDING_APPROVAL" }
                    if (pending.isEmpty()) {
                        Text("No jobs waiting approval.", color = TextMuted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pending) { job ->
                                AdminApproveCard(
                                    title = job.title,
                                    desc = job.description,
                                    subText = "Client: ${job.clientEmail} | Budget: ${job.budget} SKULL | Fee: ${job.postingFee} SKULL",
                                    onApprove = { viewModel.adminApproveJob(job.id) },
                                    onReject = { viewModel.adminRejectJob(job.id) }
                                )
                            }
                        }
                    }
                }
                "KYC" -> {
                    val pending = kycs.filter { it.status == "PENDING" }
                    if (pending.isEmpty()) {
                        Text("No KYC validations pending.", color = TextMuted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pending) { k ->
                                AdminKYCCard(kyc = k, viewModel = viewModel)
                            }
                        }
                    }
                }
                "DEPOSITS" -> {
                    val pending = topups.filter { it.status == "PENDING" }
                    if (pending.isEmpty()) {
                        Text("No deposits waiting review.", color = TextMuted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pending) { tp ->
                                AdminApproveCard(
                                    title = "bKash Deposit request",
                                    desc = "Txn ID: ${tp.transactionId}\nSender Phone: ${tp.senderNumber}",
                                    subText = "User: ${tp.userEmail} | Amount: ${tp.amount} SKULL",
                                    onApprove = { viewModel.adminApproveTopup(tp.id) },
                                    onReject = { viewModel.adminRejectTopup(tp.id) }
                                )
                            }
                        }
                    }
                }
                "WITHDRAWALS" -> {
                    val pending = withdrawals.filter { it.status == "PENDING" }
                    if (pending.isEmpty()) {
                        Text("No withdrawals waiting review.", color = TextMuted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pending) { wd ->
                                AdminApproveCard(
                                    title = "bKash Withdrawal pay request",
                                    desc = "Receiver Phone: ${wd.receiverNumber}",
                                    subText = "User: ${wd.userEmail} | Amount: ${wd.amount} SKULL | Fee: ${wd.fee} SKULL",
                                    onApprove = { viewModel.adminApproveWithdrawal(wd.id) },
                                    onReject = { viewModel.adminRejectWithdrawal(wd.id) }
                                )
                            }
                        }
                    }
                }
                "DISPUTES" -> {
                    val pending = disputes.filter { it.status == "PENDING" }
                    if (pending.isEmpty()) {
                        Text("No disputed escrow contracts.", color = TextMuted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pending) { disp ->
                                AdminDisputeCard(dispute = disp, viewModel = viewModel)
                            }
                        }
                    }
                }
                "REPORTS" -> {
                    val pending = reports.filter { it.status == "PENDING" }
                    if (pending.isEmpty()) {
                        Text("No reports pending audit.", color = TextMuted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pending) { rep ->
                                AdminReportCard(report = rep, viewModel = viewModel)
                            }
                        }
                    }
                }
                "CONFIG" -> {
                    val bkashNumber by viewModel.bkashMerchantNumber.collectAsStateWithLifecycle()
                    var inputNo by remember(bkashNumber) { mutableStateOf(bkashNumber) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "BKASH SYSTEM GATEWAY SETTINGS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NeonGreen
                        )
                        Text(
                            "This bKash merchant wallet number is shown to all users on the deposit screen. Admin can edit/modify this number live at runtime.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        CyberTextField(
                            value = inputNo,
                            onValueChange = { inputNo = it },
                            label = "ACTIVE BKASH MERCHANT NUMBER",
                            icon = Icons.Outlined.Payments,
                            keyboardType = KeyboardType.Phone,
                            tag = "bkash_config_input"
                        )

                        Button(
                            onClick = {
                                if (inputNo.isNotEmpty()) {
                                    viewModel.updateBkashMerchantNumber(inputNo)
                                } else {
                                    viewModel.triggerMessage("Number cannot be empty.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "UPDATE WALLET ROUTING NUMBER",
                                color = CyberBlack,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Admin helper cards
@Composable
fun AdminApproveCard(
    title: String,
    desc: String,
    subText: String,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberLightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subText, color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("APPROVE", color = CyberBlack, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("REJECT", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AdminKYCCard(kyc: KYCEntity, viewModel: StoreViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberLightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("KYC Verification Request", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Operator: ${kyc.userEmail} | Username: ${kyc.username}", color = TextMuted, fontSize = 12.sp)
            Text("NID Number: ${kyc.nidNumber}", color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))

            // Display standard scans preview links/thumbnails
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DocThumb("NID Front", kyc.nidFrontUri)
                DocThumb("NID Back", kyc.nidBackUri)
                DocThumb("Selfie", kyc.selfieUri)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.adminApproveKYC(kyc.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("APPROVE KYC", color = CyberBlack, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Button(
                    onClick = { viewModel.adminRejectKYC(kyc.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("REJECT KYC", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun AdminDisputeCard(dispute: DisputeEntity, viewModel: StoreViewModel) {
    var splitDescription by remember { mutableStateOf("Client Refuse") }
    var clientAmountText by remember { mutableStateOf("100") }
    var freelancerAmountText by remember { mutableStateOf("50") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberLightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("DISPUTED CONTRACT #${dispute.jobId}: ${dispute.jobTitle}", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Client: ${dispute.clientEmail}\nFreelancer: ${dispute.freelancerEmail}", color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Dispute Creator: ${dispute.disputeCreatorEmail}", color = TextLight, fontSize = 12.sp)
            Text("Reason: ${dispute.reason}\nDetails: ${dispute.description}", color = TextMuted, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Text fields for splitting funds
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = clientAmountText,
                    onValueChange = { clientAmountText = it },
                    label = { Text("Client SKULL", fontSize = 9.sp) },
                    textStyle = LocalTextStyle.current.copy(color = TextLight, fontSize = 11.sp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                )
                OutlinedTextField(
                    value = freelancerAmountText,
                    onValueChange = { freelancerAmountText = it },
                    label = { Text("Freelancer SKULL", fontSize = 9.sp) },
                    textStyle = LocalTextStyle.current.copy(color = TextLight, fontSize = 11.sp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = splitDescription,
                onValueChange = { splitDescription = it },
                label = { Text("Resolution Audit Details", fontSize = 10.sp) },
                textStyle = LocalTextStyle.current.copy(color = TextLight, fontSize = 11.sp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val clientAmt = clientAmountText.toIntOrNull() ?: 0
                    val freelancerAmt = freelancerAmountText.toIntOrNull() ?: 0
                    viewModel.adminResolveDispute(dispute.id, splitDescription, clientAmt, freelancerAmt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("EXECUTE MANUAL SPLIT", color = CyberBlack, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AdminReportCard(report: ReportEntity, viewModel: StoreViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = BorderStroke(1.dp, CyberLightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ABUSE REPORT", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            Text("Reported Account: ${report.reportedUserEmail}", color = TextLight, fontSize = 13.sp)
            Text("Reporter: ${report.reporterEmail}", color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Violation Code: ${report.reason}\nEvidence: ${report.description}", color = TextMuted, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.adminHandleReport(report.id, "WARNING") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMediumSurface),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("WARN", color = TextLight, fontSize = 10.sp)
                }
                Button(
                    onClick = { viewModel.adminHandleReport(report.id, "PERMANENT_BAN") },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("BAN OPERATOR", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun RowScope.DocThumb(label: String, url: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(60.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(CyberMediumSurface)
    ) {
        AsyncImage(
            model = url,
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBlack.copy(alpha = 0.7f))
                .align(Alignment.BottomCenter)
                .padding(vertical = 2.dp)
        ) {
            Text(
                label,
                color = TextLight,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// 11. INTERACTIVE DIALOG MODALS
// ==========================================
@Composable
fun ProductDetailsDialog(
    product: ProductEntity,
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val reviews by viewModel.getReviewsForProduct(product.id).collectAsState(initial = emptyList())
    var purchaseSuccess by remember { mutableStateOf<Boolean?>(null) }
    var reviewText by remember { mutableStateOf("") }
    var ratingSlider by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberDarkSurface,
        modifier = Modifier.testTag("product_detail_dialog"),
        title = {
            Text(
                product.title,
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberMediumSurface)
                ) {
                    AsyncImage(
                        model = product.thumbnailUri,
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("SELLER: ${product.sellerName}", color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("VERSION: ${product.version}", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(product.description, color = TextLight, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("REVIEWS & FEEDBACK LOBBY", color = CyberYellow, fontFamily = FontFamily.Monospace, fontSize = 12.sp)

                // Reviews List
                if (reviews.isEmpty()) {
                    Text("No reviews logged yet. Secure purchaser logs only.", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    for (rev in reviews) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(rev.reviewerName, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 12.sp)
                                Text("★ ".repeat(rev.rating), color = CyberYellow, fontSize = 11.sp)
                            }
                            Text(rev.reviewText, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Post Review Block
                if (currentUser != null) {
                    Text("WRITE SECURE REVIEW", color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rating: $ratingSlider Stars", color = TextLight, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = ratingSlider.toFloat(),
                            onValueChange = { ratingSlider = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                        )
                    }
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text("Write encrypted critique...") },
                        textStyle = LocalTextStyle.current.copy(color = TextLight, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (reviewText.isNotEmpty()) {
                                viewModel.submitProductReview(product.id, ratingSlider, reviewText)
                                reviewText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("POST REVIEW", color = CyberBlack, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        },
        confirmButton = {
            if (currentUser != null && currentUser?.email != product.sellerEmail) {
                Button(
                    onClick = {
                        viewModel.purchaseProduct(product.id) { success, msg ->
                            purchaseSuccess = success
                            viewModel.triggerMessage(msg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("BUY DIRECT: ${product.price} SKULL", color = CyberBlack, fontFamily = FontFamily.Monospace)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddProductDialog(viewModel: StoreViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Security Tools") }
    var priceText by remember { mutableStateOf("10") }
    var tags by remember { mutableStateOf("scanner") }
    var thumbnail by remember { mutableStateOf("https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&q=80&w=300") }
    var fileUri by remember { mutableStateOf("content://hellsec/downloads/scanner_v1.zip") }
    var version by remember { mutableStateOf("1.0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberDarkSurface,
        title = { Text("PUBLISH NEW CYBER DEPLOYMENT", fontFamily = FontFamily.Monospace, color = NeonGreen, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CyberTextField(value = title, onValueChange = { title = it }, label = "DEPLOYMENT TITLE", icon = Icons.Outlined.Title, tag = "prod_title")
                CyberTextField(value = description, onValueChange = { description = it }, label = "DESCRIPTION / SPECIFICATIONS", icon = Icons.Outlined.Description, tag = "prod_desc")
                CyberTextField(value = priceText, onValueChange = { priceText = it }, label = "PRICE (SKULLS)", icon = Icons.Outlined.Payments, keyboardType = KeyboardType.Number, tag = "prod_price")
                CyberTextField(value = tags, onValueChange = { tags = it }, label = "TAGS (COMMA SEPARATED)", icon = Icons.Outlined.Label, tag = "prod_tags")
                CyberTextField(value = thumbnail, onValueChange = { thumbnail = it }, label = "IMAGE SOURCE URL", icon = Icons.Outlined.Image, tag = "prod_thumb")
                CyberTextField(value = fileUri, onValueChange = { fileUri = it }, label = "SECURE DEPLOYMENT FILE URI", icon = Icons.Outlined.FolderZip, tag = "prod_file")
                CyberTextField(value = version, onValueChange = { version = it }, label = "BUILD VERSION", icon = Icons.Outlined.Info, tag = "prod_ver")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toIntOrNull() ?: 10
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        viewModel.submitProduct(title, description, category, price, tags, thumbnail, fileUri, version)
                        onDismiss()
                    } else {
                        viewModel.triggerMessage("All deployment specifications must be set.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("SUBMIT PRODUCT FOR AUDIT", color = CyberBlack, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("STANDBY", color = TextMuted)
            }
        }
    )
}

@Composable
fun JobDetailsDialog(
    job: JobEntity,
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val proposals by viewModel.getProposalsForJob(job.id).collectAsState(initial = emptyList())

    // Bidding Form variables
    var bidAmount by remember { mutableStateOf(job.budget.toString()) }
    var deliveryTime by remember { mutableStateOf("3 Days") }
    var portfolioUrl by remember { mutableStateOf("https://github.com/hellsec/portfolio") }
    var coverLetter by remember { mutableStateOf("") }

    // Work Submission variables
    var submissionText by remember { mutableStateOf("") }
    var showSubmissionPanel by remember { mutableStateOf(false) }

    // Dispute Form variables
    var disputeReason by remember { mutableStateOf("") }
    var disputeDesc by remember { mutableStateOf("") }
    var showDisputePanel by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberDarkSurface,
        title = { Text(job.title, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("SYSTEM ID: CONSOLE_DIRECTIVE_${job.id}", color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("CLIENT: ${job.clientName}", color = TextMuted, fontSize = 11.sp)
                Text("STATUS: ${job.status}", color = NeonGreen, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text(job.description, color = TextLight, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text("SKILLS REQUIRED: ${job.skills}", color = CyberYellow, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                Spacer(modifier = Modifier.height(12.dp))

                // Show current assigned Freelancer details
                if (job.freelancerEmail.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberMediumSurface),
                        border = BorderStroke(1.dp, CyberLightSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("CONTRACTOR ASSIGNED", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Name: ${job.freelancerName}", color = TextLight, fontSize = 12.sp)
                            Text("Email: ${job.freelancerEmail}", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }

                // If Client views their own job in PENDING / OPEN state -> Show Bids (Proposals)
                if (currentUser?.email == job.clientEmail && job.status == "OPEN") {
                    Text("ACTIVE BIDS / PROPOSALS LIST", color = CyberYellow, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    if (proposals.isEmpty()) {
                        Text("No freelancer bids recorded.", color = TextMuted, fontSize = 11.sp)
                    } else {
                        for (prop in proposals) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CyberMediumSurface),
                                border = BorderStroke(1.dp, CyberLightSurface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(prop.freelancerName, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 13.sp)
                                        Text("${prop.bidAmount} SKULL", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text("Delivery Time: ${prop.deliveryTime}", color = CyberCyan, fontSize = 11.sp)
                                    Text("Portfolio: ${prop.portfolio}", color = TextMuted, fontSize = 11.sp, overflow = TextOverflow.Ellipsis)
                                    Text("Cover: ${prop.coverLetter}", color = TextLight, fontSize = 12.sp)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { viewModel.acceptProposal(prop) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("ACCEPT BID & ESCROW FUNDS", color = CyberBlack, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                // If Freelancer is browsing -> Show Submit Bid Form (if not already assigned)
                if (currentUser?.role == "Freelancer" && job.status == "OPEN" && proposals.none { it.freelancerEmail == currentUser?.email }) {
                    Text("SUBMIT COMPLIANT BID PROPOSAL", color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    CyberTextField(value = bidAmount, onValueChange = { bidAmount = it }, label = "BID AMOUNT (SKULLS)", icon = Icons.Outlined.Payments, keyboardType = KeyboardType.Number, tag = "bid_amt")
                    CyberTextField(value = deliveryTime, onValueChange = { deliveryTime = it }, label = "DELIVERY TIMELINE", icon = Icons.Outlined.Schedule, tag = "bid_timeline")
                    CyberTextField(value = coverLetter, onValueChange = { coverLetter = it }, label = "COVER LETTER / METHODOLOGY", icon = Icons.Outlined.Article, tag = "bid_cover")

                    Button(
                        onClick = {
                            val bid = bidAmount.toIntOrNull() ?: job.budget
                            viewModel.submitProposal(job.id, job.title, bid, deliveryTime, portfolioUrl, coverLetter)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TRANSMIT BID DIRECTIVE", color = CyberBlack, fontFamily = FontFamily.Monospace)
                    }
                }

                // If assigned Freelancer -> Submit Deliverables (state = IN_PROGRESS)
                if (currentUser?.email == job.freelancerEmail && job.status == "IN_PROGRESS") {
                    if (!showSubmissionPanel) {
                        Button(
                            onClick = { showSubmissionPanel = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SUBMIT CODE & ARTIFACTS", color = CyberBlack, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Text("CONTRACT ARTIFACTS SUBMISSION", color = CyberYellow, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        OutlinedTextField(
                            value = submissionText,
                            onValueChange = { submissionText = it },
                            placeholder = { Text("Provide access passwords, zip codes, and verification hashes...") },
                            textStyle = LocalTextStyle.current.copy(color = TextLight, fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (submissionText.isNotEmpty()) {
                                        viewModel.submitWork(job.id, submissionText, "content://secure_node/deliverables.tar")
                                        showSubmissionPanel = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("TRANSMIT", color = CyberBlack)
                            }
                            Button(
                                onClick = { showSubmissionPanel = false },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberMediumSurface),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL", color = TextLight)
                            }
                        }
                    }
                }

                // If Client -> Review work submission (state = SUBMITTED)
                if (currentUser?.email == job.clientEmail && job.status == "SUBMITTED") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberMediumSurface),
                        border = BorderStroke(1.dp, CyberCyan)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("DELIVERED CODE REPORT", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(job.workSubmissionText, color = TextLight, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.approveWork(job.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("RELEASE ESCROW", color = CyberBlack, fontSize = 9.sp)
                                }
                                Button(
                                    onClick = { showDisputePanel = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("FILE DISPUTE", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                // Dispute Form Overlay Panel
                if (showDisputePanel) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                        border = BorderStroke(1.dp, NeonPink)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("INITIATE CONTRACT LITIGATION", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(8.dp))
                            CyberTextField(value = disputeReason, onValueChange = { disputeReason = it }, label = "LITIGATION REASON", icon = Icons.Outlined.Gavel, tag = "disp_reason")
                            CyberTextField(value = disputeDesc, onValueChange = { disputeDesc = it }, label = "SUPPORTING EV_TEXT", icon = Icons.Outlined.Article, tag = "disp_desc")

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                                Button(
                                    onClick = {
                                        if (disputeReason.isNotEmpty()) {
                                            viewModel.fileDispute(job.id, disputeReason, disputeDesc, "")
                                            showDisputePanel = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("INITIATE", color = Color.White)
                                }
                                Button(
                                    onClick = { showDisputePanel = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberMediumSurface),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("BACK", color = TextLight)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = TextLight)
            }
        }
    )
}

@Composable
fun PostJobDialog(viewModel: StoreViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("25") }
    var deadline by remember { mutableStateOf("5 Days") }
    var skills by remember { mutableStateOf("Reverse Engineering, Android") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberDarkSurface,
        title = { Text("INITIATE ESCROW DIRECTIVE", fontFamily = FontFamily.Monospace, color = NeonGreen, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CyberTextField(value = title, onValueChange = { title = it }, label = "CONTRACT TITLE", icon = Icons.Outlined.Title, tag = "job_title")
                CyberTextField(value = description, onValueChange = { description = it }, label = "TASK REQS & SPECS", icon = Icons.Outlined.Description, tag = "job_desc")
                CyberTextField(value = budgetText, onValueChange = { budgetText = it }, label = "BUDGET AMOUNT (SKULLS)", icon = Icons.Outlined.Payments, keyboardType = KeyboardType.Number, tag = "job_budget")
                CyberTextField(value = deadline, onValueChange = { deadline = it }, label = "PROJECT DEADLINE TIMELINE", icon = Icons.Outlined.Schedule, tag = "job_deadline")
                CyberTextField(value = skills, onValueChange = { skills = it }, label = "SKILLS DIRECTIVES (COMMA)", icon = Icons.Outlined.Label, tag = "job_skills")

                Spacer(modifier = Modifier.height(10.dp))
                // Posting Fee instructions card
                Surface(
                    color = CyberMediumSurface,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("SYSTEM ESCROW FEES RULES:", color = CyberCyan, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("• Standard Post Fee: 5 SKULL\n• Premium Post (>50 budget): 10 SKULL\n• Escrow is locked in vault immediately on list approval.", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetText.toIntOrNull() ?: 25
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        viewModel.submitJob(title, description, budget, deadline, skills) { success, msg ->
                            viewModel.triggerMessage(msg)
                            if (success) onDismiss()
                        }
                    } else {
                        viewModel.triggerMessage("Job specs cannot be empty.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("TRANSMIT ESCROW JOB", color = CyberBlack, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("STANDBY", color = TextMuted)
            }
        }
    )
}

@Composable
fun DepositBKashDialog(viewModel: StoreViewModel, onDismiss: () -> Unit) {
    var txId by remember { mutableStateOf("") }
    var senderNo by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("50") }
    val bkashMerchantNumber by viewModel.bkashMerchantNumber.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberDarkSurface,
        title = { Text("BKASH DEPOSIT PROTOCOL", fontFamily = FontFamily.Monospace, color = NeonGreen, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Instructive visual text
                Text(
                    "Deposit instructions:\n" +
                            "1. Send money via bKash personal wallet account.\n" +
                            "2. bKash Merchant Wallet No: $bkashMerchantNumber\n" +
                            "3. Input exact transaction reference below.",
                    color = TextLight,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                CyberTextField(value = txId, onValueChange = { txId = it }, label = "BKASH TRANSACTION ID (TXNID)", icon = Icons.Outlined.ConfirmationNumber, tag = "deposit_txn")
                CyberTextField(value = senderNo, onValueChange = { senderNo = it }, label = "SENDER WALLET PHONE", icon = Icons.Outlined.Phone, keyboardType = KeyboardType.Phone, tag = "deposit_sender")
                CyberTextField(value = amountText, onValueChange = { amountText = it }, label = "AMOUNT TO CREDIT (SKULL = BDT)", icon = Icons.Outlined.Payments, keyboardType = KeyboardType.Number, tag = "deposit_amount")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toIntOrNull() ?: 50
                    if (txId.isNotEmpty() && senderNo.isNotEmpty()) {
                        viewModel.submitTopup(txId, senderNo, amt, "content://bKash/screenshot_proof.jpg")
                        onDismiss()
                    } else {
                        viewModel.triggerMessage("bKash transaction credentials missing.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("TRANSMIT TRANSACTION", color = CyberBlack, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("STANDBY", color = TextMuted)
            }
        }
    )
}

@Composable
fun WithdrawBKashDialog(viewModel: StoreViewModel, onDismiss: () -> Unit) {
    var receiverNo by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberDarkSurface,
        title = { Text("BKASH WITHDRAWAL OUTPOST", fontFamily = FontFamily.Monospace, color = CyberCyan, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "System limits and requirements:\n" +
                            "• Minimum withdrawal limit: 50 SKULL\n" +
                            "• Standard protocol system fee: 5 SKULL\n" +
                            "• Execution clearance timeframe: 1-3 Business Days.",
                    color = TextLight,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                CyberTextField(value = receiverNo, onValueChange = { receiverNo = it }, label = "TARGET BKASH WALLET PHONE", icon = Icons.Outlined.Phone, keyboardType = KeyboardType.Phone, tag = "withdraw_phone")
                CyberTextField(value = amountText, onValueChange = { amountText = it }, label = "WITHDRAWAL AMOUNT", icon = Icons.Outlined.Payments, keyboardType = KeyboardType.Number, tag = "withdraw_amt")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toIntOrNull() ?: 50
                    if (receiverNo.isNotEmpty()) {
                        viewModel.submitWithdrawal(receiverNo, amt) { success, msg ->
                            viewModel.triggerMessage(msg)
                            if (success) onDismiss()
                        }
                    } else {
                        viewModel.triggerMessage("Target withdrawal wallet phone required.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("INITIATE EXTRADITION", color = CyberBlack, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("STANDBY", color = TextMuted)
            }
        }
    )
}

// ==========================================
// 12. COMMON STYLED UTILITY COMPOSABLES
// ==========================================
@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted) },
        leadingIcon = { Icon(icon, label, tint = CyberCyan, modifier = Modifier.size(20.dp)) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        textStyle = TextStyle(
            color = TextLight,
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = CyberLightSurface,
            cursorColor = NeonGreen,
            focusedContainerColor = CyberBlack,
            unfocusedContainerColor = CyberBlack
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun EmptyStateView(icon: ImageVector, title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, "Empty", tint = TextMuted, modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextLight,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = desc,
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
