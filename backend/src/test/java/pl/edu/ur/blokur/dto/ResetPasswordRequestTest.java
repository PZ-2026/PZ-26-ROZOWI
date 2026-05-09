package pl.edu.ur.blokur.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ResetPasswordRequest — walidacja pola newPassword")
class ResetPasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private ResetPasswordRequest buildRequest(String password) {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword(password);
        return req;
    }

    private Set<ConstraintViolation<ResetPasswordRequest>> validate(String password) {
        return validator.validate(buildRequest(password));
    }

    @Nested
    @DisplayName("hasła poprawne — brak naruszeń")
    class ValidPasswords {

        @ParameterizedTest(name = "\"{0}\" powinno przejść walidację")
        @ValueSource(strings = {"Haslo123", "Abc1defg", "AAAA1aaa", "1Aaaaaaaa", "Test1234!"})
        void acceptsValidPassword(String password) {
            assertThat(validate(password)).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasła zbyt krótkie — naruszenie @Size")
    class TooShortPasswords {

        @Test
        @DisplayName("hasło 7-znakowe z wielką literą i cyfrą narusza @Size")
        void rejectsSevenCharPassword() {
            Set<ConstraintViolation<ResetPasswordRequest>> violations = validate("Abc123x");
            assertThat(violations).anyMatch(v -> v.getMessage().contains("8 znaków"));
        }
    }

    @Nested
    @DisplayName("hasła bez wymaganego znaku — naruszenie @Pattern")
    class PatternViolations {

        @Test
        @DisplayName("brak cyfry — naruszenie @Pattern")
        void rejectsPasswordWithoutDigit() {
            Set<ConstraintViolation<ResetPasswordRequest>> violations = validate("Hasloabc");
            assertThat(violations)
                    .anyMatch(
                            v ->
                                    v.getMessage()
                                            .contains("wielką literę")
                                            && v.getMessage().contains("cyfrę"));
        }

        @Test
        @DisplayName("brak wielkiej litery — naruszenie @Pattern")
        void rejectsPasswordWithoutUppercase() {
            Set<ConstraintViolation<ResetPasswordRequest>> violations = validate("haslo123");
            assertThat(violations)
                    .anyMatch(
                            v ->
                                    v.getMessage()
                                            .contains("wielką literę")
                                            && v.getMessage().contains("cyfrę"));
        }

        @ParameterizedTest(name = "\"{0}\" powinno naruszyć @Pattern")
        @ValueSource(strings = {"alllower1", "nouppercase9", "NoDigitHere", "ABCDEFGH"})
        void rejectsInvalidPatterns(String password) {
            Set<ConstraintViolation<ResetPasswordRequest>> violations = validate(password);
            boolean hasPatternOrSizeViolation =
                    violations.stream()
                            .anyMatch(
                                    v ->
                                            v.getPropertyPath().toString().equals("newPassword")
                                                    && (v.getMessage().contains("wielką literę")
                                                            || v.getMessage().contains("8 znaków")));
            assertThat(hasPatternOrSizeViolation).isTrue();
        }
    }

    @Nested
    @DisplayName("puste i null — naruszenie @NotBlank")
    class BlankPasswords {

        @Test
        @DisplayName("null — naruszenie @NotBlank")
        void rejectsNullPassword() {
            Set<ConstraintViolation<ResetPasswordRequest>> violations = validate(null);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }

        @Test
        @DisplayName("pusty string — naruszenie @NotBlank")
        void rejectsBlankPassword() {
            Set<ConstraintViolation<ResetPasswordRequest>> violations = validate("");
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }
    }
}
