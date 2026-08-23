package com.weblogic.todo.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoFilterTest {

    @Test
    void allMatchesEverything() {
        var open = Todo.create("a", null);
        var done = Todo.create("b", null);
        done.setDone(true);

        assertTrue(TodoFilter.ALL.matches(open));
        assertTrue(TodoFilter.ALL.matches(done));
    }

    @Test
    void activeAndDoneMatchStatus() {
        var open = Todo.create("a", null);
        var done = Todo.create("b", null);
        done.setDone(true);

        assertTrue(TodoFilter.ACTIVE.matches(open));
        assertFalse(TodoFilter.ACTIVE.matches(done));
        assertFalse(TodoFilter.DONE.matches(open));
        assertTrue(TodoFilter.DONE.matches(done));
    }

    @Test
    void labelsArePortuguese() {
        assertEquals("Todas", TodoFilter.ALL.label());
        assertEquals("Ativas", TodoFilter.ACTIVE.label());
        assertEquals("Concluídas", TodoFilter.DONE.label());
        assertArrayEquals(new TodoFilter[] {TodoFilter.ALL, TodoFilter.ACTIVE, TodoFilter.DONE}, TodoFilter.values());
    }
}
