const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNotification = functions.region('europe-west2').firestore
    .document("/chat-rooms/{roomName}/messages/{id}")
    .onCreate((snap, context) => {
        var room = context.params.roomName;
        var topic = room.replace(/ /g, "_").replace(/\./, "");
        console.log(room);
        console.log(topic);

        const payload = {
            notification: {
                title: "New message",
                body: `New message in ${room}`
            }
        };
        const options = {
            priority: "high",
            timeToLive: 60*60*2
        };

        return admin.messaging().sendToTopic(topic, payload, options);
    });