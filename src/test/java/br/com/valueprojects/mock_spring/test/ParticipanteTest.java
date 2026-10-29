package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Participante;

class ParticipanteTest {

    @Test
    void comparaIdentidadeClasseIdENomeEPermiteNomeNulo() {
        Participante participante = new Participante(1, "Ana");
        assertTrue(participante.equals(participante));
        assertFalse(participante.equals(null));
        assertFalse(participante.equals("Ana"));
        assertFalse(participante.equals(new Participante(2, "Ana")));
        assertFalse(participante.equals(new Participante(1, "Bia")));
        assertTrue(participante.equals(new Participante(1, "Ana")));
        assertEquals(1, participante.getId());
        assertEquals("Ana", participante.getNome());

        Participante semNome = new Participante(3, null);
        assertTrue(semNome.equals(new Participante(3, null)));
        assertFalse(semNome.equals(new Participante(3, "Nome")));
        assertFalse(new Participante(3, "Nome").equals(semNome));
    }
}
