package com.zoomalan.libraryapi.api.exceptions;

import com.zoomalan.libraryapi.exceptions.BusinessException;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiErrors {
    private final List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException e) {
        this.errors = Collections.singletonList(e.getMessage());
    }

    public ApiErrors(ResponseStatusException e) {
        this.errors = Collections.singletonList(e.getReason());
    }

    public List<String> getErrors() {
        return errors;
    }
}
