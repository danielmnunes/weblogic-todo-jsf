package com.weblogic.todo.domain;

public enum TodoFilter {
    ALL,
    ACTIVE,
    DONE;

    public boolean matches(Todo todo) {
        return switch (this) {
            case ALL -> true;
            case ACTIVE -> !todo.isDone();
            case DONE -> todo.isDone();
        };
    }

    public String label() {
        return switch (this) {
            case ALL -> "Todas";
            case ACTIVE -> "Ativas";
            case DONE -> "Concluídas";
        };
    }
}
