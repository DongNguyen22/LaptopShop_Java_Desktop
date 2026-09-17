package valiateData;

import javax.swing.JComponent;

public class validationResult {
    private boolean isValid;
    private String message;
    private validationResult(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }
    public static validationResult success(){
        return new validationResult(true, null);
    }
    public static validationResult error(String message){
        return new validationResult(false, message);
    }
    public boolean isValid() {
        return isValid;
    }
    public String getMessage() {
        return message;
    }
}
