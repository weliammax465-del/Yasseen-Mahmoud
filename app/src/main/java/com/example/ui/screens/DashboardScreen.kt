package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Stock
import com.example.ui.viewmodel.StockViewModel

import com.example.ui.components.MarketSentimentGauge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StockViewModel,
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stocks by viewModel.allStocks.collectAsState()
    val marketSummary by viewModel.marketSummary.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    
    val recommendedAnalyses by viewModel.recommendedAnalyses.collectAsState()
    
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredStocks = stocks.filter { stock ->
        val matchesSearch = stock.symbol.contains(searchQuery, ignoreCase = true) ||
                stock.nameAr.contains(searchQuery) ||
                stock.nameEn.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (selectedTab) {
            1 -> true // All
            2 -> stock.isFavorite // Favorites
            else -> recommendedAnalyses.any { it.symbol == stock.symbol } // Recommendations
        }
        matchesSearch && matchesTab
    }.sortedByDescending { stock -> 
        if (selectedTab == 0) {
            recommendedAnalyses.find { it.symbol == stock.symbol }?.successProbability ?: 0
        } else {
            stock.changePercent.toInt()
        }
    }

    // Force RTL layout direction for professional Arabic interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Banner
                HeaderBanner(
                    egx30Index = marketSummary?.egx30Index ?: 30250.50,
                    egx30Change = marketSummary?.egx30Change ?: 1.25,
                    totalVolume = marketSummary?.totalVolume ?: "340M EGP",
                    overallTrend = marketSummary?.overallTrend ?: "Bullish"
                )

                // Market Sentiment Gauge
                if (marketSummary != null) {
                    MarketSentimentGauge(
                        score = marketSummary!!.sentimentScore,
                        label = marketSummary!!.sentimentSummary.ifEmpty { marketSummary!!.overallTrend }
                    )
                }

                // Market Overview Text
                marketSummary?.generalReportAr?.let { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "ملخص السوق",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = report,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Filter & Search Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("بحث عن سهم...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("ترشيحات اليوم", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("كل الأسهم", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("المفضلة", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                // Section Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "الأسهم المرشحة للشراء"
                            1 -> "أبرز الأسهم النشطة"
                            else -> "الأسهم المفضلة"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredStocks.size} أسهم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Stocks List
                if (filteredStocks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "لا يوجد أسهم",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (selectedTab) {
                                    0 -> "لا توجد ترشيحات شراء اليوم"
                                    2 -> "لم تقم بإضافة أي أسهم للمفضلة بعد"
                                    else -> "لا توجد نتائج مطابقة للبحث"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredStocks, key = { it.symbol }) { stock ->
                            StockRowItem(
                                stock = stock,
                                analysis = if (selectedTab == 0) recommendedAnalyses.find { it.symbol == stock.symbol } else null,
                                onClick = { onStockClick(stock.symbol) },
                                onFavoriteToggle = { viewModel.toggleFavorite(stock) }
                            )
                        }
                    }
                }
            }

            // Floating action button for AI Market Update
            ExtendedFloatingActionButton(
                onClick = { showApiKeyDialog = true },
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = "تحليل الأسهم") },
                text = { Text("تحديث التحليل بالذكاء الاصطناعي", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )

            // API Loading Overlay
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "البحث والتحليل الفني جارٍ حالياً...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "يقوم الذكاء الاصطناعي بالبحث الحي عن أحدث تداولات البورصة المصرية وحساب مستويات الدعم والمقاومة وإشارات RSI و MACD لكل سهم وإرسال تنبيهات تليجرام...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Dialog for API Key Setup
            if (showApiKeyDialog) {
                Dialog(onDismissRequest = { showApiKeyDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تشغيل تحليل الذكاء الاصطناعي الحي",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "برجاء التأكد من تعيين مفتاح Gemini API في لوحة الأسرار (Secrets) أو إدخاله هنا مؤقتاً لتفعيل البحث الأرضي عن الأسهم المصرية مباشرة وحساب مؤشرات RSI و MACD وإشعارات التليجرام.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                label = { Text("مفتاح API Key (اختياري إذا تم تعيينه بالخلفية)") },
                                placeholder = { Text("AIzaSy...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { showApiKeyDialog = false }) {
                                    Text("إلغاء", color = MaterialTheme.colorScheme.error)
                                }
                                Button(
                                    onClick = {
                                        showApiKeyDialog = false
                                        viewModel.triggerMarketAnalysis(apiKeyInput.ifBlank { null })
                                    }
                                ) {
                                    Text("ابدأ التحليل الآن")
                                }
                            }
                        }
                    }
                }
            }

            // Handle success / failure dialog alerts
            var showErrorDialog by remember { mutableStateOf(false) }
            var showSuccessDialog by remember { mutableStateOf(false) }
            var errorType by remember { mutableStateOf("") }

            LaunchedEffect(analysisResult) {
                analysisResult?.let { result ->
                    if (result.isSuccess) {
                        showSuccessDialog = true
                    } else {
                        val exception = result.exceptionOrNull()
                        errorType = exception?.message ?: "Unknown"
                        showErrorDialog = true
                    }
                    viewModel.resetAnalysisResult()
                }
            }

            // Add FAB to trigger daily updates
            FloatingActionButton(
                onClick = { viewModel.triggerMarketAnalysis(null) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "تحديث أسعار اليوم")
                }
            }
            
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    title = { Text("تم التحليل بنجاح", fontWeight = FontWeight.Bold) },
                    text = { Text("تم تحديث أسعار البورصة المصرية وتوليد التقارير الفنية وحساب مستويات الدعم والمقاومة، وتم إرسال الإشعارات إلى تليجرام للأسهم الأكثر صعوداً وتوصيات الشراء!") },
                    confirmButton = {
                        Button(onClick = { showSuccessDialog = false }) {
                            Text("رائع")
                        }
                    }
                )
            }

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    title = { Text("فشل في التحليل", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                    text = {
                        Text(
                            text = when (errorType) {
                                "API_KEY_MISSING" -> "مفتاح Gemini API غير موجود. برجاء إدخاله في الحقل المخصص للبدء أو تعيينه كـ GEMINI_API_KEY في الأسرار (Secrets)."
                                "PARSING_ERROR" -> "حدث خطأ في قراءة بيانات التحليل من النموذج. يرجى المحاولة لاحقاً."
                                else -> "فشل في الاتصال بمصادر البيانات الموثوقة أو بمحرك البحث. تفاصيل الخطأ: $errorType"
                            }
                        )
                    },
                    confirmButton = {
                        Button(onClick = { showErrorDialog = false }) {
                            Text("حسناً")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderBanner(
    egx30Index: Double,
    egx30Change: Double,
    totalVolume: String,
    overallTrend: String
) {
    val isPositive = egx30Change >= 0
    val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                )
            )
            .padding(top = 24.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "البورصة المصرية EGX 30",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%,.2f", egx30Index),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Index percentage change badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = trendColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPositive) "▲" else "▼",
                            color = trendColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = String.format("%s%.2f%%", if (isPositive) "+" else "", egx30Change),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = trendColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "إجمالي التداول",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = totalVolume,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "حالة السوق العامة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (overallTrend == "Bullish") "صعودي قوي 🟢" else "تصحيحي هبوطي 🔴",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (overallTrend == "Bullish") Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
fun StockRowItem(
    stock: Stock,
    analysis: com.example.data.model.StockAnalysis? = null,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val isPositive = stock.changePercent >= 0
    val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ticker Circle Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(trendColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stock.symbol.take(4),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = trendColor,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Stock Name and Sector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stock.nameAr,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stock.nameEn} • ${stock.sector}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price and change percent
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = String.format("%.2f ج.م", stock.price),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = trendColor.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = String.format("%s%.2f%%", if (isPositive) "+" else "", stock.changePercent),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = trendColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Favorite button
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (stock.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "تفضيل",
                        tint = if (stock.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            if (analysis != null) {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نسبة النجاح: ${analysis.successProbability}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "المخاطرة: ${analysis.riskLevel}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (analysis.riskLevel == "عالي") Color(0xFFEF4444) else if (analysis.riskLevel == "متوسط") Color(0xFFF59E0B) else Color(0xFF10B981)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الدعم: S1:${String.format("%.2f", analysis.support1)} S2:${String.format("%.2f", analysis.support2)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "المقاومة: R1:${String.format("%.2f", analysis.resistance1)} R2:${String.format("%.2f", analysis.resistance2)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الاستراتيجية: ${analysis.strategyDetails}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}


