package infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;

class JogoDaoTest {

    private Connection conexao;
    private JogoDao dao;

    @BeforeEach
    void preparaDao() {
        conexao = mock(Connection.class);
        dao = new JogoDao(conexao);
    }

    @Test
    void construtorPadraoAbreConexaoComDriverManager() throws Exception {
        Connection conexaoDriver = mock(Connection.class);
        try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
            driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conexaoDriver);
            assertNotNull(new JogoDao());
            driver.verify(() -> DriverManager.getConnection("jdbc:mysql://localhost/mocks", "root", ""));
        }
    }

    @Test
    void encapsulaFalhaAoAbrirConexaoERejeitaConexaoNula() throws Exception {
        SQLException erroConexao = new SQLException("conexão indisponível");
        try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
            driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenThrow(erroConexao);
            assertThrows(RuntimeException.class, JogoDao::new);
        }
        assertThrows(NullPointerException.class, () -> new JogoDao(null));
    }

    @Test
    void salvaJogoSemResultadosMesmoQuandoBancoNaoRetornaChaveGerada() throws Exception {
        PreparedStatement insert = mock(PreparedStatement.class);
        ResultSet chaves = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(insert);
        when(insert.getGeneratedKeys()).thenReturn(chaves);
        when(chaves.next()).thenReturn(false);
        Jogo jogo = new Jogo("Sem resultados");

        dao.salva(jogo);

        assertEquals(0, jogo.getId());
        verify(insert).setString(1, "Sem resultados");
        verify(insert).setDate(eq(2), org.mockito.ArgumentMatchers.any(Date.class));
        verify(insert).setBoolean(3, false);
        verify(insert).execute();
        verify(insert).close();
    }

    @Test
    void salvaJogoComResultadosEUsaChaveGeradaNasLinhasDeResultado() throws Exception {
        PreparedStatement insertJogo = mock(PreparedStatement.class);
        PreparedStatement insertResultado = mock(PreparedStatement.class);
        ResultSet chaves = mock(ResultSet.class);
        when(conexao.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(insertJogo);
        when(conexao.prepareStatement(startsWith("INSERT INTO RESULTADOS"))).thenReturn(insertResultado);
        when(insertJogo.getGeneratedKeys()).thenReturn(chaves);
        when(chaves.next()).thenReturn(true);
        when(chaves.getInt(1)).thenReturn(73);
        Participante participante = new Participante(9, "Bia");
        Jogo jogo = new Jogo("Corrida");
        jogo.anota(new Resultado(participante, 15.5));

        dao.salva(jogo);

        assertEquals(73, jogo.getId());
        verify(insertResultado).setInt(1, 73);
        verify(insertResultado).setInt(2, 9);
        verify(insertResultado).setDouble(3, 15.5);
        verify(insertResultado).execute();
        verify(insertResultado).close();
        verify(insertJogo).close();
    }

    @Test
    void encapsulaFalhaSqlAoSalvar() throws Exception {
        when(conexao.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("insert falhou"));
        assertThrows(RuntimeException.class, () -> dao.salva(new Jogo("Falha")));
    }

    @Test
    void leJogoFinalizadoComParticipanteResultadoEData() throws Exception {
        PreparedStatement consultaJogos = mock(PreparedStatement.class);
        PreparedStatement consultaResultados = mock(PreparedStatement.class);
        ResultSet jogos = mock(ResultSet.class);
        ResultSet resultados = mock(ResultSet.class);
        when(conexao.prepareStatement(startsWith("SELECT * FROM JOGO"))).thenReturn(consultaJogos);
        when(conexao.prepareStatement(startsWith("SELECT VALOR"))).thenReturn(consultaResultados);
        when(consultaJogos.executeQuery()).thenReturn(jogos);
        when(consultaResultados.executeQuery()).thenReturn(resultados);
        when(jogos.next()).thenReturn(true, false);
        when(jogos.getString("descricao")).thenReturn("Finalizado");
        when(jogos.getDate("data")).thenReturn(Date.valueOf(LocalDate.of(2026, 10, 20)));
        when(jogos.getInt("id")).thenReturn(8);
        when(jogos.getBoolean("finalizado")).thenReturn(true);
        when(resultados.next()).thenReturn(true, false);
        when(resultados.getInt("participante_id")).thenReturn(44);
        when(resultados.getString("nome")).thenReturn("Ana");
        when(resultados.getDouble("metrica")).thenReturn(25.0);

        List<Jogo> finalizados = dao.finalizados();

        assertEquals(1, finalizados.size());
        Jogo jogo = finalizados.get(0);
        assertTrue(jogo.isFinalizado());
        assertEquals(8, jogo.getId());
        assertEquals("Finalizado", jogo.getDescricao());
        assertEquals(1, jogo.getResultados().size());
        assertEquals(44, jogo.getResultados().get(0).getParticipante().getId());
        assertEquals("Ana", jogo.getResultados().get(0).getParticipante().getNome());
        assertEquals(25.0, jogo.getResultados().get(0).getMetrica(), 0.00001);
        assertEquals(Date.valueOf(LocalDate.of(2026, 10, 20)).getTime(), jogo.getData().getTimeInMillis());
        verify(consultaJogos).close();
        verify(consultaResultados).close();
        verify(jogos).close();
        verify(resultados).close();
    }

    @Test
    void leJogoEmAndamentoSemResultadosEConsultaVazia() throws Exception {
        PreparedStatement consultaJogos = mock(PreparedStatement.class);
        PreparedStatement consultaResultados = mock(PreparedStatement.class);
        ResultSet jogos = mock(ResultSet.class);
        ResultSet resultados = mock(ResultSet.class);
        when(conexao.prepareStatement(startsWith("SELECT * FROM JOGO"))).thenReturn(consultaJogos);
        when(conexao.prepareStatement(startsWith("SELECT VALOR"))).thenReturn(consultaResultados);
        when(consultaJogos.executeQuery()).thenReturn(jogos);
        when(consultaResultados.executeQuery()).thenReturn(resultados);
        when(jogos.next()).thenReturn(true, false);
        when(jogos.getString("descricao")).thenReturn("Em andamento");
        when(jogos.getDate("data")).thenReturn(Date.valueOf(LocalDate.of(2026, 10, 20)));
        when(jogos.getInt("id")).thenReturn(9);
        when(jogos.getBoolean("finalizado")).thenReturn(false);
        when(resultados.next()).thenReturn(false);

        List<Jogo> andamento = dao.emAndamento();
        assertEquals(1, andamento.size());
        assertFalse(andamento.get(0).isFinalizado());
        assertTrue(andamento.get(0).getResultados().isEmpty());

        PreparedStatement consultaVazia = mock(PreparedStatement.class);
        ResultSet semJogos = mock(ResultSet.class);
        when(conexao.prepareStatement(startsWith("SELECT * FROM JOGO"))).thenReturn(consultaVazia);
        when(consultaVazia.executeQuery()).thenReturn(semJogos);
        when(semJogos.next()).thenReturn(false);
        assertTrue(dao.emAndamento().isEmpty());
    }

    @Test
    void encapsulaFalhaSqlAoConsultarJogos() throws Exception {
        when(conexao.prepareStatement(anyString())).thenThrow(new SQLException("select falhou"));
        assertThrows(RuntimeException.class, dao::finalizados);
        assertThrows(RuntimeException.class, dao::emAndamento);
    }

    @Test
    void atualizaJogoComVencedorOuSemVencedor() throws Exception {
        PreparedStatement update = mock(PreparedStatement.class);
        when(conexao.prepareStatement(startsWith("UPDATE JOGO"))).thenReturn(update);
        Jogo semVencedor = new Jogo("Ainda sem vencedor");
        semVencedor.setId(5);

        dao.atualiza(semVencedor);
        verify(update).setNull(4, java.sql.Types.INTEGER);
        verify(update).setInt(5, 5);
        verify(update).execute();

        Jogo comVencedor = new Jogo("Concluído");
        comVencedor.setId(6);
        Participante vencedor = new Participante(99, "Vencedor");
        comVencedor.anota(new Resultado(vencedor, 10));
        comVencedor.identificaVencedor();
        comVencedor.finaliza();
        dao.atualiza(comVencedor);
        verify(update).setInt(4, 99);
        verify(update).setInt(5, 6);
        verify(update).setBoolean(3, true);
    }

    @Test
    void encapsulaFalhaSqlAoAtualizar() throws Exception {
        when(conexao.prepareStatement(anyString())).thenThrow(new SQLException("update falhou"));
        assertThrows(RuntimeException.class, () -> dao.atualiza(new Jogo("Falha")));
    }
}
