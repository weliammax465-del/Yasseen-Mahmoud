package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AlertSettings
import com.example.data.model.MarketSummary
import com.example.data.model.Stock
import com.example.data.model.StockAnalysis
import com.example.data.remote.TelegramAlertSender
import com.example.data.repository.StockRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StockRepository
    
    val allStocks: StateFlow<List<Stock>>
    val favoriteStocks: StateFlow<List<Stock>>
    val recommendedAnalyses: StateFlow<List<StockAnalysis>>
    val marketSummary: StateFlow<MarketSummary?>
    val alertSettings: StateFlow<AlertSettings?>

    private val _selectedStock = MutableStateFlow<Stock?>(null)
    val selectedStock: StateFlow<Stock?> = _selectedStock.asStateFlow()

    private val _selectedStockAnalysis = MutableStateFlow<StockAnalysis?>(null)
    val selectedStockAnalysis: StateFlow<StockAnalysis?> = _selectedStockAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<Result<MarketSummary>?>(null)
    val analysisResult: StateFlow<Result<MarketSummary>?> = _analysisResult.asStateFlow()

    private val _testTelegramResult = MutableStateFlow<Boolean?>(null)
    val testTelegramResult: StateFlow<Boolean?> = _testTelegramResult.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StockRepository(database.stockDao())

        allStocks = repository.allStocks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        favoriteStocks = repository.favoriteStocks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        recommendedAnalyses = repository.recommendedAnalyses
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        marketSummary = repository.marketSummary
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        alertSettings = repository.alertSettings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Fetch real market data on startup if empty or outdated (older than 12 hours)
        viewModelScope.launch {
            val analyses = repository.recommendedAnalyses.first()
            val list = repository.allStocks.first()
            
            val isOutdated = analyses.isNotEmpty() && (System.currentTimeMillis() - analyses.first().analysisDate > 12 * 60 * 60 * 1000)
            
            if (list.isEmpty() || isOutdated) {
                triggerMarketAnalysis(null)
            }
            
            repository.alertSettings.first().let { settings ->
                if (settings == null) {
                    repository.saveAlertSettings(AlertSettings())
                }
            }
        }
    }

    fun selectStock(symbol: String?) {
        viewModelScope.launch {
            if (symbol == null) {
                _selectedStock.value = null
                _selectedStockAnalysis.value = null
            } else {
                repository.getStockBySymbol(symbol).collect {
                    _selectedStock.value = it
                }
            }
        }
        viewModelScope.launch {
            if (symbol != null) {
                repository.getStockAnalysis(symbol).collect {
                    _selectedStockAnalysis.value = it
                }
            }
        }
    }

    fun toggleFavorite(stock: Stock) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(stock.symbol, !stock.isFavorite)
        }
    }

    fun updateAlertSettings(settings: AlertSettings) {
        viewModelScope.launch {
            repository.saveAlertSettings(settings)
        }
    }

    fun triggerMarketAnalysis(userApiKey: String? = null) {
        if (_isAnalyzing.value) return
        _isAnalyzing.value = true
        _analysisResult.value = null
        
        viewModelScope.launch {
            val result = repository.runMarketAnalysis(userApiKey)
            _analysisResult.value = result
            _isAnalyzing.value = false
        }
    }

    fun resetAnalysisResult() {
        _analysisResult.value = null
    }

    fun sendTestTelegram(token: String, chatId: String) {
        viewModelScope.launch {
            _testTelegramResult.value = null
            val messageHtml = """
                🔔 <b>تجربة تنبيهات البورصة المصرية EGX Pro Analyzer</b>
                
                الاتصال بنظام تليجرام يعمل بنجاح! 🚀
                سوف تتلقى هنا إشارات الأسهم الأكثر صعوداً وتفاصيل الدعم والمقاومة يومياً وبشكل لحظي عند التفعيل.
            """.trimIndent()
            val success = TelegramAlertSender.sendTelegramMessage(token, chatId, messageHtml)
            _testTelegramResult.value = success
        }
    }

    fun clearTestTelegramResult() {
        _testTelegramResult.value = null
    }
}
