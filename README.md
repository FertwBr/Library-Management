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
@Service
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

    // Endpoint para retorno de um empréstimo
    @PostMapping("/{loanId}/return")
    public ResponseEntity<Void> returnLoan(@PathVariable Long loanId) {
        loanService.returnLoan(loanId);
        return ResponseEntity.noContent().build();
    }

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
            notificationServiceClient.notifyLoanApproval(savedLoan);

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

        try {
            // Notificação de retorno do empréstimo
            notificationServiceClient.notifyLoanReturn(loan);
        } catch (NotificationServiceUnavailableException e) {
            throw new LoanServiceException("Erro ao enviar notificação.", e);
        }
    }
}
```

#### Principais responsabilidades:

Criar Empréstimos (createLoan):

Verifica se o livro está disponível no catálogo.
Envia uma notificação ao usuário caso o livro não esteja disponível.
Registra o empréstimo com status "Aprovado" e define as datas de empréstimo e devolução.
Atualiza o status do livro no catálogo para "Indisponível".
Envia uma notificação ao usuário confirmando a aprovação do empréstimo.
Obter Empréstimos (getAllLoans, getLoanById):

Retorna todos os empréstimos registrados ou um empréstimo específico com base no ID.
Atualizar Empréstimos (updateLoan):

Permite modificar informações de um empréstimo existente, como data de devolução e status.
Valida se a alteração de status para "Devolvido" é permitida somente se o empréstimo estiver "Aprovado".
Deletar Empréstimos (deleteLoan):

Remove um empréstimo do sistema.
Devolver Empréstimos (returnLoan):

Marca um empréstimo como "Devolvido" e registra a data de devolução.
Atualiza o status do livro no catálogo para "Disponível".
Envia uma notificação ao usuário confirmando a devolução do livro.
Gerenciar Exceções:

A classe utiliza exceções personalizadas (BookNotAvailableException, InvalidLoanStatusException, etc.) para lidar com situações como livro indisponível, status de empréstimo inválido e erros na comunicação com serviços externos.

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
