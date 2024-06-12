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
