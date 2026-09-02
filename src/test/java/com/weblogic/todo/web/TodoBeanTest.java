package com.weblogic.todo.web;

import com.weblogic.todo.domain.Todo;
import com.weblogic.todo.domain.TodoFilter;
import com.weblogic.todo.service.TodoNotFoundException;
import com.weblogic.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoBeanTest {

    @Mock
    private TodoService service;

    @Mock
    private FacesMessages messages;

    private TodoBean bean;

    @BeforeEach
    void setUp() {
        bean = new TodoBean(service, messages);
    }

    @Test
    void protectedConstructorExistsForCdi() {
        var unused = new TodoBean();
        assertTrue(unused.getClass().equals(TodoBean.class));
    }

    @Test
    void gettersAndSettersAndDefaultFilter() {
        bean.setTitle("t");
        bean.setDescription("d");
        bean.setFilter(TodoFilter.ACTIVE);

        assertEquals("t", bean.getTitle());
        assertEquals("d", bean.getDescription());
        assertEquals(TodoFilter.ACTIVE, bean.getFilter());
        assertFalse(bean.isEditing());
        assertNull(bean.getEditingId());
        assertArrayEquals(TodoFilter.values(), bean.getFilters());
    }

    @Test
    void getTodosCachesResultsUntilFilterChanges() {
        var all = List.of(Todo.create("a", null));
        var done = List.of(Todo.create("b", null));
        when(service.list(TodoFilter.ALL)).thenReturn(all);
        when(service.list(TodoFilter.DONE)).thenReturn(done);

        assertEquals(all, bean.getTodos());
        assertEquals(all, bean.getTodos());
        verify(service, times(1)).list(TodoFilter.ALL);

        bean.applyFilter(TodoFilter.DONE);
        assertEquals(done, bean.getTodos());
        assertEquals(done, bean.getTodos());
        verify(service, times(1)).list(TodoFilter.DONE);
    }

    @Test
    void successfulMutationsInvalidateCachedTodos() {
        var todos = List.of(Todo.create("a", null));
        var todo = Todo.create("b", null);
        todo.setId(2L);
        when(service.list(TodoFilter.ALL)).thenReturn(todos);

        bean.getTodos();
        bean.setTitle("Nova");
        bean.create();
        bean.getTodos();

        bean.toggle(todo);
        bean.getTodos();
        bean.delete(todo);
        bean.getTodos();

        verify(service, times(4)).list(TodoFilter.ALL);
    }

    @Test
    void createPersistsAndClearsForm() {
        bean.setTitle("Nova");
        bean.setDescription("Desc");

        bean.create();

        verify(service).create("Nova", "Desc");
        verify(messages).info("Tarefa criada");
        assertNull(bean.getTitle());
        assertNull(bean.getDescription());
    }

    @Test
    void createShowsValidationErrorWithoutClearing() {
        bean.setTitle(" ");
        doThrow(new IllegalArgumentException("O título é obrigatório"))
                .when(service).create(" ", null);

        bean.create();

        verify(messages).error("O título é obrigatório");
        assertEquals(" ", bean.getTitle());
    }

    @Test
    void editFlowUpdatesAndCanCancel() {
        var todo = Todo.create("Antiga", "d");
        todo.setId(4L);

        bean.startEdit(todo);
        assertTrue(bean.isEditing());
        assertEquals(4L, bean.getEditingId());
        assertEquals("Antiga", bean.getTitle());

        bean.setTitle("Nova");
        bean.create();

        verify(service).update(4L, "Nova", "d");
        verify(messages).info("Tarefa atualizada");
        assertFalse(bean.isEditing());

        bean.startEdit(todo);
        bean.cancelEdit();
        assertFalse(bean.isEditing());
        assertNull(bean.getTitle());
    }

    @Test
    void updateMissingTodoShowsErrorAndClears() {
        var todo = Todo.create("A", null);
        todo.setId(9L);
        bean.startEdit(todo);
        when(service.list(TodoFilter.ALL)).thenReturn(List.of(todo));
        doThrow(new TodoNotFoundException(9L)).when(service).update(9L, "A", null);

        bean.getTodos();
        bean.create();

        verify(messages).error("Tarefa 9 não encontrada");
        assertFalse(bean.isEditing());
        assertEquals(List.of(todo), bean.getTodos());
        verify(service, times(2)).list(TodoFilter.ALL);
    }

    @Test
    void toggleAndDeleteDelegate() {
        var todo = Todo.create("A", null);
        todo.setId(3L);

        bean.toggle(todo);
        verify(service).toggle(3L);

        bean.delete(todo);
        verify(service).delete(3L);
        verify(messages).info("Tarefa excluída");
    }

    @Test
    void toggleMissingShowsError() {
        var todo = Todo.create("A", null);
        todo.setId(1L);
        when(service.list(TodoFilter.ALL)).thenReturn(List.of(todo));
        doThrow(new TodoNotFoundException(1L)).when(service).toggle(1L);

        bean.getTodos();
        bean.toggle(todo);

        verify(messages).error("Tarefa 1 não encontrada");
        assertEquals(List.of(todo), bean.getTodos());
        verify(service, times(2)).list(TodoFilter.ALL);
    }

    @Test
    void deleteClearsFormWhenEditingSameTodo() {
        var todo = Todo.create("A", "b");
        todo.setId(6L);
        bean.startEdit(todo);

        bean.delete(todo);

        assertFalse(bean.isEditing());
        assertNull(bean.getTitle());
    }

    @Test
    void deleteMissingShowsError() {
        var todo = Todo.create("A", null);
        todo.setId(2L);
        when(service.list(TodoFilter.ALL)).thenReturn(List.of(todo));
        doThrow(new TodoNotFoundException(2L)).when(service).delete(2L);

        bean.getTodos();
        bean.delete(todo);

        verify(messages).error("Tarefa 2 não encontrada");
        assertEquals(List.of(todo), bean.getTodos());
        verify(service, times(2)).list(TodoFilter.ALL);
    }

    @Test
    void applyFilterDefaultsNullToAll() {
        bean.applyFilter(null);
        assertEquals(TodoFilter.ALL, bean.getFilter());
        bean.applyFilter(TodoFilter.DONE);
        assertEquals(TodoFilter.DONE, bean.getFilter());
        verifyNoInteractions(messages);
    }
}
