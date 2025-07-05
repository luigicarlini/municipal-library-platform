package it.comune.library.reservation.mapper;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.dto.HoldDto;
import org.springframework.stereotype.Component;

@Component
public class HoldMapper {

    /* entity ➜ dto */
    public HoldDto toDto(Hold entity) {
        HoldDto dto = new HoldDto();
        dto.setId(entity.getId());
        dto.setPatronId(entity.getPatronId());
        dto.setBibId(entity.getBibId());
        dto.setPickupBranch(entity.getPickupBranch());
        dto.setStatus(entity.getStatus());
        dto.setPosition(entity.getPosition());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDueDate(entity.getDueDate());          // ← NEW
        return dto;
    }

    /* dto ➜ entity */
    public Hold toEntity(HoldDto dto) {
        Hold entity = new Hold();
        entity.setId(dto.getId());
        entity.setHoldId(dto.getId());
        entity.setPatronId(dto.getPatronId());
        entity.setBibId(dto.getBibId());
        entity.setPickupBranch(dto.getPickupBranch());
        entity.setStatus(dto.getStatus());
        entity.setPosition(dto.getPosition());
        entity.setDueDate(dto.getDueDate());          // ← NEW
        return entity;
    }
}
