package it.comune.library.reservation.dto;

import lombok.Data;

@Data
public class HoldDetailsDto {
    private HoldDto hold;
    private BookDto book;
}
