package application.relatorio;

import domain.model.Inscricao;
import domain.ports.InscricaoRepository;
import domain.ports.UsuarioRepository;

import java.util.List;
import java.util.UUID;

/** RF-29: relatório de inscritos por evento/atividade, exportável como CSV (RF-31). */
public class RelatorioInscricoes implements RelatorioExportavel {

    private final List<Inscricao> inscricoes;
    private final UsuarioRepository usuarioRepository;
    private final String tituloAtividade;

    public RelatorioInscricoes(List<Inscricao> inscricoes, UsuarioRepository usuarioRepository, String tituloAtividade) {
        this.inscricoes = inscricoes;
        this.usuarioRepository = usuarioRepository;
        this.tituloAtividade = tituloAtividade;
    }

    @Override
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("atividade;participanteId;nome;email;situacao\n");
        for (Inscricao i : inscricoes) {
            String nome = "", email = "";
            var usuario = usuarioRepository.buscarPorId(i.getParticipanteId());
            if (usuario.isPresent()) {
                nome = usuario.get().getNome();
                email = usuario.get().getEmail().getEndereco();
            }
            sb.append(escaparCsv(tituloAtividade)).append(";")
              .append(i.getParticipanteId()).append(";")
              .append(escaparCsv(nome)).append(";")
              .append(escaparCsv(email)).append(";")
              .append(i.getSituacao().name()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String nomeArquivo() {
        return "relatorio_inscricoes.csv";
    }

    private static String escaparCsv(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n"))
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        return valor;
    }
}
