package Client;

import Client.UI.StartFrame;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class Client {

    protected MulticastSocket multicastSocket;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private final StartFrame startFrame;
    private final Gson jsonHelper = new Gson();

    public Client(MulticastSocket multicastSocket, String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.multicastSocket = multicastSocket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.startFrame = new StartFrame(this);
            startFrame.configFrame();


        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            throw new NotYetConnectedException();
        }
    }

    public void joinGroups(ArrayList<String> ips) throws IOException {
        System.out.println("JOIN GROUPS IP -> " + ips);
        for (String ip : ips)
            this.multicastSocket.joinGroup(InetAddress.getByName(ip));
    }

    public void joinGroup(String ip) throws IOException {
        System.out.println("JOIN GROUP IP -> " + ip);
        this.multicastSocket.joinGroup(InetAddress.getByName(ip));
    }

    public void leaveGroup(String ip) throws IOException {
        System.out.println("LEAVE GROUP IP -> " + ip);
        this.multicastSocket.leaveGroup(InetAddress.getByName(ip));
    }

    public void leaveGroups() throws IOException {
        this.multicastSocket = new MulticastSocket(4446);
    }

    public void sendMessage(String message) {
        new Thread(() -> {
            if (message != null) {
                try {
                    bufferedWriter.write(message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    private void receiveMessagesMulticast() {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buf = new byte[256];
                    DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                    multicastSocket.receive(datagramPacket);

                    String received = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());

                    this.startFrame.processResponse(received);

                    System.out.println("MULTICAST -> " + received);

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }).start();
    }

    public void receiveMessages() {
        new Thread(() -> {
            while (true) {
                try {
                    String responseMessage = bufferedReader.readLine();

                    if (responseMessage == null)
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    System.out.println(responseMessage);
                    this.startFrame.processResponse(responseMessage);

//                    System.out.println(responseMessage);

                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) bufferedReader.close();

            if (bufferedWriter != null) bufferedWriter.close();

            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(4446);
            Client client = new Client(multicastSocket, "localhost", 2048);

            client.receiveMessages();
            client.receiveMessagesMulticast();
        } catch (NotYetConnectedException | IOException e) {
            showMessageDialog(null, "Sem conexão", "", ERROR_MESSAGE);
        }
    }
}
