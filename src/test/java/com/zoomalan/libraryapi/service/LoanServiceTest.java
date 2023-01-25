package com.zoomalan.libraryapi.service;

import com.zoomalan.libraryapi.api.dto.LoanFilterDTO;
import com.zoomalan.libraryapi.exceptions.BusinessException;
import com.zoomalan.libraryapi.model.entity.Book;
import com.zoomalan.libraryapi.model.entity.Loan;
import com.zoomalan.libraryapi.model.repository.LoanRepository;
import com.zoomalan.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setup() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Should save a loan")
    public void saveLoanTest() {
        Book book = Book.builder().id(1L).build();
        Loan saving = Loan.builder()
                .book(book)
                .customer("Alan")
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .book(book)
                .customer("Alan")
                .loanDate(LocalDate.now())
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        when(repository.save(saving)).thenReturn(savedLoan);

        Loan loan = service.save(saving);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Should return a Business Error when to try a loan with book loaned")
    public void saveLoanedBookTest() {
        Book book = Book.builder().id(1L).build();

        Loan saving = Loan.builder()
                .book(book)
                .customer("Alan")
                .loanDate(LocalDate.now())
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(saving));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(saving);
    }

    @Test
    @DisplayName("Should get loan information by id")
    public void getLoanDetailsTest() {
        // scenario
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(loan));

        // execution
        Optional<Loan> result = service.getById(id);

        // verify
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Should update a loan")
    public void updateLoanTest() {
        // scenario
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();

        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Should filter loans with properties")
    public void findLoanTest() {
        // given
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Alan").isbn("isbn").build();
        Loan loan = createLoan();
        loan.setId(1L);

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> loans = Collections.singletonList(loan);

        Page<Loan> page = new PageImpl<Loan>(loans, pageRequest, loans.size());
        when(repository.findByBookIsbnOrCustomer(
                anyString(),
                anyString(),
                any(PageRequest.class)))
                .thenReturn(page);

        // when
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(loans);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    public static Loan createLoan() {
        Book book = Book.builder().id(1L).build();

        return Loan.builder()
                .book(book)
                .customer("Alan")
                .loanDate(LocalDate.now())
                .build();
    }
}
