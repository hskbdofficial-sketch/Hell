# HELLSEC STORE – ULTIMATE PRODUCTION BLUEPRINT & DEPLOYMENT GUIDE

This document provides the complete Production Database Schema, Storage Security Configurations, API Layer Architecture, and Step-by-Step Deployment instructions to connect this Android application to a live Firebase & Supabase backend.

---

## 1. SUPABASE POSTGRESQL DATABASE SCHEMA

Run this consolidated SQL script in your Supabase SQL Editor to establish all tables, primary keys, indexes, foreign keys, and Row Level Security (RLS) constraints matching the Android app architecture.

```sql
-- ==========================================
-- 1. USERS PROFILE TABLE
-- ==========================================
CREATE TABLE users (
    email VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('User', 'Freelancer', 'Admin')),
    skull_balance INT DEFAULT 10 CHECK (skull_balance >= 0),
    kyc_status VARCHAR(50) DEFAULT 'NOT_STARTED' CHECK (kyc_status IN ('NOT_STARTED', 'PENDING', 'APPROVED', 'REJECTED')),
    is_banned BOOLEAN DEFAULT FALSE,
    profile_pic_uri TEXT DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. DIGITAL PRODUCTS TABLE
-- ==========================================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    price INT NOT NULL CHECK (price >= 0),
    tags TEXT NOT NULL, -- Comma separated tags
    thumbnail_uri TEXT NOT NULL,
    product_file_uri TEXT NOT NULL,
    version VARCHAR(50) NOT NULL,
    seller_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    seller_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING_REVIEW' CHECK (status IN ('PENDING_REVIEW', 'APPROVED', 'REJECTED')),
    avg_rating DECIMAL(3,2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. PRODUCT REVIEWS TABLE
-- ==========================================
CREATE TABLE product_reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    reviewer_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    reviewer_name VARCHAR(100) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_product_reviewer UNIQUE(product_id, reviewer_email)
);

-- ==========================================
-- 4. PURCHASES RECEIPT INDEX
-- ==========================================
CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    product_title VARCHAR(255) NOT NULL,
    buyer_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    price_paid INT NOT NULL,
    receipt_id VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. ESCROW CONTRACTS (JOBS) TABLE
-- ==========================================
CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    budget INT NOT NULL CHECK (budget >= 0),
    deadline VARCHAR(100) NOT NULL,
    skills TEXT NOT NULL,
    client_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    client_name VARCHAR(100) NOT NULL,
    freelancer_email VARCHAR(255) REFERENCES users(email) ON DELETE SET NULL,
    freelancer_name VARCHAR(100) DEFAULT '',
    posting_fee INT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'OPEN', 'IN_PROGRESS', 'SUBMITTED', 'COMPLETED', 'CANCELLED', 'DISPUTED')),
    work_submission_text TEXT DEFAULT '',
    work_submission_file_uri TEXT DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 6. JOB PROPOSALS (BIDS) TABLE
-- ==========================================
CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT REFERENCES jobs(id) ON DELETE CASCADE,
    job_title VARCHAR(255) NOT NULL,
    freelancer_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    freelancer_name VARCHAR(100) NOT NULL,
    bid_amount INT NOT NULL CHECK (bid_amount >= 0),
    delivery_time VARCHAR(100) NOT NULL,
    portfolio TEXT DEFAULT '',
    cover_letter TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 7. WALLET TOPUP BKASH REQUESTS
-- ==========================================
CREATE TABLE wallet_topups (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    sender_number VARCHAR(20) NOT NULL,
    amount INT NOT NULL CHECK (amount > 0),
    screenshot_uri TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 8. WALLET WITHDRAWALS REQUESTS
-- ==========================================
CREATE TABLE withdrawals (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    receiver_number VARCHAR(20) NOT NULL,
    amount INT NOT NULL CHECK (amount >= 50),
    fee INT DEFAULT 5,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 9. CONTRACT LITIGATION (DISPUTES) TABLE
-- ==========================================
CREATE TABLE disputes (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT REFERENCES jobs(id) ON DELETE CASCADE,
    job_title VARCHAR(255) NOT NULL,
    client_email VARCHAR(255) REFERENCES users(email),
    freelancer_email VARCHAR(255) REFERENCES users(email),
    dispute_creator_email VARCHAR(255) REFERENCES users(email),
    reason VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    client_evidence_text TEXT DEFAULT '',
    freelancer_evidence_text TEXT DEFAULT '',
    client_evidence_file TEXT DEFAULT '',
    freelancer_evidence_file TEXT DEFAULT '',
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED_REFUNDED', 'RESOLVED_PAID', 'RESOLVED_SPLIT')),
    resolution_details TEXT DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 10. ABUSE REPORTS TABLE
-- ==========================================
CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    reported_user_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    reporter_email VARCHAR(255) REFERENCES users(email) ON DELETE CASCADE,
    reason VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    screenshot_uri TEXT DEFAULT '',
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED')),
    action_taken VARCHAR(100) DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 11. KYC DOCUMENT SCAN REGISTRY
-- ==========================================
CREATE TABLE nid_verifications (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) UNIQUE REFERENCES users(email) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    nid_number VARCHAR(100) NOT NULL,
    nid_front_uri TEXT NOT NULL,
    nid_back_uri TEXT NOT NULL,
    selfie_uri TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 12. ADMIN AUDIT LOGGER
-- ==========================================
CREATE TABLE admin_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_email VARCHAR(255) REFERENCES users(email),
    action VARCHAR(100) NOT NULL,
    details TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 13. INDEX OPTIMIZATIONS
-- ==========================================
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_wallet_topups_user ON wallet_topups(user_email);
CREATE INDEX idx_withdrawals_user ON withdrawals(user_email);
CREATE INDEX idx_disputes_status ON disputes(status);
```

---

## 2. ROW LEVEL SECURITY (RLS) & POLICIES

To secure your Supabase tables against unauthorized manipulations, enable RLS and deploy targeted security policies.

### Enabling RLS on All Tables:
```sql
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE jobs ENABLE ROW LEVEL SECURITY;
```

### Profile Visibility Policy:
Standard profiles are public to authenticated marketplace entities; updates are restricted strictly to owners.
```sql
CREATE POLICY "Enable read for all authenticated operators" 
ON users FOR SELECT 
TO authenticated 
USING (true);

CREATE POLICY "Enable update for profile owners" 
ON users FOR UPDATE 
TO authenticated 
USING (auth.jwt() ->> 'email' = email);
```

---

## 3. SUPABASE STORAGE BUCKET CONFIGURATION

Create three distinct secure storage buckets within your Supabase console:
1. `public-assets` — Holds standard public product images and display avatars. (Public Access: True)
2. `secure-contracts` — Encrypted deliverables repository. (Public Access: False)
3. `compliance-vault` — Highly isolated container holding NID scans and Selfie files. (Public Access: False)

### RLS Policies for `compliance-vault`:
```sql
-- Restrict read/write of KYC documents solely to owner and System Admins
CREATE POLICY "Strict read for profile owners and admins" 
ON storage.objects FOR SELECT 
TO authenticated 
USING (
    bucket_id = 'compliance-vault' 
    AND (
        auth.jwt() ->> 'email' = (storage.foldername(name))[1] 
        OR auth.jwt() ->> 'email' = 'nasifhimadri@gmail.com'
    )
);
```

---

## 4. ANDROID ENVIRONMENT VARIABLE SETTINGS

To swap the offline Room Database backend for your live Supabase database API, configure these parameters securely inside your AI Studio Secrets panel or project environment:

### `.env` Structure:
```env
# Backend API Integration Endpoints
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_KEY=your-anon-publishable-service-key-jwt
FIREBASE_API_KEY=AIzaSyB0NGR5r8...
```

---

## 5. RECOMPILING & TESTING SUITE

To compile and execute the complete test verification suite locally:

```bash
# Verify clean compilation
gradle compileDebugKotlin

# Run all local JVM Robolectric unit tests
gradle :app:testDebugUnitTest

# Run visual layout screenshot comparisons
gradle :app:verifyRoborazziDebug
```
