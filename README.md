# AC2_MockProject

# Finalização de jogos — testes e cobertura

Projeto Spring Boot usado para implementar e testar a regra de finalização dos jogos e a notificação dos vencedores.

# Regra de negócio

O serviço FinalizaJogo procura jogos em andamento com pelo menos sete dias de idade. Para cada jogo elegível, identifica como vencedor o participante com a maior métrica e marca o jogo como finalizado.

O fluxo tem duas fases:

1. Finaliza e persiste todos os jogos elegíveis, incluindo o ID do vencedor.

2. Somente depois que todas as gravações terminam com sucesso, envia o SMS a cada vencedor.

Se qualquer gravação falhar, o fluxo é interrompido e nenhum SMS é enviado. Jogos recentes permanecem em andamento. Um jogo antigo sem resultados não é persistido nem notificado, pois não é possível identificar um vencedor.

Estrutura relevante

•
src/main/java/br/com/valueprojects/mock_spring/model/FinalizaJogo.java: coordena a identificação, persistência e notificação.

•
src/main/java/br/com/valueprojects/mock_spring/model/Jogo.java: registra resultados, identifica o vencedor e mantém o estado de finalização.

•
src/main/java/br/com/valueprojects/mock_spring/service/SmsNotificador.java: contrato do gateway de envio de SMS.

•
src/main/java/infra/JogoDao.java: persistência JDBC; oferece construtor com Connection para injeção em testes.

•
src/main/resources/db/migration/V1__adiciona_vencedor_id_em_jogo.sql: script para criar a coluna de vencedor no banco.

# Testes e mocks

Os testes automatizados ficam em src/test/java.

# Regra de negócio

Em br/com/valueprojects/mock_spring/FinalizaJogoTest.java:

•
JogoDao e SmsNotificador são mocks Mockito.

•
when(dao.emAndamento()).thenReturn(...) fornece os jogos de teste.

•
doThrow(...).when(dao).atualiza(...) simula falha de persistência.

•
InOrder comprova que todas as atualizações do DAO ocorrem antes dos envios de SMS.

•
Os cenários cobrem jogos elegíveis e recentes, falha de gravação e jogo sem resultados.

# Persistência JDBC

Em infra/JogoDaoTest.java, Connection, PreparedStatement e ResultSet são mocks. DriverManager é mockado estaticamente para testar a criação da conexão sem acessar um banco real. Assim, os caminhos de consulta, gravação, chave gerada, atualização e erro SQL são testados sem uma conexão MySQL.

# Outras classes

•
MockSpringApplicationTest.java mocka estaticamente SpringApplication para testar o método main sem iniciar um servidor.

•
JogoControllerTest.java usa instâncias reais das classes de domínio em memória; não há integração externa a isolar nesse teste.

•
Os demais testes cobrem entidades, regras do domínio e builder.

•
TesteUnidade2ApplicationTests.java inicializa o contexto Spring intencionalmente; é um teste de contexto, não um teste unitário com dependências mockadas.

Executar testes e gerar o relatório

Requisitos: JDK 17 ou superior.

Na raiz do projeto, execute:

Bash

bash ./mvnw clean verify

O comando executa a suíte, gera o relatório HTML e valida os limites de cobertura configurados. O relatório fica em:

# Plain Text

target/site/jacoco/index.html

A fase verify falha se a cobertura global de linhas, ramificações ou instruções ficar abaixo de 100%.

# Banco de dados

O DAO padrão conecta a jdbc:mysql://localhost/mocks com o usuário root e senha vazia. Antes de usar essa integração, confira as credenciais e o banco no ambiente e aplique o script SQL de migração para adicionar JOGO.VENCEDOR_ID.

O DAO foi testado com mocks JDBC; esses testes não validam conectividade ou compatibilidade com uma instância real do MySQL.

Integração de SMS

SmsNotificador define o contrato usado pelo finalizador e é mockado nos testes. O projeto não inclui um provedor ou uma implementação real de SMS. Para enviar mensagens em produção, é necessário implementar esse contrato com o serviço de SMS escolhido e configurar a injeção dessa implementação na aplicação.
