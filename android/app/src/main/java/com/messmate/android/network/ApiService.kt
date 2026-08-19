package com.messmate.android.network

import com.messmate.android.data.auth.AuthResponse
import com.messmate.android.data.auth.LoginRequest
import com.messmate.android.data.auth.SignupRequest
import com.messmate.android.data.balance.BalanceResponse
import com.messmate.android.data.mess.MessResponse
import com.messmate.android.data.mess.MessMembershipResponse
import com.messmate.android.data.mess.JoinMessRequest
import com.messmate.android.data.mess.MessMemberResponse
import com.messmate.android.data.mess.MonthlySettlementResponse
import com.messmate.android.data.meal.MealToggleRequest
import com.messmate.android.data.meal.MealResponse
import com.messmate.android.data.meal.MealStatusResponse
import com.messmate.android.data.meal.MealHistorySummaryResponse
import com.messmate.android.data.meal.MealSelectionDashboardResponse
import com.messmate.android.data.meal.AdminMealDashboardResponse
import com.messmate.android.data.menu.Menu
import com.messmate.android.data.menu.MenuRequest
import com.messmate.android.data.expense.ExpenseRequest
import com.messmate.android.data.expense.ExpenseResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    @POST("auth/google") 
    suspend fun googleLogin(@Body request: com.messmate.android.data.auth.GoogleLoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: SignupRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @PUT("auth/me/fcm-token")
    suspend fun updateFcmToken(@Body request: com.messmate.android.data.auth.FcmTokenRequest)


    @GET("messes/my")
    suspend fun getMyMesses(): List<MessMembershipResponse>

    @POST("messes/join")
    suspend fun joinMess(@Body request: JoinMessRequest): MessMembershipResponse
    
    // Admin Endpoints
    @GET("messes/{messId}/members")
    suspend fun getMessMembers(@Path("messId") messId: String): List<MessMemberResponse>

    @PUT("messes/{messId}/members/{memberId}/approve")
    suspend fun approveMember(@Path("messId") messId: String, @Path("memberId") memberId: String): MessMemberResponse

    @PUT("messes/{messId}/members/{memberId}/reject")
    suspend fun rejectMember(@Path("messId") messId: String, @Path("memberId") memberId: String): MessMemberResponse
    
    @POST("messes/{messId}/announcements")
    suspend fun sendAnnouncement(@Path("messId") messId: String, @Body request: com.messmate.android.data.mess.AnnouncementRequest): Map<String, Any>

    @PUT("messes/{messId}/members/{memberId}/role")
    suspend fun changeMemberRole(@Path("messId") messId: String, @Path("memberId") memberId: String, @Query("role") role: String): MessMemberResponse

    @GET("messes/{messId}/balance/me")
    suspend fun getMyBalance(@Path("messId") messId: String): BalanceResponse

    @GET("messes/{messId}/meals/today")
    suspend fun getTodayMealStatus(@Path("messId") messId: String): MealStatusResponse

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
    ): MealHistorySummaryResponse
    
    @GET("messes/{messId}/meals/dashboard")
    suspend fun getMealSelectionDashboard(
        @Path("messId") messId: String
    ): MealSelectionDashboardResponse

    @GET("messes/{messId}/meals/admin/dashboard")
    suspend fun getAdminMealDashboard(
        @Path("messId") messId: String,
        @Query("date") date: String? = null
    ): AdminMealDashboardResponse

    @GET("messes/{messId}/menus/today")
    suspend fun getTodayMenu(
        @Path("messId") messId: String
    ): Menu

    @GET("messes/{messId}/menus")
    suspend fun getPublishedMenus(
        @Path("messId") messId: String
    ): List<Menu>

    @GET("admin/messes/{messId}/menus")
    suspend fun getAllMenusAdmin(
        @Path("messId") messId: String
    ): List<Menu>

    @POST("admin/messes/{messId}/menus")
    suspend fun upsertMenu(
        @Path("messId") messId: String,
        @Body request: MenuRequest
    )

    @POST("messes/{messId}/expenses")
    suspend fun addExpense(
        @Path("messId") messId: String,
        @Body request: ExpenseRequest
    ): ExpenseResponse

    @POST("messes/{messId}/expenses/calculate-split")
    suspend fun calculateSplit(
        @Path("messId") messId: String,
        @Body request: ExpenseRequest
    ): List<com.messmate.android.data.expense.ExpenseShare>

    @GET("messes/{messId}/expenses")
    suspend fun getAllExpenses(
        @Path("messId") messId: String
    ): List<ExpenseResponse>

    @PUT("messes/{messId}/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("messId") messId: String,
        @Path("expenseId") expenseId: String,
        @Body request: ExpenseRequest
    ): ExpenseResponse

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
    ): MonthlySettlementResponse
    
    @POST("messes/{messId}/settlements/{id}/close")
    suspend fun closeSettlement(
        @Path("messId") messId: String,
        @Path("id") id: String,
        @Query("monthYear") monthYear: String
    ): MonthlySettlementResponse
    
    @POST("messes/{messId}/settlements/{id}/reopen")
    suspend fun reopenSettlement(
        @Path("messId") messId: String,
        @Path("id") id: String,
        @Query("monthYear") monthYear: String
    ): MonthlySettlementResponse

    // ─── Balance ────────────────────────────────────────────────────────────────

    @GET("messes/{messId}/balance/me")
    suspend fun getMyBalance(@Path("messId") messId: String): com.messmate.android.data.expense.BalanceResponse

    @GET("messes/{messId}/balance/group")
    suspend fun getGroupBalances(@Path("messId") messId: String): com.messmate.android.data.expense.GroupBalanceResponse

    // ─── Notifications ────────────────────────────────────────────────────────

    @GET("notifications/status")
    suspend fun getFcmStatus(): Map<String, Any>

    @POST("notifications/test")
    suspend fun sendTestNotification(): Map<String, Any>
}
