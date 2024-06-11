package com.biblioteca.loanservice.service;

import com.biblioteca.loanservice.model.Loan;
import com.biblioteca.loanservice.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    //Todo  ... outros métodos para consultar, atualizar e devolver empréstimos
}
