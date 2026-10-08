package domain.model;

import domain.exception.AcessoNegadoException;
import domain.exception.EstadoInvalidoException;
import domain.exception.ValorInvalidoException;
import domain.vo.IntervaloTempo;

import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

public class Evento {

    /** RN-04: o estado controla visibilidade e abertura de inscrições. */
    public enum Estado { RASCUNHO, PUBLICADO, ENCERRADO }

    public static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");

    private final UUID id;
    private final UUID organizadorId;
    private final ZoneId fusoHorario;
    private String titulo;
    private String descricao;
    private IntervaloTempo periodo;
    private String localOuModalidade;
    private Estado estado;

    public Evento(UUID id, String titulo, String descricao, IntervaloTempo periodo,
                  String localOuModalidade, Estado estado, ZoneId fusoHorario, UUID organizadorId) {
        this.id = Objects.requireNonNull(id, "ID não pode ser nulo.");
        this.organizadorId = Objects.requireNonNull(organizadorId, "Organizador não pode ser nulo.");
        this.fusoHorario = Objects.requireNonNull(fusoHorario, "Fuso horário não pode ser nulo.");
        this.estado = Objects.requireNonNull(estado, "Estado não pode ser nulo.");
        validarEAtribuir(titulo, descricao, periodo, localOuModalidade);
    }

    /** Todo evento nasce em rascunho: só é visível ao público depois de publicado de propósito. */
    public static Evento novo(UUID organizadorId, String titulo, String descricao,
                              IntervaloTempo periodo, String localOuModalidade) {
        return new Evento(UUID.randomUUID(), titulo, descricao, periodo, localOuModalidade,
                Estado.RASCUNHO, FUSO_PADRAO, organizadorId);
    }

    public void alterarDados(String titulo, String descricao, IntervaloTempo periodo, String localOuModalidade) {
        exigirNaoEncerrado("alterar os dados");
        validarEAtribuir(titulo, descricao, periodo, localOuModalidade);
    }

    public void publicar() {
        exigirNaoEncerrado("publicar");
        this.estado = Estado.PUBLICADO;
    }

    public void encerrar() {
        if (estado != Estado.PUBLICADO) {
            throw new EstadoInvalidoException("Só é possível encerrar um evento publicado.");
        }
        this.estado = Estado.ENCERRADO;
    }

    public boolean isVisivelAoPublico() {
        return estado == Estado.PUBLICADO;
    }

    public boolean aceitaInscricoes() {
        return estado == Estado.PUBLICADO;
    }

    public boolean aceitaNovasAtividades() {
        return estado != Estado.ENCERRADO;
    }

    /** RN-18: administrador gerencia qualquer evento; organizador apenas os seus. */
    public boolean podeSerGerenciadoPor(Usuario usuario) {
        if (usuario == null || !usuario.podeGerenciarEventos()) return false;
        return usuario.isAdministrador() || usuario.getId().equals(organizadorId);
    }

    public void exigirGerenciadoPor(Usuario usuario) {
        if (!podeSerGerenciadoPor(usuario)) {
            throw new AcessoNegadoException("Você não tem permissão para gerenciar este evento.");
        }
    }

    private void validarEAtribuir(String titulo, String descricao, IntervaloTempo periodo, String local) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new ValorInvalidoException("O título do evento é obrigatório.");
        }
        if (periodo == null) {
            throw new ValorInvalidoException("O período do evento é obrigatório.");
        }
        this.titulo = titulo.trim();
        this.descricao = descricao;
        this.periodo = periodo;
        this.localOuModalidade = local;
    }

    private void exigirNaoEncerrado(String acao) {
        if (estado == Estado.ENCERRADO) {
            throw new EstadoInvalidoException("Não é possível " + acao + " de um evento encerrado.");
        }
    }

    public UUID getId() { return id; }
    public UUID getOrganizadorId() { return organizadorId; }
    public ZoneId getFusoHorario() { return fusoHorario; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public IntervaloTempo getPeriodo() { return periodo; }
    public String getLocalOuModalidade() { return localOuModalidade; }
    public Estado getEstado() { return estado; }
}
