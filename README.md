# Drive-Chat

Drive-Chat is a Java app for real-time messaging using sockets. Users can send messages, join group chats, and send friend requests. It also allows for traffic and weather alerts and message exchanges between friends. The app has a server for managing communication and a central node for reporting incidents and alerts.

## Architecture / Implementation

DriveChat is a Java client-server application for real-time messaging between drivers. The server manages message routing and handles multiple connections. TCP and UDP sockets are used for communication, with TCP for important information and UDP for alerts. JSON and Gson are used for message transmission and serialization. 

The user interface is built with Java Swing and currently is not fully implemented (arround 85%).

## Functionalities

- Drivers can register with the platform to receive notifications. After registration, each user can:
    - Define their current location
    - Define people/groups to send/receive messages
    - Receive notifications about incidents on the road in the surrounding area (the user can define the radius of their
      surrounding area)
    - Receive messages from the community, provided that the sender is within 1km distance
    - Send traffic crash alerts and report car traffic
    - Send messages to people/groups
    - Broadcast notifications to the entire community about incidents on the road
    - Receive notifications from the system about alerts from civil protection authorities
- The application includes a central node (server) that manages all communication between drivers and the system. It is
  responsible for:
    - User registration and login
    - Determining the density of drivers in an area and notifying other drivers of possible traffic congestion
    - Storing user information and interactions between users who consider themselves friends (for private message
      exchange)
    - Periodically reporting areas of congestion to all drivers
    - Periodically reporting alerts defined by civil protection authorities (e.g., adverse weather conditions) to all
      drivers

## Usage

At this moment the best solution is through an IDE, because the application is not fully finished. There are some
missing features on the client side, specifically in the user interface. And at this point is important see the console
output

To run the application, follow these steps:

1. Clone the repository
2. Run the server: `Server.java`
3. Run the client: `Client.java `

## Libraries and Dependencies

DriveChat requires Java 11 or later to run. The application uses the following libraries and dependencies:

- [Java Swing](https://docs.oracle.com/javase/tutorial/uiswing/index.html) for the user interface
- [Gson](https://github.com/google/gson) for JSON serialization and deserialization

## File Structure

```
├── files => where the data is persisted
│   ├── groups.json
│   ├── messages.json
│   └── users.json
└── src
    ├── Client
    │   ├── Client.java 
    │   └── UI
    │       ├── Menu.form
    │       ├── Menu.java
    │       ├── StartFrame.form 
    │       └── StartFrame.java
    ├── Models
    │   ├── ArrayListSync.java => syncronized array list
    │   ├── GeographicCoordinate.java
    │   ├── Group.java
    │   ├── GroupType.java
    │   ├── HelpersComunication => models for communication
    │   │   ├── Alert.java
    │   │   ├── FriendsRequestHelper.java
    │   │   ├── JoinLeaveCreateGroup.java
    │   │   ├── Login.java
    │   │   ├── Request.java
    │   │   ├── RequestType.java
    │   │   ├── Response.java
    │   │   ├── StatusResponse.java
    │   │   ├── UserLogin.java
    │   │   └── UserRegistration.java
    │   ├── InetAddressGenerator.java => IP Generator
    │   ├── Message.java
    │   ├── TypeMessage.java
    │   └── User.java
    └── Server
        ├── ClientHandler.java
        ├── JsonFileHelper.java
        ├── Protocol.java
        └── Server.java 
```

## Images

<div align="center">
  <img src="https://i.imgur.com/fFbQkdP.png" width="75%" height="75%">
</div>
<div align="center">
  <img src="https://i.imgur.com/HoYhITl.png" width="75%" height="75%">
</div>
<div align="center">
  <img src="https://i.imgur.com/iaIjRvJ.png" width="75%" height="75%">
</div>
<div align="center">
  <img src="https://i.imgur.com/SXiadC4.png" width="75%" height="75%">
</div>
<div align="center">
  <img src="https://i.imgur.com/QR0YzrN.png" width="75%" height="75%">
</div>
<div align="center">
  <img src="https://i.imgur.com/KD325HL.png" width="75%" height="75%">
</div>


