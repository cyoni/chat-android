
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

function sendM(recipient, sender, message, type){
    

    return admin.database().ref("users").child(recipient).once('value').then(snapshot2 => {
        const userToken = snapshot2.child("token").val()
        if (userToken !== null){
            console.log("userToken: " + userToken )
            admin.database().ref("messages").child(userToken).push({
                sender: sender,
                message: message,
                type: type,
                timestamp: Date.now()
            })
      }
        return null
    })

}

async function sendMessageToAudience(room, sender, message, type) {
   
    var nicknameArray = []
   await admin.database().ref("rooms").child(room).once('value').then(res => {
        res.forEach(snapshot => {
            const nickname = snapshot.key
            console.log("nickname: " + nickname)
            nicknameArray.push(nickname)

        })
        return null
    })

    for (var i=0; i<nicknameArray.length; i++){
        const current_nickname = nicknameArray[i]
        sendM(current_nickname, sender, message, type)

    }
    
}


exports.register = functions.https.onCall(async (request, context) => {

    const nickname = request.nickname.trim();
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
        await sendMessageToAudience(defaultRoom, nickname, `** ${nickname} joined the conversation **`, "join")

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

    const reference = admin.database().ref("users").child(nickname)
    const account = await reference.once('value')

    if (!account.child('token').val() === token) {
        return "AUTH-FAILED"
    }

    const getRoomName = account.child('room').val()
    console.log("room: " + getRoomName)
    await sendMessageToAudience(getRoomName, nickname, message, "regular")
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

                sendMessageToAudience(current_room, current_user, `** ${current_user} left the conversation **`, "leave")
            }
        })
        return null
    })
});

function removeMe(nickname, token, room){

    admin.database().ref("users").child(nickname).remove()
    admin.database().ref("messages").child(token).remove()
    admin.database().ref("rooms").child(room).child(nickname).remove()

    sendMessageToAudience(room, nickname, `** ${nickname} has left the conversation **`, "leave")

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