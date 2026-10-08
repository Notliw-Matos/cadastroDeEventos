package application.relatorio;

/**
 * Porta de saída interna: cada relatório sabe se serializar em pelo menos um formato portátil
 * (RF-31). A direção da dependência é do caso de uso para esta interface, nunca o contrário.
 *
 * Decisão de ROO-07 (interfaces em pontos de variação): foi discutida uma segunda alternativa,
 * que era ter um enum de formatos e um if/switch no caso de uso. Foi descartada porque uma nova
 * exportação (ex. PDF no S8) exigiria mexer no caso de uso. Com a interface, só se acrescenta
 * um novo implementador — o caso de uso não muda.
 */
public interface RelatorioExportavel {
    /** Retorna o conteúdo do relatório como CSV (separador ponto-e-vírgula, UTF-8). */
    String toCsv();

    /** Cabeçalho para exibição e download — ex.: "relatorio_inscricoes.csv". */
    String nomeArquivo();
}
