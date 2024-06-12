package com.biblioteca.loanservice.service;

import com.biblioteca.loanservice.client.CatalogServiceClient;
import com.biblioteca.loanservice.client.NotificationServiceClient;
import com.biblioteca.loanservice.exception.*;
import com.biblioteca.loanservice.model.Loan;
import com.biblioteca.loanservice.model.LoanStatus;
import com.biblioteca.loanservice.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// Todo #1 Verficar CatalogService e NotificationService
// Todo #2 Arrumar exceções
@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CatalogServiceClient catalogServiceClient;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    public Loan createLoan(Loan loan) {
    if (loan.getBookId() == null || loan.getUserId() == null) {
        throw new IllegalArgumentException("ID do livro e ID do usuário são obrigatórios.");
    }

    try {
        if (!catalogServiceClient.isBookAvailable(loan.getBookId())) {
            throw new BookNotAvailableException("Livro não disponível para empréstimo.");
        }

        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(2)); // Prazo de 2 semanas (personalizável)
        loan.setStatus(LoanStatus.APPROVED);
        Loan savedLoan = loanRepository.save(loan);

        catalogServiceClient.updateBookStatus(loan.getBookId(), false);
        notificationServiceClient.notifyLoanApproval(savedLoan);

        return savedLoan;
    } catch (CatalogServiceUnavailableException e) {
        throw new LoanServiceException("O serviço de catálogo está indisponível no momento. Tente novamente mais tarde.", e);
    } catch (NotificationServiceUnavailableException e) {
        throw new LoanServiceException("Erro ao enviar notificação.", e);
    }
}

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException("Empréstimo não encontrado com o ID: " + id));
    }

    public Loan updateLoan(Long id, Loan loanDetails) {
        Loan loan = getLoanById(id);

        // Atualize os campos do empréstimo com os valores de loanDetails
        // Todo #3 ... (implementar a lógica de atualização)
        return loanRepository.save(loan);
    }

    public void deleteLoan(Long id) {
        Loan loan = getLoanById(id);
        loanRepository.delete(loan);
    }

    public void returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Empréstimo não encontrado."));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new InvalidLoanStatusException("Não é possível devolver um empréstimo que não está aprovado.");
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);

        catalogServiceClient.updateBookStatus(loan.getBookId(), true);
        notificationServiceClient.notifyLoanReturn(loan);
    }
    //Todo #4 ... (Outros métodos, como checkOverdueLoans, podem ser adicionados aqui) ...
}
