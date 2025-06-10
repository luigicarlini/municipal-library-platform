package it.comune.library.reservation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<ISBN, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String digits = value.replaceAll("[^0-9X]", "");
        return switch (digits.length()) {
            case 10 -> isValidIsbn10(digits);
            case 13 -> isValidIsbn13(digits);
            default -> false;
        };
    }

    /* ---------- Helpers ---------- */

    private boolean isValidIsbn10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (i + 1) * Character.digit(isbn.charAt(i), 10);
        }
        char check = isbn.charAt(9);
        sum += (check == 'X' ? 10 : Character.digit(check, 10)) * 10;
        return sum % 11 == 0;
    }

    private boolean isValidIsbn13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.digit(isbn.charAt(i), 10);
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == Character.digit(isbn.charAt(12), 10);
    }
}

