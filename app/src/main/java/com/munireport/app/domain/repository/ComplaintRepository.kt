package com.munireport.app.domain.repository

import com.munireport.app.domain.model.Comment
import com.munireport.app.domain.model.Complaint
import com.munireport.app.domain.model.ComplaintPriority
import com.munireport.app.domain.model.ComplaintStatus
import com.munireport.app.domain.model.ComplaintUpdate
import com.munireport.app.domain.model.NewComplaint
import com.munireport.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    fun observeComplaints(viewerId: String): Flow<List<Complaint>>
    fun observeComplaint(complaintId: String, viewerId: String): Flow<Complaint?>
    fun observeUpdates(complaintId: String): Flow<List<ComplaintUpdate>>
    fun observeComments(complaintId: String): Flow<List<Comment>>
    suspend fun createComplaint(draft: NewComplaint, author: UserProfile): Complaint
    suspend fun setFollowing(complaintId: String, userId: String, following: Boolean)
    suspend fun addComment(complaintId: String, author: UserProfile, text: String)
    suspend fun updateStatus(
        complaintId: String,
        status: ComplaintStatus,
        priority: ComplaintPriority,
        note: String,
        author: UserProfile,
    )
}
