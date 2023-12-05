package WebProject.WebProject.entity;

public class AuthResponse {
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private String accessToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;

    public AuthResponse( String username ,String accessToken) {
        this.username=username ;
        this.accessToken = accessToken;
    }
}

