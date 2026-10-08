package infrastructure.http;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Codificador/decodificador JSON mínimo, escrito à mão, só com a biblioteca padrão do Java.
 *
 * Decisão (D-02): um projeto real usaria uma biblioteca (ex. Gson ou Jackson). Optamos por não
 * usar nenhuma aqui por dois motivos: (1) manter o projeto simples e sem dependência externa,
 * como pedido pela disciplina; (2) o ambiente onde este código foi originalmente montado não
 * tem acesso ao Maven Central. Para um projeto real, trocar por Gson é uma troca de poucas
 * linhas, porque só esta classe conhece o formato JSON — o resto do sistema usa Map/List comuns.
 */
public final class Json {
    private Json() {}

    // ---------- codificação (Map/List/String/Number/Boolean/null -> texto JSON) ----------

    public static String encode(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(valor, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void escrever(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof Map) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<String, Object> entrada : ((Map<String, Object>) valor).entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escreverTexto(entrada.getKey(), sb);
                sb.append(':');
                escrever(entrada.getValue(), sb);
            }
            sb.append('}');
        } else if (valor instanceof List) {
            sb.append('[');
            boolean primeiro = true;
            for (Object item : (List<Object>) valor) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escrever(item, sb);
            }
            sb.append(']');
        } else if (valor instanceof String) {
            escreverTexto((String) valor, sb);
        } else if (valor instanceof Boolean || valor instanceof Number) {
            sb.append(valor);
        } else {
            escreverTexto(valor.toString(), sb);
        }
    }

    private static void escreverTexto(String texto, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    // ---------- decodificação (texto JSON -> Map/List/String/Double/Boolean/null) ----------

    public static Map<String, Object> decodeObjeto(String texto) {
        Object valor = new Analisador(texto == null ? "{}" : texto).analisarValor();
        if (!(valor instanceof Map)) {
            throw new IllegalArgumentException("Corpo da requisição precisa ser um objeto JSON.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> mapa = (Map<String, Object>) valor;
        return mapa;
    }

    /** Analisador recursivo simples: suficiente para os DTOs planos deste projeto. */
    private static final class Analisador {
        private final String texto;
        private int pos;

        Analisador(String texto) {
            this.texto = texto;
        }

        Object analisarValor() {
            pularEspacos();
            char c = atual();
            if (c == '{') return analisarObjeto();
            if (c == '[') return analisarLista();
            if (c == '"') return analisarTexto();
            if (texto.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (texto.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (texto.startsWith("null", pos)) { pos += 4; return null; }
            return analisarNumero();
        }

        private Map<String, Object> analisarObjeto() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            pos++; // {
            pularEspacos();
            if (atual() == '}') { pos++; return mapa; }
            while (true) {
                pularEspacos();
                String chave = analisarTexto();
                pularEspacos();
                pos++; // :
                Object valor = analisarValor();
                mapa.put(chave, valor);
                pularEspacos();
                if (atual() == ',') { pos++; continue; }
                pos++; // }
                break;
            }
            return mapa;
        }

        private List<Object> analisarLista() {
            List<Object> lista = new java.util.ArrayList<>();
            pos++; // [
            pularEspacos();
            if (atual() == ']') { pos++; return lista; }
            while (true) {
                lista.add(analisarValor());
                pularEspacos();
                if (atual() == ',') { pos++; continue; }
                pos++; // ]
                break;
            }
            return lista;
        }

        private String analisarTexto() {
            StringBuilder sb = new StringBuilder();
            pos++; // "
            while (atual() != '"') {
                char c = texto.charAt(pos++);
                if (c == '\\') {
                    char esc = texto.charAt(pos++);
                    switch (esc) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'u':
                            sb.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
                            pos += 4;
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            pos++; // "
            return sb.toString();
        }

        private Double analisarNumero() {
            int inicio = pos;
            while (pos < texto.length() && "-+.eE0123456789".indexOf(texto.charAt(pos)) >= 0) pos++;
            return Double.parseDouble(texto.substring(inicio, pos));
        }

        private void pularEspacos() {
            while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) pos++;
        }

        private char atual() {
            return texto.charAt(pos);
        }
    }
}
