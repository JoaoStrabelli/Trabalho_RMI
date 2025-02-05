package com.edu.utfpr.client.components;

import com.edu.utfpr.client.ChatClient;
import com.edu.utfpr.core.entities.Chat;
import com.edu.utfpr.core.entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ChatTabsComponent extends JPanel {
    private final ChatClient chatClient;
    private JTabbedPane tabbedPane;
    private JList<Chat> myChatsList;
    private DefaultListModel<Chat> myChatsModel, groupsModel;
    private JList<User> allUsersList;
    private DefaultListModel<User> allUsersModel;

    public ChatTabsComponent(ChatClient chatClient) throws RemoteException {
        this.chatClient = chatClient;

        List<User> currentOnlineUsers = new ArrayList<>(chatClient.getCurrentUsers());
        currentOnlineUsers.removeIf(user -> user.getName().equals(chatClient.userName));
        List<Chat> groups = chatClient.getAllGroups();
        List<Chat> myChats = chatClient.getMyChats();

        initComponents(currentOnlineUsers, groups, myChats);
    }

    private void initComponents(List<User> currentOnlineUsers, List<Chat> groups, List<Chat> myChats) {
        tabbedPane = new JTabbedPane();

        setupMyChatsTab();
        setupUsersTab();
        setupGroupsTab();

        add(tabbedPane);

        setupListeners();
        updateLists(currentOnlineUsers, groups, myChats);
    }

    private void setupMyChatsTab() {
        myChatsModel = new DefaultListModel<>();
        myChatsList = new JList<>(myChatsModel);
        myChatsList.setCellRenderer(createChatListsCellRenderer());
        myChatsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Chat chat = myChatsList.getSelectedValue();
                    try {
                        chatClient.setCurrentChat(chat);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                    tabbedPane.setSelectedIndex(0);
                }
            }
        });
        JPanel chatsPanel = new JPanel(new BorderLayout());
        chatsPanel.add(new JScrollPane(myChatsList), BorderLayout.CENTER);
        tabbedPane.addTab("My Chats", chatsPanel);
    }

    private void setupUsersTab() {
        allUsersModel = new DefaultListModel<>();
        allUsersList = new JList<>(allUsersModel);
        allUsersList.setCellRenderer(createUsersListsCellRenderer());
        allUsersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    User user = allUsersList.getSelectedValue();
                    try {
                        chatClient.createPrivateChat(user.getName());
                        tabbedPane.setSelectedIndex(0);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.add(new JScrollPane(allUsersList), BorderLayout.CENTER);
        tabbedPane.addTab("Online user", usersPanel);
    }

    private void setupGroupsTab() {
        groupsModel = new DefaultListModel<>();
        JList<Chat> groupsList = new JList<>(groupsModel);
        groupsList.setCellRenderer(createChatListsCellRenderer());
        groupsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    Chat chat = groupsList.getSelectedValue();
                    try {
                        chatClient.sendInviteAdmin(chatClient.userName, chat);
                        JOptionPane.showMessageDialog(tabbedPane,
                                "Your request to join the group has been sent. Wait for the administrator to accept your entry.",
                                "Order sent",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(tabbedPane, "EError sending order.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        JPanel groupsPanel = new JPanel(new BorderLayout());
        groupsPanel.add(new JScrollPane(groupsList), BorderLayout.CENTER);
        tabbedPane.addTab("Public groups", groupsPanel);
    }

    private ListCellRenderer<? super User> createUsersListsCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(((User) value).getName());
                return this;
            }
        };
    }

    private ListCellRenderer<? super Chat> createChatListsCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(((Chat) value).getName());
                return this;
            }
        };
    }

    private void setupListeners() {
        chatClient.addChangeUserListListener(this::onChangeUserList);
        chatClient.addChangeGroupListListener(this::onChangeGroupList);
        chatClient.addChangeMyChatsListListener(this::onChangeMyChatsList);
    }

    private void updateLists(List<User> currentOnlineUsers, List<Chat> groups, List<Chat> myChats) {
        onChangeUserList(currentOnlineUsers);
        onChangeGroupList(groups);
        onChangeMyChatsList(myChats);
    }

    private void onChangeUserList(List<User> onlineUsers) {
        allUsersModel.clear();
        onlineUsers.forEach(allUsersModel::addElement);
    }

    private void onChangeGroupList(List<Chat> groups) {
        groupsModel.clear();
        groups.forEach(groupsModel::addElement);
    }

    private void onChangeMyChatsList(List<Chat> myChats) {
        myChatsModel.clear();
        myChats.forEach(myChatsModel::addElement);
    }
}
