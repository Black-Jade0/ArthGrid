package com.Prakhar.Auth_Service.DTO;





public class UserEvent {

    private String username;
    private String email;
    private String userId;
    private String resetLink;
    private String eventType;

    public UserEvent() {
    }

    public UserEvent(String username, String email, String userId, String resetLink, String eventType) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.resetLink = resetLink;
        this.eventType = eventType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResetLink() {
        return resetLink;
    }

    public void setResetLink(String resetLink) {
        this.resetLink = resetLink;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
