package com.edu.utfpr.core.exceptions;

public class InvalidUserOrPasswordException extends Exception {
    public InvalidUserOrPasswordException() {
        super("Usuário ou senha inválidos");
    }
}
