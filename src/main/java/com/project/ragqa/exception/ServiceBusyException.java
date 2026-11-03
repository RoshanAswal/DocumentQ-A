package com.project.ragqa.exception;

public class ServiceBusyException extends RuntimeException {
    public ServiceBusyException(String message) { super(message); }
}