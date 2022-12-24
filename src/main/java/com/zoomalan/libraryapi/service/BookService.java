package com.zoomalan.libraryapi.service;

import com.zoomalan.libraryapi.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {

    Book save(Book book);

    Optional<Book> getById(Long id);

    void deleteById(Long id);

    Book update(Book book);

    Page<Book> find(Book filter, Pageable pageRequest);
}
