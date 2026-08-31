package be.kdg.banditgamesbackend.security;

import java.util.List;

public interface CorsOriginProvider {
    List<String> getAllowedOrigins();
}
