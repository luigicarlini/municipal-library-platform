package it.comune.library.reservation.mapper;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.dto.HoldDto;
import org.springframework.stereotype.Component;

/**
 * Mapper tra l'entità JPA Hold e il DTO HoldDto.
 * Evita l'esposizione diretta dell'entità al client.
 */
@Component
public class HoldMapper {

    /**
     * Converte un'entità Hold in DTO.
     * @param entity Hold persistente dal database
     * @return DTO da restituire al client
     */
    public HoldDto toDto(Hold entity) {
        HoldDto dto = new HoldDto();
        dto.setId(entity.getId());
        dto.setPatronId(entity.getPatronId());
        dto.setBibId(entity.getBibId());
        dto.setPickupBranch(entity.getPickupBranch());
        dto.setStatus(entity.getStatus());
        dto.setPosition(entity.getPosition());
        dto.setCreatedAt(entity.getCreatedAt()); // creato in @PrePersist
        return dto;
    }

    /**
     * Converte un DTO HoldDto in entità persistente Hold.
     * N.B. `createdAt` viene ignorato, gestito da @PrePersist nell'entità.
     * @param dto DTO ricevuto dal client
     * @return entità pronta per il salvataggio
     */
    public Hold toEntity(HoldDto dto) {
        Hold entity = new Hold();
        entity.setId(dto.getId()); // se null, lo generi prima di chiamare save
        entity.setHoldId(dto.getId()); // sincronizzato con id per ora
        entity.setPatronId(dto.getPatronId());
        entity.setBibId(dto.getBibId());
        entity.setPickupBranch(dto.getPickupBranch());
        entity.setStatus(dto.getStatus());
        entity.setPosition(dto.getPosition());
        // createdAt è gestito internamente da @PrePersist
        return entity;
    }
}