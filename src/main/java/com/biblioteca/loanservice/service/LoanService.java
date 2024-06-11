package com.biblioteca.loanservice.service;

import com.biblioteca.loanservice.model.Loan;
import com.biblioteca.loanservice.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    public Loan createLoan(Loan loan) {
        // Todo Lógica para verificar a disponibilidade do livro no CatalogService
        // Todo Lógica para registrar o empréstimo no banco de dados
        // Todo Lógica para notificar o usuário sobre a aprovação do empréstimo
        return loanRepository.save(loan);
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
