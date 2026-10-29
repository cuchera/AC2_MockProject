package infra;

import java.util.ArrayList;
import java.util.List;

import br.com.valueprojects.mock_spring.model.Jogo;

/** Implementação em memória usada por testes e exemplos didáticos. */
public class JogoDaoFalso {

    private final List<Jogo> jogos = new ArrayList<>();

    public void salva(Jogo jogo) {
        jogos.add(jogo);
    }

    public List<Jogo> finalizados() {
        List<Jogo> selecionados = new ArrayList<>();
        for (Jogo jogo : jogos) {
            if (jogo.isFinalizado()) {
                selecionados.add(jogo);
            }
        }
        return selecionados;
    }

    public List<Jogo> emAndamento() {
        List<Jogo> selecionados = new ArrayList<>();
        for (Jogo jogo : jogos) {
            if (!jogo.isFinalizado()) {
                selecionados.add(jogo);
            }
        }
        return selecionados;
    }

    public void atualiza(Jogo jogo) {
        // Os objetos são mantidos por referência; portanto, não é necessária atualização.
    }
}
