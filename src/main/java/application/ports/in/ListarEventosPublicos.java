package application.ports.in;

import application.EventoPublicoDTO;

import java.util.List;

/** Porta de entrada (RF-10): o que o site público mostra na página inicial. */
public interface ListarEventosPublicos {
    List<EventoPublicoDTO> executar();
}
