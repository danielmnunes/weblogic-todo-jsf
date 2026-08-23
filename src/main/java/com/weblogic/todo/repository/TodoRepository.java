package com.weblogic.todo.repository;

import com.weblogic.todo.domain.Todo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TodoRepository {

    @PersistenceContext(unitName = "todoPU")
    private EntityManager entityManager;

    protected TodoRepository() {
    }

    public TodoRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Todo> findAll() {
        return entityManager
                .createQuery("select t from Todo t order by t.createdAt desc", Todo.class)
                .getResultList();
    }

    public List<Todo> findByDone(boolean done) {
        return entityManager
                .createQuery("select t from Todo t where t.done = :done order by t.createdAt desc", Todo.class)
                .setParameter("done", done)
                .getResultList();
    }

    public Optional<Todo> findById(long id) {
        return Optional.ofNullable(entityManager.find(Todo.class, id));
    }

    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            entityManager.persist(todo);
            return todo;
        }
        return entityManager.merge(todo);
    }

    public void delete(Todo todo) {
        var managed = entityManager.contains(todo) ? todo : entityManager.merge(todo);
        entityManager.remove(managed);
    }
}
