package com.Serviseyem.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.services.FirebaseService

@Composable
fun WamTopBar(
    onInfoClicked: () -> Unit = {},
    onSecretPortalOpened: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by FirebaseService.settings.collectAsState()

    // 5-Clicks detection
    var clickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var typedPassword by remember { mutableStateOf("") }

    val handleLogoClick = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 2000) {
            clickCount++
        } else {
            clickCount = 1
        }
        lastClickTime = currentTime

        if (clickCount >= 5) {
            clickCount = 0
            showPasswordDialog = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left - Home Icon with secret click
        IconButton(
            onClick = { handleLogoClick() },
            modifier = Modifier.testTag("home_secret_trigger")
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home Backdoor",
                tint = Color(0xFFD4AF37)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Large Premium Title
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { handleLogoClick() }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = settings.appNameAr,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("topbar_app_title")
            )
        }

        // Info Detail Trigger Icon
        if (settings.infoIconVisible) {
            IconButton(
                onClick = onInfoClicked,
                modifier = Modifier.size(settings.infoIconSize.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About Platform",
                    tint = Color.White,
                    modifier = Modifier.size((settings.infoIconSize - 4).dp)
                )
            }
        }
    }

    // Secret passcode dialog Pop up
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                typedPassword = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بوابة الإعدادات السرية الموحدة", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("الرجاء كتابة رمز التحقق السري للوصول إلى مركز البرمجة وبوابة المزامنة لـ WAM:", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = typedPassword,
                        onValueChange = { typedPassword = it },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("backdoor_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD4AF37)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Accept both maher--736462 (explicit user backdoor) and the customizable admin/manager password (defaults to WAM2026 or from settings)!
                        if (typedPassword == "maher--736462" || typedPassword == settings.adminPassword || typedPassword == "WAM2026") {
                            showPasswordDialog = false
                            typedPassword = ""
                            onSecretPortalOpened()
                            Toast.makeText(context, "تم التحقق الفوري! أهلاً بك في البوابة السرية لـ WAM.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "عذراً الرمز خاطئ! يرجى مراجعة الإدارة.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("تحقق ودخول", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        typedPassword = ""
                    }
                ) {
                    Text("إلغاء", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
