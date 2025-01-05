package org.netty.server.exceptions;

public class UserFoundException extends Exception {
    public UserFoundException(String message) {
        super(message);
    }
    public UserFoundException() {
        super("User already exists");
    }
}
