package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

/** DTO dla opcji głosowania w uchwale. */
@Data
public class ResolutionOptionDto {

    private UUID id;
    private String optionText;

    public ResolutionOptionDto(UUID id, String optionText) {
        this.id = id;
        this.optionText = optionText;
    }
}
