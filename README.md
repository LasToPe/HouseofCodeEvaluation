# House of Code Evaluation

## Requirements
- Splash Screen
- Login Screen
- Chat rooms
- Send and recieve messages
- Push functionality
- Upload images to chat room

## Documentation
The House of Code Evaluation application is a chat room and messaging app built for android with Java using Android Studio.
I have no prior experience with Android programming specifically, so please excuse any minor errors.
The application uses Google Firebase for the data layer, which I also had no prior experience with.

### Splash screen
The splash screen is implemented as its own activity. A postDelayed handler is used to set a short delay before the user is redirected to the login screen/chat room screen depending on whether or not the user is logged in.
The splash screen itself consists of an image, I used the House of Code logo.

**Post Delayed Handler**
```java
new Handler().postDelayed(new Runnable() {
    @Override
    public void run() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(SplashActivity.this, ChatRoomsActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }
}, 2000);
```

### Login screen
The login screen is made up entirely of the Firebase AuthUI, which builds a very nice login screen with very little effort.
To set it up authentication needs to be set up in Firebase. To set up the google login, the applications sha1 key must be linked to the firebase authentication manager. The sha1 value is extracted using `keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore`. To set up the facebook login, the application must be registered as a facebook linked application. This is done through the <https://developers.facebook.com> website, it is important to follow the instructions all the way through or the link will not work. One important thing to do correctly is use OpenSsl to cahnge the sha1 into a base64 hash, that facebook uses to identify the application.

**Setup Logins**
```java
private void setupAuth() {
    AuthUI.IdpConfig p[] = new AuthUI.IdpConfig[] {
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build()
    };

    List<AuthUI.IdpConfig> providers = Arrays.asList(p);
    startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), 123);
}
```

### Chat rooms
The chat rooms screen consists of two separate parts of the view, the activity layout with the basic structure and functionality, and the partial that describes how each chat room should be shown and the functionality that are required for each chat room. The activity layout consists primarily of a recycler that uses the ChatRoomAdapter to map the values onto the partial. Each partial consist of a relative view containing three text views and an image view. The text views hold the name of the chat room, the description and the number of messages in the room. The image view is a chevrom, that can be pressed to view the details of the room.

**Get Chat Rooms**
```java
private void getChatRooms() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    try {
        db.collection("chat-rooms").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<ChatRoom> chatRooms = new ArrayList<>();

                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            ChatRoom room = doc.toObject(ChatRoom.class);
                            chatRooms.add(room);
                        }
                        // Fill recycler
                        recyclerView = findViewById(R.id.chat_room_recycler);
                        adapter = new ChatRoomAdapter(chatRooms);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("Error", e);
                        Error();
                    }
                });
    } catch (Exception e) {
        Log.wtf("Error", e);
        Error();
    }
}
```

### Send and receive messages
The messaging screen is like the chat rooms screen made up of the activity layout and the partial. The activity layout again consists of a recycler that contains the partials for the messages, and a small linear layout at the bottom containing a text field for the user to write their message in and a send button. Each message partial layout consitsts of a text view for the user name the person who sent the message, a text view for the time that the message was sent, a text view for the message itself and an image view that should show the users image/avatar. The image view does not seem to work when given a Uri, but I do not know why since the Uri itself works just fine. The messaging screen will automatically update when a new message is detected in the database.

**Send Message**
```java
private void sendMessage() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String text = messageText.getText().toString();
    if (text == "") {
        return;
    }
    messageText.setText("");
    Message message = new Message(text);
    try {
        db.collection("chat-rooms").document(currentRoom).collection("messages").add(message);
        db.collection("chat-rooms").document(currentRoom).update("numberOfMessages", messageList.size()+1);
    } catch (Exception e) {
        Log.wtf("Error", e);
    }
}
```
**Database listener**
```java
private void attachListener() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    try {
        CollectionReference messagesRef = db.collection("chat-rooms").document(currentRoom).collection("messages");
        messagesRef.orderBy("date")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e != null) {
                            Log.wtf("Error", e);
                        }

                        List<Message> messages = new ArrayList<>();
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            messages.add(message);
                        }
                        messageList = messages;
                        postToView(messages);
                    }
                });
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### Push notifications
The push notifications are handled with the Firebase Cloud Messaging service (FCM). FCM can be used to send messages to specific devices or groups of users, or to topics that the users can subscribe to. In this application topics are used to subscribe to specific chat rooms. Firebase Functions is used for handling the sending of notifications when a new message is created in a chat room. Firebase Functions uses NodeJS for javascript functions deployed to the server. These functions can be made to handle specific serverside functionality. In this project there is a function called `sendNotification` that is in charge of sending the notifications to the given topic.
The topics in this project are the chat room names trimmed down to contain only [a-zA-Z0-9-_.~%].
**Subscribtion handler**
```java
FirebaseMessaging.getInstance().subscribeToTopic(trimmedRoomName)
    .addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(!task.isSuccessful()) {
                Log.e("Error", "Something went wrong subscribing to the topic...");
            } else {
                Log.i("Info","Subscribed to " + currentRoom);
                // Update shared preferences
                SharedPreferences preferences = getSharedPreferences("SubscribedTopics", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(currentRoom, true);
                editor.apply();
            }
        }
    });
```
**Send Notifications Function**
```javascript
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
```