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

### Login screen
The login screen is made up entirely of the Firebase AuthUI, which builds a very nice login screen with very little effort.
To set it up authentication needs to be set up in Firebase. To set up the google login, the applications sha1 key must be linked to the firebase authentication manager. The sha1 value is extracted using `keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore`. To set up the facebook login, the application must be registered as a facebook linked application. This is done through the <https://developers.facebook.com> website, it is important to follow the instructions all the way through or the link will not work. One important thing to do correctly is use OpenSsl to cahnge the sha1 into a base64 hash, that facebook uses to identify the application.

### Chat rooms
The chat rooms screen consists of two separate parts of the view, the activity layout with the basic structure and functionality, and the partial that describes how each chat room should be shown and the functionality that are required for each chat room. The activity layout consists primarily of a recycler that uses the ChatRoomAdapter to map the values onto the partial. Each partial consist of a relative view containing three text views and an image view. The text views hold the name of the chat room, the description and the number of messages in the room. The image view is a chevrom, that can be pressed to view the details of the room.

### Send and receive messages
The messaging screen is like the chat rooms screen made up of the activity layout and the partial. The activity layout again consists of a recycler that contains the partials for the messages, and a small linear layout at the bottom containing a text field for the user to write their message in and a send button. Each message partial layout consitsts of a text view for the user name the person who sent the message, a text view for the time that the message was sent, a text view for the message itself and an image view that should show the users image/avatar. The image view does not seem to work when given a Uri, but I do not know why since the Uri itself works just fine.