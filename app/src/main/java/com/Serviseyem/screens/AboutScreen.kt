package com.Serviseyem.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.services.FirebaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val settings by FirebaseService.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معلومات المالك والبرنامج 👑", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Brand Crown Logo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF064E3B), RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "WAM Logo",
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = settings.appNameAr,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4AF37),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "الإصدار السحابي السريع v2.1 (مزامنة فورية)",
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Brand explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "حول WAM للخدمات الفاخرة:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يعتبر دليل WAM البوابة الرائدة والأولى في اليمن لتسجيل وربط وتنسيق المعاملات المباشرة بين المواطنين ومقدمي الخدمات المحترفين والمشرفين الإداريين.\n\nتعتمد كافة العمليات وتحديث الخدمات على قواعد البيانات السحابية لـ Firebase Firestore بشكل فوري ولحظي لحل جميع مشاكل التعليق والمزامنة.",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Support Details Cards from Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // Top accent Goldborder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFFD4AF37))
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "وسائل التواصل والدعم الإداري والمالي السريع:",
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Phone row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.supportPhone}"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Phone", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("رقم الاتصال والدعم الإداري الموحد:", fontSize = 11.sp, color = Color.LightGray)
                            Text(settings.supportPhone, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Whatsapp row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=${settings.supportWhatsapp}")
                                }
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("خط الواتساب الإداري المباشر السريع:", fontSize = 11.sp, color = Color.LightGray)
                            Text(settings.supportWhatsapp, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Email row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${settings.supportEmail}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "لا يتوفر تطبيق بريد إلكتروني حالياً", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("البريد الإلكتروني المعتمد للشكاوى:", fontSize = 11.sp, color = Color.LightGray)
                            Text(settings.supportEmail, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Footer / Rights text
            Text(
                text = "كافة الحقوق محفوظة لـ: " + settings.footerText,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
