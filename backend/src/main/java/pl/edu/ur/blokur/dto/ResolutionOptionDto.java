package pl.edu.ur.blokur.dto;

import java.util.UUID;

/** DTO dla opcji głosowania w uchwale. */
public class ResolutionOptionDto {

    private UUID id;
    private String optionText;

    public ResolutionOptionDto() {}

    public ResolutionOptionDto(UUID id, String optionText) {
        this.id = id;
        this.optionText = optionText;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}
