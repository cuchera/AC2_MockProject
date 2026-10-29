package br.com.valueprojects.mock_spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import br.com.valueprojects.mock_spring.model.FinalizaJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;
import br.com.valueprojects.mock_spring.service.SmsNotificador;
import infra.JogoDao;

class FinalizaJogoTest {

    private JogoDao dao;
    private SmsNotificador smsNotificador;

    @BeforeEach
    void configuraDependencias() {
        dao = org.mockito.Mockito.mock(JogoDao.class);
        smsNotificador = org.mockito.Mockito.mock(SmsNotificador.class);
    }

    @Test
    void persisteTodosOsJogosESeusVencedoresAntesDeEnviarQualquerSms() {
        Jogo corrida = jogoAntigo("Corrida", 10, 20);
        Jogo quebraCabeca = jogoAntigo("Quebra-cabeça", 15, 30);
        Jogo jogoAindaRecente = jogoRecenteSemResultados("Jogo recente");
        when(dao.emAndamento()).thenReturn(Arrays.asList(corrida, quebraCabeca, jogoAindaRecente));

        FinalizaJogo finalizador = new FinalizaJogo(dao, smsNotificador);
        finalizador.finaliza();

        assertTrue(corrida.isFinalizado());
        assertTrue(quebraCabeca.isFinalizado());
        assertFalse(jogoAindaRecente.isFinalizado());
        assertSame(corrida.getResultados().get(1).getParticipante(), corrida.getVencedor());
        assertSame(quebraCabeca.getResultados().get(1).getParticipante(), quebraCabeca.getVencedor());
        assertEquals(2, finalizador.getTotalFinalizados());

        InOrder ordem = inOrder(dao, smsNotificador);
        ordem.verify(dao).atualiza(corrida);
        ordem.verify(dao).atualiza(quebraCabeca);
        ordem.verify(smsNotificador).enviarSms(corrida.getVencedor(),
                "Parabéns, Vencedor Corrida! Você venceu o jogo \"Corrida\".");
        ordem.verify(smsNotificador).enviarSms(quebraCabeca.getVencedor(),
                "Parabéns, Vencedor Quebra-cabeça! Você venceu o jogo \"Quebra-cabeça\".");
        verify(dao, never()).atualiza(jogoAindaRecente);
    }

    @Test
    void naoPersisteNemNotificaQuandoNaoHaJogoDaSemanaAnterior() {
        when(dao.emAndamento()).thenReturn(List.of(jogoRecenteSemResultados("Ainda em andamento")));

        FinalizaJogo finalizador = new FinalizaJogo(dao, smsNotificador);
        finalizador.finaliza();

        assertEquals(0, finalizador.getTotalFinalizados());
        verify(dao, never()).atualiza(org.mockito.ArgumentMatchers.any(Jogo.class));
        verifyNoInteractions(smsNotificador);
    }

    @Test
    void falhaAoSalvarImpedeEnvioDeSmsMesmoQueUmJogoAnteriorJaTenhaSidoSalvo() {
        Jogo primeiro = jogoAntigo("Primeiro", 1, 2);
        Jogo segundo = jogoAntigo("Segundo", 3, 4);
        when(dao.emAndamento()).thenReturn(Arrays.asList(primeiro, segundo));
        doThrow(new IllegalStateException("Falha no banco de dados")).when(dao).atualiza(segundo);

        FinalizaJogo finalizador = new FinalizaJogo(dao, smsNotificador);
        assertThrows(IllegalStateException.class, finalizador::finaliza);

        verify(dao).atualiza(primeiro);
        verify(dao).atualiza(segundo);
        verifyNoInteractions(smsNotificador);
        assertEquals(0, finalizador.getTotalFinalizados());
    }

    @Test
    void jogoAntigoSemResultadosNaoPodeSerSalvoNemNotificado() {
        Jogo semResultados = new Jogo("Sem resultados", dataHaDias(10));
        when(dao.emAndamento()).thenReturn(List.of(semResultados));

        FinalizaJogo finalizador = new FinalizaJogo(dao, smsNotificador);
        assertThrows(IllegalStateException.class, finalizador::finaliza);

        assertFalse(semResultados.isFinalizado());
        verify(dao, never()).atualiza(org.mockito.ArgumentMatchers.any(Jogo.class));
        verifyNoInteractions(smsNotificador);
        assertEquals(0, finalizador.getTotalFinalizados());
    }

    private Jogo jogoAntigo(String descricao, double metricaPerdedor, double metricaVencedor) {
        Jogo jogo = new Jogo(descricao, dataHaDias(10));
        jogo.anota(new Resultado(new Participante("Participante " + descricao), metricaPerdedor));
        jogo.anota(new Resultado(new Participante("Vencedor " + descricao), metricaVencedor));
        return jogo;
    }

    private Jogo jogoRecenteSemResultados(String descricao) {
        return new Jogo(descricao, dataHaDias(2));
    }

    private Calendar dataHaDias(int dias) {
        Calendar data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_MONTH, -dias);
        return data;
    }
}
