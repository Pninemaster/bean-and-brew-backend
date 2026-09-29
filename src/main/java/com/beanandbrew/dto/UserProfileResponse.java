package com.beanandbrew.dto;

public class UserProfileResponse {

    private String name;
    private String email;
    private String phone;
    private boolean verified;

    public UserProfileResponse(String name, String email, String phone, boolean verified) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.verified = verified;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isVerified() {
        return verified;
    }
}