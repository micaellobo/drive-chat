package Client.UI;

import Client.Client;
import Models.GeographicCoordinate;
import Models.HelpersComunication.*;
import Models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

import static javax.swing.JOptionPane.*;

public class StartFrame extends JFrame {
    private JPanel panel;
    private JPasswordField textPassLogin;
    private JTabbedPane tabbedPane;
    private JTextField textUsernameLogin;
    private JButton buttonLogin;
    private JTextField textUsernameReg;
    private JTextField textPassReg;
    private JTextField textLatitude;
    private JTextField textName;
    private JTextField textLongitude;
    private JButton buttonRegisto;
    private JTextField textCreateGrupo;
    private JScrollPane scrollGroups;
    private JList<String> listGroups;
    private JTextField textRaioNotificacao;

    private final Gson jsonHelper;

    private final Client client;
    private Menu menuFrame;

    public StartFrame(Client client) throws IOException {
        this.jsonHelper = new GsonBuilder().serializeNulls().create();
        this.client = client;
        configButtons();

        this.client.sendMessage(this.jsonHelper.toJson(new Request<>(RequestType.GET_GROUPS_REGISTRATION)));
    }

    private void configButtons() {

        buttonLogin.addActionListener(e -> {
            Login login = new Login(textUsernameLogin.getText(), String.valueOf(textPassLogin.getPassword()));
            Request<Login> request = new Request<>(RequestType.LOGIN, login);
            this.client.sendMessage(new Gson().toJson(request));
        });

        buttonRegisto.addActionListener(e -> {

            GeographicCoordinate geographicCoordinate = new GeographicCoordinate(Double.parseDouble(textLatitude.getText()), Double.parseDouble(textLongitude.getText()));
            User user = new User(textName.getText(), textUsernameReg.getText(), textPassReg.getText(), Integer.parseInt(textRaioNotificacao.getText()), geographicCoordinate);

            UserRegistration userRegistration = new UserRegistration(user);

            user.addGroups(new ArrayList<>(this.listGroups.getSelectedValuesList()));

            userRegistration.groupToCreate = textCreateGrupo.getText().isEmpty() ? null : textCreateGrupo.getText();

            Request<UserRegistration> request = new Request<>(RequestType.REGISTRATION, userRegistration);

            this.client.sendMessage(new Gson().toJson(request));
        });
    }

    public void configListGroups(ArrayList<String> groups) {


        listGroups.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listGroups.setLayoutOrientation(JList.VERTICAL);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addAll(groups);

        listGroups.setModel(listModel);

        scrollGroups.setViewportView(listGroups);

        listGroups.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (listGroups.getSelectedIndex() == -1) {
                    System.out.println("No Selection");
                } else {
                    System.out.println(listGroups.getSelectedValuesList());
                }
            }
        });
    }

    private void cleanVariables() {
        //REGISTO
        textName.setText("");
        textUsernameReg.setText("");
        textPassReg.setText("");
        textLatitude.setText("");
        textCreateGrupo.setText("");
        textRaioNotificacao.setText("");
        textLongitude.setText("");

        //LOGIN
        textPassLogin.setText("");
        textUsernameLogin.setText("");

    }


    public void configFrame() {
        setContentPane(panel);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        setPreferredSize(new Dimension(600, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
    }


    @SuppressWarnings("EnhancedSwitchMigration")
    public synchronized void processResponse(String message) {
        Response<Object> response = jsonHelper.<Response<Object>>fromJson(message, Response.class);

        if (response.status != StatusResponse.OK) {
            showMessageDialog(new JDialog(), response.message, "", ERROR_MESSAGE);
            return;
        }

        switch (response.type) {
            case LOGIN:
                UserLogin userLogin = this.jsonHelper.<Response<UserLogin>>fromJson(message, new TypeToken<Response<UserLogin>>() {
                }.getType()).data;

                try {
                    this.client.joinGroups(userLogin.listIpsToJoin);
                } catch (IOException e) {
                    dialogShow("Erro inesperado ao juntar aos grupos", ERROR_MESSAGE, 1000);
                    return;
                }

                this.menuFrame = new Menu(this, this.client, userLogin.user, userLogin.usersNotFriend, userLogin.groupsToAvalilableJoin, userLogin.messagesUsersToUsers);
                this.menuFrame.configFrame();

                cleanVariables();
                this.setVisible(false);
                break;
            case REGISTRATION:
                showMessageDialog(new JDialog(), response.message, "", PLAIN_MESSAGE);
                tabbedPane.setSelectedIndex(0);
                cleanVariables();
                break;
            case GET_GROUPS_REGISTRATION:
                configListGroups(this.jsonHelper.<Response<ArrayList<String>>>fromJson(message, Response.class).data);
                break;
            case FEEDBACK_SIMPLE_RESPONSE:
                showMessageDialog(new JDialog(), response.message, "", PLAIN_MESSAGE);
                break;
            case ALERT_USER_CIVIL_PROTECTION:
                showMessageDialog(null, response.message, "", WARNING_MESSAGE);
                break;
            case ALERT_APP_CIVIL_PROTECTION:
                System.out.println(response.message);
                break;
            default:
                this.menuFrame.processMessage(message, response.type);
                break;
        }

    }

    public void leaveGroups() {
        try {
            this.client.leaveGroups();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
