# Security Specification — EduBank MCQ Firestore

## 1. Data Invariants
1. **Default Deny**: All paths not explicitly matched are rejected (`allow read, write: if false;`).
2. **Authentication**: No unauthenticated reads or writes are permitted on any collection (`request.auth != null`).
3. **Path ID Hardening**: Document IDs (`userId`, `adminId`) must match `^[a-zA-Z0-9_\-]+$` with length `1..128`.
4. **PII Isolation**: User profile documents (`/users/{userId}`) contain `email` and may only be read (`get`/`list`) by the document owner (`request.auth.uid == userId` / `resource.data.uid == request.auth.uid`) or a verified administrator (`isAdmin()`).
5. **Anti-Privilege Escalation**:
   - Regular users creating their own `/users/{userId}` document must set `uid == request.auth.uid`, `role == 'MEMBER'` (or `'member'`), and `status == 'ACTIVE'` (or `'active'`).
   - Regular users updating their own `/users/{userId}` document may only modify `['displayName', 'updatedAt']`; `role`, `status`, `uid`, `email`, and `createdAt` must remain unchanged.
   - Only verified admins (`exists(/databases/$(database)/documents/admins/$(request.auth.uid))` or verified owner email `mzmdagosh@gmail.com`) may update `role` or `status`.
6. **Temporal Integrity**: `createdAt` and `updatedAt` must be valid Firestore `timestamp` objects `<= request.time`, and `createdAt` is immutable on update.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read**: `unauthDb.collection("users").doc("alice").get()` -> REJECT
2. **Cross-User PII Read**: `aliceDb.collection("users").doc("bob").get()` -> REJECT
3. **Unfiltered Collection List**: `aliceDb.collection("users").get()` -> REJECT
4. **Identity Spoofing on Create**: `aliceDb.collection("users").doc("alice").set({ uid: "bob", ... })` -> REJECT
5. **Self-Assigned Admin Role on Create**: `aliceDb.collection("users").doc("alice").set({ uid: "alice", role: "ADMIN", ... })` -> REJECT
6. **Privilege Escalation on Update**: `aliceDb.collection("users").doc("alice").update({ role: "ADMIN" })` -> REJECT
7. **Shadow Field Injection on Create**: `aliceDb.collection("users").doc("alice").set({ ..., isSuperUser: true })` -> REJECT
8. **Shadow Field Injection on Update**: `aliceDb.collection("users").doc("alice").update({ displayName: "Alice", isVerified: true })` -> REJECT
9. **Value Poisoning on Update**: `aliceDb.collection("users").doc("alice").update({ displayName: 12345 })` -> REJECT
10. **String Boundary Overflow**: `aliceDb.collection("users").doc("alice").update({ displayName: "A".repeat(500) })` -> REJECT
11. **Immortal Field Tampering (`createdAt`)**: `aliceDb.collection("users").doc("alice").update({ displayName: "Alice", createdAt: newTimestamp })` -> REJECT
12. **Unverified Admin Email Spoof**: User with `email: "mzmdagosh@gmail.com", email_verified: false` reading another user's profile -> REJECT
