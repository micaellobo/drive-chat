package Models;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Stream;

public class InetAddressGenerator implements Enumeration<InetAddress> {

    private int ipIncremental;

    private final int[] upperBound;
    private final int[] nextIp;

    public InetAddressGenerator(InetAddress lowerBound, InetAddress upperBound) {
        this.upperBound = Stream.of(upperBound.getHostAddress().split("\\.")).mapToInt(Integer::parseInt).toArray();
        this.nextIp = Stream.of(lowerBound.getHostAddress().split("\\.")).mapToInt(Integer::parseInt).toArray();
        this.ipIncremental = this.nextIp[3];
    }

    @Override
    public boolean hasMoreElements() {
        return nextIp[0] != upperBound[0] || nextIp[1] != upperBound[1] || nextIp[2] != upperBound[2] || nextIp[3] != upperBound[3];
    }

    @Override
    public InetAddress nextElement() throws NoSuchElementException {
        if (!hasMoreElements())
            throw new NoSuchElementException();

        InetAddress inetAddress;

        nextIp[3] = ipIncremental++;

        try {
            inetAddress = InetAddress.getByName(String.join(".", Arrays.stream(nextIp).mapToObj(String::valueOf).toArray(String[]::new)));
        } catch (UnknownHostException e) {
            throw new NoSuchElementException("Invalid IP!");
        }

        if (hasMoreElements()) {
            if (nextIp[1] == upperBound[1] && nextIp[2] == upperBound[2] && nextIp[3] == upperBound[3]) {
                nextIp[0]++;
                nextIp[1] = 0;
            }
            if (nextIp[2] == upperBound[2] && nextIp[3] == upperBound[3]) {
                nextIp[1]++;
                nextIp[2] = 0;
            }
            if (nextIp[3] == upperBound[3]) {
                nextIp[2]++;
                ipIncremental = 0;
            }
        }

        return inetAddress;
    }
}
