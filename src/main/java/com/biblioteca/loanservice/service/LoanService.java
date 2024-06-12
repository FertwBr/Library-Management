package com.biblioteca.loanservice.service;

import com.biblioteca.loanservice.client.CatalogServiceClient;
import com.biblioteca.loanservice.client.NotificationServiceClient;
import com.biblioteca.loanservice.model.Loan;
import com.biblioteca.loanservice.model.LoanStatus;
import com.biblioteca.loanservice.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO verificar CatalagServiceClient e NotificationServiceClient
@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CatalogServiceClient catalogServiceClient;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    public Loan createLoan(Loan loan) {
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
            // ...
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

}
}



