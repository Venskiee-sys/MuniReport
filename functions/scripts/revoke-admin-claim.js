'use strict';

/**
 * Remove administrator access from an existing account.
 *
 * Usage:
 *   node functions/scripts/revoke-admin-claim.js <firebase-uid>
 */

const admin = require('firebase-admin');

const uid = process.argv[2];
if (!uid) {
  console.error('Usage: node functions/scripts/revoke-admin-claim.js <firebase-uid>');
  process.exit(1);
}

admin.initializeApp({
  projectId: process.env.GCLOUD_PROJECT
    || process.env.FIREBASE_PROJECT_ID
    || undefined,
});

async function main() {
  const auth = admin.auth();
  const db = admin.firestore();

  const authUser = await auth.getUser(uid);
  const profileRef = db.collection('users').doc(uid);
  const profileSnapshot = await profileRef.get();
  if (!profileSnapshot.exists) {
    throw new Error(`No users/${uid} profile exists.`);
  }

  const claims = { ...(authUser.customClaims || {}) };
  delete claims.role;

  await auth.setCustomUserClaims(uid, claims);
  await profileRef.update({
    role: 'RESIDENT',
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  await auth.revokeRefreshTokens(uid);

  console.log(`Administrator access removed from ${authUser.email || uid}.`);
  console.log('The user must sign in again before the resident role takes effect.');
}

main().catch((error) => {
  console.error(error.message || error);
  process.exit(1);
});
