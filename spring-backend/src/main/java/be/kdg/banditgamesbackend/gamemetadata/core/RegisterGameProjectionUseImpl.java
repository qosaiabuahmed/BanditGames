package be.kdg.banditgamesbackend.gamemetadata.core;

import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameInfoProjection;
import be.kdg.banditgamesbackend.gamemetadata.adapter.out.persistence.GameInfoProjectionRepository;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterGameProjectionCommand;
import be.kdg.banditgamesbackend.gamemetadata.port.in.RegisterGameProjectionUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterGameProjectionUseImpl implements RegisterGameProjectionUseCase {

    private final GameInfoProjectionRepository projectionRepository;

    @Override
    public void registerGameProjection(RegisterGameProjectionCommand command) {
        try {

            GameInfoProjection projection = projectionRepository.findById(command.gameId())
                    .orElseGet(() -> {
                        log.info("Creating new projection for game: {}", command.gameId());
                        return new GameInfoProjection();
                    });

            projection.setGameId(command.gameId());
            projection.setName(command.name());
            projection.setDescription(command.description());
            projection.setStatus(String.valueOf(command.status()));
            projection.setRegisteredAt(command.registeredAt());
            projection.setFrontendUrl(command.frontendUrl());
            projection.setPictureUrl(command.pictureUrl());
            projection.setRenderType(command.renderType());


            projectionRepository.save(projection);

            log.info("Projection saved: gameId={}, name={}, frontendUrl={}",
                    command.gameId(), command.name(), command.frontendUrl());

        } catch (Exception e) {
            log.error("Failed to create/update projection for game: {}", command.gameId(), e);
            throw e;
        }

    }
}
