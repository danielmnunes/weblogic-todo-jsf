package com.weblogic.todo.repository;

import com.weblogic.todo.domain.Todo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Todo> query;

    private TodoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TodoRepository(entityManager);
    }

    @Test
    void protectedConstructorExistsForCdi() {
        var unused = new TodoRepository();
        assertTrue(unused.getClass().equals(TodoRepository.class));
    }

    @Test
    void findAllUsesCreatedAtDescending() {
        var todo = Todo.create("A", null);
        when(entityManager.createQuery(anyString(), eq(Todo.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(todo));

        var result = repository.findAll();

        assertEquals(List.of(todo), result);
        verify(entityManager).createQuery("select t from Todo t order by t.createdAt desc", Todo.class);
    }

    @Test
    void findByDoneSetsParameter() {
        when(entityManager.createQuery(anyString(), eq(Todo.class))).thenReturn(query);
        when(query.setParameter("done", true)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        var result = repository.findByDone(true);

        assertTrue(result.isEmpty());
        verify(entityManager).createQuery(
                "select t from Todo t where t.done = :done order by t.createdAt desc", Todo.class);
        verify(query).setParameter("done", true);
    }

    @Test
    void findByIdWrapsEntityManager() {
        var todo = Todo.create("A", null);
        when(entityManager.find(Todo.class, 4L)).thenReturn(todo);
        when(entityManager.find(Todo.class, 99L)).thenReturn(null);

        var result = repository.findById(4L).orElseThrow();
        assertTrue(result == todo);
        assertTrue(repository.findById(99L).isEmpty());
    }

    @Test
    void savePersistsNewAndMergesExisting() {
        var created = Todo.create("Nova", null);
        var existing = Todo.create("Velha", null);
        existing.setId(3L);
        when(entityManager.merge(existing)).thenReturn(existing);

        assertTrue(created == repository.save(created));
        verify(entityManager).persist(created);

        assertTrue(existing == repository.save(existing));
        verify(entityManager).merge(existing);
    }

    @Test
    void deleteRemovesManagedEntity() {
        var todo = Todo.create("A", null);
        when(entityManager.contains(todo)).thenReturn(true);

        repository.delete(todo);

        verify(entityManager).remove(todo);
    }

    @Test
    void deleteMergesDetachedEntityBeforeRemove() {
        var todo = Todo.create("A", null);
        var managed = Todo.create("A", null);
        when(entityManager.contains(todo)).thenReturn(false);
        when(entityManager.merge(todo)).thenReturn(managed);

        repository.delete(todo);

        verify(entityManager).merge(todo);
        verify(entityManager).remove(managed);
    }
}
