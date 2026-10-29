# MuniReport

MuniReport is an Android app for barangay residents to report local concerns and
for authorized barangay staff to verify residents, manage reports, and review
operations. It is built with Kotlin, Jetpack Compose, Navigation Compose, MVVM,
Firebase Authentication, Cloud Firestore, and Firebase Storage.

The repository can run immediately in a fully local demo mode. Adding a Firebase
configuration switches the same screens and ViewModels to the production data
layer.

## Demo mode

If `app/google-services.json` is absent, the app uses an in-memory demo store. No
network or Firebase project is required.

| Role | Email | Password |
| --- | --- | --- |
| Resident | `resident@munireport.app` | `Resident123!` |
| Administrator | `admin@munireport.app` | `Admin123!` |

Demo data resets when the app process ends. Sign-up, report filing, comments,
status updates, residency decisions, account editing, analytics, and PDF export
all work against the in-memory store.

## Features

- Email/password sign-in, resident sign-up, and password recovery.
- Resident residency verification with a private government ID upload.
- Report filing with category, location, and up to three photos.
- Report tracking with an official timeline, comments, following, and in-app
  notifications.
- Resident profile, contact editing, and account settings.
- A separate administrator console with:
  - operations overview;
  - residency verification queue and decisions;
  - report search, filtering, priority review, and official updates;
  - analytics, charts, and a shareable PDF snapshot;
  - administrator account settings.
- Demo mode when Firebase is not configured.

## Project layout

```text
app/src/main/java/com/munireport/app/
├── MainActivity.kt
├── MuniReportApplication.kt
├── gettingin/                 # auth and residency onboarding
├── resident/                  # resident screens, ViewModels, navigation
├── admin/                     # staff console screens, ViewModels, navigation
├── core/                      # design system, navigation switch, utilities
├── data/                      # demo store, Firebase repositories, mappers
├── domain/                    # shared models, repository contracts, use cases
└── di/                        # Firebase and repository composition
```

`core/navigation/AppNavHost.kt` is the only top-level role switch. It composes
either `ResidentNavGraph` or `AdminNavGraph`, never both. The resident and admin
feature packages have independent navigation graphs and bottom navigation bars.

## Build

Requirements:

- Android Studio with JDK 17 or the Android Studio JBR
- Android SDK 37
- `minSdk` 24

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
```

The debug APK is written under `app/build/outputs/apk/debug/`. Open the project
in Android Studio for the normal emulator/device workflow.

## Firebase setup

Firebase is optional. To use real data:

1. Create a Firebase project and add an Android app with package name
   `com.munireport.app`.
2. Download the Firebase Android config file and place it at
   `app/google-services.json`. The repository ignores this file. A structural
   placeholder is available at `app/google-services.json.example`.
3. Enable **Email/Password** under Firebase Authentication.
4. Create a Cloud Firestore database and a Firebase Storage bucket.
5. Copy `.firebaserc.example` to `.firebaserc` and set your project ID.
6. Install the Firebase CLI, sign in, and deploy the trust rules:

   ```powershell
   firebase login
   firebase use --add
   firebase deploy --only firestore:rules,firestore:indexes,storage
   ```

7. Launch the app and create a normal resident account. Registration always
   writes `role = "RESIDENT"`; the sign-up form has no role selector.
8. Provision an administrator with the trusted Admin SDK script described
   below.
9. Optional: deploy the notification functions described below.

The Google Services Gradle plugin is applied only when
`app/google-services.json` exists, so a fresh checkout builds without secrets.
Without the file, the app intentionally selects demo mode.

## Administrator provisioning

Administrator status has two independent parts:

1. `users/{uid}.role == "ADMIN"` in Firestore.
2. A Firebase Auth custom claim `role == "ADMIN"`.

`AuthSessionManager` asks the repository to resolve the authorized role. The
repository returns `ADMIN` only when both values are present; otherwise it
returns `RESIDENT`. The app cannot write custom claims.

After installing the functions dependencies and configuring Application Default
Credentials (a service account JSON with Firebase Admin and Firestore access):

```powershell
npm --prefix functions install
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\path\to\service-account.json"
$env:FIREBASE_PROJECT_ID = "your-firebase-project-id"
node functions/scripts/set-admin-claim.js <firebase-uid>
```

The script updates the Firestore profile role, sets the custom claim, and revokes
refresh tokens. The user must sign in again. To remove access:

```powershell
node functions/scripts/revoke-admin-claim.js <firebase-uid>
```

Never commit a service account key. Do not provision an administrator by editing
the Firestore document alone; the custom claim is the trusted authorization
boundary.

## Security model

### App and navigation

- `AppNavHost` checks the session state and authorized role. Resident sessions
  cannot mount administrator destinations, and admin sessions do not mount
  resident-only screens.
- Resident and administrator navigation graphs are separate and each has its own
  bottom navigation configuration.
- The sign-up flow hardcodes `UserRole.RESIDENT` and the Firebase user document
  creation is also forced to `RESIDENT` with `NOT_SUBMITTED` verification.
- Role checks in UI code are convenience checks, not the security boundary.

### Firestore rules

`firestore.rules` enforces the same boundaries on the server:

- Administrator authority is read from `request.auth.token.role`, the custom
  claim that clients cannot modify.
- A user profile can only be created for the signed-in UID as a resident with
  `NOT_SUBMITTED` verification.
- Residents can edit only their contact fields. They may move their own
  verification status to `PENDING` when re-submitting, but cannot approve it.
- Only an administrator claim can change report status/priority or record a
  residency decision.
- Report authorship and resident-provided details are immutable after creation.
- Verified residents may file reports and participate in comments/follows.
- Notification documents cannot be created or deleted by clients. Clients may
  only read their own notifications and mark them read.
- Every unlisted path is denied.

### Storage rules

`storage.rules` keeps residency documents private:

- `complaints/{uid}/...` images are readable by signed-in residents and staff;
  only verified residents may upload their own files.
- `verifications/{uid}/...` files are readable only by that resident or an
  administrator claim; only that resident may upload.
- Uploads are limited to 10 MB and to image content (plus PDF for residency
  documents).
- Deletion through the client is denied.

### Operational notes

- Firestore rules can only enforce the custom claim after it has been issued.
  Sign out/in (or wait for token refresh) after changing an administrator role.
- Firebase Storage download URLs contain access tokens. Treat residency document
  URLs as sensitive, avoid logging them, and rotate keys/tokens if leaked. Keep
  the storage bucket private.
- The rules trust the Admin SDK/manual provisioning process. Restrict service
  account key access to trusted operators.
- Demo mode has no server-side enforcement and is not a substitute for Firebase
  rules in production.

## Optional notification functions

The app stores notifications under
`users/{uid}/notifications/{notificationId}`. In Firebase mode, the optional
Cloud Functions in `functions/index.js` create them using the Admin SDK:

- `notifyOnOfficialUpdate` notifies the report author and followers about
  official status changes.
- `notifyOnComment` notifies the author and followers about new comments, except
  the commenter.
- `notifyOnVerificationReview` notifies a resident when their application is
  approved or rejected.

Deploy with:

```powershell
npm --prefix functions install
firebase deploy --only functions
```

Cloud Functions require an appropriate Firebase billing plan. The functions only
create in-app notification records; the app does not currently register FCM push
tokens.

## Firestore data model

```text
users/{uid}
users/{uid}/notifications/{notificationId}

complaints/{complaintId}
complaints/{complaintId}/updates/{updateId}
complaints/{complaintId}/comments/{commentId}

verifications/{uid}

follows/{uid}_{complaintId}
```

`verifications` documents use the resident UID as the document ID, so a resident
can fetch only their own application while administrators can read the full
queue.

## Verification and troubleshooting

- If the app opens in demo mode, confirm `app/google-services.json` is present
  and rebuild.
- If an administrator still sees the resident UI, sign out and sign in again,
  then confirm both the Firestore profile role and the custom claim.
- If Firestore requests are denied, deploy the latest rules and check that the
  user's verification status is `APPROVED` before filing reports, commenting, or
  following.
- If Cloud Functions deployment fails, confirm the project is on the required
  billing plan and `firebase.json` points at the `functions` directory.
