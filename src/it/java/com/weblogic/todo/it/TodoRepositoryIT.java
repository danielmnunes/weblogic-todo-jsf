package com.weblogic.todo.it;

import com.weblogic.todo.domain.Todo;
import com.weblogic.todo.repository.TodoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class TodoRepositoryIT {

    @Container
    static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withUsername("todo")
            .withPassword("TodoPassword1");

    private static EntityManagerFactory factory;
    private EntityManager entityManager;
    private TodoRepository repository;

    @BeforeAll
    static void startPersistence() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "oracle.jdbc.OracleDriver");
        properties.put("jakarta.persistence.jdbc.url", ORACLE.getJdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", ORACLE.getUsername());
        properties.put("jakarta.persistence.jdbc.password", ORACLE.getPassword());
        properties.put("eclipselink.ddl-generation", "drop-and-create-tables");
        properties.put("eclipselink.ddl-generation.output-mode", "database");
        properties.put("eclipselink.logging.level", "WARNING");
        factory = Persistence.createEntityManagerFactory("todoIT", properties);
    }

    @AfterAll
    static void closePersistence() {
        if (factory != null) {
            factory.close();
        }
    }

    @BeforeEach
    void open() {
        entityManager = factory.createEntityManager();
        repository = new TodoRepository(entityManager);
        entityManager.getTransaction().begin();
    }

    @AfterEach
    void close() {
        if (entityManager == null) {
            return;
        }
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
        entityManager.close();
    }

    @Test
    void persistsAndFindsByStatus() {
        var open = repository.save(Todo.create("Aberta", "uma"));
        var done = Todo.create("Feita", "duas");
        done.setDone(true);
        repository.save(done);
        entityManager.flush();

        assertTrue(repository.findById(open.getId()).isPresent());
        assertEquals(2, repository.findAll().size());
        assertEquals(1, repository.findByDone(false).size());
        assertEquals(1, repository.findByDone(true).size());
        assertEquals("Aberta", repository.findByDone(false).getFirst().getTitle());
        assertFalse(repository.findById(open.getId()).orElseThrow().isDone());
    }
}
