package com.personal.gridbot.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * طبقة اتصال MetaApi.
 *
 * هذا الملف يعرّف العقد فقط. الاتصال الحقيقي يبقى معطلاً ما لم تُضبط بيانات
 * الاعتماد الآمنة لاحقاً. مرحلة المشروع الحالية Demo ولا تنفذ تداولاً حقيقياً.
 */

data class TradeRequest(
    val actionType: String,
    val symbol: String,
    val volume: Double,
    val openPrice: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null
)

data class TradeResponse(
    val numericCode: Int?,
    val stringCode: String?,
    val message: String?,
    val orderId: String?
)

data class AccountInformation(
    val balance: Double?,
    val equity: Double?,
    val margin: Double?,
    val freeMargin: Double?,
    val currency: String?
)

data class SymbolPrice(
    val symbol: String,
    val bid: Double,
    val ask: Double,
    val profitTickValue: Double?,
    val lossTickValue: Double?
)

interface MetaApiService {

    @Headers("Content-Type: application/json")
    @POST("users/current/accounts/{accountId}/trade")
    suspend fun executeTrade(
        @Header("auth-token") token: String,
        @Path("accountId") accountId: String,
        @Body trade: TradeRequest
    ): TradeResponse

    @GET("users/current/accounts/{accountId}/account-information")
    suspend fun getAccountInformation(
        @Header("auth-token") token: String,
        @Path("accountId") accountId: String
    ): AccountInformation

    @GET("users/current/accounts/{accountId}/symbols/{symbol}/current-price")
    suspend fun getCurrentPrice(
        @Header("auth-token") token: String,
        @Path("accountId") accountId: String,
        @Path("symbol", encoded = true) symbol: String
    ): SymbolPrice
}

object MetaApiClient {
    private const val BASE_URL = "https://mt-client-api-v1.new-york.agiliumtrade.ai/"

    // لا توجد أسرار حقيقية داخل المستودع.
    // يتم تفعيل الاتصال لاحقاً عبر طبقة إعدادات آمنة.
    var authToken: String = ""
    var accountId: String = ""

    val isConfigured: Boolean
        get() = authToken.isNotBlank() && accountId.isNotBlank()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: MetaApiService = retrofit.create(MetaApiService::class.java)
}
