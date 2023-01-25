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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    @DisplayName("Should get book by id")
    public void getByIdTest() {
        // given
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        // when
        Optional<Book> foundBook = service.getById(id);

        // then
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Should return empty when getting a book by id when it doesn't exist")
    public void bookNotFoundByIdTest() {
        // given
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // when
        Optional<Book> book = service.getById(id);

        // then
        assertThat(book.isPresent()).isFalse();
    }


    @Test
    @DisplayName("Should delete a book by id")
    public void deleteBookByIdTest() {
        // given
        Book book = Book.builder().id(1L).build();

        // when
        assertDoesNotThrow(() -> service.deleteById(book.getId()));

        // then
        verify(repository, times(1)).deleteById(book.getId());

    }

    @Test
    @DisplayName("Should throw a IllegalArgumentException when trying delete a book without id")
    public void deleteInvalidBookTest() {
        // given
        Book book = new Book();

        // when
        assertThrows(IllegalArgumentException.class, () -> service.deleteById(book.getId()));

        // then
        verify(repository, never()).deleteById(book.getId());
    }

    @Test
    @DisplayName("Should update a book by id")
    public void updateBookTest() {
        // given
        Long id = 1L;
        Book updatingBook = Book.builder().id(id).build();

        Book updatedBook = createValidBook();
        updatedBook.setId(id);

        when(repository.save(updatingBook)).thenReturn(updatedBook);

        // when
        Book book = service.update(updatingBook);

        // then
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Should throw a IllegalArgumentException when trying update a book without id")
    public void updateInvalidBookTest() {
        // given
        Book book = new Book();

        // when
        assertThrows(IllegalArgumentException.class, () -> service.update(book));

        // then
        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Should filter books with properties")
    public void findBookTest() {
        // given
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> books = Collections.singletonList(book);

        Page<Book> page = new PageImpl<Book>(books, pageRequest, 1);
        when(repository.findAll(any(Example.class), any(PageRequest.class)))
                .thenReturn(page);

        // when
        Page<Book> result = service.find(book, pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(books);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should get a book by Isbn")
    public void getBookByIsbnTest() {
        String isbn = "1230";
        when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);
    }

    private static Book createValidBook() {
        return Book.builder().author("Alan").title("Spring Boot").isbn("123").build();
    }

}
