package com.weblogic.todo.service;

import com.weblogic.todo.domain.Todo;
import com.weblogic.todo.domain.TodoFilter;
import com.weblogic.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository repository;

    private TodoService service;

    @BeforeEach
    void setUp() {
        service = new TodoService(repository);
    }

    @Test
    void protectedConstructorExistsForCdi() {
        var unused = new TodoService();
        assertTrue(unused.getClass().equals(TodoService.class));
    }

    @Test
    void listUsesRepositoryByFilterAndDefaultsNullToAll() {
        var all = List.of(Todo.create("a", null));
        var active = List.of(Todo.create("b", null));
        var done = List.of(Todo.create("c", null));
        when(repository.findAll()).thenReturn(all);
        when(repository.findByDone(false)).thenReturn(active);
        when(repository.findByDone(true)).thenReturn(done);

        assertEquals(all, service.list(null));
        assertEquals(all, service.list(TodoFilter.ALL));
        assertEquals(active, service.list(TodoFilter.ACTIVE));
        assertEquals(done, service.list(TodoFilter.DONE));
    }

    @Test
    void createNormalizesFieldsAndPersists() {
        when(repository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.create("  Título  ", "  desc  ");

        assertEquals("Título", saved.getTitle());
        assertEquals("desc", saved.getDescription());
        verify(repository).save(saved);
    }

    @Test
    void createRejectsBlankAndOversizedTitle() {
        assertThrows(IllegalArgumentException.class, () -> service.create("  ", "x"));
        assertThrows(IllegalArgumentException.class, () -> service.create(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> service.create("x".repeat(201), "x"));
    }

    @Test
    void createTreatsBlankDescriptionAsNullAndRejectsTooLong() {
        when(repository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertNull(service.create("ok", "   ").getDescription());
        assertNull(service.create("ok", null).getDescription());
        assertThrows(IllegalArgumentException.class, () -> service.create("ok", "d".repeat(1001)));
    }

    @Test
    void updateAppliesChanges() {
        var existing = Todo.create("old", "d");
        existing.setId(8L);
        when(repository.findById(8L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        var updated = service.update(8L, "novo", "nd");

        assertEquals("novo", updated.getTitle());
        assertEquals("nd", updated.getDescription());
    }

    @Test
    void toggleFlipsDone() {
        var existing = Todo.create("t", null);
        existing.setId(2L);
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertTrue(service.toggle(2L).isDone());
        assertFalse(service.toggle(2L).isDone());
    }

    @Test
    void deleteRemovesExistingTodo() {
        var existing = Todo.create("t", null);
        existing.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));

        service.delete(5L);

        var captor = ArgumentCaptor.forClass(Todo.class);
        verify(repository).delete(captor.capture());
        assertEquals(5L, captor.getValue().getId());
    }

    @Test
    void missingTodoThrows() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        var ex = assertThrows(TodoNotFoundException.class, () -> service.delete(77L));
        assertEquals("Tarefa 77 não encontrada", ex.getMessage());
        assertThrows(TodoNotFoundException.class, () -> service.toggle(77L));
        assertThrows(TodoNotFoundException.class, () -> service.update(77L, "a", "b"));
    }
}
