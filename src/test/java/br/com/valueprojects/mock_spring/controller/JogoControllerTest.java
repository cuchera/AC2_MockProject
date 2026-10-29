package br.com.valueprojects.mock_spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;

class JogoControllerTest {

    @Test
    void criaListaEJulgaJogo() {
        JogoController controller = new JogoController();
        Jogo jogo = controller.criarJogo("Corrida");
        jogo.anota(new Resultado(new Participante("Ana"), 100));
        jogo.anota(new Resultado(new Participante("Bia"), 200));

        assertEquals("Corrida", jogo.getDescricao());
        assertEquals("Primeiro colocado: 200.0, Último colocado: 100.0", controller.julgarJogo(0));
        assertSame(jogo, controller.listarJogos().get(0));
        assertEquals(1, controller.listarJogos().size());
    }
}
