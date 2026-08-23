package com.weblogic.todo.service;

import com.weblogic.todo.domain.Todo;
import com.weblogic.todo.domain.TodoFilter;
import com.weblogic.todo.repository.TodoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class TodoService {

    static final int TITLE_MAX = 200;
    static final int DESCRIPTION_MAX = 1000;

    private final TodoRepository repository;

    @Inject
    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    protected TodoService() {
        this.repository = null;
    }

    public List<Todo> list(TodoFilter filter) {
        var effective = filter == null ? TodoFilter.ALL : filter;
        return switch (effective) {
            case ALL -> repository.findAll();
            case ACTIVE -> repository.findByDone(false);
            case DONE -> repository.findByDone(true);
        };
    }

    public Todo create(String title, String description) {
        var todo = Todo.create(requireTitle(title), normalizeDescription(description));
        return repository.save(todo);
    }

    public Todo update(long id, String title, String description) {
        var todo = requireTodo(id);
        todo.apply(requireTitle(title), normalizeDescription(description));
        return repository.save(todo);
    }

    public Todo toggle(long id) {
        var todo = requireTodo(id);
        todo.toggleDone();
        return repository.save(todo);
    }

    public void delete(long id) {
        repository.delete(requireTodo(id));
    }

    private Todo requireTodo(long id) {
        return repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }

    static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("O título é obrigatório");
        }
        var normalized = title.strip();
        if (normalized.length() > TITLE_MAX) {
            throw new IllegalArgumentException("O título deve ter no máximo %d caracteres".formatted(TITLE_MAX));
        }
        return normalized;
    }

    static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        var normalized = description.strip();
        if (normalized.length() > DESCRIPTION_MAX) {
            throw new IllegalArgumentException(
                    "A descrição deve ter no máximo %d caracteres".formatted(DESCRIPTION_MAX));
        }
        return normalized;
    }
}
