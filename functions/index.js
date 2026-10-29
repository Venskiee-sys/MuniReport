'use strict';

/**
 * Optional trusted notification fan-out.
 *
 * The Android app creates report updates, comments, and verification reviews.
 * These functions mirror those events into each resident's in-app notification
 * feed. They use the Admin SDK, so Firestore client rules stay closed for
 * notification creation.
 *
 * Deploy with: firebase deploy --only functions
 */

const { initializeApp } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { onDocumentCreated, onDocumentUpdated } = require('firebase-functions/v2/firestore');
const logger = require('firebase-functions/logger');

initializeApp();
const db = getFirestore();

const STATUS_LABELS = {
  PENDING: 'Pending',
  ACKNOWLEDGED: 'Acknowledged',
  IN_PROGRESS: 'In progress',
  RESOLVED: 'Resolved',
  REJECTED: 'Closed',
};

async function followerIds(complaintId) {
  const snapshot = await db
    .collection('follows')
    .where('complaintId', '==', complaintId)
    .get();
  return snapshot.docs
    .map((doc) => doc.get('userId'))
    .filter((userId) => typeof userId === 'string' && userId.length > 0);
}

function notificationReference(userId, id) {
  return db.collection('users').doc(userId).collection('notifications').doc(id);
}

async function commitNotifications(notifications) {
  for (let index = 0; index < notifications.length; index += 450) {
    const batch = db.batch();
    notifications.slice(index, index + 450).forEach(({ reference, data }) => {
      batch.set(reference, data);
    });
    await batch.commit();
  }
}

exports.notifyOnOfficialUpdate = onDocumentCreated(
  'complaints/{complaintId}/updates/{updateId}',
  async (event) => {
    const update = event.data?.data();
    if (!update || update.isOfficial !== true) return;

    const complaintId = event.params.complaintId;
    const updateId = event.params.updateId;
    const complaintSnapshot = await db.collection('complaints').doc(complaintId).get();
    if (!complaintSnapshot.exists) {
      logger.warn('Complaint disappeared before notification', { complaintId });
      return;
    }

    const complaint = complaintSnapshot.data();
    const recipients = new Set([complaint.authorId, ...(await followerIds(complaintId))]);
    const statusLabel = STATUS_LABELS[update.status] || 'Status updated';
    const note = typeof update.note === 'string' ? update.note : '';
    const notifications = [...recipients]
      .filter((userId) => typeof userId === 'string' && userId.length > 0)
      .map((userId) => ({
        reference: notificationReference(userId, `update_${updateId}`),
        data: {
          recipientId: userId,
          title: `Report ${complaint.ticketNumber || ''}: ${statusLabel}`.trim(),
          message: note.slice(0, 300) || 'The barangay office updated this report.',
          type: 'STATUS_CHANGE',
          complaintId,
          isRead: false,
          createdAt: FieldValue.serverTimestamp(),
        },
      }));

    await commitNotifications(notifications);
  },
);

exports.notifyOnComment = onDocumentCreated(
  'complaints/{complaintId}/comments/{commentId}',
  async (event) => {
    const comment = event.data?.data();
    if (!comment) return;

    const complaintId = event.params.complaintId;
    const commentId = event.params.commentId;
    const complaintSnapshot = await db.collection('complaints').doc(complaintId).get();
    if (!complaintSnapshot.exists) return;

    const complaint = complaintSnapshot.data();
    const recipients = new Set([complaint.authorId, ...(await followerIds(complaintId))]);
    const title = comment.authorRole === 'ADMIN'
      ? 'Barangay office replied'
      : `${comment.authorName || 'A resident'} commented`;
    const message = typeof comment.text === 'string'
      ? comment.text.slice(0, 180)
      : 'A new comment was added to this report.';
    const notifications = [...recipients]
      .filter((userId) => typeof userId === 'string' && userId.length > 0)
      .filter((userId) => userId !== comment.authorId)
      .map((userId) => ({
        reference: notificationReference(userId, `comment_${commentId}`),
        data: {
          recipientId: userId,
          title,
          message,
          type: 'COMMENT',
          complaintId,
          isRead: false,
          createdAt: FieldValue.serverTimestamp(),
        },
      }));

    await commitNotifications(notifications);
  },
);

exports.notifyOnVerificationReview = onDocumentUpdated(
  'verifications/{verificationId}',
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status) return;
    if (after.status !== 'APPROVED' && after.status !== 'REJECTED') return;

    const approved = after.status === 'APPROVED';
    const reviewNote = typeof after.reviewNote === 'string' ? after.reviewNote.trim() : '';
    await notificationReference(after.userId, `verification_${event.id}`).set({
      recipientId: after.userId,
      title: approved ? 'Residency verified' : 'Residency verification needs attention',
      message: reviewNote || (approved
        ? 'You can now file concerns and join discussions in MuniReport.'
        : 'Open MuniReport to review the note and submit a clearer document.'),
      type: 'VERIFICATION',
      isRead: false,
      createdAt: FieldValue.serverTimestamp(),
    });
  },
);
