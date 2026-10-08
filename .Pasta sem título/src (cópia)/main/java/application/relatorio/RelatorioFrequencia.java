package application.relatorio;

import domain.model.Atividade;
import domain.model.Inscricao;
import domain.model.RegistroPresenca;
import domain.ports.InscricaoRepository;
import domain.ports.RegistroFrequenciaRepository;
import domain.ports.UsuarioRepository;

import java.util.List;

/** RF-30: relatório de frequência e situação por participante/atividade. */
public class RelatorioFrequencia implements RelatorioExportavel {

    private final List<Inscricao> inscricoes;
    private final UsuarioRepository usuarioRepository;
    private final RegistroFrequenciaRepository registroFrequenciaRepository;
    private final Atividade atividade;

    public RelatorioFrequencia(List<Inscricao> inscricoes, UsuarioRepository usuarioRepository,
                                RegistroFrequenciaRepository registroFrequenciaRepository, Atividade atividade) {
        this.inscricoes = inscricoes;
        this.usuarioRepository = usuarioRepository;
        this.registroFrequenciaRepository = registroFrequenciaRepository;
        this.atividade = atividade;
    }

    @Override
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("atividade;participanteId;nome;totalMarcacoes;presente\n");
        for (Inscricao i : inscricoes) {
            String nome = usuarioRepository.buscarPorId(i.getParticipanteId())
                    .map(u -> u.getNome()).orElse("(não encontrado)");
            List<RegistroPresenca> registros = registroFrequenciaRepository.listarPorInscricao(i.getId());
            boolean presente = atividade.getCriterioFrequencia().validador().isPresencaValida(registros);
            sb.append(escaparCsv(atividade.getTitulo())).append(";")
              .append(i.getParticipanteId()).append(";")
              .append(escaparCsv(nome)).append(";")
              .append(registros.size()).append(";")
              .append(presente ? "SIM" : "NAO").append("\n");
        }
        return sb.toString();
    }

    @Override
    public String nomeArquivo() {
        return "relatorio_frequencia.csv";
    }

    private static String escaparCsv(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n"))
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        return valor;
    }
}
