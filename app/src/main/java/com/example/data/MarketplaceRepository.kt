package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class MarketplaceRepository(private val appDao: AppDao) {

    // --- USERS ---
    fun getUserFlow(email: String): Flow<UserEntity?> = appDao.getUserFlow(email)
    suspend fun getUser(email: String): UserEntity? = appDao.getUser(email)
    suspend fun insertUser(user: UserEntity) = appDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = appDao.updateUser(user)
    suspend fun updateUserBalance(email: String, newBalance: Int) = appDao.updateUserBalance(email, newBalance)
    suspend fun updateUserKycStatus(email: String, status: String) = appDao.updateUserKycStatus(email, status)
    fun getAllUsers(): Flow<List<UserEntity>> = appDao.getAllUsers()
    suspend fun getUserCount(): Int = appDao.getUserCount()
    suspend fun updateUserBanStatus(email: String, isBanned: Boolean) = appDao.updateUserBanStatus(email, isBanned)

    // --- PRODUCTS ---
    fun getApprovedProducts(): Flow<List<ProductEntity>> = appDao.getApprovedProducts()
    fun getAllProducts(): Flow<List<ProductEntity>> = appDao.getAllProducts()
    fun getPendingProducts(): Flow<List<ProductEntity>> = appDao.getPendingProducts()
    fun getProductsBySeller(sellerEmail: String): Flow<List<ProductEntity>> = appDao.getProductsBySeller(sellerEmail)
    suspend fun getProductById(id: Long): ProductEntity? = appDao.getProductById(id)
    suspend fun insertProduct(product: ProductEntity) = appDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = appDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = appDao.deleteProduct(product)

    // --- REVIEWS ---
    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>> = appDao.getReviewsForProduct(productId)
    suspend fun insertReview(review: ReviewEntity) {
        appDao.insertReview(review)
        // Recalculate average rating
        val product = appDao.getProductById(review.productId) ?: return
        appDao.getReviewsForProduct(review.productId).firstOrNull()?.let { list ->
            val total = list.sumOf { it.rating }
            val avg = if (list.isNotEmpty()) total.toDouble() / list.size else 0.0
            appDao.updateProduct(product.copy(avgRating = avg, reviewCount = list.size))
        }
    }
    suspend fun hasUserReviewed(productId: Long, email: String): Boolean = appDao.hasUserReviewed(productId, email) > 0

    // --- PURCHASES ---
    fun getPurchasesByBuyer(buyerEmail: String): Flow<List<PurchaseEntity>> = appDao.getPurchasesByBuyer(buyerEmail)
    suspend fun hasPurchasedProduct(productId: Long, buyerEmail: String): Boolean = appDao.hasPurchasedProduct(productId, buyerEmail) > 0
    suspend fun buyProduct(productId: Long, buyerEmail: String): Boolean {
        val product = appDao.getProductById(productId) ?: return false
        val buyer = appDao.getUser(buyerEmail) ?: return false
        if (buyer.skullBalance < product.price) return false

        // Charge Buyer
        val newBuyerBalance = buyer.skullBalance - product.price
        appDao.updateUserBalance(buyerEmail, newBuyerBalance)

        // Pay Seller
        val seller = appDao.getUser(product.sellerEmail)
        if (seller != null) {
            appDao.updateUserBalance(product.sellerEmail, seller.skullBalance + product.price)
        }

        // Insert Purchase
        val receiptId = "TXN-SEC-${System.currentTimeMillis() % 10000000}"
        appDao.insertPurchase(
            PurchaseEntity(
                productId = productId,
                productTitle = product.title,
                buyerEmail = buyerEmail,
                pricePaid = product.price,
                receiptId = receiptId
            )
        )
        return true
    }

    // --- JOBS ---
    fun getOpenJobs(): Flow<List<JobEntity>> = appDao.getOpenJobs()
    fun getAllJobs(): Flow<List<JobEntity>> = appDao.getAllJobs()
    fun getPendingJobs(): Flow<List<JobEntity>> = appDao.getPendingJobs()
    fun getJobsByClient(email: String): Flow<List<JobEntity>> = appDao.getJobsByClient(email)
    fun getJobsByFreelancer(email: String): Flow<List<JobEntity>> = appDao.getJobsByFreelancer(email)
    suspend fun getJobById(id: Long): JobEntity? = appDao.getJobById(id)
    suspend fun insertJob(job: JobEntity) = appDao.insertJob(job)
    suspend fun updateJob(job: JobEntity) = appDao.updateJob(job)

    // --- PROPOSALS ---
    fun getProposalsForJob(jobId: Long): Flow<List<ProposalEntity>> = appDao.getProposalsForJob(jobId)
    fun getProposalsByFreelancer(email: String): Flow<List<ProposalEntity>> = appDao.getProposalsByFreelancer(email)
    suspend fun getProposalById(id: Long): ProposalEntity? = appDao.getProposalById(id)
    suspend fun insertProposal(proposal: ProposalEntity) = appDao.insertProposal(proposal)
    suspend fun updateProposal(proposal: ProposalEntity) = appDao.updateProposal(proposal)

    // --- TOPUPS ---
    fun getAllTopups(): Flow<List<TopupEntity>> = appDao.getAllTopups()
    fun getTopupsByUser(email: String): Flow<List<TopupEntity>> = appDao.getTopupsByUser(email)
    suspend fun getTopupById(id: Long): TopupEntity? = appDao.getTopupById(id)
    suspend fun insertTopup(topup: TopupEntity) = appDao.insertTopup(topup)
    suspend fun updateTopup(topup: TopupEntity) = appDao.updateTopup(topup)

    // --- WITHDRAWALS ---
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>> = appDao.getAllWithdrawals()
    fun getWithdrawalsByUser(email: String): Flow<List<WithdrawalEntity>> = appDao.getWithdrawalsByUser(email)
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity? = appDao.getWithdrawalById(id)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity) = appDao.insertWithdrawal(withdrawal)
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity) = appDao.updateWithdrawal(withdrawal)

    // --- DISPUTES ---
    fun getAllDisputes(): Flow<List<DisputeEntity>> = appDao.getAllDisputes()
    fun getDisputesByUser(email: String): Flow<List<DisputeEntity>> = appDao.getDisputesByUser(email)
    suspend fun getDisputeByJobId(jobId: Long): DisputeEntity? = appDao.getDisputeByJobId(jobId)
    suspend fun getDisputeById(id: Long): DisputeEntity? = appDao.getDisputeById(id)
    suspend fun insertDispute(dispute: DisputeEntity) = appDao.insertDispute(dispute)
    suspend fun updateDispute(dispute: DisputeEntity) = appDao.updateDispute(dispute)

    // --- REPORTS ---
    fun getAllReports(): Flow<List<ReportEntity>> = appDao.getAllReports()
    suspend fun getReportById(id: Long): ReportEntity? = appDao.getReportById(id)
    suspend fun insertReport(report: ReportEntity) = appDao.insertReport(report)
    suspend fun updateReport(report: ReportEntity) = appDao.updateReport(report)

    // --- KYC ---
    fun getAllKYC(): Flow<List<KYCEntity>> = appDao.getAllKYC()
    suspend fun getKYCByUser(email: String): KYCEntity? = appDao.getKYCByUser(email)
    suspend fun getKYCById(id: Long): KYCEntity? = appDao.getKYCById(id)
    suspend fun insertKYC(kyc: KYCEntity) = appDao.insertKYC(kyc)
    suspend fun updateKYC(kyc: KYCEntity) = appDao.updateKYC(kyc)

    // --- ADMIN LOGS ---
    fun getAllAdminLogs(): Flow<List<AdminLogEntity>> = appDao.getAllAdminLogs()
    suspend fun insertAdminLog(log: AdminLogEntity) = appDao.insertAdminLog(log)


    // --- INITIAL SEEDING ---
    suspend fun seedInitialData() {
        // No predefined Admin is seeded anymore. First registered user becomes Admin automatically.
        
        // Add standard vendors if not exists (for product relationship)
        val vendor1 = "shadow_coder@hellsec.com"
        if (appDao.getUser(vendor1) == null) {
            appDao.insertUser(UserEntity(email = vendor1, firstName = "Shadow", lastName = "Coder", username = "shadow_coder", mobileNumber = "01811122233", country = "Bangladesh", dateOfBirth = "1998-04-20", role = "Freelancer", skullBalance = 250, kycStatus = "APPROVED"))
        }
        val vendor2 = "cyber_sentinel@hellsec.com"
        if (appDao.getUser(vendor2) == null) {
            appDao.insertUser(UserEntity(email = vendor2, firstName = "Cyber", lastName = "Sentinel", username = "cyber_sentinel", mobileNumber = "01999888777", country = "Bangladesh", dateOfBirth = "1999-12-05", role = "Freelancer", skullBalance = 500, kycStatus = "APPROVED"))
        }

        // 2. Seed some premium cybersecurity products if store is empty
        val currentProductsFlow = appDao.getApprovedProducts()
        // Let's do a fast check
        val allProductsList = appDao.getProductById(1)
        if (allProductsList == null) {
            appDao.insertProduct(
                ProductEntity(
                    id = 1,
                    title = "HellSec Hydra-Port Scanner Pro",
                    description = "Ultra-fast asynchronous TCP/UDP port scanner with built-in service version detection, Banner Grabbing, and CVE mapping engine. Optimized for cybersecurity experts and red team researchers.",
                    category = "Security Tools",
                    price = 45,
                    tags = "scanner,hydra,port,recon,cve",
                    thumbnailUri = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&q=80&w=300",
                    productFileUri = "content://hellsec/downloads/hydra_scanner_pro_v2.bin",
                    version = "2.1.4",
                    sellerEmail = "shadow_coder@hellsec.com",
                    sellerName = "Shadow Coder",
                    status = "APPROVED",
                    avgRating = 4.8,
                    reviewCount = 2
                )
            )
            appDao.insertProduct(
                ProductEntity(
                    id = 2,
                    title = "AI Vulnerability Auto-Patcher",
                    description = "Advanced AI tool utilizing deep neural networks to inspect source code repository directories, automatically detect OWASP Top 10 vulnerabilities, and generate compliant patch pull-requests instantly.",
                    category = "AI Tools",
                    price = 80,
                    tags = "ai,patcher,owasp,vulnerabilities",
                    thumbnailUri = "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&q=80&w=300",
                    productFileUri = "content://hellsec/downloads/ai_patcher_v1.0.zip",
                    version = "1.0.0",
                    sellerEmail = "cyber_sentinel@hellsec.com",
                    sellerName = "Cyber Sentinel",
                    status = "APPROVED",
                    avgRating = 5.0,
                    reviewCount = 1
                )
            )
            appDao.insertProduct(
                ProductEntity(
                    id = 3,
                    title = "KubeDefender: Kubernetes Security Hardening Tool",
                    description = "Production-ready CLI script that audits Kubernetes cluster setups, checks RBAC policies, scans docker container images, and generates YAML security context configurations.",
                    category = "Security Tools",
                    price = 30,
                    tags = "kubernetes,k8s,cloud-security,rbac",
                    thumbnailUri = "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?auto=format&fit=crop&q=80&w=300",
                    productFileUri = "content://hellsec/downloads/kubedefender_audit.sh",
                    version = "1.2.0",
                    sellerEmail = "shadow_coder@hellsec.com",
                    sellerName = "Shadow Coder",
                    status = "APPROVED",
                    avgRating = 4.5,
                    reviewCount = 1
                )
            )

            // Seed reviews
            appDao.insertReview(ReviewEntity(productId = 1, reviewerEmail = "cyber_sentinel@hellsec.com", reviewerName = "Cyber Sentinel", rating = 5, reviewText = "Blazing fast scanner. It mapped all vulnerability CVEs correctly in less than 5 minutes! Best tool in my arsenal."))
            appDao.insertReview(ReviewEntity(productId = 1, reviewerEmail = "client@hellsec.com", reviewerName = "John Client", rating = 4, reviewText = "Extremely useful tool, GUI would be nice but CLI is highly detailed and solid."))
            appDao.insertReview(ReviewEntity(productId = 2, reviewerEmail = "shadow_coder@hellsec.com", reviewerName = "Shadow Coder", rating = 5, reviewText = "This AI tool has saved our Dev team hours of back-and-forth fixing code security issues. Incredible work."))
            appDao.insertReview(ReviewEntity(productId = 3, reviewerEmail = "client@hellsec.com", reviewerName = "John Client", rating = 4, reviewText = "A must-have CLI tool for anyone running cloud services. Simple and highly informative."))
        }

        // 3. Seed some open escrow jobs if empty
        val jobCheck = appDao.getJobById(1)
        if (jobCheck == null) {
            appDao.insertJob(
                JobEntity(
                    id = 1,
                    title = "Penetration Testing of FinTech Android Application",
                    description = "We require a certified cybersecurity professional to execute a deep penetration test on our upcoming fintech mobile app. Deliverables must include a comprehensive report outlining vulnerabilities, exploit vectors, and mitigation techniques. All findings must remain strictly confidential.",
                    budget = 150,
                    deadline = "7 Days",
                    skills = "Android Pentesting, Reverse Engineering, OWASP Mobile, Security Audit",
                    clientEmail = "client@hellsec.com",
                    clientName = "John Client",
                    postingFee = 10,
                    status = "OPEN"
                )
            )
            appDao.insertJob(
                JobEntity(
                    id = 2,
                    title = "Secure Flask REST API Development",
                    description = "Need a backend developer to build a high-security REST API using Python Flask. Must implement JWT, Rate Limiting, input validation, bcrypt hashing, and secure SQL Queries (protection against injection). Provide clean documentation.",
                    budget = 40,
                    deadline = "3 Days",
                    skills = "Python, Flask, REST API, Secure Coding, PostgreSQL",
                    clientEmail = "system_ops@hellsec.com",
                    clientName = "System Operator",
                    postingFee = 5,
                    status = "OPEN"
                )
            )
        }
    }
}
