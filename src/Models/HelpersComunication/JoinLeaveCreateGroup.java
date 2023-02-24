package Models.HelpersComunication;

public class JoinLeaveCreateGroup {

    public String user;
    public String group;
    public String ip;

    public JoinLeaveCreateGroup(String user, String group, String ip) {
        this.user = user;
        this.group = group;
        this.ip = ip;
    }

    public JoinLeaveCreateGroup(String user, String group) {
        this(user, group, null);
    }
}
