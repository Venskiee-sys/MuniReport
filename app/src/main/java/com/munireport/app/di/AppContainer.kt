package com.munireport.app.di

import android.content.Context
import com.munireport.app.data.local.DemoMuniStore
import com.munireport.app.data.remote.FirebaseStorageDataSource
import com.munireport.app.data.repository.DemoAuthRepository
import com.munireport.app.data.repository.DemoComplaintRepository
import com.munireport.app.data.repository.DemoNotificationRepository
import com.munireport.app.data.repository.DemoVerificationRepository
import com.munireport.app.data.repository.FirebaseAuthRepository
import com.munireport.app.data.repository.FirebaseComplaintRepository
import com.munireport.app.data.repository.FirebaseNotificationRepository
import com.munireport.app.data.repository.FirebaseVerificationRepository
import com.munireport.app.domain.repository.AuthRepository
import com.munireport.app.domain.repository.ComplaintRepository
import com.munireport.app.domain.repository.NotificationRepository
import com.munireport.app.domain.repository.VerificationRepository
import com.munireport.app.domain.usecase.CreateComplaintUseCase
import com.munireport.app.domain.usecase.ReviewVerificationUseCase
import com.munireport.app.domain.usecase.SignInUseCase
import com.munireport.app.gettingin.session.AuthSessionManager

class AppContainer(context: Context) {
    private val firebase = FirebaseModule(context.applicationContext)

    val isDemoMode: Boolean = !firebase.isConfigured

    private val demoStore: DemoMuniStore? = if (isDemoMode) DemoMuniStore() else null

    val authRepository: AuthRepository = if (isDemoMode) {
        DemoAuthRepository(requireNotNull(demoStore))
    } else {
        FirebaseAuthRepository(firebase.auth(), firebase.firestore())
    }

    val complaintRepository: ComplaintRepository = if (isDemoMode) {
        DemoComplaintRepository(requireNotNull(demoStore))
    } else {
        FirebaseComplaintRepository(
            firestore = firebase.firestore(),
            storage = FirebaseStorageDataSource(firebase.storage()),
        )
    }

    val verificationRepository: VerificationRepository = if (isDemoMode) {
        DemoVerificationRepository(requireNotNull(demoStore))
    } else {
        FirebaseVerificationRepository(
            firestore = firebase.firestore(),
            storage = FirebaseStorageDataSource(firebase.storage()),
        )
    }

    val notificationRepository: NotificationRepository = if (isDemoMode) {
        DemoNotificationRepository(requireNotNull(demoStore))
    } else {
        FirebaseNotificationRepository(firebase.firestore())
    }

    val sessionManager = AuthSessionManager(authRepository)

    val signInUseCase = SignInUseCase(authRepository)
    val createComplaintUseCase = CreateComplaintUseCase(complaintRepository)
    val reviewVerificationUseCase = ReviewVerificationUseCase(verificationRepository)
}
