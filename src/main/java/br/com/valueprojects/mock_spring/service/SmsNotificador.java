package br.com.valueprojects.mock_spring.service;

import br.com.valueprojects.mock_spring.model.Participante;

/** Gateway externo para envio de SMS ao participante vencedor. */
public interface SmsNotificador {

    void enviarSms(Participante destinatario, String mensagem);
}
