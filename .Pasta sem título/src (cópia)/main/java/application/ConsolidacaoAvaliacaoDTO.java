package application;

import domain.model.avaliacao.RespostaQuestionario;
import domain.model.avaliacao.Pergunta;
import domain.model.avaliacao.Questionario;
import domain.model.avaliacao.TipoPergunta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RF-28: consolidação dos resultados — quantidades, distribuições e comentários textuais.
 * É calculado aqui, não no domínio puro (Questionario não sabe de RespostaQuestionario — baixo
 * acoplamento) e não na API (separação de responsabilidades).
 */
public final class ConsolidacaoAvaliacaoDTO {
    private final String tituloQuestionario;
    private final int totalRespostas;
    private final List<Map<String, Object>> perguntas;

    private ConsolidacaoAvaliacaoDTO(String tituloQuestionario, int totalRespostas, List<Map<String, Object>> perguntas) {
        this.tituloQuestionario = tituloQuestionario;
        this.totalRespostas = totalRespostas;
        this.perguntas = perguntas;
    }

    public static ConsolidacaoAvaliacaoDTO calcular(Questionario questionario, List<RespostaQuestionario> respostas) {
        List<Map<String, Object>> perguntasConsolidadas = new ArrayList<>();

        for (Pergunta pergunta : questionario.getPerguntas()) {
            List<String> valores = respostas.stream()
                    .flatMap(r -> r.getRespostas().stream())
                    .filter(r -> r.getPerguntaId().equals(pergunta.getId()))
                    .map(r -> r.getValor())
                    .collect(Collectors.toList());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("enunciado", pergunta.getEnunciado());
            item.put("tipo", pergunta.getTipo().name());
            item.put("totalRespostas", valores.size());

            if (pergunta.getTipo() == TipoPergunta.ESCALA_NUMERICA && !valores.isEmpty()) {
                double media = valores.stream().mapToInt(Integer::parseInt).average().orElse(0);
                item.put("media", Math.round(media * 10.0) / 10.0);
            } else if (pergunta.getTipo() == TipoPergunta.ESCOLHA_UNICA) {
                Map<String, Long> distribuicao = valores.stream()
                        .collect(Collectors.groupingBy(v -> v, Collectors.counting()));
                item.put("distribuicao", distribuicao);
            } else {
                // TEXTO_LIVRE: devolve todos os comentários (RN-15: política de identificação
                // declarada antes do envio; aqui devolvemos os textos sem o id do participante)
                item.put("comentarios", valores);
            }

            perguntasConsolidadas.add(item);
        }

        return new ConsolidacaoAvaliacaoDTO(questionario.getTitulo(), respostas.size(), perguntasConsolidadas);
    }

    public Map<String, Object> paraMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("questionario", tituloQuestionario);
        mapa.put("totalRespostas", totalRespostas);
        mapa.put("perguntas", perguntas);
        return mapa;
    }
}
