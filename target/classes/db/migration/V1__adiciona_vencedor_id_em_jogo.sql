-- Executar uma vez no banco antes de usar JogoDao.atualiza().
-- A aplicação não possui Flyway configurado; este arquivo é a migração de referência.
ALTER TABLE JOGO
    ADD COLUMN VENCEDOR_ID INT NULL,
    ADD CONSTRAINT FK_JOGO_VENCEDOR
        FOREIGN KEY (VENCEDOR_ID) REFERENCES PARTICIPANTE (ID);
