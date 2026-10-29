package infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Jogo;

class JogoDaoFalsoTest {

    @Test
    void salvaFiltraEAtualizaObjetosMantidosEmMemoria() {
        JogoDaoFalso dao = new JogoDaoFalso();
        Jogo finalizado = new Jogo("Finalizado");
        finalizado.finaliza();
        Jogo emAndamento = new Jogo("Em andamento");
        dao.salva(finalizado);
        dao.salva(emAndamento);

        assertEquals(1, dao.finalizados().size());
        assertSame(finalizado, dao.finalizados().get(0));
        assertEquals(1, dao.emAndamento().size());
        assertSame(emAndamento, dao.emAndamento().get(0));
        dao.atualiza(emAndamento);
        assertEquals(2, dao.finalizados().size() + dao.emAndamento().size());
    }

    @Test
    void listasFicamVaziasQuandoNadaFoiSalvo() {
        JogoDaoFalso dao = new JogoDaoFalso();
        assertEquals(0, dao.finalizados().size());
        assertEquals(0, dao.emAndamento().size());
    }
}
