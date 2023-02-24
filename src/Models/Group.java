package Models;

import Server.JsonFileHelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class Group {

    private static InetAddressGenerator iPGenerator;

    static {
        try {
            iPGenerator = new InetAddressGenerator(InetAddress.getByName("224.0.0.3"), InetAddress.getByName("239.255.255.255"));
            int size = new JsonFileHelper("files/").getGroups().size();
            int i = 0;
            while (i < size) {
                iPGenerator.nextElement();
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final GroupType groupType;
    private final String name;
    private final InetAddress ip;

    public Group(String name, GroupType groupType) {
        this.name = name;
        this.groupType = groupType;
        this.ip = iPGenerator.nextElement();
    }

    public String getName() {
        return name;
    }

    public InetAddress getIp() {
        return ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(ip, group.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip);
    }

    @Override
    public String toString() {
        return name;
    }

    public GroupType getGroupType() {
        return groupType;
    }
}
