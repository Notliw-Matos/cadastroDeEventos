package application.ports.in;

import application.AtividadeDTO;

import java.util.List;
import java.util.UUID;

/** Porta de entrada (RF-09): programação de um evento, para o site e para a agenda. */
public interface ListarAtividadesDoEvento {
    List<AtividadeDTO> executar(UUID eventoId);
}
