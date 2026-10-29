package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;

class JogoTest {

    @Test
    void constroiJogoEExpõeDadosSemPermitirAlterarListaDeResultados() {
        Calendar data = Calendar.getInstance();
        Jogo jogo = new Jogo("Jogo de corrida", data);
        Participante participante = new Participante(7, "Leonardo");
        Resultado resultado = new Resultado(participante, 150.0);

        assertEquals("Jogo de corrida", jogo.getDescricao());
        assertEquals(0, jogo.getResultados().size());
        jogo.anota(resultado);
        assertEquals(1, jogo.getResultados().size());
        assertSame(resultado, jogo.getResultados().get(0));
        assertEquals(150.0, jogo.getResultados().get(0).getMetrica(), 0.00001);
        assertSame(participante, jogo.getResultados().get(0).getParticipante());
        assertThrows(UnsupportedOperationException.class,
                () -> jogo.getResultados().add(new Resultado(participante, 99)));

        Calendar dataObtida = jogo.getData();
        assertEquals(data.getTimeInMillis(), dataObtida.getTimeInMillis());
        assertNotSame(dataObtida, jogo.getData());
        dataObtida.add(Calendar.DAY_OF_MONTH, 1);
        assertNotSame(dataObtida.getTimeInMillis(), jogo.getData().getTimeInMillis());

        jogo.setData(dataObtida);
        assertEquals(dataObtida.getTimeInMillis(), jogo.getData().getTimeInMillis());
        jogo.setId(42);
        assertEquals(42, jogo.getId());
        assertFalse(jogo.isFinalizado());
        jogo.finaliza();
        assertTrue(jogo.isFinalizado());
        assertEquals("Leonardo", participante.getNome());
        assertEquals(7, participante.getId());
        assertEquals(42, jogo.getId());
        assertEquals(null, jogo.getVencedor());
    }

    @Test
    void aceitaResultadosAlternadosERejeitaResultadoConsecutivoDoMesmoParticipante() {
        Jogo jogo = new Jogo("Alternância");
        Participante ana = new Participante("Ana");
        Participante bia = new Participante("Bia");

        jogo.anota(new Resultado(ana, 1));
        jogo.anota(new Resultado(ana, 2)); // mesmo participante consecutivo: ignorado
        assertEquals(1, jogo.getResultados().size());

        jogo.anota(new Resultado(bia, 3));
        jogo.anota(new Resultado(ana, 4)); // mesmo participante, mas não consecutivo: aceito
        assertEquals(3, jogo.getResultados().size());
    }

    @Test
    void limitaCadaParticipanteAcincoResultados() {
        Jogo jogo = new Jogo("Limite");
        Participante ana = new Participante("Ana");
        Participante bia = new Participante("Bia");
        Participante cida = new Participante("Cida");

        jogo.anota(new Resultado(ana, 1));
        jogo.anota(new Resultado(bia, 1));
        jogo.anota(new Resultado(ana, 2));
        jogo.anota(new Resultado(bia, 2));
        jogo.anota(new Resultado(ana, 3));
        jogo.anota(new Resultado(bia, 3));
        jogo.anota(new Resultado(ana, 4));
        jogo.anota(new Resultado(bia, 4));
        jogo.anota(new Resultado(ana, 5));
        jogo.anota(new Resultado(cida, 1));
        jogo.anota(new Resultado(ana, 6)); // já possui cinco resultados: ignorado

        assertEquals(10, jogo.getResultados().size());
    }

    @Test
    void identificaMaiorMetricaEEmEmpateMantemPrimeiroResultado() {
        Jogo jogo = new Jogo("Vencedor");
        Participante primeiro = new Participante("Primeiro");
        Participante vencedor = new Participante("Vencedor");
        jogo.anota(new Resultado(primeiro, 8));
        jogo.anota(new Resultado(vencedor, 12));
        assertSame(vencedor, jogo.identificaVencedor());
        assertSame(vencedor, jogo.getVencedor());

        Jogo empate = new Jogo("Empate");
        empate.anota(new Resultado(primeiro, 12));
        empate.anota(new Resultado(vencedor, 12));
        assertSame(primeiro, empate.identificaVencedor());
    }

    @Test
    void naoIdentificaVencedorSemResultados() {
        Jogo jogo = new Jogo("Vazio");
        assertThrows(IllegalStateException.class, jogo::identificaVencedor);
    }
}
