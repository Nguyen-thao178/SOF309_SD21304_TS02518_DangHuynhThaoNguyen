package com.cafe.oauth;

import com.cafe.config.RuntimeSecrets;

public final class OAuthConstants {
    public static final String GOOGLE_CLIENT_ID = configured("GOOGLE_OAUTH_CLIENT_ID");
    public static final String GOOGLE_CLIENT_SECRET = configured("GOOGLE_OAUTH_CLIENT_SECRET");
    public static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    public static final String GOOGLE_REDIRECT_URI = configured(
            "GOOGLE_OAUTH_REDIRECT_URI",
            "http://localhost:8080/ChidoriManagementSystem/oauth-callback?provider=google");

    public static final String FACEBOOK_CLIENT_ID = configured("FACEBOOK_OAUTH_CLIENT_ID");
    public static final String FACEBOOK_CLIENT_SECRET = configured("FACEBOOK_OAUTH_CLIENT_SECRET");
    public static final String FACEBOOK_AUTH_URL = "https://www.facebook.com/v18.0/dialog/oauth";
    public static final String FACEBOOK_TOKEN_URL = "https://graph.facebook.com/v18.0/oauth/access_token";
    public static final String FACEBOOK_USERINFO_URL = "https://graph.facebook.com/v18.0/me";
    public static final String FACEBOOK_REDIRECT_URI = configured(
            "FACEBOOK_OAUTH_REDIRECT_URI",
            "http://localhost:8080/ChidoriManagementSystem/oauth-callback?provider=facebook");

    public static final String GITHUB_CLIENT_ID = configured("GITHUB_OAUTH_CLIENT_ID");
    public static final String GITHUB_CLIENT_SECRET = configured("GITHUB_OAUTH_CLIENT_SECRET");
    public static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
    public static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    public static final String GITHUB_USERINFO_URL = "https://api.github.com/user";
    public static final String GITHUB_REDIRECT_URI = configured(
            "GITHUB_OAUTH_REDIRECT_URI",
            "http://localhost:8080/ChidoriManagementSystem/oauth-callback?provider=github");

    private OAuthConstants() {
    }

    public static boolean isConfigured(String provider) {
        return switch (provider) {
            case "google" -> configuredPair(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET);
            case "facebook" -> configuredPair(FACEBOOK_CLIENT_ID, FACEBOOK_CLIENT_SECRET);
            case "github" -> configuredPair(GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
            default -> false;
        };
    }

    private static boolean configuredPair(String id, String secret) {
        return id != null && !id.isBlank() && secret != null && !secret.isBlank();
    }

    private static String configured(String name) {
        return configured(name, "");
    }

    private static String configured(String name, String defaultValue) {
        return RuntimeSecrets.valueOrDefault(name, defaultValue);
    }
}
