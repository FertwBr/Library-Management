# Library Management

## Descrição
Este é um projeto de gerenciamento de bibliotecas, desenvolvido utilizando Spring Boot.

## Requisitos

- Java 17
- Maven 3.3.0 ou superior

## Tecnologias Utilizadas
- Spring Boot 3.3.0
- Spring Data JPA
- H2 Database
- Lombok
- Spring Cloud Netflix Eureka Client

## Configuração do Projeto

### Passos para configurar o projeto:

1. Clone o repositório:
    ```bash
    git clone https://github.com/FertwBr/Library-Management/
    ```

2. Navegue até o diretório do projeto:
    ```bash
    cd Library-Management
    ```

3. Compile o projeto utilizando Maven:
    ```bash
    mvn clean install
    ```

4. Execute o projeto:
    ```bash
    mvn spring-boot:run
    ```

## Dependências

As principais dependências utilizadas no projeto são:

- `spring-boot-starter-actuator`: fornece funcionalidades de monitoramento e gerenciamento para a aplicação Spring Boot.
- `spring-boot-starter-data-jpa`: simplifica a implementação da camada de persistência de dados utilizando JPA.
- `spring-boot-starter-web`: facilita a criação de aplicações web RESTful com Spring MVC.
- `h2`: banco de dados em memória utilizado para desenvolvimento e testes.
- `lombok`: biblioteca para simplificar a criação de entidades Java com menos código boilerplate.
- `spring-boot-starter-test`: inclui bibliotecas para testes unitários e de integração com Spring Boot.
- `spring-cloud-starter-netflix-eureka-client`: permite que a aplicação se registre como um cliente Eureka para descoberta de serviços.

## Estrutura do Projeto

O projeto segue a estrutura padrão de um projeto Spring Boot:



### Classe: LoanService

```java
public class LoanService {

   // Injeção de dependência do repositório de empréstimos
   @Autowired
   private LoanRepository loanRepository;

   // Injeção de dependência do cliente do serviço de catálogo
   @Autowired
   private CatalogServiceClient catalogServiceClient;

   // Injeção de dependência do cliente do serviço de notificação
   @Autowired
   private NotificationServiceClient notificationServiceClient;

   // Método para criar um novo empréstimo
   public Loan createLoan(Loan loan) {
      // Validação dos IDs de livro e usuário
      if (loan.getBookId() == null || loan.getUserId() == null) {
         throw new IllegalArgumentException("ID do livro e ID do usuário são obrigatórios.");
      }

      try {
         // Verificação da disponibilidade do livro
         int availableCopies = catalogServiceClient.getBookAvailability(loan.getBookId());

         if (availableCopies <= 1) {
            // Notificação de indisponibilidade do livro
            notificationServiceClient.notifyBookUnavailability(loan.getBookId(), loan.getUserId());
            throw new BookNotAvailableException("Livro não disponível para empréstimo. Apenas 1 cópia disponível.");
         }

         // Configuração das datas do empréstimo
         loan.setLoanDate(LocalDate.now());
         loan.setDueDate(LocalDate.now().plusWeeks(2)); // Prazo de 2 semanas (personalizável)
         loan.setStatus(LoanStatus.APPROVED);

         // Salvamento do empréstimo no repositório
         Loan savedLoan = loanRepository.save(loan);

         // Atualização do status do livro e notificação de aprovação do empréstimo
         catalogServiceClient.updateBookStatus(loan.getBookId(), false);
         notifyLoan(savedLoan, "Empréstimo de livro", "O livro foi emprestado com sucesso.");

         return savedLoan;
      } catch (CatalogServiceUnavailableException e) {
         throw new LoanServiceException("O serviço de catálogo está indisponível no momento. Tente novamente mais tarde.", e);
      } catch (NotificationServiceUnavailableException e) {
         throw new LoanServiceException("Erro ao enviar notificação.", e);
      }
   }

   // Método para obter todos os empréstimos
   public List<Loan> getAllLoans() {
      return loanRepository.findAll();
   }

   // Método para obter um empréstimo por ID
   public Loan getLoanById(Long id) {
      return loanRepository.findById(id)
              .orElseThrow(() -> new LoanNotFoundException("Empréstimo não encontrado com o ID: " + id));
   }

   // Método para atualizar um empréstimo
   public Loan updateLoan(Long id, Loan loanDetails) {
      Loan loan = getLoanById(id);

      // Validação do status do empréstimo
      if (loanDetails.getStatus() == LoanStatus.RETURNED && loan.getStatus() != LoanStatus.APPROVED) {
         throw new InvalidLoanStatusException("Não é possível marcar um empréstimo como devolvido se ele não estiver aprovado.");
      }

      // Atualização dos detalhes do empréstimo
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

   // Método para deletar um empréstimo
   public boolean deleteLoan(Long id) {
      Loan loan = getLoanById(id);
      loanRepository.delete(loan);
      return true;
   }

   // Método para retornar um empréstimo
   public void returnLoan(Long loanId) {
      Loan loan = loanRepository.findById(loanId)
              .orElseThrow(() -> new LoanNotFoundException("Empréstimo não encontrado."));

      // Validação do status do empréstimo
      if (loan.getStatus() != LoanStatus.APPROVED) {
         throw new InvalidLoanStatusException("Não é possível devolver um empréstimo que não está aprovado.");
      }

      // Atualização do status e data de retorno do empréstimo
      loan.setStatus(LoanStatus.RETURNED);
      loan.setReturnDate(LocalDate.now());
      loanRepository.save(loan);

      try {
         // Atualização do status do livro no catálogo
         catalogServiceClient.updateBookStatus(loan.getBookId(), true);
      } catch (CatalogServiceUnavailableException e) {
         throw new LoanServiceException("O serviço de catálogo está indisponível no momento. Tente novamente mais tarde.", e);
      }

      // Notificação de retorno do empréstimo
      notifyLoan(loan, "Devolução de livro", "O livro foi devolvido com sucesso.");
   }

   // Método auxiliar para enviar notificações de empréstimo
   private void notifyLoan(Loan loan, String subject, String text) {
      Email email = new Email();
      email.setOwner("Carlos");
      email.setEmailFrom("carrlosreservateste@gmail.com");
      email.setEmailTo("carrlosreserva@gmail.com");
      email.setSubject(subject);
      email.setText(text);

      notificationServiceClient.notifyLoan(email);
   }
}
```

## Contribuição

Para contribuir com este projeto, siga os passos abaixo:

1. Faça um fork do projeto.
2. Crie uma branch para a sua feature:
    ```bash
    git checkout -b minha-feature
    ```
3. Faça commit das suas mudanças:
    ```bash
    git commit -m 'Adicionei uma nova feature'
    ```
4. Faça push para a branch:
    ```bash
    git push origin minha-feature
    ```
5. Abra um Pull Request.

## Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para mais detalhes.
```
