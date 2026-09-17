package DTO;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Mã hóa
    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    // Kiểm tra khi login
    public static boolean checkPassword(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }
}
