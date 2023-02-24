package Models.HelpersComunication;

import Models.Message;
import Models.User;

import java.util.ArrayList;

public class UserLogin {

    public User user;
    public ArrayList<String> groupsToAvalilableJoin;
    public ArrayList<String> usersNotFriend;
    public ArrayList<String> listIpsToJoin;
    public ArrayList<Message> messagesUsersToUsers;

    public UserLogin(User user, ArrayList<String> groupsToJoin, ArrayList<String> usersNotFriend, ArrayList<String> listIpsToJoin, ArrayList<Message> messagesUsersToUsers) {
        this.user = user;
        this.messagesUsersToUsers = messagesUsersToUsers;
        this.groupsToAvalilableJoin = groupsToJoin;
        this.usersNotFriend = usersNotFriend;
        this.listIpsToJoin = listIpsToJoin;
    }

}
