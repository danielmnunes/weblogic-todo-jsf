package com.weblogic.todo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoTest {

    @Test
    void createStartsOpenWithoutTimestamp() {
        var todo = Todo.create("Comprar pão", "Padaria");

        assertNull(todo.getId());
        assertEquals("Comprar pão", todo.getTitle());
        assertEquals("Padaria", todo.getDescription());
        assertFalse(todo.isDone());
        assertNull(todo.getCreatedAt());
    }

    @Test
    void prePersistFillsTimestampOnce() {
        var todo = Todo.create("A", null);
        todo.prePersist();
        var first = todo.getCreatedAt();
        assertNotNull(first);

        todo.prePersist();
        assertEquals(first, todo.getCreatedAt());
    }

    @Test
    void settersAndToggleWork() {
        var todo = Todo.create("A", "B");
        var stamp = LocalDateTime.of(2026, 1, 2, 3, 4);

        todo.setId(9L);
        todo.setTitle("Novo");
        todo.setDescription("Desc");
        todo.setDone(true);
        todo.setCreatedAt(stamp);

        assertEquals(9L, todo.getId());
        assertEquals("Novo", todo.getTitle());
        assertEquals("Desc", todo.getDescription());
        assertTrue(todo.isDone());
        assertEquals(stamp, todo.getCreatedAt());

        todo.toggleDone();
        assertFalse(todo.isDone());
        todo.toggleDone();
        assertTrue(todo.isDone());
    }

    @Test
    void applyReplacesTitleAndDescription() {
        var todo = Todo.create("A", "B");
        todo.apply("C", "D");
        assertEquals("C", todo.getTitle());
        assertEquals("D", todo.getDescription());
    }

    @Test
    void equalsAndHashCodeUseId() {
        var left = Todo.create("A", null);
        var right = Todo.create("B", null);
        var other = Todo.create("A", null);

        left.setId(1L);
        right.setId(1L);
        other.setId(2L);

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, other);
        assertNotEquals(left, "todo");
        assertNotEquals(left, null);

        var unsaved = Todo.create("X", null);
        assertEquals(unsaved, unsaved);
        assertNotEquals(left, unsaved);
        assertEquals(0, unsaved.hashCode());
    }

    @Test
    void protectedConstructorExistsForJpa() {
        var todo = new Todo();
        assertNull(todo.getId());
        assertFalse(todo.isDone());
    }
}
