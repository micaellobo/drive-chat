# Drive-Chat

Drive-Chat is a Java application that uses sockets for real-time messaging between users. It includes features for
sending messages, group chats, and friend requests. Enabling users to send and receive traffic or vehicle cluster alerts
in a given area. It also allows the creation of a "friendship" system for sending messages with traffic warnings,
weather conditions, well as the exchange of messages between users, among others.

The application consists of a server that manages communication between users and a central node. The server registers
users, logs in, determines the density of drivers in an area, and notifies other drivers of the possibility of transit,
in addition to storing interactions between users who consider themselves friends. The central node is responsible for
periodically reporting the number of incidents detected, as well as alerts defined by the protection civil.

## Architecture / Implementation

DriveChat uses a client-server architecture, with the server handling message routing between clients. The application
uses sockets to establish and maintain connections between clients and the server, allowing for real-time messaging.

The server is capable of handling multiple connections from drivers simultaneously and stores all relevant information
persistently. Periodic alert notifications are sent via multicast (when targeting a group of drivers) and broadcast (
when the event is for the entire population).

The application uses both TCP and UDP sockets for communication. TCP (Transmission Control Protocol) is a reliable,
connection-oriented protocol that ensures that all data is delivered in the correct order and without loss. This makes
it ideal for sending important information such as user authentication and messages. UDP (User Datagram Protocol), on
the other hand, is a connectionless protocol that is faster and more efficient for sending small amounts of data, such
as alerts and notifications.

The app uses JSON strings for message transmission and Gson for the serialization and deserialization of the messages.

For the client Java Swing was used for the user interface.

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


