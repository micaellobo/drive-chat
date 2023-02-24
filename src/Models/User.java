package Models;

import java.util.*;

public class User {

    protected String name;
    protected String username;
    protected String password;
    protected int radius;
    protected GeographicCoordinate geoCordi;
    protected String mainGroup;
    protected final ArrayList<String> groupsChat;
    protected final ArrayList<String> groupsAlert;
    protected final ArrayList<String> friendRequestsReceived;
    protected final ArrayList<String> friendRequestsSent;
    protected final ArrayList<String> friends;

    public User(String name, String username, String password, int radius, GeographicCoordinate geoCordi) {
        this.groupsChat = new ArrayList<>();
        this.groupsAlert = new ArrayList<>();
        this.friendRequestsReceived = new ArrayList<>();
        this.friendRequestsSent = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.name = name;
        this.username = username;
        this.password = password;
        this.radius = radius;
        this.geoCordi = geoCordi;
    }


    public ArrayList<String> getGroupsChat() {
        return groupsChat;
    }

    public ArrayList<String> getFriendRequestsReceived() {
        return friendRequestsReceived;
    }

    public ArrayList<String> getFriendRequestsSent() {
        return friendRequestsSent;
    }

    public ArrayList<String> getFriends() {
        return this.friends;
    }

    public boolean removeGroupChat(String groupName) {
        return this.groupsChat.remove(groupName);
    }

    public boolean addGroupChat(String groupName) {
        if (this.groupsChat.contains(groupName))
            return false;
        return this.groupsChat.add(groupName);
    }

    public ArrayList<String> getGroupsAlert() {
        return groupsAlert;
    }

    public boolean removeGroupAlert(String groupName) {
        return this.groupsChat.remove(groupName);
    }

    public boolean addGroupAlert(String groupName) {
        if (this.groupsAlert.contains(groupName))
            return false;
        return this.groupsAlert.add(groupName);
    }

    public boolean addGroups(ArrayList<String> groups) {
        if (this.groupsChat.stream().noneMatch(groups::contains)) {
            return this.groupsChat.addAll(groups);
        }
        return false;
    }

    public boolean addFriendRequestReceived(String friendRequestName) {
        if (this.friendRequestsReceived.contains(friendRequestName))
            return false;
        return this.friendRequestsReceived.add(friendRequestName);
    }

    public boolean removeFriendRequestReceived(String friendRequestName) {
        if (this.friendRequestsReceived.contains(friendRequestName))
            return false;
        return this.friendRequestsReceived.add(friendRequestName);
    }

    public boolean addFriendRequestSent(String friendRequestName) {
        if (this.friendRequestsSent.contains(friendRequestName))
            return false;
        return this.friendRequestsSent.add(friendRequestName);
    }

    public boolean removeFriendRequestSent(String friendRequestName) {
        if (this.friendRequestsSent.contains(friendRequestName))
            return false;
        return this.friendRequestsSent.add(friendRequestName);
    }

    public boolean addFriend(String friendRequestName) {
        if (friendRequestsReceived.remove(friendRequestName) || friendRequestsSent.remove(friendRequestName)) {
            return this.friends.add(friendRequestName);
        }
        return false;
    }

    public String getMainGroup() {
        return mainGroup;
    }

    public void setMainGroup(String mainGroup) {
        this.mainGroup = mainGroup;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public GeographicCoordinate getGeoCordi() {
        return geoCordi;
    }

    public void setGeoCordi(GeographicCoordinate geoCordi) {
        this.geoCordi = geoCordi;
    }

    public boolean isInsideRadius(GeographicCoordinate geoGraficToCompare, String otherUsername) {
        System.out.println(this.username + " -> " +otherUsername);
        System.out.println(this.geoCordi.distanceInKmBetweenEarthCoordinates(geoGraficToCompare) + " --- " + this.radius);
        return this.geoCordi.distanceInKmBetweenEarthCoordinates(geoGraficToCompare) <= this.radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return this.username;
    }
}
