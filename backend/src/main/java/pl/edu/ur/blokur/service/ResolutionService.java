package pl.edu.ur.blokur.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pl.edu.ur.blokur.dto.CastVoteRequest;
import pl.edu.ur.blokur.models.Resolution;
import pl.edu.ur.blokur.models.ResolutionOption;
import pl.edu.ur.blokur.models.ResolutionVote;
import pl.edu.ur.blokur.models.User;
import pl.edu.ur.blokur.repository.ResolutionOptionRepository;
import pl.edu.ur.blokur.repository.ResolutionRepository;
import pl.edu.ur.blokur.repository.ResolutionVoteRepository;
import pl.edu.ur.blokur.repository.UserRepository;

/**
 * Serwis dostarczający logikę biznesową dla modułu uchwał i głosowań.
 * Odpowiada za walidację danych wejściowych, sprawdzenie stanu uchwały
 * oraz zapis oddanego głosu z zabezpieczeniem przed podwójnym głosowaniem.
 */
@Service
public class ResolutionService {

    private final ResolutionRepository resolutionRepository;
    private final ResolutionOptionRepository resolutionOptionRepository;
    private final ResolutionVoteRepository resolutionVoteRepository;
    private final UserRepository userRepository;

    /**
     * Tworzy instancję serwisu z wymaganymi repozytoriami.
     *
     * @param resolutionRepository       repozytorium uchwał
     * @param resolutionOptionRepository repozytorium opcji głosowania
     * @param resolutionVoteRepository   repozytorium oddanych głosów
     * @param userRepository             repozytorium użytkowników
     */
    public ResolutionService(ResolutionRepository resolutionRepository,
                             ResolutionOptionRepository resolutionOptionRepository,
                             ResolutionVoteRepository resolutionVoteRepository,
                             UserRepository userRepository) {
        this.resolutionRepository = resolutionRepository;
        this.resolutionOptionRepository = resolutionOptionRepository;
        this.resolutionVoteRepository = resolutionVoteRepository;
        this.userRepository = userRepository;
    }

    /**
     * Rejestruje głos zalogowanego użytkownika na wybraną opcję w uchwale.
     * Metoda weryfikuje istnienie uchwały i opcji, a następnie próbuje zapisać
     * rekord głosu. W przypadku próby podwójnego głosowania — gdy baza danych
     * zgłasza naruszenie unikalnego klucza {@code UNIQUE (resolution_id, voter_id)}
     * — przechwytywany jest wyjątek {@link DataIntegrityViolationException}
     * i zwracany jest błąd HTTP 409 Conflict.
     *
     * @param resolutionId identyfikator UUID uchwały, w której oddawany jest głos
     * @param request      obiekt DTO zawierający {@code optionId} wybranej opcji
     * @param username     adres e-mail zalogowanego użytkownika (Subject z JWT)
     * @throws ResponseStatusException z kodem 404 gdy uchwała lub opcja nie istnieje,
     *                                 z kodem 409 gdy użytkownik już oddał głos w tej uchwale,
     *                                 z kodem 400 gdy wybrana opcja nie należy do wskazanej uchwały
     */
    public void castVote(UUID resolutionId, CastVoteRequest request, String username) {
        Resolution resolution = resolutionRepository.findById(resolutionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Uchwała o podanym identyfikatorze nie istnieje."));

        ResolutionOption option = resolutionOptionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Opcja głosowania nie istnieje."));

        if (!option.getResolution().getId().equals(resolutionId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Wybrana opcja nie należy do wskazanej uchwały.");
        }

        User voter = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Użytkownik nie został odnaleziony."));

        ResolutionVote vote = new ResolutionVote();
        vote.setResolution(resolution);
        vote.setOption(option);
        vote.setVoter(voter);

        try {
            resolutionVoteRepository.save(vote);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Użytkownik oddał już głos w tej uchwale.");
        }
    }
}
