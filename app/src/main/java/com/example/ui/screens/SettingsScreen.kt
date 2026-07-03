package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSettings
import com.example.ui.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.alertSettings.collectAsState()
    val testResult by viewModel.testTelegramResult.collectAsState()

    var botToken by remember { mutableStateOf("") }
    var chatId by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var isTelegramEnabled by remember { mutableStateOf(false) }
    var isEmailEnabled by remember { mutableStateOf(false) }
    var autoAlertOnStrongBuy by remember { mutableStateOf(true) }

    var tokenVisible by remember { mutableStateOf(false) }

    // Synchronize local states when settings loaded from Room DB
    LaunchedEffect(settings) {
        settings?.let {
            botToken = it.telegramBotToken
            chatId = it.telegramChatId
            emailAddress = it.emailAddress
            isTelegramEnabled = it.isTelegramEnabled
            isEmailEnabled = it.isEmailEnabled
            autoAlertOnStrongBuy = it.autoAlertOnStrongBuy
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("إعدادات التنبيهات والاتصال", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro Guide Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "التنبيهات اللحظية",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تنبيهات فنية لحظية عبر تليجرام والبريد",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "عند تفعيل الإشعارات، سيقوم النظام تلقائياً بإرسال إشارة شراء ودعم ومقاومة مفصلة إلى حسابك الخاص على تليجرام فور صدور نتائج التحليل اليومي للأسهم الأكثر صعوداً بالذكاء الاصطناعي.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Telegram Alert Setup Section
                Text(
                    text = "🤖 إشعارات تليجرام (Telegram Alerts)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تفعيل تنبيهات تليجرام",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Switch(
                                checked = isTelegramEnabled,
                                onCheckedChange = { isTelegramEnabled = it }
                            )
                        }

                        if (isTelegramEnabled) {
                            OutlinedTextField(
                                value = botToken,
                                onValueChange = { botToken = it },
                                label = { Text("رمز بوت تليجرام (Bot Token)") },
                                placeholder = { Text("123456789:ABCdefGh...") },
                                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = "Token") },
                                trailingIcon = {
                                    IconButton(onClick = { tokenVisible = !tokenVisible }) {
                                        Icon(
                                            imageVector = if (tokenVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "رؤية"
                                        )
                                    }
                                },
                                visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = chatId,
                                onValueChange = { chatId = it },
                                label = { Text("معرف الدردشة الخاص بك (Chat ID)") },
                                placeholder = { Text("987654321") },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Chat ID") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Telegram Bot Guide Card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "💡 كيف تحصل على هذه البيانات في دقيقة؟",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "1. ابحث في تليجرام عن @BotFather وأرسل له /newbot لإنشاء بوت مخصص واحصل على الـ Token الخاص به.\n2. ابحث في تليجرام عن @userinfobot وأرسل له أي رسالة للحصول على الـ Chat ID الخاص بك.\n3. أرسل رسالة ترحيبية للبوت الخاص بك أولاً لتفعيل المحادثة قبل تجربة الإرسال.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Test and Status Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.sendTestTelegram(botToken, chatId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "إرسال")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("أرسل رسالة تجريبية")
                                }
                            }

                            // Show Test Result Status
                            testResult?.let { success ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (success) Color(0xFFE6F4EA) else Color(0xFFFCE8E6))
                                    .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = "حالة الإرسال",
                                            tint = if (success) Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (success) "تم إرسال التنبيه التجريبي بنجاح! تفقد تطبيق تليجرام." else "فشل الإرسال. تأكد من الرمز والـ Chat ID وتأكد من بدء تفعيل البوت.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (success) Color(0xFF137333) else Color(0xFFC5221F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Email Alert Setup Section
                Text(
                    text = "📧 تنبيهات البريد الإلكتروني (Email Alerts)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تفعيل تنبيهات البريد",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Switch(
                                checked = isEmailEnabled,
                                onCheckedChange = { isEmailEnabled = it }
                            )
                        }

                        if (isEmailEnabled) {
                            OutlinedTextField(
                                value = emailAddress,
                                onValueChange = { emailAddress = it },
                                label = { Text("عنوان البريد الإلكتروني الخاص بك") },
                                placeholder = { Text("user@example.com") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Alert Policy Toggles
                Text(
                    text = "⚙️ خيارات الإشعارات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تنبيه تلقائي عند إشارات الشراء",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "إرسال إشعار فوري عند تحديد السهم بتوصية Strong Buy أو Buy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoAlertOnStrongBuy,
                                onCheckedChange = { autoAlertOnStrongBuy = it }
                            )
                        }
                    }
                }

                // Save Settings Button
                Button(
                    onClick = {
                        val currentSettings = settings ?: AlertSettings()
                        viewModel.updateAlertSettings(
                            currentSettings.copy(
                                telegramBotToken = botToken,
                                telegramChatId = chatId,
                                emailAddress = emailAddress,
                                isTelegramEnabled = isTelegramEnabled,
                                isEmailEnabled = isEmailEnabled,
                                autoAlertOnStrongBuy = autoAlertOnStrongBuy
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "حفظ")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ التغييرات", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
