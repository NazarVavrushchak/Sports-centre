package sports.center.com.dto.security;

public class AuthResponse {
    private String token;
    private String error;

    public AuthResponse(String token) {
        this.token = token;
        this.error = null;
    }

    public AuthResponse(String token, String error) {
        this.token = token;
        this.error = error;
    }

    public String getToken() {
        return token;
    }

    public String getError() {
        return error;
    }
}
