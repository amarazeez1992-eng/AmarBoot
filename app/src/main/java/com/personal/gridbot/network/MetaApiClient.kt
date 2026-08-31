package com.personal.gridbot.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * طبقة الاتصال بخدمة MetaApi.cloud - الجسر بين التطبيق وحساب MT5 الحقيقي بـ JustMarkets.
 *
 * ⚠️ مهم:
 * - الـ Base URL هنا (new-york.agiliumtrade.ai) هو للمنطقة الافتراضية new-york فقط.
 *   تحقق من لوحة MetaApi (Region الخاص بحسابك) وعدّله إذا لزم.
 * - لا تضع الـ Token مباشرة بالكود بشكل نهائي - استخدم مكانًا آمنًا (مثل
 *   EncryptedSharedPreferences) عند التوزيع الفعلي، هنا موضوع كمتغير للتبسيط فقط.
 * - هذا هيكل أولي؛ راجع التوثيق الرسمي (metaapi.cloud/docs/client) قبل الاعتماد
 *   عليه بحساب حقيقي.
 */

data class TradeRequest(
    val actionType: String,     // مثال: ORDER_TYPE_BUY_LIMIT / ORDER_TYPE_SELL_LIMIT
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
}

object MetaApiClient {

    // عدّل المنطقة حسب حسابك في لوحة MetaApi (new-york / london / singapore ...)
    private const val BASE_URL = "https://mt-client-api-v1.new-york.agiliumtrade.ai/"

    // 🔑 عبّي هذي القيم من حسابك في metaapi.cloud بعد ما تربط حساب JustMarkets Demo
    var authToken: String = "PUT_YOUR_METAAPI_TOKEN_HERE"
    var accountId: String = "PUT_YOUR_METAAPI_ACCOUNT_ID_HERE"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: MetaApiService = retrofit.create(MetaApiService::class.java)
}
