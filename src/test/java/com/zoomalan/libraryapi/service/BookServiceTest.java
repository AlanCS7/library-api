package com.zoomalan.libraryapi.service;

import com.zoomalan.libraryapi.exceptions.BusinessException;
import com.zoomalan.libraryapi.model.entity.Book;
import com.zoomalan.libraryapi.model.repository.BookRepository;
import com.zoomalan.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setup() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Should save a book")
    public void saveBookTest() {
        // scenario
        Book book = createValidBook();
        when(repository.existsByIsbn(anyString())).thenReturn(false);
        when(repository.save(book)).thenReturn(
                Book.builder().id(1L)
                        .author("Alan")
                        .title("Spring Boot")
                        .isbn("123").build());

        // execution
        Book savedBook = service.save(book);

        // verification
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo("Alan");
        assertThat(savedBook.getTitle()).isEqualTo("Spring Boot");
        assertThat(savedBook.getIsbn()).isEqualTo("123");
    }

    @Test
    @DisplayName("Should throw a business error when trying to save a book with dulicate isbn")
    public void shouldNotSaveABookWithDuplicateIsnb() {
        // scenery
        Book book = createValidBook();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        // execution
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        // verification
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn already registered");

        verify(repository, never()).save(book);
    }

    private static Book createValidBook() {
        return Book.builder().author("Alan").title("Spring Boot").isbn("123").build();
    }

}
