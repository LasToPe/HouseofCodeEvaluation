const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNotification = functions.region('europe-west2').firestore
    .document("/chat-rooms/{roomName}/messages/{id}")
    .onCreate((snap, context) => {
        var room = context.params.roomName;
        console.log(room);
        var payload = {
            notification: {
                title: "New message",
                body: `New message in ${room}`
            }
        };
        admin.messaging().sendToTopic(room.replace(" ", "_").replace(".", ""), payload);
        return null;
    });