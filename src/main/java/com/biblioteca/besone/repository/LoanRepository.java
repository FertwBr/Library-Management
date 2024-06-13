package com.biblioteca.besone.repository;

import com.biblioteca.besone.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> { }