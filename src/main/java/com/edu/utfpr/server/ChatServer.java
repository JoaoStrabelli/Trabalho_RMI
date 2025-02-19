package com.edu.utfpr.server;

import com.edu.utfpr.client.IChatClient;
import com.edu.utfpr.core.entities.Chat;
import com.edu.utfpr.core.entities.Messages;
import com.edu.utfpr.core.entities.User;
import com.edu.utfpr.core.exceptions.InvalidUserOrPasswordException;
import com.edu.utfpr.core.exceptions.UserAlreadyRegisteredException;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ChatServer extends UnicastRemoteObject implements IChatServer {
    private final Vector<User> users;
    private final Map<String, String> userCredentials = new HashMap<>();
    private final List<Chat> groups = new ArrayList<>();
    private final List<Chat> privateGroups = new ArrayList<>();

    public ChatServer() throws RemoteException {
        super();
        users = new Vector<>(10, 1);
    }

    public static void main(String[] args) {
        startRMIRegistry();
        String hostName = "localhost";
        String serviceName = "GroupChatService";

        if (args.length == 2) {
            hostName = args[0];
            serviceName = args[1];
        }

        try {
            ChatServer server = new ChatServer();
            Naming.rebind("rmi://" + hostName + "/" + serviceName, server);
            System.out.println("Servidor RMI WhatsUI executando...");
        } catch (Exception e) {
            System.out.println("Erro na inicialização do servidor");
        }
    }

    public static void startRMIRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            System.out.println("Servidor RMI online");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createChatGroup(String chatName, String creatorUsername, boolean exitAdminMethodRandom)
            throws RemoteException {
        User creator = users.stream().filter(user -> user.getName().equals(creatorUsername)).findFirst().orElse(null);

        UUID guid = UUID.randomUUID();

        Chat chat = new Chat(
                guid,
                chatName,
                creator,
                creator,
                true,
                exitAdminMethodRandom);

        groups.add(chat);
        updatePublicGroupList();
        updateMyChatsList(creator);
    }

    @Override
    public void createInviteGroup(String userName, Chat chatParam) throws RemoteException {
        User userParam = users.stream().filter(user -> user.getName().equals(userName)).findFirst().orElse(null);
        Chat chat = groups.stream().filter(chats -> chats.getName().equals(chatParam.name)).findFirst().orElse(null);
        Boolean hasInvite = chat.pendingUsers.contains(userParam);
        Boolean isIn = chat.members.contains(userParam);
        if (!hasInvite && !isIn) {
            chat.pendingUsers.add(userParam);
            Integer indexChat = groups.indexOf(chat);
            groups.set(indexChat, chat);
            updatePublicGroupList();
        }
        System.err.println("Lista de ordenação" + chat.pendingUsers);
    }

    @Override
    public void acceptInviteGroup(String userName, Chat chatParam) throws RemoteException {

        Chat chat = groups.stream().filter(chats -> chats.getName().equals(chatParam.name)).findFirst().orElse(null);
        User userParam = chat.pendingUsers.stream().filter(user -> user.getName().equals(userName)).findFirst()
                .orElse(null);
        int indexUser = chat.pendingUsers.indexOf(userParam);

        if (userParam != null) {
            chat.pendingUsers.remove(indexUser);
            chat.members.add(userParam);

            Integer indexChat = groups.indexOf(chat);
            groups.set(indexChat, chat);

            updatePublicGroupList();
            updateMyChatsList(userParam);
        }
    }

    @Override
    public void removeUserGroup(String userName, Chat chatParam) throws RemoteException {

        Chat chat = groups.stream().filter(chats -> chats.getName().equals(chatParam.name)).findFirst().orElse(null);
        User userParam = chat.members.stream().filter(user -> user.getName().equals(userName)).findFirst().orElse(null);
        int indexUser = chat.members.indexOf(userParam);

        if (userParam != null) {
            chat.members.remove(indexUser);

            Integer indexChat = groups.indexOf(chat);
            groups.set(indexChat, chat);

            updatePublicGroupList();
            updateMyChatsList(userParam);
        }
    }

    @Override
    public void createPrivateChat(String user1, String user2) throws RemoteException {
        UUID guid = UUID.randomUUID();

        User currentUser = users.stream().filter(user -> user.getName().equals(user1)).findFirst().orElse(null);

        User destinationUser = users.stream().filter(user -> user.getName().equals(user2)).findFirst().orElse(null);

        Chat chat = new Chat(
                guid,
                currentUser.getName() + " e " + destinationUser.getName(),
                currentUser,
                currentUser,
                false,
                false,
                List.of(currentUser, destinationUser),
                new ArrayList<>(),
                new ArrayList<>());

        privateGroups.add(chat);

        updateMyChatsList(currentUser);
        updateMyChatsList(destinationUser);
    }

    @Override
    public void leaveGroup(String UserName, Chat chatParam) throws RemoteException {

        User currentUser = users.stream().filter(user -> user.getName().equals(UserName)).findFirst().orElse(null);
        Chat chat = groups.stream().filter(chats -> chats.getName().equals(chatParam.name)).findFirst().orElse(null);
        Messages newMessage = null;
        Messages newMessage2 = null;

        boolean isAdmin = false;
        if (chat.admin == currentUser) {
            isAdmin = true;
        }

        if (!isAdmin) {
            int indexUser = chat.members.indexOf(currentUser);
            chat.members.remove(indexUser);
            newMessage = new Messages(currentUser, "Saiu do grupo!");
            chat.messages.add(newMessage);
            updatePublicGroupList();
            updateMyChatsList(currentUser);
        } else {
            if (chat.exitAdminMethodRandom) {
                if (chat.members.size() > 1) {
                    int indexUser = chat.members.indexOf(currentUser);
                    chat.members.remove(indexUser);
                    chat.admin = chat.members.get(0);
                    newMessage = new Messages(currentUser, "Saiu do grupo!");
                    chat.messages.add(newMessage);
                    newMessage2 = new Messages(chat.admin, chat.admin.name + " é o novo administrador do grupo!");
                    chat.messages.add(newMessage2);
                    updatePublicGroupList();
                    updateMyChatsList(currentUser);
                } else {
                    groups.remove(chat);
                    updatePublicGroupList();
                    updateMyChatsList(currentUser);
                }
            } else {
                groups.remove(chat);
                updatePublicGroupList();
                updateMyChatsList(currentUser);
            }
        }

        for (User groupUser : chat.members) {
            if (groupUser.getClient() != null) {
                if (groupUser.getClient().getCurrentChatId().equals(chat.chatId) && newMessage != null) {
                    groupUser.getClient().receiveMessage(newMessage);
                }
                if (groupUser.getClient().getCurrentChatId().equals(chat.chatId) && newMessage2 != null) {
                    groupUser.getClient().receiveMessage(newMessage2);
                }
            }
        }
    }

    @Override
    public void login(String userName, String password, String hostName, String clientServiceName)
            throws RemoteException, MalformedURLException, NotBoundException, InvalidUserOrPasswordException {
        if (userCredentials.containsKey(userName) && userCredentials.get(userName).equals(password)) {
            IChatClient nextClient = (IChatClient) Naming.lookup("rmi://" + hostName + "/" + clientServiceName);
            users.addElement(new User(userName, nextClient));
            updateUserList();
        } else {
            throw new InvalidUserOrPasswordException();
        }
    }

    @Override
    public void registerUser(String userName, String password)
            throws RemoteException, UserAlreadyRegisteredException {
        if (userCredentials.containsKey(userName)) {
            throw new UserAlreadyRegisteredException();
        }

        userCredentials.put(userName, password);
    }

    private void updateUserList() {
        List<User> currentUsers = getUserList();
        currentUsers.removeIf(user -> user.getClient() == null);
        for (User c : users) {
            try {
                if (c.getClient() != null) {
                    List<User> candidates = new ArrayList<>(currentUsers);
                    candidates.remove(c);
                    c.getClient().updateUserList(candidates);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePublicGroupList() {
        List<User> currentUsers = getUserList();
        currentUsers.removeIf(user -> user.getClient() == null);
        for (User c : users) {
            try {
                if (c.getClient() != null) {
                    c.getClient().updatePublicGroupList(groups);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateMyChatsList(User currentUser) {
        List<Chat> myChats;
        try {
            myChats = getMyChats(currentUser.getName());
            currentUser.getClient().updateChatList(myChats);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private List<User> getUserList() {
        List<User> allUsers = new ArrayList<>(users.size());
        allUsers.addAll(users);
        return allUsers;
    }

    @Override
    public List<User> getCurrentUsers() throws RemoteException {
        return getUserList();
    }

    @Override
    public List<Chat> getAllGroups() throws RemoteException {
        return groups;
    }

    @Override
    public List<Chat> getMyChats(String userName) throws RemoteException {
        List<Chat> myChats = new ArrayList<>();

        for (Chat chat : groups) {
            boolean userFound = chat.members.stream().anyMatch(member -> member.name.equals(userName));
            if (userFound) {
                myChats.add(chat);
            }
        }

        for (Chat chat : privateGroups) {
            boolean userFound = chat.members.stream().anyMatch(member -> member.name.equals(userName));
            if (userFound) {
                myChats.add(chat);
            }
        }

        return myChats;
    }

    @Override
    public void sendMessage(String user, Chat chatParam, String message, JPanel inputPanel) throws RemoteException {
        User userFound = users.stream().filter(u -> u.name.equals(user)).findFirst().orElse(null);
        Chat chat;
        chat = groups.stream().filter(chats -> chats.getName().equals(chatParam.name)).findFirst()
                .orElse(privateGroups.stream().filter(chats -> chats.getName().equals(chatParam.name)).findFirst()
                        .orElse(null));

        Messages newMessage;

        if (chatParam.isGroup) {
            switch (message) {
                case "/members":
                    JDialog dialog = new JDialog((Frame) null, "Criar novo grupo", true);
                    dialog.setTitle("Lista de pessoas - " + chat.name);
                    dialog.setAlwaysOnTop(true);
                    dialog.setSize(200, 400);
                    JPanel panel = new JPanel();
                    panel.setLayout(new GridLayout());
                    for (User member : chat.members) {
                        JLabel memberName = new JLabel(member.equals(chat.admin) ? "Admin: " : "Usuário: ");
                        memberName.setText(memberName.getText() + member.name + "; ");
                        panel.add(memberName);
                    }
                    dialog.add(panel, BorderLayout.NORTH);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                    newMessage = null;
                    break;

                case "/invites":
                    if (userFound.equals(chat.admin)) {
                        JDialog dialogInvites = new JDialog((Frame) null, "Criar novo grupo", true);
                        dialogInvites.setTitle("Lista de ordenação - " + chat.name);
                        dialogInvites.setAlwaysOnTop(true);
                        dialogInvites.setSize(200, 400);
                        JPanel panelInvites = new JPanel();
                        panelInvites.setLayout(new GridLayout());
                        GridBagConstraints constraints = new GridBagConstraints();
                        constraints.fill = GridBagConstraints.HORIZONTAL;
                        constraints.gridwidth = 2;
                        constraints.gridx = 0;
                        constraints.gridy = 0;
                        for (User member : chat.pendingUsers) {
                            JLabel memberName = new JLabel("Nome do usuário: " + member.name + "; ");
                            panelInvites.add(memberName, constraints);
                            constraints.gridy++;
                        }
                        dialogInvites.add(panelInvites, BorderLayout.NORTH);
                        dialogInvites.setLocationRelativeTo(null);
                        dialogInvites.setVisible(true);
                        newMessage = null;
                    } else {
                        newMessage = new Messages(userFound, message);
                        chat.messages.add(newMessage);
                    }
                    break;

                case "/exit":
                    JDialog dialogExit = new JDialog((Frame) null, true);
                    dialogExit.setTitle("Sair do grupo " + chat.name + " ?");
                    dialogExit.setAlwaysOnTop(true);
                    dialogExit.setSize(300, 300);
                    JPanel panelExit = new JPanel();
                    panelExit.setLayout(new GridLayout());
                    JButton confirmButton = new JButton("Confirmar");
                    confirmButton.addActionListener(e -> {
                        try {
                            leaveGroup(userFound.name, chat);
                            dialogExit.dispose();
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                    });

                    JButton cancelButton = new JButton("Cancelar");
                    cancelButton.addActionListener(e -> {
                        dialogExit.dispose();
                    });

                    panelExit.add(confirmButton);
                    panelExit.add(cancelButton);
                    dialogExit.add(panelExit, BorderLayout.NORTH);
                    dialogExit.setLocationRelativeTo(null);
                    dialogExit.setVisible(true);
                    newMessage = null;
                    break;

                default:
                    String isCommand = message.substring(0, 1);
                    if (isCommand.equals("/")) {
                        message = message + " ";
                        try {
                            String command = message.substring(0, message.indexOf(" "));
                            System.err.println("Comand " + command);
                            String username = message.substring(message.indexOf(" "));
                            if (command.trim().equals("/accept")) {
                                if (userFound.equals(chat.admin)) {
                                    System.err.println("User " + username);
                                    User userToAccept = chat.pendingUsers.stream()
                                            .filter(u -> u.name.equals(username.trim())).findFirst().orElse(null);
                                    if (userToAccept != null) {
                                        this.acceptInviteGroup(userToAccept.name, chat);
                                        newMessage = new Messages(userFound, "Usuário " + userToAccept.name
                                                + " Foi aceito ao grupo pelo administrador.");
                                        chat.messages.add(newMessage);
                                    } else {
                                        JOptionPane.showMessageDialog(inputPanel, "Usuário inválido!", "Erro",
                                                JOptionPane.ERROR_MESSAGE);
                                        newMessage = new Messages(userFound, message);
                                        chat.messages.add(newMessage);
                                    }
                                } else {
                                    newMessage = new Messages(userFound, message);
                                    chat.messages.add(newMessage);
                                }
                            } else if (command.trim().equals("/ban")) {
                                if (userFound.equals(chat.admin)) {
                                    System.err.println("Usuário " + username);
                                    User userToRemove = chat.members.stream()
                                            .filter(u -> u.name.equals(username.trim())).findFirst().orElse(null);
                                    if (userToRemove != null && userToRemove != userFound) {
                                        this.removeUserGroup(userToRemove.name, chat);
                                        newMessage = new Messages(userFound, "Usuário " + userToRemove.name
                                                + " Foi removido do grupo pelo administrador");
                                        chat.messages.add(newMessage);
                                    } else {
                                        JOptionPane.showMessageDialog(inputPanel, "Erro ao remover usuário!", "Erro",
                                                JOptionPane.ERROR_MESSAGE);
                                        newMessage = new Messages(userFound, message);
                                        chat.messages.add(newMessage);
                                    }
                                } else {
                                    newMessage = new Messages(userFound, message);
                                    chat.messages.add(newMessage);
                                }
                            } else {
                                newMessage = new Messages(userFound, message);
                                chat.messages.add(newMessage);
                            }
                        } catch (RemoteException e) {
                            newMessage = new Messages(userFound, message);
                            chat.messages.add(newMessage);
                        }
                    } else {
                        newMessage = new Messages(userFound, message);
                        chat.messages.add(newMessage);
                    }
                    break;
            }

            for (User groupUser : chat.members) {
                if (groupUser.getClient() != null) {
                    if (groupUser.getClient().getCurrentChatId().equals(chat.chatId) && newMessage != null) {
                        groupUser.getClient().receiveMessage(newMessage);
                    }
                }
            }

        } else {
            Chat privateGroup = privateGroups.stream()
                    .filter(other -> chatParam.getChatId().equals(other.getChatId()))
                    .findFirst()
                    .orElse(null);
            newMessage = new Messages(userFound, message);

            if (privateGroup != null) {
                privateGroup.messages.add(newMessage);
            }

            for (User groupUser : privateGroup.members) {
                if (groupUser.getClient() != null && groupUser.getClient().getCurrentChatId() != null) {
                    if (groupUser.getClient().getCurrentChatId().equals(privateGroup.chatId) && newMessage != null) {
                        groupUser.getClient().receiveMessage(newMessage);
                    }
                }
            }
        }
    }

}
