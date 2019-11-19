const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNotification = functions.firestore
    .document("chat-rooms/{room-name}/messages/{id}")
    .onCreate((snap, context) => {
        var payload = {
            notification: {
                title: "New message!",
                body: "There's a new message waiting for you."
            }
        };
    });