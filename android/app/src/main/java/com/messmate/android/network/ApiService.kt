package com.messmate.android.network

import com.messmate.android.data.auth.AuthResponse
import com.messmate.android.data.auth.LoginRequest
import com.messmate.android.data.auth.SignupRequest
import com.messmate.android.data.balance.BalanceResponse
import com.messmate.android.data.mess.MessResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import com.messmate.android.data.meal.MealToggleRequest
import com.messmate.android.data.meal.MealResponse

interface ApiService {
    
    @POST("auth/google") 
    suspend fun googleLogin(@Body request: com.messmate.android.data.auth.GoogleLoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: SignupRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

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
    ): com.messmate.android.data.meal.MealHistorySummaryResponse
    
    @GET("messes/{messId}/meals/dashboard")
    suspend fun getMealSelectionDashboard(
        @Path("messId") messId: String
    ): com.messmate.android.data.meal.MealSelectionDashboardResponse

    @GET("admin/messes/{messId}/meals/dashboard")
    suspend fun getAdminMealDashboard(
        @Path("messId") messId: String
    ): com.messmate.android.data.meal.AdminMealDashboardResponse

    @GET("messes/{messId}/menus/today")
    suspend fun getTodayMenu(
        @Path("messId") messId: String
    ): com.messmate.android.data.menu.Menu

    @GET("messes/{messId}/menus")
    suspend fun getPublishedMenus(
        @Path("messId") messId: String
    ): List<com.messmate.android.data.menu.Menu>

    @GET("admin/messes/{messId}/menus")
    suspend fun getAllMenusAdmin(
        @Path("messId") messId: String
    ): List<com.messmate.android.data.menu.Menu>

    @POST("admin/messes/{messId}/menus")
    suspend fun upsertMenu(
        @Path("messId") messId: String,
        @Body request: com.messmate.android.data.menu.MenuRequest
    )

    @POST("messes/{messId}/expenses")
    suspend fun addExpense(
        @Path("messId") messId: String,
        @Body request: com.messmate.android.data.expense.ExpenseRequest
    ): com.messmate.android.data.expense.ExpenseResponse

    @GET("messes/{messId}/expenses")
    suspend fun getAllExpenses(
        @Path("messId") messId: String
    ): List<com.messmate.android.data.expense.ExpenseResponse>

    @PUT("messes/{messId}/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("messId") messId: String,
        @Path("expenseId") expenseId: String,
        @Body request: com.messmate.android.data.expense.ExpenseRequest
    ): com.messmate.android.data.expense.ExpenseResponse

    @DELETE("messes/{messId}/expenses/{expenseId}")
    suspend fun cancelExpense(
        @Path("messId") messId: String,
        @Path("expenseId") expenseId: String
    )
    
    // Settlement Endpoints
    @POST("messes/{messId}/settlements/generate")
    suspend fun generateSettlement(
        @Path("messId") messId: String,
        @Query("monthYear") monthYear: String
    ): com.messmate.android.data.mess.MonthlySettlementResponse
    
    @POST("messes/{messId}/settlements/{id}/close")
    suspend fun closeSettlement(
        @Path("messId") messId: String,
        @Path("id") id: String,
        @Query("monthYear") monthYear: String
    ): com.messmate.android.data.mess.MonthlySettlementResponse
    
    @POST("messes/{messId}/settlements/{id}/reopen")
    suspend fun reopenSettlement(
        @Path("messId") messId: String,
        @Path("id") id: String,
        @Query("monthYear") monthYear: String
    ): com.messmate.android.data.mess.MonthlySettlementResponse
}
