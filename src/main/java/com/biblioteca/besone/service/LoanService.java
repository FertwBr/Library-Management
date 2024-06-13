package com.biblioteca.besone.service;

import com.biblioteca.besone.client.CatalogServiceClient;
import com.biblioteca.besone.client.NotificationServiceClient;
import com.biblioteca.besone.exception.*;
import com.biblioteca.besone.model.Email;
import com.biblioteca.besone.model.Loan;
import com.biblioteca.besone.model.LoanStatus;
import com.biblioteca.besone.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
            int availableCopies = catalogServiceClient.getBookAvailability(loan.getBookId());

            if (availableCopies <= 1) {
                notificationServiceClient.notifyBookUnavailability(loan.getBookId(), loan.getUserId());
                throw new BookNotAvailableException("Livro não disponível para empréstimo. Apenas 1 cópia disponível.");
            }

            loan.setLoanDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusWeeks(2)); // Prazo de 2 semanas (personalizável)
            loan.setStatus(LoanStatus.APPROVED);
            Loan savedLoan = loanRepository.save(loan);

            catalogServiceClient.updateBookStatus(loan.getBookId(), false);
            notifyLoan(savedLoan, "Empréstimo de livro", "O livro foi emprestado com sucesso.");

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

        if (loanDetails.getStatus() == LoanStatus.RETURNED && loan.getStatus() != LoanStatus.APPROVED) {
            throw new InvalidLoanStatusException("Não é possível marcar um empréstimo como devolvido se ele não estiver aprovado.");
        }
        if (loanDetails.getDueDate() != null) {
            loan.setDueDate(loanDetails.getDueDate());
        }
        if (loanDetails.getStatus() != null) {
            loan.setStatus(loanDetails.getStatus());
        }
        if (loanDetails.getReturnDate() != null && loan.getStatus() == LoanStatus.RETURNED) {
            loan.setReturnDate(loanDetails.getReturnDate());
        }

        return loanRepository.save(loan);
    }

    public boolean deleteLoan(Long id) {
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

        try {
            catalogServiceClient.updateBookStatus(loan.getBookId(), true);
        } catch (CatalogServiceUnavailableException e) {
            throw new LoanServiceException("O serviço de catálogo está indisponível no momento. Tente novamente mais tarde.", e);
        }

        notifyLoan(loan, "Devolução de livro", "O livro foi devolvido com sucesso."); // Sem tratamento de erro aqui
    }

    private void notifyLoan(Loan loan, String subject, String text) {
        Email email = new Email();
        email.setEmailFrom("biblioteca@example.com"); // Defina o remetente
        email.setEmailTo(loan.getUserEmail()); // Use o email do usuário do empréstimo
        email.setSubject(subject);
        email.setText(text); // Inclua informações do livro

        notificationServiceClient.notifyLoan(email);
    }

}
