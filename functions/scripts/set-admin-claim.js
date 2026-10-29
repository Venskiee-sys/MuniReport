'use strict';

/**
 * Provision an existing resident account as a MuniReport administrator.
 *
 * This script uses the Admin SDK with Application Default Credentials. It first
 * writes users/{uid}.role, then sets the trusted custom claim read by both the
 * Android app and Firestore rules. If the claim step fails, the app remains
 * resident because both requirements must match.
 *
 * Usage:
 *   node functions/scripts/set-admin-claim.js <firebase-uid>
 */

const admin = require('firebase-admin');

const uid = process.argv[2];
if (!uid) {
  console.error('Usage: node functions/scripts/set-admin-claim.js <firebase-uid>');
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
    throw new Error(
      `No users/${uid} profile exists. Sign up or create the profile before granting administrator access.`,
    );
  }

  if (authUser.customClaims?.role === 'ADMIN') {
    console.log(`${authUser.email || uid} already has the ADMIN claim.`);
  }

  await profileRef.update({
    role: 'ADMIN',
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  await auth.setCustomUserClaims(uid, {
    ...(authUser.customClaims || {}),
    role: 'ADMIN',
  });

  // Force a fresh token so a client that is already signed in cannot keep
  // operating with a stale resident token.
  await auth.revokeRefreshTokens(uid);

  console.log(`Administrator access granted to ${authUser.email || uid}.`);
  console.log('The user must sign in again before the ADMIN claim takes effect.');
}

main().catch((error) => {
  console.error(error.message || error);
  process.exit(1);
});
