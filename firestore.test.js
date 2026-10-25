const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function pastDate() {
  return new Date(Date.now() - 1000);
}

// --- DIRTY DOZEN SECURITY BOUNDS TESTS ---

test("1. Unauthenticated user: cannot read users collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("2. Cross-User PII Read: Alice cannot read Bob's user document", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      uid: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob Smith",
      role: "MEMBER",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("3. Unfiltered Collection List: Alice cannot list all users without uid filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").get());
});

test("4. Identity Spoofing on Create: Alice cannot create a user doc with Bob's uid", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: BOB_UID,
      email: "alice@example.com",
      displayName: "Alice",
      role: "MEMBER",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("5. Self-Assigned Admin Role on Create: Alice cannot self-assign ADMIN role on create", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      role: "ADMIN",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("6. Privilege Escalation on Update: Alice cannot escalate her own role to ADMIN", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      role: "MEMBER",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "ADMIN",
      updatedAt: pastDate(),
    })
  );
});

test("7. Shadow Field Injection on Create: rejects extra ghost field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      role: "MEMBER",
      status: "ACTIVE",
      isSuperAdmin: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("8. Unverified Admin Email Spoof: unverified owner email cannot read other users", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      uid: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob",
      role: "MEMBER",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    });
  });

  const spoofDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: "mzmdagosh@gmail.com",
      email_verified: false,
    })
    .firestore();
  await assertFails(spoofDb.collection("users").doc(BOB_UID).get());
});

// --- POSITIVE & CLIENT QUERY ALIGNMENT TESTS ---

test("9. Authenticated user: can create their own MEMBER profile and read their role", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      role: "MEMBER",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("10. Authenticated user: can read pre-provisioned ADMIN/MODERATOR/ADVISOR role on their own UID", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice Moderator",
      role: "MODERATOR",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const snap = await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  if (snap.data().role !== "MODERATOR") {
    throw new Error("Expected role MODERATOR");
  }
});

test("11. Verified Admin: can update another user's role in /users/{userId}", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("admins").doc(ADMIN_UID).set({
      uid: ADMIN_UID,
    });
    await context.firestore().collection("users").doc(BOB_UID).set({
      uid: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob",
      role: "MEMBER",
      status: "ACTIVE",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    });
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(
    adminDb.collection("users").doc(BOB_UID).update({
      role: "ADVISOR",
      updatedAt: pastDate(),
    })
  );
});
