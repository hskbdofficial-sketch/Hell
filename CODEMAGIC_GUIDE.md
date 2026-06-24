# CODEMAGIC.IO CI/CD INTEGRATION & DEPLOYMENT GUIDE

This guide provides the complete research, configuration, and step-by-step setup to connect **HELLSEC STORE** to Codemagic’s continuous integration and delivery (CI/CD) platform.

---

## 1. Overview of Codemagic for Android
Codemagic is a dedicated cloud-based CI/CD tool designed specifically for mobile applications. It allows developers to automate building, testing, signing, and deploying Android applications to Google Play Store, Firebase App Distribution, and custom endpoints.

For this project, the pipeline is fully defined in the declarative configuration file `codemagic.yaml`.

---

## 2. Walkthrough of `codemagic.yaml`
The build pipeline in this project is configured to perform automated verification, testing, and compilation. Here is how the workflow is structured:

### Build Environment
- **Workflow ID**: `android-build`
- **Instance Type**: `mac_mini_m1` (supports fast M1 compilation)
- **Java Platform**: OpenJDK 17 (set up dynamically using custom search scripts)

### Automated Pipeline Steps
1. **Set up JDK**: Configures paths to use JDK 17 as required by modern Android Gradle Plugin (AGP) and Kotlin.
2. **Generate `.env` Configuration**: Dynamically extracts sensitive API keys from Codemagic environment variables and populates `.env` at build time so the Secrets Gradle Plugin can securely inject them into the code without hardcoding secrets in git.
3. **Configure Keystore**: Decodes release signing keys if provided (using Base64 encoding).
4. **Repair and Regenerate Gradle Wrapper**: Cleans out corrupt Gradle wrapper files and dynamically recreates a pristine Gradle 9.3.1 wrapper.
5. **Run Unit Tests**: Executes local Unit Tests to ensure no regression is introduced before building.
6. **Build Debug APK**: Compiles the debug version of the application.
7. **Build Release APK / App Bundle**: Builds the release artifact (`.apk` and `.aab`) if signing keys are supplied.

### Artifacts & Outputs
The workflow captures and archives:
- All built APKs (`app/build/outputs/apk/**/*.apk`)
- All built Android App Bundles (`app/build/outputs/bundle/**/*.aab`)

---

## 3. Configuration in Codemagic UI (Step-by-Step)

To integrate this repository with Codemagic:

1. **Connect Repository**:
   - Log in to [Codemagic.io](https://codemagic.io).
   - Go to **Applications** -> **Add Application**.
   - Connect your GitHub/GitLab repository hosting the **HELLSEC STORE** codebase.

2. **Select Configuration Source**:
   - When prompted, choose **codemagic.yaml** as your build configuration source.

3. **Configure Environment Variables (CRITICAL)**:
   - Go to the **Environment Variables** tab of your application in Codemagic.
   - Create a variable group named `hellsec_store_credentials` (this matches the group specified in `codemagic.yaml`).
   - Add the following variables within this group:

| Variable Name | Description | Example / Recommended Value |
|---|---|---|
| `GEMINI_API_KEY` | Key used for secure Gemini AI assistant features. | *Your Gemini API Key* |
| `FIREBASE_API_KEY` | Firebase API Key. | *Your Firebase Web API Key* |
| `SUPABASE_URL` | Live Supabase REST and Auth Endpoint. | `https://your-project.supabase.co` |
| `SUPABASE_ANON_KEY` | Supabase Anon JWT key. | *Your Supabase Anon JWT* |
| `STORE_PASSWORD` | Password for your release keystore. | *Your Keystore Password* |
| `KEY_PASSWORD` | Password for the specific key alias. | *Your Key Password* |
| `KEY_ALIAS` | Alias name in the keystore. | `upload` (default) |
| `KEYSTORE_BASE64` | Base64-encoded binary keystore file (`.jks`). | *See section below on how to generate this* |

### How to generate `KEYSTORE_BASE64`:
Run this local terminal command on your release `.jks` file, and paste the output string directly into Codemagic as `KEYSTORE_BASE64`:
```bash
base64 -i my-upload-key.jks -o -
# On macOS/Linux, or use certutil on Windows
```

---

## 4. Triggering Automated Builds
Under the **Triggering** section in Codemagic UI:
- Enable **Trigger on push** to automatically rebuild every time code is pushed.
- Enable **Trigger on pull request** to verify code compiles and passes tests before merging.

---

## 5. Performance Optimizations & Caching
To speed up builds, you can optionally add caching to `codemagic.yaml` under the workflow definition:
```yaml
    cacheing:
      decrypted_files_cache: false
      cache_paths:
        - ~/.gradle/caches
        - ~/.gradle/wrapper
```
This is pre-configured and commented out to keep configuration clean, but can be enabled to save up to 2-3 minutes per build by reusing cached Gradle dependencies.
