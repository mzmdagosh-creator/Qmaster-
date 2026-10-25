package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, UserRole) -> Unit,
    onQuickLoginPersona: (String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Sign In, 1: Sign Up

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regDisplayName by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf(UserRole.MEMBER) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App Logo & Header
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(EduBluePrimary, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = "EduBank MCQ Platform",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Question Bank & Role-Based MCQ Management",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Error message if present
        if (!errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Sign In", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Create Account", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Sign In Form
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text("Email or Username") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_identifier_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                singleLine = true
            )

            Button(
                onClick = { onLogin(identifier, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_login_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign In with Credentials", fontWeight = FontWeight.Bold)
            }
        } else {
            // Sign Up Form
            OutlinedTextField(
                value = regUsername,
                onValueChange = { regUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = regDisplayName,
                onValueChange = { regDisplayName = it },
                label = { Text("Full Name / Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Password (min 6 characters)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Role selection
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select Account Role:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    UserRole.values().forEach { role ->
                        FilterChip(
                            selected = regRole == role,
                            onClick = { regRole = role },
                            label = { Text(role.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Button(
                onClick = { onRegister(regUsername, regEmail, regPassword, regDisplayName, regRole) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_register_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Register Account", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()

        // 1-Tap Quick Persona Logins for instant evaluation
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Instant RBAC Persona Login:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap any role to immediately test its secure dashboard and permission boundaries",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            val personas = listOf(
                Triple("usr_admin", "Dr. Arthur Pendelton (Admin)", EduRoseDanger),
                Triple("usr_moderator", "Elena Rostova (Moderator)", EduCyanModerator),
                Triple("usr_advisor", "Prof. Marcus Vance (Advisor)", EduVioletAdvisor),
                Triple("usr_student", "Maya Lin (Member)", EduBlueLight)
            )

            personas.forEach { (uid, label, color) ->
                OutlinedButton(
                    onClick = { onQuickLoginPersona(uid) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("quick_login_$uid"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}
