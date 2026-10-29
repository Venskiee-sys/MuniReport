package com.munireport.app.domain.usecase

import com.munireport.app.core.common.AppException
import com.munireport.app.core.validation.Validators
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.NewComplaint
import com.munireport.app.domain.model.UserProfile
import com.munireport.app.domain.repository.ComplaintRepository

class CreateComplaintUseCase(private val complaintRepository: ComplaintRepository) {
    suspend operator fun invoke(draft: NewComplaint, author: UserProfile): Result<Complaint> = runCatching {
        if (!author.isVerified) {
            throw AppException.PermissionDenied()
        }
        if (Validators.complaintTitle(draft.title) != null) {
            throw AppException.InvalidInput("Add a clear title for the concern.")
        }
        if (Validators.complaintDescription(draft.description) != null) {
            throw AppException.InvalidInput("Add more detail so the office can act on it.")
        }
        if (Validators.address(draft.address) != null) {
            throw AppException.InvalidInput("Add the location of the concern.")
        }
        if (draft.photoUris.size > 3) {
            throw AppException.InvalidInput("Attach up to three photos.")
        }
        complaintRepository.createComplaint(draft, author)
    }
}
