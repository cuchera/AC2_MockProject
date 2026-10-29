package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Juiz;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;

class JulgamentoTest {

    @Test
    void encontraMaiorEMenorMetricaComAtualizacoesEValoresQueNaoAtualizamOsLimites() {
        Juiz juiz = new Juiz();
        Jogo jogo = new Jogo("Derruba barreiras");
        jogo.anota(new Resultado(new Participante("João"), 90));
        jogo.anota(new Resultado(new Participante("Pedro"), 95));
        jogo.anota(new Resultado(new Participante("Katia"), 92));
        jogo.anota(new Resultado(new Participante("Maria"), 88));

        juiz.julga(jogo);

        assertEquals(95, juiz.getPrimeiroColocado(), 0.00001);
        assertEquals(88, juiz.getUltimoColocado(), 0.00001);
    }

    @Test
    void naoJulgaJogoSemResultado() {
        Juiz juiz = new Juiz();
        assertThrows(RuntimeException.class, () -> juiz.julga(new Jogo("Sem resultados")));
    }
}
