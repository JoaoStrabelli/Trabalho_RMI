package com.edu.utfpr.client;

import com.edu.utfpr.core.entities.Chat;
import com.edu.utfpr.core.entities.Messages;
import com.edu.utfpr.core.entities.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface IChatClient extends Remote {
    UUID getCurrentChatId() throws RemoteException;

    void updateUserList(List<User> currentUsers) throws RemoteException;

    void updatePublicGroupList(List<Chat> groups) throws RemoteException;

    void updateChatList(List<Chat> MyChats) throws RemoteException;

    void receiveMessage(Messages message) throws RemoteException;

    void sendInviteAdmin(String userName, Chat chat) throws RemoteException;
}
