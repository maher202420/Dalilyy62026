package com.Serviseyem

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.Serviseyem.models.AppViewModel
import com.Serviseyem.screens.AdminScreen
import com.Serviseyem.screens.MainScreen
import com.Serviseyem.ui.theme.ServisEmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Post Notifications permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val navController = rememberNavController()
            val context = LocalContext.current

            // Listens to local notification broadcasts from AppViewModel
            LaunchedEffect(Unit) {
                appViewModel.localNotificationFlow.collect { (title, body) ->
                    sendSystemNotification(context, title, body)
                }
            }

            ServisEmTheme(
                primaryColor = appViewModel.appPrimaryColor,
                secondaryColor = appViewModel.appSecondaryColor,
                backgroundColor = appViewModel.appBackgroundColor,
                textColor = appViewModel.appTextColor,
                fontFamily = appViewModel.appFontFamily
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            viewModel = appViewModel,
                            onNavigateToAdmin = {
                                navController.navigate("admin")
                            }
                        )
                    }
                    composable("admin") {
                        AdminScreen(
                            viewModel = appViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun sendSystemNotification(context: Context, title: String, body: String) {
        val channelId = "booking_alerts"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تنبيهات الحجوزات اليمنية"
            val descriptionText = "قناة تنبيهات مواعيد الصيانة وإشعارات مزودي الخدمة بالدليل"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
