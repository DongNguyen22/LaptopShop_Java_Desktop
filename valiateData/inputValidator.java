package valiateData;

import java.util.regex.Pattern;

public class inputValidator {
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(03|05|07|08|09)\\d{8}$");

    // Check số điện thoại
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;

        phone = phone.trim();
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
