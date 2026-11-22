package com.example.propertymanagementsystem

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.ui.theme.PropertyManagementSystemTheme
import com.example.propertymanagementsystem.ui.viewmodel.PropertyViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences("property_app_settings", Context.MODE_PRIVATE)
            }

            var themeMode by remember {
                mutableStateOf(sharedPrefs.getString("app_theme_mode", "SYSTEM") ?: "SYSTEM")
            }

            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemDark
            }

            PropertyManagementSystemTheme(
                darkTheme = useDarkTheme,
                dynamicColor = (themeMode == "SYSTEM")
            ) {
                var isLoggedIn by remember { mutableStateOf(false) }
                var loggedInUserEmail by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (!isLoggedIn) {
                        LoginScreen(
                            themeMode = themeMode,
                            onThemeModeChanged = { newMode ->
                                themeMode = newMode
                                sharedPrefs.edit().putString("app_theme_mode", newMode).apply()
                            },
                            onLoginSuccess = { email ->
                                loggedInUserEmail = email
                                isLoggedIn = true
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        PropertyDashboardScreen(
                            userEmail = loggedInUserEmail,
                            themeMode = themeMode,
                            onThemeModeChanged = { newMode ->
                                themeMode = newMode
                                sharedPrefs.edit().putString("app_theme_mode", newMode).apply()
                            },
                            onLogout = {
                                isLoggedIn = false
                                loggedInUserEmail = ""
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    themeMode: String,
    onThemeModeChanged: (String) -> Unit,
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegistering by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("property_auth_prefs", Context.MODE_PRIVATE)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showThemeDialog = true }) {
                    Icon(
                        imageVector = if (themeMode == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Change Theme"
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Property Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isRegistering) "Create a new account" else "Sign in to manage your portfolio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    successMessage?.let { success ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = success,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (!isRegistering) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showForgotPasswordDialog = true }) {
                                Text("Forgot Password?", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (isRegistering) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val trimmedEmail = email.trim()
                            val trimmedPassword = password.trim()
                            if (trimmedEmail.isEmpty()) {
                                errorMessage = "Email address is required."
                                successMessage = null
                            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                errorMessage = "Enter a valid email address."
                                successMessage = null
                            } else if (trimmedPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                successMessage = null
                            } else if (isRegistering && trimmedPassword != confirmPassword.trim()) {
                                errorMessage = "Passwords do not match."
                                successMessage = null
                            } else {
                                errorMessage = null
                                isLoading = true

                                if (isRegistering) {
                                    sharedPrefs.edit()
                                        .putString("reg_email_$trimmedEmail", trimmedEmail)
                                        .putString("reg_pass_$trimmedEmail", trimmedPassword)
                                        .apply()
                                    successMessage = "Account registered successfully! You can now sign in."
                                    isLoading = false
                                    isRegistering = false
                                    password = ""
                                    confirmPassword = ""
                                } else {
                                    val savedPass = sharedPrefs.getString("reg_pass_$trimmedEmail", null)
                                    if (savedPass != null && savedPass != trimmedPassword) {
                                        errorMessage = "Incorrect password."
                                        isLoading = false
                                    } else {
                                        if (savedPass == null) {
                                            sharedPrefs.edit()
                                                .putString("reg_email_$trimmedEmail", trimmedEmail)
                                                .putString("reg_pass_$trimmedEmail", trimmedPassword)
                                                .apply()
                                        }
                                        onLoginSuccess(trimmedEmail)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isRegistering) "Register Account" else "Sign In", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    TextButton(
                        onClick = {
                            isRegistering = !isRegistering
                            errorMessage = null
                            successMessage = null
                            password = ""
                            confirmPassword = ""
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isRegistering) "Already have an account? Sign In" else "New user? Register here"
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    TextButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Need Help? Contact Support")
                    }
                }
            }
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = themeMode,
                onDismiss = { showThemeDialog = false },
                onSelectTheme = { selected ->
                    onThemeModeChanged(selected)
                    showThemeDialog = false
                }
            )
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordDialog = false },
                onResetPassword = { resetEmail, newPass ->
                    val savedPass = sharedPrefs.getString("reg_pass_$resetEmail", null)
                    if (savedPass == null) {
                        Pair(false, "Email address not found in registered accounts.")
                    } else {
                        sharedPrefs.edit()
                            .putString("reg_pass_$resetEmail", newPass)
                            .apply()
                        Pair(true, "Password updated successfully! Please sign in.")
                    }
                }
            )
        }

        if (showHelpDialog) {
            HelpSupportDialog(
                onDismiss = { showHelpDialog = false },
                onCallSupport = { phone ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                },
                onEmailSupport = { emailAddress ->
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$emailAddress")
                        putExtra(Intent.EXTRA_SUBJECT, "Property Management Support Request")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                }
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelectTheme: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App Theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (currentTheme == "LIGHT"),
                        onClick = { onSelectTheme("LIGHT") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("☀️ Light Mode")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (currentTheme == "DARK"),
                        onClick = { onSelectTheme("DARK") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🌙 Dark Mode")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (currentTheme == "SYSTEM"),
                        onClick = { onSelectTheme("SYSTEM") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📱 System Default")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun HelpSupportDialog(
    onDismiss: () -> Unit,
    onCallSupport: (String) -> Unit,
    onEmailSupport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Help & Support")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Need assistance with your account or managing your properties?",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                Text("Common Assistance:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("• Forgotten Password: Click 'Forgot Password?' on the login screen to reset your password.", style = MaterialTheme.typography.bodySmall)
                Text("• New User: Click 'New user? Register here' to create a new property portfolio account.", style = MaterialTheme.typography.bodySmall)
                Text("• Technical Support: Contact our 24/7 customer care team below.", style = MaterialTheme.typography.bodySmall)

                HorizontalDivider()

                Text("Contact Customer Care:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCallSupport("+254700000000") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Care")
                    }

                    OutlinedButton(
                        onClick = { onEmailSupport("support@propertymanagement.co.ke") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email Us")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onResetPassword: (email: String, newPass: String) -> Pair<Boolean, String>
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                errorMessage?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                successMessage?.let { success ->
                    Text(text = success, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Registered Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (min 6 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPass = newPassword.trim()
                    if (trimmedEmail.isEmpty()) {
                        errorMessage = "Email is required."
                        successMessage = null
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                        errorMessage = "Enter a valid email address."
                        successMessage = null
                    } else if (trimmedPass.length < 6) {
                        errorMessage = "Password must be at least 6 characters."
                        successMessage = null
                    } else if (trimmedPass != confirmPassword.trim()) {
                        errorMessage = "Passwords do not match."
                        successMessage = null
                    } else {
                        val result = onResetPassword(trimmedEmail, trimmedPass)
                        if (result.first) {
                            successMessage = result.second
                            errorMessage = null
                        } else {
                            errorMessage = result.second
                            successMessage = null
                        }
                    }
                }
            ) {
                Text("Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDashboardScreen(
    userEmail: String,
    themeMode: String,
    onThemeModeChanged: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PropertyViewModel = viewModel()
) {
    LaunchedEffect(userEmail) {
        viewModel.setCurrentUser(userEmail)
    }

    val propertyCount by viewModel.propertyCountText.observeAsState("Total Properties: 0")
    val occupancySummary by viewModel.occupancySummaryText.observeAsState("Occupied: 0 | Vacant: 0")
    val totalRent by viewModel.totalMonthlyRentText.observeAsState("Total Monthly Rent: KSh 0.00")
    val properties by viewModel.allProperties.observeAsState(emptyList())

    val context = LocalContext.current
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var propertyToEdit by remember { mutableStateOf<Property?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Navigation Menu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Text(
                        text = "User: $userEmail",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("Dashboard") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    NavigationDrawerItem(
                        label = { Text("Add Property") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showAddDialog = true
                        },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Property") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    NavigationDrawerItem(
                        label = { Text("Call Caretaker") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            if (properties.isNotEmpty()) {
                                val firstPhone = properties.first().caretakerPhone
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$firstPhone"))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    feedbackMessage = "Unable to launch dialer"
                                }
                            } else {
                                feedbackMessage = "No registered properties available to call caretaker."
                            }
                        },
                        icon = { Icon(Icons.Default.Phone, contentDescription = "Call Caretaker") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    NavigationDrawerItem(
                        label = { Text("App Theme") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showThemeDialog = true
                        },
                        icon = {
                            Icon(
                                imageVector = if (themeMode == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Theme"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    NavigationDrawerItem(
                        label = { Text("Web Portal") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Davidrodges/Property-Mangement-Project"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                feedbackMessage = "Unable to open browser"
                            }
                        },
                        icon = { Icon(Icons.Default.Language, contentDescription = "Web Portal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onLogout()
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Portfolio (${userEmail.ifEmpty { "Guest" }})") },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { showThemeDialog = true }) {
                            Icon(
                                imageVector = if (themeMode == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Theme"
                            )
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Signed in as $userEmail") },
                                onClick = { },
                                enabled = false
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("App Theme Settings") },
                                onClick = {
                                    showMenu = false
                                    showThemeDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (themeMode == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = "Theme"
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Visit Web Portal") },
                                onClick = {
                                    showMenu = false
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Davidrodges/Property-Mangement-Project"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        feedbackMessage = "Unable to open browser"
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = "Web Portal") }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout") }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Property")
                }
            },
            modifier = modifier
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Portfolio Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Statistics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Count", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = propertyCount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Occupancy", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = occupancySummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Financial Overview", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = totalRent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                feedbackMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = msg, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { feedbackMessage = null }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Registered Properties",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (properties.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No properties registered yet. Tap 'Add Property' to begin.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        properties.forEach { property ->
                            PropertyCard(
                                property = property,
                                onCallCaretaker = { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phone")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        feedbackMessage = "Unable to launch dialer"
                                    }
                                },
                                onEdit = {
                                    propertyToEdit = property
                                },
                                onDelete = {
                                    viewModel.deleteProperty(property)
                                    feedbackMessage = "Property deleted successfully"
                                }
                            )
                        }
                    }
                }

                // Bottom spacer to avoid FAB overlapping content in landscape mode
                Spacer(modifier = Modifier.height(72.dp))
            }

            if (showAddDialog) {
                AddPropertyDialog(
                    onDismiss = { showAddDialog = false },
                    onAddProperty = { name, location, type, rent, phone, isOccupied, totalUnits, vacantUnits ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val currentDate = dateFormat.format(Date())
                        val lastVacant = if (isOccupied) "Occupied (Last Vacant: $currentDate)" else "Vacant since $currentDate"

                        val result = viewModel.validateAndAddPropertyWithUnits(
                            name = name,
                            location = location,
                            type = type,
                            rentInput = rent,
                            phone = phone,
                            isOccupied = isOccupied,
                            totalUnits = totalUnits,
                            vacantUnits = vacantUnits,
                            lastVacantDate = lastVacant
                        )
                        if (result.first) {
                            feedbackMessage = result.second
                        }
                        result
                    }
                )
            }

            if (showThemeDialog) {
                ThemeSelectionDialog(
                    currentTheme = themeMode,
                    onDismiss = { showThemeDialog = false },
                    onSelectTheme = { selected ->
                        onThemeModeChanged(selected)
                        showThemeDialog = false
                    }
                )
            }

            propertyToEdit?.let { prop ->
                EditPropertyDialog(
                    property = prop,
                    onDismiss = { propertyToEdit = null },
                    onSave = { updatedProperty ->
                        viewModel.updateProperty(updatedProperty)
                        feedbackMessage = "Property updated successfully"
                    }
                )
            }
        }
    }
}

@Composable
fun PropertyCard(
    property: Property,
    onCallCaretaker: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusText = if (property.isOccupied) "Occupied" else "Vacant"
    val statusColor = if (property.isOccupied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val formattedRent = String.format(Locale.US, "%,.2f", property.monthlyRent)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = property.propertyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(containerColor = statusColor) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Text("Location: ${property.location}", style = MaterialTheme.typography.bodyMedium)
            Text("Type: ${property.propertyType}", style = MaterialTheme.typography.bodyMedium)
            Text("Monthly Rent: KSh $formattedRent", style = MaterialTheme.typography.bodyMedium)
            Text("Caretaker Phone: ${property.caretakerPhone}", style = MaterialTheme.typography.bodyMedium)
            Text("Vacancy: ${property.vacantUnits} vacant / ${property.totalUnits} total units", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Last Status Change: ${property.lastVacantDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onCallCaretaker(property.caretakerPhone) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Caretaker")
                }

                OutlinedButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                }

                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onAddProperty: (name: String, location: String, type: String, rent: String, phone: String, isOccupied: Boolean, totalUnits: Int, vacantUnits: Int) -> Pair<Boolean, String>
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Residential") }
    var rent by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isOccupied by remember { mutableStateOf(false) }
    var totalUnitsStr by remember { mutableStateOf("1") }
    var vacantUnitsStr by remember { mutableStateOf("1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Property") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Property Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Property Type (e.g. Residential)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it },
                    label = { Text("Monthly Rent (KSh)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Caretaker Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalUnitsStr,
                    onValueChange = { totalUnitsStr = it },
                    label = { Text("Total Units") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = vacantUnitsStr,
                    onValueChange = { vacantUnitsStr = it },
                    label = { Text("Vacant Units") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is Occupied?")
                    Switch(
                        checked = isOccupied,
                        onCheckedChange = { isOccupied = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val totalVal = totalUnitsStr.toIntOrNull() ?: 1
                    val vacantVal = vacantUnitsStr.toIntOrNull() ?: 1
                    val result = onAddProperty(name, location, type, rent, phone, isOccupied, totalVal, vacantVal)
                    if (result.first) {
                        onDismiss()
                    } else {
                        errorMessage = result.second
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPropertyDialog(
    property: Property,
    onDismiss: () -> Unit,
    onSave: (Property) -> Unit
) {
    var propertyName by remember { mutableStateOf(property.propertyName) }
    var location by remember { mutableStateOf(property.location) }
    var propertyType by remember { mutableStateOf(property.propertyType) }
    var monthlyRent by remember { mutableStateOf(property.monthlyRent.toString()) }
    var caretakerPhone by remember { mutableStateOf(property.caretakerPhone) }
    var isOccupied by remember { mutableStateOf(property.isOccupied) }
    var vacantUnits by remember { mutableStateOf(property.vacantUnits.toString()) }
    var totalUnits by remember { mutableStateOf(property.totalUnits.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Property & Vacancy") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                errorMessage?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = propertyName,
                    onValueChange = { propertyName = it },
                    label = { Text("Property Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = propertyType,
                    onValueChange = { propertyType = it },
                    label = { Text("Property Type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = monthlyRent,
                    onValueChange = { monthlyRent = it },
                    label = { Text("Monthly Rent (KSh)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = caretakerPhone,
                    onValueChange = { caretakerPhone = it },
                    label = { Text("Caretaker Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalUnits,
                    onValueChange = { totalUnits = it },
                    label = { Text("Total Units") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = vacantUnits,
                    onValueChange = { vacantUnits = it },
                    label = { Text("Vacant Units Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Is Occupied?")
                    Switch(
                        checked = isOccupied,
                        onCheckedChange = { isOccupied = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rentVal = monthlyRent.toDoubleOrNull()
                    val totalVal = totalUnits.toIntOrNull()
                    val vacantVal = vacantUnits.toIntOrNull()
                    if (propertyName.isBlank()) {
                        errorMessage = "Property name is required."
                    } else if (rentVal == null || rentVal <= 0.0) {
                        errorMessage = "Enter valid monthly rent."
                    } else if (totalVal == null || totalVal < 1) {
                        errorMessage = "Total units must be at least 1."
                    } else if (vacantVal == null || vacantVal < 0 || vacantVal > totalVal) {
                        errorMessage = "Invalid vacant units amount."
                    } else {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val currentDate = dateFormat.format(Date())
                        val statusChanged = (property.isOccupied != isOccupied) || (property.vacantUnits != vacantVal)
                        val lastVacant = if (statusChanged) {
                            if (isOccupied) "Occupied (Last Vacant: $currentDate)" else "Vacant since $currentDate"
                        } else {
                            property.lastVacantDate
                        }

                        val updated = property.copy(
                            propertyName = propertyName.trim(),
                            location = location.trim(),
                            propertyType = propertyType.trim(),
                            monthlyRent = rentVal,
                            caretakerPhone = caretakerPhone.trim(),
                            isOccupied = isOccupied,
                            totalUnits = totalVal,
                            vacantUnits = vacantVal,
                            lastVacantDate = lastVacant
                        )
                        onSave(updated)
                        onDismiss()
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
