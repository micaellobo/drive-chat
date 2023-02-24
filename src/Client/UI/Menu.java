package Client.UI;

import Client.Client;
import Models.HelpersComunication.*;
import Models.Message;
import Models.TypeMessage;
import Models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import static javax.swing.JOptionPane.*;

public class Menu extends JFrame {

    //Main
    private JPanel mainPanel;
    private JPanel tabMessager;
    private JPanel tabFriendsGroups;
    private JPanel tabAlerts;
    private JPanel tabSettings;
    private JButton buttonTabMessager;
    private JButton buttonTabFriendsGroups;
    private JButton buttonAlerts;
    private JButton buttonSettings;
    private JButton buttonLogout;

    //TAB Settings
    private JTextField textUsername;
    private JTextField textPassword;
    private JTextField textName;
    private JTextField textLatitude;
    private JTextField textLongitude;
    private JTextField textRadiusNotification;
    private JButton buttonSaveSettings;

    //TAB Alerts
    private JTextField textAlertMessage;
    private JButton buttonSendAlert;
    private JTable tableAlerts;
    private JScrollPane scrollTableAlerts;

    //TAB FriendGroups
    private JList<String> listFriendsRequests;
    private JList<String> listUsersNotFriend;
    private JList<String> listAvalilableGroupsToJoin;
    private JScrollPane scrollFriendsRequests;
    private JScrollPane scrollUsersNotFriend;
    private JScrollPane scrollAvailableGroupsToJoin;
    private JButton buttonAcceptFriend;
    private JButton buttonCreateGroup;
    private JButton buttonJoinGroup;
    private JButton buttonSendRequestFriend;
    private JTextField textCreateGroup;

    //TAB Messager
    private JButton buttonSendMessage;
    private JButton buttonChooseGroups;
    private JButton buttonChooseFriends;
    private JScrollPane scroolFriendsGroups;
    private JList<String> listFriendsGroups;
    private JTextField textChatMessage;
    private JTextArea textChat;
    private JButton buttonLeaveGroup;
    private JList<String> listGroupsToLeave;
    private JScrollPane scrollGroupsToLeave;
    private JLabel labelUserName;

    //Current Session
    private final Gson jsonHelper;
    private final User currentUser;
    private final Client client;
    private final StartFrame startFrame;
    private final ArrayList<String> usersNotFriend;
    private final ArrayList<Message> userMessages;
    private final ArrayList<String> groupsAvailableToJoin;

    public Menu(StartFrame loginRegistoFrame, Client client, User userLoggedIn, ArrayList<String> usersNotFriend, ArrayList<String> groupsToJoin, ArrayList<Message> userMessages) {
        setContentPane(mainPanel);

        this.jsonHelper = new Gson();
        this.userMessages = userMessages;
        this.startFrame = loginRegistoFrame;
        this.usersNotFriend = usersNotFriend;
        this.groupsAvailableToJoin = groupsToJoin;
        this.client = client;
        this.currentUser = userLoggedIn;

        this.labelUserName.setText(this.currentUser.getName());

        configTabsMenu();
        configTabMessager();
        configTabFriendsGroups();

        buttonSendAlert.addActionListener(e -> {
            String text = textAlertMessage.getText();
            if (text.isEmpty()) {
                dialogShow("Escreva mensgem", ERROR_MESSAGE, 1000);
                return;
            }
            Request<Alert> request = new Request<>(RequestType.ALERT, new Alert(this.currentUser.getUsername(), text));

            this.client.sendMessage(this.jsonHelper.toJson(request));
        });
    }

    private void configTabMessager() {
        loadJList(scroolFriendsGroups, listFriendsGroups, this.currentUser.getGroupsChat());

        //Quando escolhe list Grupos
        buttonChooseGroups.addActionListener(e -> {
            buttonChooseFriends.setSelected(false);
            buttonChooseGroups.setSelected(true);
            loadJList(scroolFriendsGroups, listFriendsGroups, this.currentUser.getGroupsChat());
        });

        //Quando escolhe list Amigos
        buttonChooseFriends.addActionListener(e -> {
            buttonChooseFriends.setSelected(true);
            buttonChooseGroups.setSelected(false);
            textChat.setText("");
                StringBuilder text = new StringBuilder();
            for (Message message : userMessages){
                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.ofInstant(message.getDate().toInstant(), ZoneId.systemDefault()));

                if (message.getFrom().equals(this.currentUser.getUsername())) {
                    text.append(message.getFrom()).append(" -> ").append(date).append("\n");
                }else {
                    text.append(message.getTo()).append(" -> ").append(date).append("\n");
                }
                text.append(message.getMessage());

                textChat.setText(text.toString());
            };



            loadJList(scroolFriendsGroups, listFriendsGroups, this.currentUser.getFriends());
        });
        listFriendsGroups.addListSelectionListener(e ->{
listFriendsGroups.getSelectedValue();
        });

        //Send Message
        buttonSendMessage.addActionListener(e -> {
            if (listFriendsGroups.isSelectionEmpty()) {
                dialogShow("Necessario escolher um destinatario!", ERROR_MESSAGE, 1000);
                return;
            }
            TypeMessage typeMessage = buttonChooseFriends.isSelected() ? TypeMessage.USER_TO_USER : TypeMessage.USER_TO_GROUP;

            Message message = new Message(textChatMessage.getText(), new Date(), this.currentUser.getUsername(), listFriendsGroups.getSelectedValue(), typeMessage);

            Request<Message> request = new Request<>(RequestType.MESSAGE, message);

            this.client.sendMessage(this.jsonHelper.toJson(request));
        });
    }

    private void configTabFriendsGroups() {
        //Lista de Pedidos de Amizade
        loadJList(scrollFriendsRequests, listFriendsRequests, this.currentUser.getFriendRequestsReceived());

        //Lista de Amigos disponiveis para fazer pedido
        loadJList(scrollUsersNotFriend, listUsersNotFriend, this.usersNotFriend);

        //Lista de Grupos disponiveis para fazer pedido
        loadJList(scrollAvailableGroupsToJoin, listAvalilableGroupsToJoin, this.groupsAvailableToJoin);

        //Lista de que faz parte
        loadJList(scrollGroupsToLeave, listGroupsToLeave, this.currentUser.getGroupsChat());

        //Entrar num grupo
        buttonJoinGroup.addActionListener(e -> {
            if (listAvalilableGroupsToJoin.isSelectionEmpty()) {
                showMessageDialog(null, "Escolha um grupo", "", ERROR_MESSAGE);
                return;
            }
            String selectGroup = listAvalilableGroupsToJoin.getSelectedValue();

            JoinLeaveCreateGroup leaveGroup = new JoinLeaveCreateGroup(this.currentUser.getUsername(), selectGroup);

            Request<JoinLeaveCreateGroup> request = new Request<>(RequestType.JOIN_GROUP, leaveGroup);

            this.client.sendMessage(this.jsonHelper.toJson(request));
        });

        //butao deixar grupo
        buttonLeaveGroup.addActionListener(e -> {
            if (listGroupsToLeave.isSelectionEmpty()) {
                showMessageDialog(new JFrame(), "Escolha um grupo", "", ERROR_MESSAGE);
                return;
            }
            JoinLeaveCreateGroup joinLeaveGroup = new JoinLeaveCreateGroup(this.currentUser.getUsername(), listGroupsToLeave.getSelectedValue());

            Request<JoinLeaveCreateGroup> request = new Request<>(RequestType.LEAVE_GROUP, joinLeaveGroup);

            this.client.sendMessage(this.jsonHelper.toJson(request));

        });

        //butao criar grupo
        buttonCreateGroup.addActionListener(e -> {
            if (textCreateGroup.getText().isEmpty()) {
                dialogShow("Insire um nome para o grupo", ERROR_MESSAGE, 1000);
                return;
            }
            JoinLeaveCreateGroup joinLeaveGroup = new JoinLeaveCreateGroup(this.currentUser.getUsername(), textCreateGroup.getText());

            Request<JoinLeaveCreateGroup> request = new Request<>(RequestType.CREATE_GROUP, joinLeaveGroup);

            this.client.sendMessage(this.jsonHelper.toJson(request));

        });

        //Enviar pedido amizade
        buttonSendRequestFriend.addActionListener(e -> {
            if (listUsersNotFriend.isSelectionEmpty()) {
                showMessageDialog(new JFrame(), "Escolha um utilizador", "", ERROR_MESSAGE);
                return;
            }
            String selectFriend = listUsersNotFriend.getSelectedValue();

            FriendsRequestHelper friendsRequestHelper = new FriendsRequestHelper(this.currentUser.getUsername(), selectFriend);
            Request<FriendsRequestHelper> request = new Request<>(RequestType.SENT_FRIEND_REQUEST, friendsRequestHelper);

            this.client.sendMessage(this.jsonHelper.toJson(request));
        });

        //butao aceitar pedido amizade
        this.buttonAcceptFriend.addActionListener(e -> {
            if (listFriendsRequests.isSelectionEmpty()) {
                showMessageDialog(new JFrame(), "Escolha um pedido de amizade", "", ERROR_MESSAGE);
                return;
            }
            String selectFriendRequest = listFriendsRequests.getSelectedValue();

            FriendsRequestHelper friendsRequestHelper = new FriendsRequestHelper(this.currentUser.getUsername(), selectFriendRequest);
            Request<FriendsRequestHelper> request = new Request<>(RequestType.ACCEPT_FRIEND, friendsRequestHelper);

            this.client.sendMessage(this.jsonHelper.toJson(request));
        });
    }


    public void processMessage(String message, RequestType type) {

        switch (type) {
            case FEEDBACK_SENT_FRIEND_REQUEST:
                System.out.println("FEEDBACK_SENT_FRIEND_REQUEST");
                Response<FriendsRequestHelper> feedbackSentFriendRequest = this.jsonHelper.fromJson(message, new TypeToken<Response<FriendsRequestHelper>>() {
                }.getType());

                this.currentUser.addFriend(feedbackSentFriendRequest.data.userToRequest);

                loadJList(scrollFriendsRequests, listFriendsRequests, this.currentUser.getFriendRequestsReceived());
                dialogShow(feedbackSentFriendRequest.message, PLAIN_MESSAGE, 1000);
                break;
            case FEEDBACK_REQUEST_FRIEND_REQUEST:
                System.out.println("FEEDBACK_REQUEST_FRIEND_REQUEST");
                Response<FriendsRequestHelper> responseFeedBackFriendRequest = this.jsonHelper.fromJson(message, new TypeToken<Response<FriendsRequestHelper>>() {
                }.getType());

                this.currentUser.addFriendRequestSent(responseFeedBackFriendRequest.data.userToRequest);
                this.usersNotFriend.remove(responseFeedBackFriendRequest.data.userToRequest);

                loadJList(scrollFriendsRequests, listFriendsRequests, this.currentUser.getFriendRequestsReceived());
                loadJList(scrollUsersNotFriend, listUsersNotFriend, this.usersNotFriend);

                dialogShow(responseFeedBackFriendRequest.message, PLAIN_MESSAGE, 1000);
                break;
            case RECEIVE_FRIEND_REQUEST:
                System.out.println("RECEIVE_FRIEND_REQUEST");
                Response<FriendsRequestHelper> responseFriendRequest = this.jsonHelper.fromJson(message, new TypeToken<Response<FriendsRequestHelper>>() {
                }.getType());

                this.currentUser.addFriendRequestReceived(responseFriendRequest.data.userSender);
                this.usersNotFriend.remove(responseFriendRequest.data.userSender);

                loadJList(scrollFriendsRequests, listFriendsRequests, this.currentUser.getFriendRequestsReceived());
                loadJList(scrollUsersNotFriend, listUsersNotFriend, this.usersNotFriend);

                dialogShow(responseFriendRequest.message, PLAIN_MESSAGE, 1000);
                break;
            case ACCEPT_FRIEND:
                System.out.println("ACEITAR PEDIDO AMIZADE");
                Response<FriendsRequestHelper> responseAcceptRequest = this.jsonHelper.fromJson(message, new TypeToken<Response<FriendsRequestHelper>>() {
                }.getType());

                this.currentUser.addFriend(responseAcceptRequest.data.userSender);

                loadJList(scrollFriendsRequests, listFriendsRequests, this.currentUser.getFriendRequestsReceived());

                dialogShow(responseAcceptRequest.message, PLAIN_MESSAGE, 1000);

                break;
            case JOIN_GROUP:
                System.out.println("JOIN_GROUP");
                Response<JoinLeaveCreateGroup> responseJoinGroup = this.jsonHelper.fromJson(message, new TypeToken<Response<JoinLeaveCreateGroup>>() {
                }.getType());

                try {
                    this.client.joinGroup(responseJoinGroup.data.ip);

                    this.currentUser.addGroupChat(responseJoinGroup.data.group);
                    this.groupsAvailableToJoin.remove(responseJoinGroup.data.group);

                    loadJList(scrollAvailableGroupsToJoin, listAvalilableGroupsToJoin, this.groupsAvailableToJoin);
                    loadJList(scrollGroupsToLeave, listGroupsToLeave, this.currentUser.getGroupsChat());

                    dialogShow(responseJoinGroup.message, PLAIN_MESSAGE, 1000);
                } catch (IOException e) {
                    dialogShow("Erro inesperado ao juntar ao grupo", ERROR_MESSAGE, 1000);
                    return;
                }
                break;
            case CREATE_GROUP:
                System.out.println("CREATE_GROUP");
                Response<JoinLeaveCreateGroup> responseCreateGroup = this.jsonHelper.fromJson(message, new TypeToken<Response<JoinLeaveCreateGroup>>() {
                }.getType());

                try {
                    this.client.joinGroup(responseCreateGroup.data.ip);

                    this.currentUser.addGroupChat(responseCreateGroup.data.group);

                    loadJList(scrollGroupsToLeave, listGroupsToLeave, this.currentUser.getGroupsChat());
                    this.textCreateGroup.setText("");
                    dialogShow(responseCreateGroup.message, PLAIN_MESSAGE, 1000);
                } catch (IOException e) {
                    dialogShow("Erro inesperado ao juntar ao grupo", ERROR_MESSAGE, 1000);
                    return;
                }

                break;
            case LEAVE_GROUP:
                System.out.println("LEAVE_GROUP");
                Response<JoinLeaveCreateGroup> responseLeaveGroup = this.jsonHelper.fromJson(message, new TypeToken<Response<JoinLeaveCreateGroup>>() {
                }.getType());

                try {
                    this.client.leaveGroup(responseLeaveGroup.data.ip);

                    this.currentUser.removeGroupChat(responseLeaveGroup.data.group);
                    this.groupsAvailableToJoin.add(responseLeaveGroup.data.group);

                    loadJList(scrollAvailableGroupsToJoin, listAvalilableGroupsToJoin, this.groupsAvailableToJoin);
                    loadJList(scrollGroupsToLeave, listGroupsToLeave, this.currentUser.getGroupsChat());

                    dialogShow(responseLeaveGroup.message, PLAIN_MESSAGE, 1000);
                } catch (IOException e) {
                    dialogShow("Erro inesperado ao sair do grupo", ERROR_MESSAGE, 1000);
                }
                break;

            case ALERT:
                break;
            case MESSAGE:
                System.out.println("MESSAGE");
                Message m = this.jsonHelper.<Response<Message>>fromJson(message, new TypeToken<Response<Message>>() {
                }.getType()).data;

                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));

                this.textChatMessage.setText("");
                StringBuilder text = new StringBuilder(textChat.getText());
                if (m.getFrom().equals(this.currentUser.getUsername())) {
                    text.append("\n").append(m.getFrom()).append(" -> ").append(date).append("\n");
                }else {
                    text.append("\n").append(m.getTo()).append(" -> ").append(date).append("\n");
                }
                text.append(m.getMessage());

                textChat.setText(text.toString());
                this.userMessages.add(m);

                break;
        }
    }

    private void loadJList(JScrollPane scroll, JList<String> jlist, ArrayList<String> data) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addAll(data);
        jlist.setModel(listModel);
        scroll.setViewportView(jlist);
    }

    private void setTextChatMessage() {
        String[][] arr = {{"1", "2"}, {"3", "4"}};
        String[] arr2 = {"Data", "Mensagem"};
        tableAlerts.setModel(new DefaultTableModel(arr, arr2));
        tableAlerts.getColumnModel().getColumn(0).setMinWidth(50);
        tableAlerts.getColumnModel().getColumn(1).setMinWidth(600);
        scrollTableAlerts.setViewportView(tableAlerts);
    }


    private void configTabsMenu() {

        this.buttonChooseGroups.setSelected(true);

        buttonTabMessager.addActionListener(e -> {
            buttonTabMessager.setEnabled(false);
            buttonTabFriendsGroups.setEnabled(true);
            buttonAlerts.setEnabled(true);
            buttonSettings.setEnabled(true);

            tabMessager.setVisible(true);
            tabFriendsGroups.setVisible(false);
            tabAlerts.setVisible(false);
            tabSettings.setVisible(false);
        });
        buttonTabFriendsGroups.addActionListener(e -> {
            buttonTabMessager.setEnabled(true);
            buttonTabFriendsGroups.setEnabled(false);
            buttonAlerts.setEnabled(true);
            buttonSettings.setEnabled(true);

            tabMessager.setVisible(false);
            tabFriendsGroups.setVisible(true);
            tabAlerts.setVisible(false);
            tabSettings.setVisible(false);
        });
        buttonAlerts.addActionListener(e -> {
            buttonTabMessager.setEnabled(true);
            buttonTabFriendsGroups.setEnabled(true);
            buttonAlerts.setEnabled(false);
            buttonSettings.setEnabled(true);

            tabMessager.setVisible(false);
            tabFriendsGroups.setVisible(false);
            tabAlerts.setVisible(true);
            tabSettings.setVisible(false);
        });
        buttonSettings.addActionListener(e -> {
            buttonTabMessager.setEnabled(true);
            buttonTabFriendsGroups.setEnabled(true);
            buttonAlerts.setEnabled(true);
            buttonSettings.setEnabled(false);

            tabMessager.setVisible(false);
            tabFriendsGroups.setVisible(false);
            tabAlerts.setVisible(false);
            tabSettings.setVisible(true);
        });

        buttonLogout.addActionListener(e -> {
            buttonLogout.setEnabled(false);
            this.startFrame.leaveGroups();
            this.startFrame.setVisible(true);
            this.dispose();

        });
    }

    private void dialogShow(String message, int typeMessage, int time) {
        final JLabel label = new JLabel(message);
        new Timer(0, e -> {
            ((Timer) e.getSource()).stop();
            Window win = SwingUtilities.getWindowAncestor(label);
            if (win != null)
                win.dispose();
        }) {{
            setInitialDelay(time);
        }}.start();
        showMessageDialog(null, label, "", typeMessage);
    }

    public void configFrame() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        setPreferredSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }


    private void createUIComponents() {


        // TODO: place custom component creation code here
    }
}
