package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String, // Firebase email serves as unique identifier
    val firstName: String,
    val lastName: String,
    val username: String,
    val mobileNumber: String,
    val country: String,
    val dateOfBirth: String,
    val role: String, // "User" or "Freelancer" or "Admin"
    val skullBalance: Int = 10, // Default 10 SKULL free upon sign up
    val kycStatus: String = "NOT_STARTED", // "NOT_STARTED", "PENDING", "APPROVED", "REJECTED"
    val isBanned: Boolean = false,
    val profilePicUri: String = "",
    val passwordHash: String = "",
    val securityQuestion: String = "What is your high school name?",
    val securityAnswerHash: String = ""
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // Python Tools, Android Apps, Desktop Software, Source Code, APIs, Templates, Security Tools, AI Tools, Digital Assets
    val price: Int, // in Skull (1 Skull = 1 BDT)
    val tags: String, // Comma separated tags
    val thumbnailUri: String,
    val productFileUri: String,
    val version: String,
    val sellerEmail: String,
    val sellerName: String,
    val status: String = "PENDING_REVIEW", // "PENDING_REVIEW", "APPROVED", "REJECTED"
    val avgRating: Double = 0.0,
    val reviewCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val reviewerEmail: String,
    val reviewerName: String,
    val rating: Int, // 1 to 5 stars
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productTitle: String,
    val buyerEmail: String,
    val pricePaid: Int,
    val receiptId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val budget: Int, // in Skull
    val deadline: String,
    val skills: String, // Comma separated
    val clientEmail: String,
    val clientName: String,
    val freelancerEmail: String = "",
    val freelancerName: String = "",
    val postingFee: Int,
    val status: String = "PENDING_APPROVAL", // "PENDING_APPROVAL", "OPEN", "IN_PROGRESS", "SUBMITTED", "COMPLETED", "CANCELLED", "DISPUTED"
    val workSubmissionText: String = "",
    val workSubmissionFileUri: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_applications")
data class ProposalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val jobTitle: String,
    val freelancerEmail: String,
    val freelancerName: String,
    val bidAmount: Int,
    val deliveryTime: String,
    val portfolio: String,
    val coverLetter: String,
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_topups")
data class TopupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val username: String,
    val transactionId: String,
    val senderNumber: String,
    val amount: Int,
    val screenshotUri: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val username: String,
    val receiverNumber: String, // bKash receiver
    val amount: Int, // Skull amount
    val fee: Int = 5, // Fee is 5 Skull
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val jobTitle: String,
    val clientEmail: String,
    val freelancerEmail: String,
    val disputeCreatorEmail: String,
    val reason: String,
    val description: String,
    val clientEvidenceText: String = "",
    val freelancerEvidenceText: String = "",
    val clientEvidenceFile: String = "",
    val freelancerEvidenceFile: String = "",
    val status: String = "PENDING", // "PENDING", "RESOLVED_REFUNDED", "RESOLVED_PAID", "RESOLVED_SPLIT"
    val resolutionDetails: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportedUserEmail: String,
    val reporterEmail: String,
    val reason: String,
    val description: String,
    val screenshotUri: String,
    val status: String = "PENDING", // "PENDING", "RESOLVED"
    val actionTaken: String = "", // "WARNING", "SUSPENDED", "PERMANENT_BAN"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "nid_verifications")
data class KYCEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val username: String,
    val nidNumber: String,
    val nidFrontUri: String,
    val nidBackUri: String,
    val selfieUri: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminEmail: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
