package com.artillexstudios.axcoins.database.dto;

import java.util.UUID;

public record UserDTO(Integer id, UUID uuid, Integer currencyId, String value) {
}
