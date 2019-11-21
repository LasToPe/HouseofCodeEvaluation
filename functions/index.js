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
    .object().onFinalize((object) => {

        var room = object.name.match(/.*?(?=\/)/g)[0];
        var message = object.name.match(/\/.*/g)[0].replace("/", "");

        var url = mediaLinkToDownloadableUrl(object);
        console.log(url);

        return admin.firestore().collection('chat-rooms').doc(room)
            .collection('messages').doc(message).update({imageUri: url});
    });

function mediaLinkToDownloadableUrl(object) {
    var firstPartUrl = object.mediaLink.split("?")[0] // 'https://www.googleapis.com/download/storage/v1/b/house-of-code-evaluation.appspot.com/o/...'
    var secondPartUrl = object.mediaLink.split("?")[1] // 'generation=...&alt=media'

    firstPartUrl = firstPartUrl.replace("https://www.googleapis.com/download/storage", "https://firebasestorage.googleapis.com")
    firstPartUrl = firstPartUrl.replace("v1", "v0")

    firstPartUrl += "?" + secondPartUrl.split("&")[1]; // 'alt=media'
    firstPartUrl += "&token=" + object.metadata.firebaseStorageDownloadTokens

    return firstPartUrl
}