
// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');
admin.initializeApp();


exports.register = functions.https.onCall(async (request, context) => {

    const nickname = request.nickname;
    const reference = admin.database().ref("users").child(nickname);

    const xx = await reference.once('value').then(ans => {
        if (!ans.exists()) {
            const privateKey = reference.push().key

            reference.set({
                nickname: nickname,
                privateKey: privateKey,
                timestamp: Date.now()
            })
            return privateKey
        }
        return "busy"
    })

    return xx
})

