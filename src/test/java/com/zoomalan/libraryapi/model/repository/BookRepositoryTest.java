package com.zoomalan.libraryapi.model.repository;

import com.zoomalan.libraryapi.model.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Should return true when there is a book in the database with informed isbn")
    public void returnTrueWhenIsnbExists() {
        // scenario
        String isbn = "123";
        Book book = Book.builder().author("Alan").title("Spring Boot").isbn(isbn).build();
        entityManager.persist(book);

        // execution
        boolean exists = repository.existsByIsbn(isbn);

        // verification
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when there is no a book in the database with informed isbn")
    public void returnFalseWhenIsnbDoesntExists() {
        // scenario
        String isbn = "123";

        // execution
        boolean exists = repository.existsByIsbn(isbn);

        // verification
        assertThat(exists).isFalse();
    }
}
