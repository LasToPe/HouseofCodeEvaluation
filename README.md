# House of Code Evaluation

## Requirements
- Splash Screen
- Login Screen
- Chat rooms
- Send and recieve messages
- Push functionality
- Upload images to chat room

## Documentation
The House of Code Evaluation application is a chat room and messaging app built for android with Java.
I have no prior experience with Android programming specifically, so please excuse any minor errors.
The application uses Google Firebase for the data layer, which I also had no prior experience with.
The project was built using Android Studio 3.5.2, testet on the Google Nexus 5X emulator as well as my personal phone a OnePlus 7 pro.

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
To set it up authentication needs to be set up in Firebase. To set up the google login, the applications sha1 key must be linked to the firebase authentication manager. The sha1 value is extracted using `keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore`. To set up the facebook login, the application must be registered as a facebook linked application. This is done through the <https://developers.facebook.com> website, it is important to follow the instructions all the way through or the link will not work. One important thing to do correctly is use OpenSSL to change the sha1 into a base64 hash, that facebook uses to identify the application. Currently facebook authentication only works if the user is registered as tester/developer, as the application is not ready for production. If you wish to use facebook authentication, please send me a mail <mailto:lassep95@gmail.com> with your facebook profile url, so I can add you to testers.

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
The messaging screen is like the chat rooms screen made up of the activity layout and the partial. The activity layout again consists of a recycler that contains the partials for the messages, and a small linear layout at the bottom containing a text field for the user to write their message in and a send button. Each message partial layout consists of a text view for the user name the person who sent the message, a text view for the time that the message was sent, a text view for the message itself and an image view shows the users image/avatar. The image is loaded with the Glide framework <https://github.com/bumptech/glide>. The messaging screen will automatically update when a new message is detected in the database.

**Send Message**
```java
private void sendMessage() {
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    String text = messageText.getText().toString();
    if (text.equals("") && filepath == null) {
        return;
    }

    HandleSubscription();

    try {
        Message message = new Message(text);
        db.collection("chat-rooms").document(currentRoom).collection("messages").add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                uploadImage(documentReference);
            }
        });
        db.collection("chat-rooms").document(currentRoom).update("numberOfMessages", messageList.size() + 1);
        db.collection("chat-rooms").document(currentRoom).update("newestMessage", message.getDate());
    } catch (Exception e) {
        Log.e("Error", e.getMessage());
    } finally {
        messageText.setText("");
        findViewById(R.id.preview).setVisibility(View.GONE);
    }
}
```
**Database listener**
```java
private void attachListener() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    try {
        CollectionReference messagesRef = db.collection("chat-rooms").document(currentRoom).collection("messages");
        messagesRef.orderBy("date", Query.Direction.DESCENDING).limit(numberOfMessages)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e != null) {
                            Log.e("Error", e.getMessage());
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
### Image upload
The image upload feature is handled on a chat room basis by the firebase storage functionality. The image is uploaded from the device to the firebase storage and is given the same name as the message it is attached to. The url for the image is set on the message with a firebase function that handles all uploaded files. When the image button is clicked the user is taken to a screen on which an image can be selected. When an image has been selected the user is returned to the messaging screen where a preview of the image is shown above the messaging box. On pressing send the image is uploaded and will shortly thereafter appear in as part of the message.

The firebase function that handles the image when it has been uploaded first splits up the name of the object into the room that it belongs to and the name of the message. A method for getting the downloadable url from the medialink and the download token is called and the message is updated in the firestore with the url as the value for the imageUri parameter.

**Choose Image**
```java
private void chooseImage() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
        filepath = data.getData().toString();
        ImageView preview = findViewById(R.id.preview);
        preview.setImageURI(data.getData());
        preview.setVisibility(View.VISIBLE);
    }
}
```
**Upload Image**
```java
private void uploadImage(DocumentReference documentReference) {
    try {
        if (filepath != null) {
            Uri path = Uri.parse(filepath);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final String imagePath = currentRoom + "/" + documentReference.getId();
            final StorageReference reference = storage.getReference().child(imagePath);
            reference.putFile(path).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Log.d("Debug", task.getResult().toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Error", e.getMessage());
                }
            });
        }
    } catch (Exception e) {
        e.printStackTrace();
    }finally {
        filepath = null;
    }
}
```
**Firebase Handle upload function**
```javascript
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
```
### Unfulfilled Accepttests
There are two accepttests that are unfortunateley not fulfilled, as I have no idea of how to implement it.

1. When a user presses the push notifications they are not taken to the correct room. I do not know how to implement deep links in a notification.
2. You cannot open the camera directly from the messaging screen. I could not figure out how to set up two different actions for one button.

## Time overview
In total I spent just shy of 29 hours on this project. I tracked the time using toggl.

| Task                    | Time (hrs) |
| ----------------------- |:----------:|
| Analysis                | 2          |
| Documentation           | 3          |
| Image upload            | 6.5        |
| Login                   | 3          |
| Messages and chat rooms | 9          |
| Push notifications      | 4          |
| Splash screen           | 1.5        |
| **Total**               | **29**     |
