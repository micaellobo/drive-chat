package Server;

import Models.ArrayListSync;
import Models.HelpersComunication.RequestType;
import Models.HelpersComunication.Response;
import Models.HelpersComunication.StatusResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    public static final String MAIN_GROUP_IP = "224.0.0.2";
    private final ServerSocket serverSocket;
    private final DatagramSocket datagramSocket;

    private final ArrayListSync<ClientHandler> clientHandlers;
    private final Gson jsonHelper;

    public Server(ServerSocket serverSocket, DatagramSocket datagramSocket) {
        this.clientHandlers = new ArrayListSync<>();
        this.jsonHelper = new Gson();
        this.serverSocket = serverSocket;
        this.datagramSocket = datagramSocket;
    }

    public void startServer() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();

                new Thread(new ClientHandler(clientHandlers, socket, this)).start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendContinuously() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            multicastMessage(this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.ALERT_APP_CIVIL_PROTECTION, "BUSINESS LOGIC NOT IMPLEMENTED, REPORT PERIODICALLY COLLISIONS")), MAIN_GROUP_IP);
        }, 5, 10, TimeUnit.SECONDS);
    }

    protected void multicastMessage(String message, String group) {
        try {
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group), 4446);
            System.out.println("UDP -> " + message);
            datagramSocket.send(packet);
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void sendAlertsCivilProtection() {
        new Thread(() -> {
            while (true) {
                System.out.print("Alert Civil Protection: ");
                String message = this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.ALERT_USER_CIVIL_PROTECTION, new Scanner(System.in).nextLine()));

                byte[] buf = message.getBytes();
                try {
                    datagramSocket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(MAIN_GROUP_IP), 4446));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(4445);
        Server server = new Server(new ServerSocket(2048), datagramSocket);
        server.sendAlertsCivilProtection();
        server.sendContinuously();
        server.startServer();
    }
}
