package be.kdg.banditgamesbackend.user.adapter.in.request;

import jakarta.validation.constraints.Email;

public record UpdateUserProfileRequest(
    String username,

    @Email(message = "Email must be valid")
    String email,

    String playerTag,

    String avatar
) {}