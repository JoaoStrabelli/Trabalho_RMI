package com.edu.utfpr.core.exceptions;

public class UserAlreadyRegisteredException extends Exception {
    public UserAlreadyRegisteredException() {
        super("Usuário já está registrado");
    }
}
