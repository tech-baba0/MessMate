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
    
    @POST("auth/google") 
    suspend fun googleLogin(@Body request: com.messmate.android.data.auth.GoogleLoginRequest): AuthResponse

    @GET("messes/my")
    suspend fun getMyMesses(): List<com.messmate.android.data.mess.MessMembershipResponse>

    @POST("messes/join")
    suspend fun joinMess(@Body request: com.messmate.android.data.mess.JoinMessRequest): com.messmate.android.data.mess.MessMembershipResponse
    
    // Admin Endpoints
    @GET("messes/{messId}/members")
    suspend fun getMessMembers(@Path("messId") messId: String): List<com.messmate.android.data.mess.MessMemberResponse>

    @PUT("messes/{messId}/members/{memberId}/approve")
    suspend fun approveMember(@Path("messId") messId: String, @Path("memberId") memberId: String): com.messmate.android.data.mess.MessMemberResponse

    @PUT("messes/{messId}/members/{memberId}/reject")
    suspend fun rejectMember(@Path("messId") messId: String, @Path("memberId") memberId: String): com.messmate.android.data.mess.MessMemberResponse
    
    @PUT("messes/{messId}/members/{memberId}/role")
    suspend fun changeMemberRole(@Path("messId") messId: String, @Path("memberId") memberId: String, @Query("role") role: String): com.messmate.android.data.mess.MessMemberResponse

    @GET("messes/{messId}/balance/me")
    suspend fun getMyBalance(@Path("messId") messId: String): BalanceResponse

    @GET("messes/{messId}/meals/today")
    suspend fun getTodayMealStatus(@Path("messId") messId: String): com.messmate.android.data.meal.MealStatusResponse

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
