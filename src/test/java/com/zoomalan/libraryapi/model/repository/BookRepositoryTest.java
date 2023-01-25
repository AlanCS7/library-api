package com.zoomalan.libraryapi.model.repository;

import com.zoomalan.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execution
        boolean exists = repository.existsByIsbn(isbn);

        // verification
        assertThat(exists).isTrue();
    }

    public static Book createNewBook(String isbn) {
        return Book.builder().author("Alan").title("Spring Boot").isbn(isbn).build();
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

    @Test
    @DisplayName("Should get book by id")
    public void findByIdTest() {
        // scenario
        Book book = createNewBook("123");
        entityManager.persist(book);

        // execution
        Optional<Book> foundBook = repository.findById(book.getId());

        // verification
        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should save a book")
    public void saveBookTest() {
        // scenario
        Book book = createNewBook("123");

        // execution
        Book savedBook = repository.save(book);

        // verification
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should delete a book")
    public void deleteBookTest() {
        // scenario
        Book book = createNewBook("123");
        entityManager.persist(book);
        Book foundBook = entityManager.find(Book.class, book.getId());

        // execution
        repository.deleteById(foundBook.getId());

        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }
}
