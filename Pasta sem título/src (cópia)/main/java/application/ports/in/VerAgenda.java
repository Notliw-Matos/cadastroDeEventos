package application.ports.in;

import application.ItemAgendaDTO;

import java.util.List;
import java.util.UUID;

/** Porta de entrada (RF-16, RF-17): agenda pessoal do participante, em ordem cronológica. */
public interface VerAgenda {
    List<ItemAgendaDTO> executar(UUID participanteId);
}
