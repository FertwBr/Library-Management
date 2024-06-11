import com.biblioteca.loanservice.model.Loan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@GetMapping
public ResponseEntity<List<Loan>> getAllLoans() {
    List<Loan> loans = loanService.getAllLoans();
    return new ResponseEntity<>(loans, HttpStatus.OK);
}

@GetMapping("/{id}")
public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
    Loan loan = loanService.getLoanById(id);
    if (loan == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(loan, HttpStatus.OK);
}

@PutMapping("/{id}")
public ResponseEntity<Loan> updateLoan(@PathVariable Long id, @RequestBody Loan loanDetails) {
    Loan updatedLoan = loanService.updateLoan(id, loanDetails);
    if (updatedLoan == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
    boolean deleted = loanService.deleteLoan(id);
    if (!deleted) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
}
