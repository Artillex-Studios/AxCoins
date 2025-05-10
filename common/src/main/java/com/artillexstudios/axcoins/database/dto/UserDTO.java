package com.artillexstudios.axcoins.database.dto;

import java.util.UUID;

public record UserDTO(int id, String name, UUID uuid, int currencyId, String value) {
}
