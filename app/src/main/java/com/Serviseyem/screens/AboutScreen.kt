package com.Serviseyem.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Serviseyem.services.FirebaseService

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val settingsState by FirebaseService.settings.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Large Logo Header
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = "WAM Logo",
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = settingsState.appNameAr,
            color = Color(0xFFD4AF37),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("about_app_name")
        )

        Text(
            text = "الإصدار الذهبي 2026",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Welcome Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "رسالة الدليل",
                    color = Color(0xFFD4AF37),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = settingsState.welcomeMsg,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Support Details List
        Text(
            text = "📞 اتصل بنا للدعم المباشر ومقترحات الشراكة",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        // Tel Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settingsState.supportPhone}"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "لم نتمكن من فتح واجهة الاتصال", Toast.LENGTH_SHORT).show()
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFD4AF37))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("الاتصال الموحد المباشر", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(settingsState.supportPhone, color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        // WhatsApp Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    try {
                        val wpUrl = "https://wa.me/${settingsState.supportWhatsapp}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wpUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "الرجاء مراجعة تطبيق واتساب", Toast.LENGTH_SHORT).show()
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
            border = BorderStroke(1.dp, Color(0xFF059669))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color.Green)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("محادثة واتساب الفورية للتنسيق", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(settingsState.supportWhatsapp, color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        // Email Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(settingsState.supportEmail))
                            putExtra(Intent.EXTRA_SUBJECT, "طلب استفسار لدليل WAM")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "تطبيق الإيميل غير متوفر", Toast.LENGTH_SHORT).show()
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFD4AF37))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("البريد الإلكتروني للإدارة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(settingsState.supportEmail, color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Premium Footer text
        Text(
            text = settingsState.footerText,
            color = Color(0xFFD4AF37),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .testTag("promotional_footer")
        )
    }
}
