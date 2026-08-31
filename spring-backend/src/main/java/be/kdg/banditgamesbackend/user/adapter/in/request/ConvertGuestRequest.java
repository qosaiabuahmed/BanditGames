package be.kdg.banditgamesbackend.user.adapter.in.request;

import java.util.UUID;

public record ConvertGuestRequest(
        UUID guestUserId,
        String username,
        String email,
        String password
) {}
