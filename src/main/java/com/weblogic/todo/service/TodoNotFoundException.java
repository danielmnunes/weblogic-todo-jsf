package com.weblogic.todo.service;

public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(long id) {
        super("Tarefa %d não encontrada".formatted(id));
    }
}
