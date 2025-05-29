package it.comune.library.reservation.mapper;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.dto.HoldDto;
import org.springframework.stereotype.Component;

@Component
public class HoldMapper {

    public HoldDto toDto(Hold entity) {
        HoldDto dto = new HoldDto();
        dto.setId(entity.getId());
        dto.setPatronId(entity.getPatronId());
        dto.setBibId(entity.getBibId());
        dto.setPickupBranch(entity.getPickupBranch());
        dto.setStatus(entity.getStatus());
        dto.setPosition(entity.getPosition());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Hold toEntity(HoldDto dto) {
        Hold entity = new Hold();
        entity.setId(dto.getId());
        entity.setHoldId(dto.getId()); // per ora identico a id
        entity.setPatronId(dto.getPatronId());
        entity.setBibId(dto.getBibId());
        entity.setPickupBranch(dto.getPickupBranch());
        entity.setStatus(dto.getStatus());
        entity.setPosition(dto.getPosition());
        return entity;
    }
}


