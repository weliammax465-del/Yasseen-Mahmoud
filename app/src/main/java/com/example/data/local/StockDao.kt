package com.example.data.local

import androidx.room.*
import com.example.data.model.Stock
import com.example.data.model.StockAnalysis
import com.example.data.model.AlertSettings
import com.example.data.model.MarketSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks ORDER BY changePercent DESC")
    fun getAllStocks(): Flow<List<Stock>>

    @Query("SELECT * FROM stocks WHERE isFavorite = 1 ORDER BY changePercent DESC")
    fun getFavoriteStocks(): Flow<List<Stock>>

    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    suspend fun getStockBySymbolDirect(symbol: String): Stock?

    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    fun getStockBySymbol(symbol: String): Flow<Stock?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<Stock>)

    @Query("UPDATE stocks SET isFavorite = :isFavorite WHERE symbol = :symbol")
    suspend fun updateFavoriteStatus(symbol: String, isFavorite: Boolean)

    @Query("SELECT * FROM stock_analyses WHERE symbol = :symbol")
    fun getStockAnalysis(symbol: String): Flow<StockAnalysis?>

    @Query("SELECT * FROM stock_analyses WHERE recommendation IN ('Strong Buy', 'Buy', 'Strong Buy أو Buy') ORDER BY successProbability DESC")
    fun getRecommendedAnalyses(): Flow<List<StockAnalysis>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockAnalysis(analysis: StockAnalysis)

    @Query("SELECT * FROM alert_settings WHERE id = 1")
    fun getAlertSettings(): Flow<AlertSettings?>

    @Query("SELECT * FROM alert_settings WHERE id = 1")
    suspend fun getAlertSettingsDirect(): AlertSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertSettings(settings: AlertSettings)

    @Query("SELECT * FROM market_summary WHERE id = 1")
    fun getMarketSummary(): Flow<MarketSummary?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketSummary(summary: MarketSummary)

    @Query("DELETE FROM stocks")
    suspend fun clearStocks()

    @Query("DELETE FROM stock_analyses")
    suspend fun clearAnalyses()
}
