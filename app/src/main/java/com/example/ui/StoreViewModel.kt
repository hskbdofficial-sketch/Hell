package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HellSecApplication
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HellSecApplication
    private val repo = app.repository
    private val session = app.sessionManager

    // --- SESSION STATE ---
    val currentUserEmail: StateFlow<String?> = session.currentUserEmail

    val currentUser: StateFlow<UserEntity?> = currentUserEmail
        .flatMapLatest { email ->
            if (email != null) repo.getUserFlow(email) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- DB LISTS (REACTIVELY UPDATED) ---
    val allUsers: StateFlow<List<UserEntity>> = repo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repo.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedProducts: StateFlow<List<ProductEntity>> = repo.getApprovedProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingProducts: StateFlow<List<ProductEntity>> = repo.getPendingProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<JobEntity>> = repo.getAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openJobs: StateFlow<List<JobEntity>> = repo.getOpenJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingJobs: StateFlow<List<JobEntity>> = repo.getPendingJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTopups: StateFlow<List<TopupEntity>> = repo.getAllTopups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repo.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDisputes: StateFlow<List<DisputeEntity>> = repo.getAllDisputes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ReportEntity>> = repo.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKYCs: StateFlow<List<KYCEntity>> = repo.getAllKYC()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminLogs: StateFlow<List<AdminLogEntity>> = repo.getAllAdminLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getReviewsForProduct(productId: Long): kotlinx.coroutines.flow.Flow<List<com.example.data.ReviewEntity>> {
        return repo.getReviewsForProduct(productId)
    }

    fun getProposalsForJob(jobId: Long): kotlinx.coroutines.flow.Flow<List<com.example.data.ProposalEntity>> {
        return repo.getProposalsForJob(jobId)
    }

    private val storePrefs = application.getSharedPreferences("hellsec_store_prefs", android.content.Context.MODE_PRIVATE)

    private val decryptedBkashNumber: String = storePrefs.getString("bkash_merchant_number", null)?.let {
        com.example.data.CryptoUtils.decrypt(it)
    } ?: "+8801700998877"

    private val _bkashMerchantNumber = MutableStateFlow(decryptedBkashNumber)
    val bkashMerchantNumber: StateFlow<String> = _bkashMerchantNumber.asStateFlow()

    fun updateBkashMerchantNumber(newNumber: String) {
        val encrypted = com.example.data.CryptoUtils.encrypt(newNumber)
        storePrefs.edit().putString("bkash_merchant_number", encrypted).apply()
        _bkashMerchantNumber.value = newNumber
        triggerMessage("bKash Merchant number updated securely.")
    }


    // --- UI EVENT MESSAGES ---
    private val _uiEventMessage = MutableSharedFlow<String>()
    val uiEventMessage = _uiEventMessage.asSharedFlow()

    fun triggerMessage(msg: String) {
        viewModelScope.launch {
            _uiEventMessage.emit(msg)
        }
    }


    // --- AUTHENTICATION ACTIONS ---
    fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        passwordHash: String, // Represent secure password handle
        mobileNumber: String,
        country: String,
        dateOfBirth: String,
        role: String, // "User" or "Freelancer"
        securityQuestion: String,
        securityAnswer: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val existing = repo.getUser(email)
            if (existing != null) {
                onResult(false, "Email is already registered.")
                return@launch
            }

            // The very first opening account will be saved as Admin automatically
            val userCount = repo.getUserCount()
            val isFirstUser = (userCount == 0)
            val resolvedRole = if (isFirstUser) "Admin" else role
            val startingBalance = if (isFirstUser) 999999 else 10

            val encryptedPassword = com.example.data.CryptoUtils.encrypt(passwordHash)
            val encryptedAnswer = com.example.data.CryptoUtils.encrypt(securityAnswer.lowercase().trim())

            val newUser = UserEntity(
                email = email,
                firstName = firstName,
                lastName = lastName,
                username = username,
                mobileNumber = mobileNumber,
                country = country,
                dateOfBirth = dateOfBirth,
                role = resolvedRole,
                skullBalance = startingBalance,
                kycStatus = if (isFirstUser) "APPROVED" else "NOT_STARTED",
                passwordHash = encryptedPassword,
                securityQuestion = securityQuestion,
                securityAnswerHash = encryptedAnswer
            )

            repo.insertUser(newUser)
            session.login(email)
            if (isFirstUser) {
                onResult(true, "First operator registered! Secured Admin rights granted.")
            } else {
                onResult(true, "Registration successful! You received 10 SKULL FREE!")
            }
        }
    }

    fun loginUser(email: String, passwordHash: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repo.getUser(email)
            if (user == null) {
                onResult(false, "No account found with this email.")
                return@launch
            }
            if (user.isBanned) {
                onResult(false, "Your account has been permanently suspended by HellSec Admin.")
                return@launch
            }

            if (user.passwordHash.isNotEmpty()) {
                val decryptedSaved = com.example.data.CryptoUtils.decrypt(user.passwordHash)
                if (decryptedSaved != passwordHash) {
                    onResult(false, "Invalid credentials.")
                    return@launch
                }
            }

            session.login(email)
            onResult(true, "Login successful.")
        }
    }

    fun recoverPassword(
        email: String,
        securityAnswer: String,
        newPasswordHash: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = repo.getUser(email)
            if (user == null) {
                onResult(false, "No account found with this email.")
                return@launch
            }
            if (user.securityAnswerHash.isEmpty()) {
                onResult(false, "This account does not have recovery security configured.")
                return@launch
            }

            val decryptedSavedAnswer = com.example.data.CryptoUtils.decrypt(user.securityAnswerHash).lowercase().trim()
            val providedAnswer = securityAnswer.lowercase().trim()

            if (decryptedSavedAnswer != providedAnswer) {
                onResult(false, "Incorrect answer to security recovery question.")
                return@launch
            }

            val encryptedNewPassword = com.example.data.CryptoUtils.encrypt(newPasswordHash)
            repo.updateUser(user.copy(passwordHash = encryptedNewPassword))
            onResult(true, "Password recovered successfully! Please login with your new password.")
        }
    }

    fun logoutUser() {
        session.logout()
    }

    fun updateProfile(firstName: String, lastName: String, mobileNumber: String, country: String) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch
            repo.updateUser(
                user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    mobileNumber = mobileNumber,
                    country = country
                )
            )
            triggerMessage("Profile updated successfully.")
        }
    }


    // --- KYC VERIFICATION FLOW ---
    fun submitKYC(nidNumber: String, nidFrontUri: String, nidBackUri: String, selfieUri: String) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch

            val kyc = KYCEntity(
                userEmail = email,
                username = user.username,
                nidNumber = nidNumber,
                nidFrontUri = nidFrontUri,
                nidBackUri = nidBackUri,
                selfieUri = selfieUri,
                status = "PENDING"
            )

            repo.insertKYC(kyc)
            repo.updateUserKycStatus(email, "PENDING")
            triggerMessage("KYC documents submitted securely for review.")
        }
    }


    // --- BKASH WALLET SYSTEM ---
    fun submitTopup(transactionId: String, senderNumber: String, amount: Int, screenshotUri: String) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch

            val topup = TopupEntity(
                userEmail = email,
                username = user.username,
                transactionId = transactionId,
                senderNumber = senderNumber,
                amount = amount,
                screenshotUri = screenshotUri,
                status = "PENDING"
            )

            repo.insertTopup(topup)
            triggerMessage("bKash top-up request submitted. Pending review.")
        }
    }

    fun submitWithdrawal(receiverNumber: String, amount: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch

            if (amount < 50) {
                onResult(false, "Minimum withdrawal limit is 50 Skull.")
                return@launch
            }

            val totalCost = amount + 5 // Fee is 5 Skull
            if (user.skullBalance < totalCost) {
                onResult(false, "Insufficient balance. Minimum fee 5 Skull applies.")
                return@launch
            }

            // Deduct immediately (held in withdrawal request)
            repo.updateUserBalance(email, user.skullBalance - totalCost)

            val withdrawal = WithdrawalEntity(
                userEmail = email,
                username = user.username,
                receiverNumber = receiverNumber,
                amount = amount,
                fee = 5,
                status = "PENDING"
            )

            repo.insertWithdrawal(withdrawal)
            onResult(true, "bKash withdrawal request submitted successfully.")
        }
    }


    // --- DIGITAL PRODUCT MARKETPLACE ---
    fun submitProduct(
        title: String,
        description: String,
        category: String,
        price: Int,
        tags: String,
        thumbnailUri: String,
        productFileUri: String,
        version: String
    ) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch

            val product = ProductEntity(
                title = title,
                description = description,
                category = category,
                price = price,
                tags = tags,
                thumbnailUri = thumbnailUri,
                productFileUri = productFileUri,
                version = version,
                sellerEmail = email,
                sellerName = user.username,
                status = "PENDING_REVIEW"
            )

            repo.insertProduct(product)
            triggerMessage("Product submitted successfully. Pending Admin review.")
        }
    }

    fun purchaseProduct(productId: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val success = repo.buyProduct(productId, email)
            if (success) {
                onResult(true, "Product purchased successfully. Code/file available for download.")
            } else {
                onResult(false, "Purchase failed. Verify your balance and try again.")
            }
        }
    }

    fun submitProductReview(productId: Long, rating: Int, reviewText: String) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch

            val review = ReviewEntity(
                productId = productId,
                reviewerEmail = email,
                reviewerName = user.username,
                rating = rating,
                reviewText = reviewText
            )

            repo.insertReview(review)
            triggerMessage("Thank you for your rating.")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repo.deleteProduct(product)
            triggerMessage("Product deleted successfully.")
        }
    }


    // --- JOBS & BIDDING ESCROW SYSTEM ---
    fun submitJob(
        title: String,
        description: String,
        budget: Int,
        deadline: String,
        skills: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch

            // Job posting fee logic:
            // Posting Fee: 5 Skull. If Budget > 50 Skull: Additional 5 Skull (Total 10 Skull).
            val postingFee = if (budget > 50) 10 else 5
            val totalRequired = postingFee + budget // Budget is held in escrow immediately upon post approval or submission?
            // "Client Creates Job -> Wallet Deduction -> Escrow Wallet"
            if (user.skullBalance < totalRequired) {
                onResult(false, "Insufficient balance. Job post requires $postingFee Skull fee and $budget Skull for escrow.")
                return@launch
            }

            // Deduct total budget + fee immediately from client wallet
            repo.updateUserBalance(email, user.skullBalance - totalRequired)

            val job = JobEntity(
                title = title,
                description = description,
                budget = budget,
                deadline = deadline,
                skills = skills,
                clientEmail = email,
                clientName = user.username,
                postingFee = postingFee,
                status = "PENDING_APPROVAL"
            )

            repo.insertJob(job)
            onResult(true, "Job posted successfully. Deducted $postingFee SKULL posting fee and $budget SKULL held in Escrow. Pending Admin approval.")
        }
    }

    fun submitProposal(jobId: Long, jobTitle: String, bidAmount: Int, deliveryTime: String, portfolio: String, coverLetter: String) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            val user = repo.getUser(email) ?: return@launch

            val proposal = ProposalEntity(
                jobId = jobId,
                jobTitle = jobTitle,
                freelancerEmail = email,
                freelancerName = user.username,
                bidAmount = bidAmount,
                deliveryTime = deliveryTime,
                portfolio = portfolio,
                coverLetter = coverLetter,
                status = "PENDING"
            )

            repo.insertProposal(proposal)
            triggerMessage("Proposal submitted successfully to the Client.")
        }
    }

    fun acceptProposal(proposal: ProposalEntity) {
        viewModelScope.launch {
            val job = repo.getJobById(proposal.jobId) ?: return@launch
            // Update job status to IN_PROGRESS, assign freelancer
            val updatedJob = job.copy(
                status = "IN_PROGRESS",
                freelancerEmail = proposal.freelancerEmail,
                freelancerName = proposal.freelancerName
            )
            repo.updateJob(updatedJob)

            // Update proposal status
            repo.updateProposal(proposal.copy(status = "ACCEPTED"))

            // Automatically decline other proposals for this job
            repo.getProposalsForJob(job.id).firstOrNull()?.forEach { prop ->
                if (prop.id != proposal.id) {
                    repo.updateProposal(prop.copy(status = "REJECTED"))
                }
            }

            triggerMessage("Proposal accepted! Job is now In Progress.")
        }
    }

    fun submitWork(jobId: Long, submissionText: String, submissionFileUri: String) {
        viewModelScope.launch {
            val job = repo.getJobById(jobId) ?: return@launch
            val updatedJob = job.copy(
                status = "SUBMITTED",
                workSubmissionText = submissionText,
                workSubmissionFileUri = submissionFileUri
            )
            repo.updateJob(updatedJob)
            triggerMessage("Work deliverables submitted securely. Client review pending.")
        }
    }

    fun approveWork(jobId: Long, isApprovedByClient: Boolean) {
        viewModelScope.launch {
            val job = repo.getJobById(jobId) ?: return@launch
            if (isApprovedByClient) {
                // Escrow wallet -> Freelancer wallet
                val freelancer = repo.getUser(job.freelancerEmail)
                if (freelancer != null) {
                    repo.updateUserBalance(job.freelancerEmail, freelancer.skullBalance + job.budget)
                }
                repo.updateJob(job.copy(status = "COMPLETED"))
                triggerMessage("Job completed! ${job.budget} SKULL released from Escrow to Freelancer.")
            }
        }
    }


    // --- DISPUTES ---
    fun fileDispute(jobId: Long, reason: String, description: String, screenshotUri: String) {
        viewModelScope.launch {
            val job = repo.getJobById(jobId) ?: return@launch
            val email = currentUserEmail.value ?: return@launch

            val dispute = DisputeEntity(
                jobId = jobId,
                jobTitle = job.title,
                clientEmail = job.clientEmail,
                freelancerEmail = job.freelancerEmail,
                disputeCreatorEmail = email,
                reason = reason,
                description = description,
                clientEvidenceText = if (email == job.clientEmail) description else "",
                freelancerEvidenceText = if (email == job.freelancerEmail) description else "",
                clientEvidenceFile = if (email == job.clientEmail) screenshotUri else "",
                freelancerEvidenceFile = if (email == job.freelancerEmail) screenshotUri else "",
                status = "PENDING"
            )

            repo.insertDispute(dispute)
            repo.updateJob(job.copy(status = "DISPUTED"))
            triggerMessage("Dispute ticket filed. HellSec Admin will investigate.")
        }
    }

    fun submitDisputeEvidence(disputeId: Long, text: String, fileUri: String) {
        viewModelScope.launch {
            val dispute = repo.getDisputeById(disputeId) ?: return@launch
            val email = currentUserEmail.value ?: return@launch

            val updated = if (email == dispute.clientEmail) {
                dispute.copy(clientEvidenceText = text, clientEvidenceFile = fileUri)
            } else {
                dispute.copy(freelancerEvidenceText = text, freelancerEvidenceFile = fileUri)
            }

            repo.updateDispute(updated)
            triggerMessage("Evidence submitted successfully.")
        }
    }


    // --- USER REPORTING ---
    fun submitReport(reportedUserEmail: String, reason: String, description: String, screenshotUri: String) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch

            val report = ReportEntity(
                reportedUserEmail = reportedUserEmail,
                reporterEmail = email,
                reason = reason,
                description = description,
                screenshotUri = screenshotUri,
                status = "PENDING"
            )

            repo.insertReport(report)
            triggerMessage("User reported securely. Admin will audit profile activity.")
        }
    }


    // --- ADMIN ACTION HANDLERS ---
    fun adminApproveKYC(kycId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val kyc = repo.getKYCById(kycId) ?: return@launch

            repo.updateKYC(kyc.copy(status = "APPROVED"))
            repo.updateUserKycStatus(kyc.userEmail, "APPROVED")

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "KYC_APPROVE",
                    details = "Approved KYC verification for user ${kyc.userEmail} (Username: ${kyc.username})"
                )
            )
            triggerMessage("KYC approved successfully.")
        }
    }

    fun adminRejectKYC(kycId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val kyc = repo.getKYCById(kycId) ?: return@launch

            repo.updateKYC(kyc.copy(status = "REJECTED"))
            repo.updateUserKycStatus(kyc.userEmail, "REJECTED")

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "KYC_REJECT",
                    details = "Rejected KYC verification for user ${kyc.userEmail}"
                )
            )
            triggerMessage("KYC rejected successfully.")
        }
    }

    fun adminApproveTopup(topupId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val topup = repo.getTopupById(topupId) ?: return@launch

            repo.updateTopup(topup.copy(status = "APPROVED"))

            // Credit balance
            val user = repo.getUser(topup.userEmail)
            if (user != null) {
                repo.updateUserBalance(topup.userEmail, user.skullBalance + topup.amount)
            }

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "TOPUP_APPROVE",
                    details = "Approved bKash deposit of ${topup.amount} SKULL for ${topup.userEmail}. TXN: ${topup.transactionId}"
                )
            )
            triggerMessage("Deposit approved! ${topup.amount} SKULL credited.")
        }
    }

    fun adminRejectTopup(topupId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val topup = repo.getTopupById(topupId) ?: return@launch

            repo.updateTopup(topup.copy(status = "REJECTED"))

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "TOPUP_REJECT",
                    details = "Rejected bKash deposit of ${topup.amount} SKULL for ${topup.userEmail}. TXN: ${topup.transactionId}"
                )
            )
            triggerMessage("Deposit rejected successfully.")
        }
    }

    fun adminApproveWithdrawal(withdrawalId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val withdrawal = repo.getWithdrawalById(withdrawalId) ?: return@launch

            repo.updateWithdrawal(withdrawal.copy(status = "APPROVED"))

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "WITHDRAW_APPROVE",
                    details = "Approved and paid bKash withdrawal of ${withdrawal.amount} SKULL to ${withdrawal.receiverNumber} (User: ${withdrawal.userEmail})"
                )
            )
            triggerMessage("Withdrawal approved and completed.")
        }
    }

    fun adminRejectWithdrawal(withdrawalId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val withdrawal = repo.getWithdrawalById(withdrawalId) ?: return@launch

            repo.updateWithdrawal(withdrawal.copy(status = "REJECTED"))

            // Refund balance (withdrawal amount + fee)
            val user = repo.getUser(withdrawal.userEmail)
            if (user != null) {
                repo.updateUserBalance(withdrawal.userEmail, user.skullBalance + withdrawal.amount + withdrawal.fee)
            }

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "WITHDRAW_REJECT",
                    details = "Rejected withdrawal of ${withdrawal.amount} SKULL for ${withdrawal.userEmail}. Refunded client."
                )
            )
            triggerMessage("Withdrawal rejected. Balance refunded to user.")
        }
    }

    fun adminApproveProduct(productId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val product = repo.getProductById(productId) ?: return@launch

            repo.updateProduct(product.copy(status = "APPROVED"))

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "PRODUCT_APPROVE",
                    details = "Approved digital product: ${product.title} (Seller: ${product.sellerEmail})"
                )
            )
            triggerMessage("Product approved and published.")
        }
    }

    fun adminRejectProduct(productId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val product = repo.getProductById(productId) ?: return@launch

            repo.updateProduct(product.copy(status = "REJECTED"))

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "PRODUCT_REJECT",
                    details = "Rejected digital product: ${product.title} (Seller: ${product.sellerEmail})"
                )
            )
            triggerMessage("Product review rejected.")
        }
    }

    fun adminApproveJob(jobId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val job = repo.getJobById(jobId) ?: return@launch

            repo.updateJob(job.copy(status = "OPEN"))

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "JOB_APPROVE",
                    details = "Approved escrow job posting: ${job.title} (Client: ${job.clientEmail})"
                )
            )
            triggerMessage("Escrow job approved and listed.")
        }
    }

    fun adminRejectJob(jobId: Long) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val job = repo.getJobById(jobId) ?: return@launch

            repo.updateJob(job.copy(status = "CANCELLED"))

            // Refund client (posting fee + budget)
            val client = repo.getUser(job.clientEmail)
            if (client != null) {
                repo.updateUserBalance(job.clientEmail, client.skullBalance + job.postingFee + job.budget)
            }

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "JOB_REJECT",
                    details = "Rejected escrow job posting: ${job.title}. Refunding ${job.postingFee + job.budget} SKULL."
                )
            )
            triggerMessage("Job rejected. Posting fee and escrow budget refunded.")
        }
    }

    fun adminResolveDispute(disputeId: Long, resolution: String, clientAmount: Int, freelancerAmount: Int) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val dispute = repo.getDisputeById(disputeId) ?: return@launch
            val job = repo.getJobById(dispute.jobId) ?: return@launch

            // Perform distribution of escrowed budget based on resolution
            // Double check total matches job budget
            if (clientAmount + freelancerAmount != job.budget) {
                triggerMessage("Error: Sum of split amounts must equal total budget (${job.budget} SKULL).")
                return@launch
            }

            // Distribute to Client
            if (clientAmount > 0) {
                val client = repo.getUser(dispute.clientEmail)
                if (client != null) {
                    repo.updateUserBalance(dispute.clientEmail, client.skullBalance + clientAmount)
                }
            }

            // Distribute to Freelancer
            if (freelancerAmount > 0) {
                val freelancer = repo.getUser(dispute.freelancerEmail)
                if (freelancer != null) {
                    repo.updateUserBalance(dispute.freelancerEmail, freelancer.skullBalance + freelancerAmount)
                }
            }

            val status = when {
                clientAmount > 0 && freelancerAmount == 0 -> "RESOLVED_REFUNDED"
                freelancerAmount > 0 && clientAmount == 0 -> "RESOLVED_PAID"
                else -> "RESOLVED_SPLIT"
            }

            repo.updateDispute(
                dispute.copy(
                    status = status,
                    resolutionDetails = "Resolution: $resolution. Client refunded: $clientAmount SKULL. Freelancer paid: $freelancerAmount SKULL."
                )
            )

            repo.updateJob(job.copy(status = "COMPLETED")) // close escrow

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "DISPUTE_RESOLVE",
                    details = "Resolved dispute for job #${job.id}: $resolution. Split client: $clientAmount, freelancer: $freelancerAmount"
                )
            )
            triggerMessage("Dispute resolved and funds distributed.")
        }
    }

    fun adminHandleReport(reportId: Long, action: String) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            val report = repo.getReportById(reportId) ?: return@launch

            repo.updateReport(report.copy(status = "RESOLVED", actionTaken = action))

            if (action == "SUSPENDED" || action == "PERMANENT_BAN") {
                repo.updateUserBanStatus(report.reportedUserEmail, true)
            }

            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "REPORT_RESOLVE",
                    details = "Resolved report against ${report.reportedUserEmail}. Action taken: $action"
                )
            )
            triggerMessage("Report resolved. User action executed.")
        }
    }

    fun adminUnbanUser(email: String) {
        viewModelScope.launch {
            val adminEmail = currentUserEmail.value ?: return@launch
            repo.updateUserBanStatus(email, false)
            repo.insertAdminLog(
                AdminLogEntity(
                    adminEmail = adminEmail,
                    action = "USER_UNBAN",
                    details = "Unbanned user account: $email"
                )
            )
            triggerMessage("User unbanned successfully.")
        }
    }
}
