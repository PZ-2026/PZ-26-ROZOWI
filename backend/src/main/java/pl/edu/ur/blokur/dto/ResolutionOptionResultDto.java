package pl.edu.ur.blokur.dto;

import java.util.UUID;
import lombok.Data;

/** DTO dla wyników opcji głosowania w uchwale. */
@Data
public class ResolutionOptionResultDto {

    private UUID optionId;
    private String optionText;
    private long votesCount;

    public ResolutionOptionResultDto(UUID optionId, String optionText, long votesCount) {
        this.optionId = optionId;
        this.optionText = optionText;
        this.votesCount = votesCount;
    }
}
