package game.Happy_Zombie_Farm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramAuthDto(

        @JsonProperty("id")
        Long id,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("username")
        String username,

        @JsonProperty("photo_url")
        String photoUrl,

        @JsonProperty("auth_date")
        Long authDate,

        @JsonProperty("hash")
        String hash
) {}

