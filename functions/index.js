
// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');
admin.initializeApp();
const defaultRoom = "lobby";

function makeToken(length) {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

async function getToken(nickname) {
    return admin.database().ref("users").child(nickname).once('value').then(res => {
        return res.child("token")
    })
}

function sendMsgToUser(recipient, sender, message, type, tracker){
    
    return admin.database().ref("users").child(recipient).once('value').then(snapshot2 => {
        const userToken = snapshot2.child("token").val()
        if (userToken !== null){
            console.log("userToken: " + userToken )
            admin.database().ref("messages").child(userToken).push({
                sender: sender,
                message: message,
                type: type,
                tracker: tracker,
                timestamp: Date.now()
            })
      }
        return null
    })

}

async function sendMessageToAudience(room, sender, message, type, tracker) {
   
    var nicknameArray = []
   await admin.database().ref("rooms").child(room).once('value').then(res => {
        res.forEach(snapshot => {
            const nickname = snapshot.key
            nicknameArray.push(nickname)
        })
        return null
    })

    for (var i=0; i<nicknameArray.length; i++){
        const current_nickname = nicknameArray[i]
        sendMsgToUser(current_nickname, sender, message, type, tracker)
    }
    
}

function validateNickname(nickname){
    return !(
            nickname === "" ||
            nickname.length > 20 ||
            nickname.includes("$") ||
            nickname.includes(".") || 
            nickname.includes("/") ||
            nickname.includes("[") || 
            nickname.includes("]") ||
            nickname.includes("\\")
            )
}

exports.register = functions.https.onCall(async (request, context) => {

    const nickname = request.nickname.trim();

    if (!validateNickname(nickname))
         return "INVALID-NICKNAME"

    const reference = await admin.database().ref("users").child(nickname);
    const isUserAlive = await reference.once('value')

    if (!isUserAlive.exists()) {

        const token = makeToken(20);
        const query = admin.database().ref("users").child(nickname).set({
            nickname: nickname,
            token: token,
            room: defaultRoom,
            timestamp: Date.now()
        })


        const joiningRoom = admin.database().ref("rooms").child(defaultRoom).child(nickname).set({
            timestamp: Date.now()
        })

        await joiningRoom
        await query
        await sendMessageToAudience(defaultRoom, nickname, `** ${nickname} has joined the room **`, "join", 0)

        return token
    }
    else {
        return "busy"
    }
})

function isMsgValid(message){
    if (message.trim() === "")
        return false
    return true
}


exports.sendMessage = functions.https.onCall(async (request, context) => {

    const nickname = request.nickname
    const token = request.token
    const message = request.message
    const tracker = request.MsgCounterACK

    const reference = admin.database().ref("users").child(nickname)
    const account = await reference.once('value')

    if (!isMsgValid(message)){
        return "MSG-NOT-VALID"
    }

    if (!account.child('token').val() === token) {
        return "AUTH-FAILED"
    }

    const getRoomName = account.child('room').val()
    console.log("room: " + getRoomName)
    await sendMessageToAudience(getRoomName, nickname, message, "regular", tracker)
    return "OK"
})


exports.manageUsers = functions.pubsub.schedule('every 2 minutes').onRun(async (context) => {

    const now = Date.now()

    return admin.database().ref("users").once('value').then(snapshot => {
        snapshot.forEach(childSnapshot => {

            const current_user = childSnapshot.key
            const lastSeen = childSnapshot.child("timestamp").val()
            const current_token_user = childSnapshot.child("token").val()
            const current_room = childSnapshot.child("room").val()

            if (now - lastSeen > 30000) {
                admin.database().ref("users").child(current_user).remove()
                admin.database().ref("messages").child(current_token_user).remove()
                admin.database().ref("rooms").child(current_room).child(current_user).remove()

                sendMessageToAudience(current_room, current_user, `** ${current_user} has left the room **`, "leave", 0)
            }
        })
        return null
    })
});

function removeMe(nickname, token, room){

    admin.database().ref("users").child(nickname).remove()
    admin.database().ref("messages").child(token).remove()
    admin.database().ref("rooms").child(room).child(nickname).remove()

    sendMessageToAudience(room, nickname, `** ${nickname} has left the room **`, "leave", 0)

}

exports.logOut = functions.https.onCall(async (request, context) => {

    const token = request.token
    const nickname = request.nickname

    const auth = await admin.database().ref("users").child(nickname).once('value')
    if (auth.child("token").val() !== token)
        return "AUTH-FAILED"

    const room = auth.child("room").val()
    return removeMe(nickname, token, room)

})