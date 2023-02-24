package Server;

import Models.*;
import Models.HelpersComunication.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Protocol {

    private final static String NAME_GROUP_ALERT_RADIUS = "_Radius";
    private final static int RADIUS_ALERT = 1;
    private final JsonFileHelper jsonFileHelper;
    private final Gson jsonHelper;
    private final ArrayListSync<ClientHandler> clientHandlers;
    private final ClientHandler clientHandler;
    protected Server server;

    public Protocol(ClientHandler clientHandler, ArrayListSync<ClientHandler> clientHandlers, Server server) throws IOException {
        this.clientHandler = clientHandler;
        this.server = server;
        this.clientHandlers = clientHandlers;
        this.jsonHelper = new Gson();
        this.jsonFileHelper = new JsonFileHelper("files/");
    }

    @SuppressWarnings({"EnhancedSwitchMigration"})
    protected synchronized String processMessage(String requestMessage) {
        RequestType requestType;
        ArrayList<User> currentUsers;
        ArrayList<Group> currentGroups;

        try {
            requestType = this.jsonHelper.fromJson(requestMessage, Request.class).type;
            currentUsers = this.jsonFileHelper.getUsers();
            currentGroups = this.jsonFileHelper.getGroups();
        } catch (IOException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro inesperado durante o registo!"));
        }

        switch (requestType) {
            case LOGIN:
                return loginHandler(requestMessage, currentUsers, currentGroups);
            case REGISTRATION:
                return registrationHandler(requestMessage, currentUsers, currentGroups);
            case MESSAGE:
                return messagesHandler(requestMessage, currentUsers, currentGroups);
            case SENT_FRIEND_REQUEST:
                return friendRequestHandler(requestMessage, currentUsers);
            case ACCEPT_FRIEND:
                return acceptFriendRequestHandler(requestMessage, currentUsers);
            case JOIN_GROUP:
                return joinGroupHandler(requestMessage, currentUsers, currentGroups);
            case LEAVE_GROUP:
                return leaveGroupHandler(requestMessage, currentUsers, currentGroups);
            case CREATE_GROUP:
                return createGroupHandler(requestMessage, currentUsers, currentGroups);
            case ALERT:
                return userAlertMainGroup(requestMessage, currentUsers, currentGroups);
            case GET_GROUPS_REGISTRATION:
                return getGroupsRegistrationHandler(currentGroups);
            default:
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Pedido nao suportado!"));
        }
    }


    private String loginHandler(String requestMessage, ArrayList<User> currentUsers, ArrayList<Group> currentGroups) {
        try {
            Login login = this.jsonHelper.<Request<Login>>fromJson(requestMessage, new TypeToken<Request<Login>>() {
            }.getType()).data;

            User userDb = currentUsers.stream().filter(user -> user.getPassword().equals(login.password) && user.getUsername().equals(login.username)).findFirst().orElse(null);

            if (userDb == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Credenciais Invalidas!"));

            ArrayList<String> usersNotFriend = currentUsers.stream().map(User::getUsername).collect(Collectors.toCollection(ArrayList::new));
            usersNotFriend.remove(userDb.getUsername());
            usersNotFriend.removeAll(userDb.getFriends());
            usersNotFriend.removeAll(userDb.getFriendRequestsReceived());
            usersNotFriend.removeAll(userDb.getFriendRequestsSent());

            //todos os grupos
            ArrayList<String> groupsAvailableToJoin = currentGroups.stream().
                    filter(group -> group.getGroupType() == GroupType.CHAT)
                    .map(Group::getName)
                    .collect(Collectors.toCollection(ArrayList::new));

            groupsAvailableToJoin.removeAll(userDb.getGroupsChat());

            //cria lista de ips
            ArrayList<String> ipsToJoin = currentGroups.stream()
                    .filter(group -> (userDb.getGroupsChat().contains(group.getName()) && group.getGroupType() == GroupType.CHAT)
                            || userDb.getGroupsAlert().contains(group.getName()) && group.getGroupType() == GroupType.ALERTS)
                    .map(group -> group.getIp().getHostAddress())
                    .collect(Collectors.toCollection(ArrayList::new));

            //join main group
            ipsToJoin.add(0, Server.MAIN_GROUP_IP);

            try {
                ArrayList<Message> messagesUser = this.jsonFileHelper.getMessagesUser(userDb.getUsername());
                UserLogin userLogin = new UserLogin(userDb, groupsAvailableToJoin, usersNotFriend, ipsToJoin, messagesUser);

                //Definir o usernamme no ClientHandler
                this.clientHandler.userUsername = userDb.getUsername();

                //Send alert transito se for o caso
                determineDensityUsers(currentUsers, RADIUS_ALERT, userDb.getGeoCordi());

                return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.LOGIN, "Login com Sucesso", userLogin));
            } catch (IOException e) {
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro ao carregar o seu historico!"));
            }
        } catch (ClassCastException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Pedido login invalido!"));
        }
    }

    private String registrationHandler(String requestMessage, ArrayList<User> currentUsers, ArrayList<Group> currentGroups) {
        try {
            UserRegistration userRegistration = this.jsonHelper.<Request<UserRegistration>>fromJson(requestMessage, new TypeToken<Request<UserRegistration>>() {
            }.getType()).data;

            boolean userExists = currentUsers.contains(userRegistration.user);

            //Caso tenho criado um grupo durante o registo
            if (userRegistration.groupToCreate != null) {
                boolean groupExists = currentGroups.stream().anyMatch(group -> group.getName().equals(userRegistration.groupToCreate));

                if (userExists && groupExists) {
                    return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, RequestType.REGISTRATION, "O username \"" + userRegistration.user.getUsername() + "\" e o grupo \"" + userRegistration.groupToCreate + "\" já existem!"));
                } else if (userExists) {
                    return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, RequestType.REGISTRATION, "O username \"" + userRegistration.user.getUsername() + "\" já está em uso!"));
                } else if (groupExists) {
                    return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, RequestType.REGISTRATION, "O grupo \"" + userRegistration.groupToCreate + "\" já existe!"));
                } else {

                    //Criar grupo alertas
                    Group personalGroupAlert = new Group(userRegistration.user.getUsername() + NAME_GROUP_ALERT_RADIUS, GroupType.ALERTS);

                    //definir o grupo principal
                    userRegistration.user.addGroupAlert(personalGroupAlert.getName());
                    userRegistration.user.setMainGroup(personalGroupAlert.getName());

                    //associar o grupo que o user criou
                    userRegistration.user.addGroupChat(userRegistration.groupToCreate);

                    setUserAlertGroups(userRegistration.user, currentUsers);

                    currentUsers.add(userRegistration.user);

                    jsonFileHelper.addGroup(personalGroupAlert);
                    jsonFileHelper.addGroup(new Group(userRegistration.groupToCreate, GroupType.CHAT));
                    jsonFileHelper.updateUsers(currentUsers);
                    return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.REGISTRATION, "Registo efetuado com sucesso"));
                }
            } else {
                if (userExists) {
                    return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, RequestType.REGISTRATION, "O username \"" + userRegistration.user.getUsername() + "\" já está em uso!"));
                } else {
                    //Criar grupo alertas
                    Group personalGroupAlert = new Group(userRegistration.user.getUsername() + NAME_GROUP_ALERT_RADIUS, GroupType.ALERTS);
                    userRegistration.user.addGroupAlert(personalGroupAlert.getName());
                    userRegistration.user.setMainGroup(personalGroupAlert.getName());

                    setUserAlertGroups(userRegistration.user, currentUsers);

                    currentUsers.add(userRegistration.user);

                    jsonFileHelper.addGroup(personalGroupAlert);
                    jsonFileHelper.updateUsers(currentUsers);
                    return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.REGISTRATION, "Registo efetuado com sucesso"));
                }
            }
        } catch (ClassCastException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, RequestType.REGISTRATION, "Pedido registo invalido!"));
        } catch (IOException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.REGISTRATION, "Erro ao inesperado ao guardar alterações!"));
        }
    }

    private String messagesHandler(String requestMessage, ArrayList<User> currentUsers, ArrayList<Group> currentGroups) {
        Message message = this.jsonHelper.<Request<Message>>fromJson(requestMessage, new TypeToken<Request<Message>>() {
        }.getType()).data;

        TypeMessage type = message.getType();

        User userFrom = currentUsers.stream().filter(user -> user.getUsername().equals(message.getFrom())).findFirst().orElse(null);

        if (userFrom == null) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Suas credencias estão erradas!"));
        }

        //Caso seja mensagem de user para user
        if (type == TypeMessage.USER_TO_USER) {
            User userTo = currentUsers.stream().filter(user -> user.getUsername().equals(message.getTo())).findFirst().orElse(null);

            if (userTo == null) {
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Utilizador \"" + message.getTo() + "\" não existe!"));
            }
            try {
                this.jsonFileHelper.addMessage(message);
                return sendMessageToUser(message);
            } catch (IOException e) {
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro ao guardar a mensagem"));
            }
        }
        //Caso seja mensagem de user para grupo
        if (type == TypeMessage.USER_TO_GROUP) {
            Group groupTo = currentGroups.stream().filter(group -> group.getName().equals(message.getTo())).findFirst().orElse(null);

            if (groupTo == null) {
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Grupo \"" + message.getTo() + "\" não existe!"));
            }
            boolean isUserInGroup = userFrom.getGroupsChat().contains(groupTo.getName());

            if (!isUserInGroup) {
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Não pertence ao grupo \"" + groupTo.getName() + "\"!"));
            }
            return sendMessageToGroup(message, groupTo);
        }
        return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Formato mensagem invalido!"));
    }

    private String sendMessageToUser(Message messageToSend) {

        ClientHandler targetClientHandler = getClientHandlerOtherUser(messageToSend.getTo());

        if (targetClientHandler == null)
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "O \"" + messageToSend.getTo() + "\" não se encontra online!"));

        String stringMessage = this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.MESSAGE, messageToSend));
        targetClientHandler.sendMessage(stringMessage);

        //Send feedback for the User who sent the message (FROM)
        return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.MESSAGE, messageToSend));
    }

    private String sendMessageToGroup(Message message, Group group) {
        String stringMessage = this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.MESSAGE, message));

        this.server.multicastMessage(stringMessage, group.getIp().getHostAddress());

        return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.FEEDBACK_SIMPLE_RESPONSE, "Mensagem enviada com sucesso!"));
    }

    private String createGroupHandler(String requestMessage, ArrayList<User> currentUsers, ArrayList<Group> currentGroups) {
        JoinLeaveCreateGroup createGroup = this.jsonHelper.<Request<JoinLeaveCreateGroup>>fromJson(requestMessage, new TypeToken<Request<JoinLeaveCreateGroup>>() {
        }.getType()).data;

        User user = currentUsers.stream().filter(usr -> usr.getUsername().equals(createGroup.user)).findFirst().orElse(null);

        boolean groupExists = currentGroups.stream().anyMatch(group -> group.getName().equals(createGroup.group));

        if (groupExists)
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Ja exist eum grupo com esse nome"));

        if (user == null)
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Suas credencias estão erradas!"));

        try {
            Group group = new Group(createGroup.group, GroupType.CHAT);
            if (!currentGroups.add(group))
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Não exitem ips disponiveis"));

            user.addGroupChat(group.getName());
            this.jsonFileHelper.addGroup(group);
            this.jsonFileHelper.updateUser(user);
            createGroup.ip = group.getIp().getHostAddress();
            return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.CREATE_GROUP, "Grupo criado", createGroup));
        } catch (NoSuchElementException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Não exitem ips disponiveis"));
        } catch (IOException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro ao guardar alteracoes"));
        }
    }

    private String friendRequestHandler(String requestMessage, ArrayList<User> currentUsers) {
        try {
            FriendsRequestHelper friendRequest = this.jsonHelper.<Request<FriendsRequestHelper>>fromJson(requestMessage, new TypeToken<Request<FriendsRequestHelper>>() {
            }.getType()).data;

            User userSender = currentUsers.stream().filter(user -> user.getUsername().equals(friendRequest.userSender)).findFirst().orElse(null);

            User userToRequest = currentUsers.stream().filter(user -> user.getUsername().equals(friendRequest.userToRequest)).findFirst().orElse(null);

            if (userSender == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Suas credencias estão erradas!"));

            if (userToRequest == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Utilizador \"" + friendRequest.userToRequest + "\" não existe!"));

            userToRequest.addFriendRequestReceived(userSender.getUsername());
            userSender.addFriendRequestSent(userToRequest.getUsername());

            try {
                this.jsonFileHelper.updateUsers(currentUsers);

                //Enviar para o outro utilizador
                ClientHandler targetClientHandler = getClientHandlerOtherUser(userToRequest.getUsername());
                if (targetClientHandler != null) {
                    targetClientHandler.sendMessage(this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.RECEIVE_FRIEND_REQUEST, "Recebeu pedido amizade de \"" + userSender.getUsername() + "\"", friendRequest)));
                }

                return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.FEEDBACK_REQUEST_FRIEND_REQUEST, "Pedido amizade enviado", friendRequest));
            } catch (IOException e) {
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro inesperado ao guardar alterações!"));
            }
        } catch (ClassCastException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Pedido invalido!"));
        }
    }

    private String acceptFriendRequestHandler(String requestMessage, ArrayList<User> currentUsers) {
        try {
            FriendsRequestHelper friendRequest = this.jsonHelper.<Request<FriendsRequestHelper>>fromJson(requestMessage, new TypeToken<Request<FriendsRequestHelper>>() {
            }.getType()).data;

            User userSender = currentUsers.stream().filter(user -> user.getUsername().equals(friendRequest.userSender)).findFirst().orElse(null);

            User userToRequest = currentUsers.stream().filter(user -> user.getUsername().equals(friendRequest.userToRequest)).findFirst().orElse(null);

            if (userSender == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Suas credencias estão erradas!"));

            if (userToRequest == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Utilizador \"" + friendRequest.userToRequest + "\" não existe!"));

            userSender.addFriend(userToRequest.getUsername());
            userToRequest.addFriend(userSender.getUsername());

            try {
                this.jsonFileHelper.updateUsers(currentUsers);
                //Enviar para o outro utilizador
                ClientHandler targetClientHandler = getClientHandlerOtherUser(userToRequest.getUsername());
                if (targetClientHandler != null) {
                    targetClientHandler.sendMessage(this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.ACCEPT_FRIEND, "\"" + userSender.getUsername() + "\" aceitou o seu pedido de amizade", friendRequest)));
                }

                return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.FEEDBACK_SENT_FRIEND_REQUEST, "Pedido amizade aceite", friendRequest));
            } catch (IOException e) {
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro inesperado ao guardar alterações!"));
            }
        } catch (ClassCastException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Pedido invalido!"));
        }
    }

    private String userAlertMainGroup(String requestMessage, ArrayList<User> currentUsers, ArrayList<Group> currentGroups) {
        Alert alert = this.jsonHelper.<Request<Alert>>fromJson(requestMessage, new TypeToken<Request<Alert>>() {
        }.getType()).data;

        User userDb = currentUsers.stream().filter(user -> user.getUsername().equals(alert.user)).findFirst().orElse(null);
        Group group = currentGroups.stream().filter(grp -> grp.getName().equals(userDb.getMainGroup())).findFirst().orElse(null);
        this.server.multicastMessage(this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.ALERT_USER_CIVIL_PROTECTION, alert.message)), group.getIp().getHostAddress());
        return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.FEEDBACK_SIMPLE_RESPONSE, "Mensagem enviada com sucesso!"));
    }


    private String joinGroupHandler(String requestMessage, ArrayList<User> currentUsers, ArrayList<Group> currentGroups) {
        try {
            JoinLeaveCreateGroup joinGroup = this.jsonHelper.<Request<JoinLeaveCreateGroup>>fromJson(requestMessage, new TypeToken<Request<JoinLeaveCreateGroup>>() {
            }.getType()).data;

            User user = currentUsers.stream().filter(usr -> usr.getUsername().equals(joinGroup.user)).findFirst().orElse(null);
            Group group = currentGroups.stream().filter(grp -> grp.getName().equals(joinGroup.group) && grp.getGroupType() == GroupType.CHAT).findFirst().orElse(null);

            if (user == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Credenciais Invalidas!"));

            if (group == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "O grupo \"" + joinGroup.group + "\" não existe"));

            if (!user.addGroupChat(joinGroup.group))
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Já pertence ao grupo \"" + joinGroup.group + "\""));

            if (!this.jsonFileHelper.updateUser(user))
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro ao guardar alterações \"" + joinGroup.group + "\""));

            joinGroup.ip = group.getIp().getHostAddress();

            return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.JOIN_GROUP, "Entrou no grupo \"" + joinGroup.group + "\"", joinGroup));
        } catch (
                ClassCastException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Pedido invalido!"));
        } catch (IOException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro ao inesperado ao guardar alterações!"));
        }

    }

    private String leaveGroupHandler(String requestMessage, ArrayList<User> currentUsers, ArrayList<Group> currentGroups) {
        try {
            JoinLeaveCreateGroup joinLeave = this.jsonHelper.<Request<JoinLeaveCreateGroup>>fromJson(requestMessage, new TypeToken<Request<JoinLeaveCreateGroup>>() {
            }.getType()).data;

            User user = currentUsers.stream().filter(usr -> usr.getUsername().equals(joinLeave.user)).findFirst().orElse(null);
            Group group = currentGroups.stream().filter(grp -> grp.getName().equals(joinLeave.group) && grp.getGroupType() == GroupType.CHAT).findFirst().orElse(null);

            if (user == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Credenciais Invalidas!"));

            if (group == null)
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "O grupo \"" + joinLeave.group + "\" não existe"));

            if (!user.removeGroupChat(joinLeave.group))
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Não faz parte do grupo \"" + joinLeave.group + "\""));

            if (!this.jsonFileHelper.updateUser(user))
                return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro ao guardar alterações \"" + joinLeave.group + "\""));

            JoinLeaveCreateGroup leaveGroupInfo = new JoinLeaveCreateGroup(user.getUsername(), group.getName(), group.getIp().getHostAddress());

            return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.LEAVE_GROUP, "Deixou de fazer parte do grupo \"" + joinLeave.group + "\"", leaveGroupInfo));
        } catch (
                ClassCastException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Pedido invalido!"));
        } catch (
                IOException e) {
            return this.jsonHelper.toJson(new Response<>(StatusResponse.NOOK, "Erro ao inesperado ao guardar alterações!"));
        }
    }

    private String getGroupsRegistrationHandler(ArrayList<Group> currentGroups) {
        ArrayList<String> groups = currentGroups.stream().filter(group -> group.getGroupType() == GroupType.CHAT).map(Group::getName).collect(Collectors.toCollection(ArrayList::new));
        return this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.GET_GROUPS_REGISTRATION, groups));
    }

    private ClientHandler getClientHandlerOtherUser(String username) {
        return this.clientHandlers.stream().filter(clientHandler -> clientHandler.userUsername.equals(username)).findFirst().orElse(null);
    }

    private void setUserAlertGroups(User user, ArrayList<User> currentUsers) {

        ArrayList<User> usersInRadius = currentUsers.stream()
                .filter(usr -> user.isInsideRadius(usr.getGeoCordi(), user.getUsername()))
                .collect(Collectors.toCollection(ArrayList::new));

        //Entrar no grupo de alert do utilizadores que estao no raio
        usersInRadius.forEach(usr -> user.addGroupAlert(usr.getMainGroup()));

        //Percorrer todos os outros e ver se o novo user esta no seu raio
        currentUsers.forEach(usr -> {
            if (usr.isInsideRadius(user.getGeoCordi(), user.getUsername())) {
                usr.addGroupAlert(user.getMainGroup());
            }
        });
    }

    private void determineDensityUsers(ArrayList<User> currentUser, int radius, GeographicCoordinate geographicCoordinate) {

        long count = currentUser.stream().filter(usr -> usr.getGeoCordi().distanceInKmBetweenEarthCoordinates(geographicCoordinate) < radius).collect(Collectors.toCollection(ArrayList::new)).size();
        if (count > 3) {
            this.server.multicastMessage(this.jsonHelper.toJson(new Response<>(StatusResponse.OK, RequestType.FEEDBACK_SIMPLE_RESPONSE, "Possivel transito em " + geographicCoordinate)), Server.MAIN_GROUP_IP);
        }
    }

}
