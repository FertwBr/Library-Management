package com.biblioteca.loanservice.repository;

import com.biblioteca.loanservice.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> { }