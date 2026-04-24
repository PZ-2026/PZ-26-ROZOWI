package pl.edu.ur.blokur.dto;

import java.util.UUID;

/** DTO dla wyników opcji głosowania w uchwale. */
public class ResolutionOptionResultDto {

    private UUID optionId;
    private String optionText;
    private long votesCount;

    public ResolutionOptionResultDto() {}

    public ResolutionOptionResultDto(UUID optionId, String optionText, long votesCount) {
        this.optionId = optionId;
        this.optionText = optionText;
        this.votesCount = votesCount;
    }

    public UUID getOptionId() {
        return optionId;
    }

    public void setOptionId(UUID optionId) {
        this.optionId = optionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public long getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(long votesCount) {
        this.votesCount = votesCount;
    }
}
