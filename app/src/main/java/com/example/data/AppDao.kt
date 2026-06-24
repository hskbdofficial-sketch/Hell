package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USERS ---
    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserFlow(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET skullBalance = :newBalance WHERE email = :email")
    suspend fun updateUserBalance(email: String, newBalance: Int)

    @Query("UPDATE users SET kycStatus = :status WHERE email = :email")
    suspend fun updateUserKycStatus(email: String, status: String)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("UPDATE users SET isBanned = :isBanned WHERE email = :email")
    suspend fun updateUserBanStatus(email: String, isBanned: Boolean)


    // --- PRODUCTS ---
    @Query("SELECT * FROM products WHERE status = 'APPROVED' ORDER BY timestamp DESC")
    fun getApprovedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE status = 'PENDING_REVIEW' ORDER BY timestamp DESC")
    fun getPendingProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerEmail = :sellerEmail ORDER BY timestamp DESC")
    fun getProductsBySeller(sellerEmail: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)


    // --- PRODUCT REVIEWS ---
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY timestamp DESC")
    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT COUNT(*) FROM product_reviews WHERE productId = :productId AND reviewerEmail = :email")
    suspend fun hasUserReviewed(productId: Long, email: String): Int


    // --- PURCHASES ---
    @Query("SELECT * FROM purchases WHERE buyerEmail = :buyerEmail ORDER BY timestamp DESC")
    fun getPurchasesByBuyer(buyerEmail: String): Flow<List<PurchaseEntity>>

    @Query("SELECT COUNT(*) FROM purchases WHERE productId = :productId AND buyerEmail = :buyerEmail")
    suspend fun hasPurchasedProduct(productId: Long, buyerEmail: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)


    // --- JOBS ---
    @Query("SELECT * FROM jobs WHERE status = 'OPEN' ORDER BY timestamp DESC")
    fun getOpenJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs ORDER BY timestamp DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE status = 'PENDING_APPROVAL' ORDER BY timestamp DESC")
    fun getPendingJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE clientEmail = :email ORDER BY timestamp DESC")
    fun getJobsByClient(email: String): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE freelancerEmail = :email ORDER BY timestamp DESC")
    fun getJobsByFreelancer(email: String): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getJobById(id: Long): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)


    // --- PROPOSALS (JOB APPLICATIONS) ---
    @Query("SELECT * FROM job_applications WHERE jobId = :jobId ORDER BY bidAmount ASC")
    fun getProposalsForJob(jobId: Long): Flow<List<ProposalEntity>>

    @Query("SELECT * FROM job_applications WHERE freelancerEmail = :email ORDER BY timestamp DESC")
    fun getProposalsByFreelancer(email: String): Flow<List<ProposalEntity>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getProposalById(id: Long): ProposalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: ProposalEntity)

    @Update
    suspend fun updateProposal(proposal: ProposalEntity)


    // --- WALLET TOPUPS (BKASH) ---
    @Query("SELECT * FROM wallet_topups ORDER BY timestamp DESC")
    fun getAllTopups(): Flow<List<TopupEntity>>

    @Query("SELECT * FROM wallet_topups WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getTopupsByUser(email: String): Flow<List<TopupEntity>>

    @Query("SELECT * FROM wallet_topups WHERE id = :id")
    suspend fun getTopupById(id: Long): TopupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopup(topup: TopupEntity)

    @Update
    suspend fun updateTopup(topup: TopupEntity)


    // --- WITHDRAWALS (BKASH) ---
    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getWithdrawalsByUser(email: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)


    // --- DISPUTES ---
    @Query("SELECT * FROM disputes ORDER BY timestamp DESC")
    fun getAllDisputes(): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE clientEmail = :email OR freelancerEmail = :email ORDER BY timestamp DESC")
    fun getDisputesByUser(email: String): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE jobId = :jobId")
    suspend fun getDisputeByJobId(jobId: Long): DisputeEntity?

    @Query("SELECT * FROM disputes WHERE id = :id")
    suspend fun getDisputeById(id: Long): DisputeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Update
    suspend fun updateDispute(dispute: DisputeEntity)


    // --- REPORTS ---
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: Long): ReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)


    // --- KYC VERIFICATIONS ---
    @Query("SELECT * FROM nid_verifications ORDER BY timestamp DESC")
    fun getAllKYC(): Flow<List<KYCEntity>>

    @Query("SELECT * FROM nid_verifications WHERE userEmail = :email ORDER BY timestamp DESC")
    suspend fun getKYCByUser(email: String): KYCEntity?

    @Query("SELECT * FROM nid_verifications WHERE id = :id")
    suspend fun getKYCById(id: Long): KYCEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKYC(kyc: KYCEntity)

    @Update
    suspend fun updateKYC(kyc: KYCEntity)


    // --- ADMIN LOGS ---
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllAdminLogs(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLogEntity)
}
