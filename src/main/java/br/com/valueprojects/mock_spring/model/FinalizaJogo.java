package br.com.valueprojects.mock_spring.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import br.com.valueprojects.mock_spring.service.SmsNotificador;
import infra.JogoDao;

/** Finaliza jogos antigos, persiste seus vencedores e notifica-os após a persistência. */
public class FinalizaJogo {

    private int total = 0;
    private final JogoDao dao;
    private final SmsNotificador smsNotificador;

    public FinalizaJogo(JogoDao dao, SmsNotificador smsNotificador) {
        this.dao = Objects.requireNonNull(dao, "dao não pode ser nulo");
        this.smsNotificador = Objects.requireNonNull(smsNotificador, "smsNotificador não pode ser nulo");
    }

    public void finaliza() {
        List<Jogo> jogosParaFinalizar = new ArrayList<>();
        for (Jogo jogo : dao.emAndamento()) {
            if (iniciouSemanaAnterior(jogo)) {
                jogo.identificaVencedor();
                jogosParaFinalizar.add(jogo);
            }
        }

        // Fase 1: finalizar e persistir todos os jogos elegíveis. Se qualquer gravação
        // falhar, a exceção interrompe o fluxo e nenhum SMS será enviado.
        for (Jogo jogo : jogosParaFinalizar) {
            jogo.finaliza();
            dao.atualiza(jogo);
        }

        total += jogosParaFinalizar.size();

        // Fase 2: só é alcançada depois que todas as atualizações do banco terminaram.
        for (Jogo jogo : jogosParaFinalizar) {
            smsNotificador.enviarSms(jogo.getVencedor(), mensagemDeVitoria(jogo));
        }
    }

    private String mensagemDeVitoria(Jogo jogo) {
        return "Parabéns, " + jogo.getVencedor().getNome()
                + "! Você venceu o jogo \"" + jogo.getDescricao() + "\".";
    }

    private boolean iniciouSemanaAnterior(Jogo jogo) {
        return diasEntre(jogo.getData(), Calendar.getInstance()) >= 7;
    }

    private int diasEntre(Calendar inicio, Calendar fim) {
        Calendar data = (Calendar) inicio.clone();
        int diasNoIntervalo = 0;
        while (data.before(fim)) {
            data.add(Calendar.DAY_OF_MONTH, 1);
            diasNoIntervalo++;
        }
        return diasNoIntervalo;
    }

    public int getTotalFinalizados() {
        return total;
    }
}
