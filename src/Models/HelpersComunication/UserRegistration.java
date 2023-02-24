package Models.HelpersComunication;

import Models.User;

public class UserRegistration {

    public User user;
    public String groupToCreate;

    public UserRegistration(User user, String group) {
        this.user = user;
        this.groupToCreate = group;
    }

    public UserRegistration(User user) {
        this.user = user;
    }
}
