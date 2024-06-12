package com.biblioteca.loanservice.service;

import com.biblioteca.loanservice.client.CatalogServiceClient;
import com.biblioteca.loanservice.client.NotificationServiceClient;
import com.biblioteca.loanservice.exception.CatalogServiceUnavailableException;
import com.biblioteca.loanservice.exception.LoanNotFoundException;
import com.biblioteca.loanservice.exception.LoanServiceException;
import com.biblioteca.loanservice.exception.NotificationServiceUnavailableException;
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
        try {
            // Verificar a disponibilidade do livro no CatalogService
            if (!catalogServiceClient.isBookAvailable(loan.getBookId())) {
                loan.setStatus(LoanStatus.REJECTED);
                notificationServiceClient.notifyLoanRejection(loan); // Notificar rejeição
                return loanRepository.save(loan);
            }

            // Registrar o empréstimo no banco de dados
            loan.setStatus(LoanStatus.APPROVED);
            Loan savedLoan = loanRepository.save(loan);

            // Notificar o usuário sobre a aprovação do empréstimo
            notificationServiceClient.notifyLoanApproval(savedLoan); // Notificar aprovação

            // Atualizar o status do livro para indisponível no CatalogService
            catalogServiceClient.updateBookStatus(loan.getBookId(), false);

            return savedLoan;
        } catch (CatalogServiceUnavailableException e) {
            // Lidar com a indisponibilidade do CatalogService
            throw new LoanServiceException("Erro ao verificar a disponibilidade do livro.", e);
        } catch (NotificationServiceUnavailableException e) {
            // Lidar com a indisponibilidade do NotificationService
            // (Opcional: Registrar o erro para posterior processamento)
            throw new LoanServiceException("Erro ao enviar notificação.", e);
        }
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id).orElse(null);
    }

    public Loan updateLoan(Long id, Loan loanDetails) {
        Loan loan = getLoanById(id);
        if (loan != null) {
            // Atualize os campos do empréstimo com os valores de loanDetails
            // ... (implementar a lógica de atualização)
            return loanRepository.save(loan);
        }
        return null;
    }

    public boolean deleteLoan(Long id) {
        Loan loan = getLoanById(id);
        if (loan != null) {
            loanRepository.delete(loan);
            return true;
        }
        return false;
    }

    public void returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Empréstimo não encontrado."));

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);

        catalogServiceClient.updateBookStatus(loan.getBookId(), true);
        notificationServiceClient.notifyLoanReturn(loan);
    }
}
