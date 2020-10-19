
// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

function makeToken(length) {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function sendMessageToAudience(nickname, message, type){
    return admin.database().ref("users").once('value').then(snapshot => {
        snapshot.forEach(childSnapshot => {
            const userToken = childSnapshot.child("token").val()
            admin.database().ref("messages").child(userToken).push({
                sender: nickname,
                message: message,
                type: type,
                timestamp: Date.now()
            })
        })
        return null
    })
}


exports.register = functions.https.onCall(async (request, context) => {

    const nickname = request.nickname;
    const reference = await admin.database().ref("users").child(nickname);

    const isUserAlive = await reference.once('value')

        if (!isUserAlive.exists()) {

            const token = makeToken(20);

            const query = admin.database().ref("users").child(nickname).set({
                nickname: nickname,
                token: token,
                timestamp: Date.now()
            })
            await query
            await sendMessageToAudience("", `** ${nickname} joined the conversation **`, "announcement")

            return token
        }
        else {
            return "busy"
        }
})



exports.sendMessage = functions.https.onCall(async (request, context) => {

    const nickname = request.nickname
    const token = request.token
    const message = request.message

    const reference = admin.database().ref("users").child(nickname).child("token")

    const auth = await reference.once('value').then(snapshot => {
        return snapshot.val() === token
    })

    if (!auth) {
        return "AUTH-FAILED"
    }

    await sendMessageToAudience(nickname, message, "regular")

    return "OK"    
})


exports.manageUsers = functions.pubsub.schedule('every 2 minutes').onRun(async (context) => {
    
    const now = Date.now()

    return admin.database().ref("users").once('value').then(snapshot => {
        snapshot.forEach(childSnapshot => {
            const current_user = childSnapshot.key
            const lastSeen = childSnapshot.child("timestamp").val()
            const current_token_user = childSnapshot.child("token").val()
            if (now - lastSeen > 60000){
                admin.database().ref("users").child(current_user).remove()
                admin.database().ref("messages").child(current_token_user).remove()
                sendMessageToAudience("", `** ${current_user} left the conversation **`, "announcement")
            }
        })
        return null
    })
  });