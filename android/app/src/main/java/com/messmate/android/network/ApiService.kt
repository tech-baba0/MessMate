package com.messmate.android.network

import com.messmate.android.data.auth.AuthResponse
import com.messmate.android.data.auth.LoginRequest
import com.messmate.android.data.auth.SignupRequest
import com.messmate.android.data.balance.BalanceResponse
import com.messmate.android.data.mess.MessResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.messmate.android.data.meal.MealToggleRequest
import com.messmate.android.data.meal.MealResponse

interface ApiService {
    
    @POST("auth/login") 
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: SignupRequest): Any // Returns MessageResponse normally

    @GET("messes/my")
    suspend fun getMyMesses(): List<MessResponse>

    @GET("messes/{messId}/balance/me")
    suspend fun getMyBalance(@Path("messId") messId: String): BalanceResponse

    @POST("messes/{messId}/meals")
    suspend fun toggleMeal(
        @Path("messId") messId: String, 
        @Body request: MealToggleRequest
    ): MealResponse

    @GET("messes/{messId}/meals/history")
    suspend fun getMealHistory(
        @Path("messId") messId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): List<MealResponse>

    @POST("messes/{messId}/expenses")
    suspend fun addExpense(
        @Path("messId") messId: String,
        @Body request: com.messmate.android.data.expense.ExpenseRequest
    ): com.messmate.android.data.expense.ExpenseResponse
}
