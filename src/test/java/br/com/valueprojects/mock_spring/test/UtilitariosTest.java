package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Pagamento;
import br.com.valueprojects.mock_spring.model.Participante;

class UtilitariosTest {

    @Test
    void builderConfiguraJogoEExpõeEstadoDoBuilder() {
        Calendar data = Calendar.getInstance();
        Participante participante = new Participante(7, "Bia");
        CriadorDeJogo builder = new CriadorDeJogo();
        assertSame(builder, builder.para("Corrida"));
        assertSame(builder, builder.resultado(participante, 10));
        assertSame(builder, builder.naData(data));
        assertSame(builder, builder.finaliza());
        assertTrue(builder.isFinalizado());
        assertSame(builder, builder.comId(9));
        assertEquals(9, builder.getId());

        Jogo jogo = builder.constroi();
        assertEquals("Corrida", jogo.getDescricao());
        assertEquals(1, jogo.getResultados().size());
        assertSame(participante, jogo.getResultados().get(0).getParticipante());
        assertEquals(10, jogo.getResultados().get(0).getMetrica(), 0.00001);
        assertEquals(data.getTimeInMillis(), jogo.getData().getTimeInMillis());

        CriadorDeJogo builderNovo = new CriadorDeJogo().para("Novo");
        assertFalse(builderNovo.isFinalizado());
    }

    @Test
    void pagamentoRetornaValorEDataInformados() {
        Calendar data = Calendar.getInstance();
        Pagamento pagamento = new Pagamento(123.45, data);
        assertEquals(123.45, pagamento.getValor(), 0.00001);
        assertSame(data, pagamento.getData());
    }
}
