const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Region must be the same as the region for the firebase project, in this case europe-west2
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

exports.createMessageWithImage = functions.region('europe-west2').storage
    .object().onFinalize(async (object) => {

        var room = object.name.match(/.*?(?=\/)/g)[0];
        var message = object.name.match(/\/.*/g)[0].replace("/", "");
        return admin.firestore().collection('chat-rooms').doc(room)
            .collection('messages').doc(message).update({imageUri: object.mediaLink});
    });