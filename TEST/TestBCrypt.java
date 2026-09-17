package TEST;

import org.mindrot.jbcrypt.BCrypt;

public class TestBCrypt {
    public static void main(String[] args) {

        String password = "abc123";

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));

        System.out.println(hash);
    }
}