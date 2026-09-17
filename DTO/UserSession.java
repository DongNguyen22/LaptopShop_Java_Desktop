package DTO;

public class UserSession {

    private static UserSession instance;
    private AccountDTO account;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setAccount(AccountDTO account) {
        this.account = account;
    }

    public AccountDTO getAccount() {
        return account;
    }

    public void clear() {
        account = null;
    }

    public boolean isLogin() {
        return account != null;
    }
}
