package com.gumraze.drive.drive_backend.courtmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gumraze.drive.drive_backend.courtmanager.constants.ParticipantGender;
import com.gumraze.drive.drive_backend.courtmanager.constants.ParticipantGrade;
import com.gumraze.drive.drive_backend.courtmanager.constants.TournamentVisibility;
import com.gumraze.drive.drive_backend.courtmanager.dto.CourtTournamentResponse;
import com.gumraze.drive.drive_backend.courtmanager.dto.CreateCourtTournamentRequest;
import com.gumraze.drive.drive_backend.courtmanager.entity.CourtTournament;
import com.gumraze.drive.drive_backend.courtmanager.entity.CourtTournamentParticipant;
import com.gumraze.drive.drive_backend.courtmanager.repository.CourtTournamentRepository;
import com.gumraze.drive.drive_backend.user.entity.OauthUser;
import com.gumraze.drive.drive_backend.user.service.OauthUserService;

@Service
@Transactional
public class CourtTournamentService {

    private final CourtTournamentRepository tournamentRepository;
    private final OauthUserService oauthUserService;

    public CourtTournamentService(CourtTournamentRepository tournamentRepository, OauthUserService oauthUserService) {
        this.tournamentRepository = tournamentRepository;
        this.oauthUserService = oauthUserService;
    }

    public CourtTournamentResponse createTournament(CreateCourtTournamentRequest request, Long organizerId) {
        OauthUser organizer = oauthUserService.findById(organizerId)
            .orElseThrow(() -> new IllegalArgumentException("Organizer not found for id: " + organizerId));

        CourtTournament tournament = CourtTournament.builder()
            .name(request.name())
            .visibility(TournamentVisibility.of(request.visibility()))
            .organizer(organizer)
            .build();

        request.participants().forEach(participantRequest -> {
            CourtTournamentParticipant participant = CourtTournamentParticipant.builder()
                .participantName(participantRequest.name())
                .gender(ParticipantGender.from(participantRequest.gender()))
                .grade(ParticipantGrade.from(participantRequest.grade()))
                .build();
            tournament.addParticipant(participant);
        });

        CourtTournament saved = tournamentRepository.save(tournament);
        return toResponse(saved);
    }

    private CourtTournamentResponse toResponse(CourtTournament tournament) {
        List<CourtTournamentResponse.Participant> participants = tournament.getParticipants().stream()
            .map(p -> new CourtTournamentResponse.Participant(
                p.getId(),
                p.getParticipantName(),
                p.getGender().getDisplayName(),
                p.getGrade().getDisplayName()
            ))
            .toList();

        return new CourtTournamentResponse(
            tournament.getId(),
            tournament.getName(),
            tournament.getVisibility().name(),
            tournament.getOrganizer().getId(),
            tournament.getCreatedAt(),
            participants
        );
    }
}
