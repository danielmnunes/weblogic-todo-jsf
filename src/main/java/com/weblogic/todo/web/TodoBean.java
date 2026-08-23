package com.weblogic.todo.web;

import com.weblogic.todo.domain.Todo;
import com.weblogic.todo.domain.TodoFilter;
import com.weblogic.todo.service.TodoNotFoundException;
import com.weblogic.todo.service.TodoService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class TodoBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final TodoService service;
    private final FacesMessages messages;

    private String title;
    private String description;
    private Long editingId;
    private TodoFilter filter = TodoFilter.ALL;

    @Inject
    public TodoBean(TodoService service, FacesMessages messages) {
        this.service = service;
        this.messages = messages;
    }

    protected TodoBean() {
        this.service = null;
        this.messages = null;
    }

    public List<Todo> getTodos() {
        return service.list(filter);
    }

    public TodoFilter[] getFilters() {
        return TodoFilter.values();
    }

    public void create() {
        try {
            if (editingId == null) {
                service.create(title, description);
                messages.info("Tarefa criada");
            } else {
                service.update(editingId, title, description);
                messages.info("Tarefa atualizada");
            }
            clearForm();
        } catch (IllegalArgumentException ex) {
            messages.error(ex.getMessage());
        } catch (TodoNotFoundException ex) {
            messages.error(ex.getMessage());
            clearForm();
        }
    }

    public void startEdit(Todo todo) {
        editingId = todo.getId();
        title = todo.getTitle();
        description = todo.getDescription();
    }

    public void cancelEdit() {
        clearForm();
    }

    public void toggle(Todo todo) {
        try {
            service.toggle(todo.getId());
        } catch (TodoNotFoundException ex) {
            messages.error(ex.getMessage());
        }
    }

    public void delete(Todo todo) {
        try {
            service.delete(todo.getId());
            if (todo.getId().equals(editingId)) {
                clearForm();
            }
            messages.info("Tarefa excluída");
        } catch (TodoNotFoundException ex) {
            messages.error(ex.getMessage());
        }
    }

    public void applyFilter(TodoFilter filter) {
        this.filter = filter == null ? TodoFilter.ALL : filter;
    }

    public boolean isEditing() {
        return editingId != null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEditingId() {
        return editingId;
    }

    public TodoFilter getFilter() {
        return filter;
    }

    public void setFilter(TodoFilter filter) {
        applyFilter(filter);
    }

    private void clearForm() {
        title = null;
        description = null;
        editingId = null;
    }
}
