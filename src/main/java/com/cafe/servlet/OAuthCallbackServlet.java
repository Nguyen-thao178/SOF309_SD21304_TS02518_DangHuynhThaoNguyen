package com.cafe.servlet;

import com.cafe.dao.LoyaltyDAO;
import com.cafe.dao.UserDAO;
import com.cafe.model.User;
import com.cafe.oauth.OAuthConstants;
import com.cafe.utils.PasswordUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.security.MessageDigest;

@WebServlet("/oauth-callback")
public class OAuthCallbackServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private LoyaltyDAO loyaltyDAO = new LoyaltyDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String provider = req.getParameter("provider");
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        Object expectedState = req.getSession().getAttribute("oauthState");
        req.getSession().removeAttribute("oauthState");

        if (code == null || state == null || !(expectedState instanceof String)
                || !MessageDigest.isEqual(state.getBytes(StandardCharsets.UTF_8),
                expectedState.toString().getBytes(StandardCharsets.UTF_8))) {
            resp.sendRedirect(req.getContextPath() + "/login?error=oauth_failed");
            return;
        }

        // 1. Lấy access token
        String accessToken = getAccessToken(provider, code);
        if (accessToken == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=oauth_token");
            return;
        }

        // 2. Lấy thông tin user từ provider
        Map<String, String> userInfo = getUserInfo(provider, accessToken);
        if (userInfo == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=oauth_userinfo");
            return;
        }

        String email = userInfo.get("email");
        String fullname = userInfo.get("name");
        if (email == null) {
            email = userInfo.get("id") + "@" + provider + ".local";
        }

        try {
            // 3. Kiểm tra user đã tồn tại chưa
            User existing = userDAO.findByUsername(email);

            if (existing == null) {
                // Tạo user mới
                User newUser = new User();
                newUser.setUsername(email);
                newUser.setFullname(fullname != null ? fullname : provider + "_user");
                newUser.setEmail(email);
                newUser.setRole("customer");

                // Tạo mật khẩu ngẫu nhiên và hash
                String randomPassword = UUID.randomUUID().toString();
                newUser.setPasswordHash(PasswordUtil.hash(randomPassword));

                // Lưu vào database
                int userId = userDAO.insert(newUser);

                if (userId > 0) {
                    // Lấy lại user vừa tạo để lưu session
                    existing = userDAO.findByUsername(email);
                    // Tạo loyalty points cho user mới
                    loyaltyDAO.createEmpty(existing.getId());
                } else {
                    resp.sendRedirect(req.getContextPath() + "/login?error=oauth_create");
                    return;
                }
            }

            // 4. Lưu user vào session và chuyển hướng
            req.getSession();
            req.changeSessionId();
            req.getSession().setAttribute("user", existing);
            resp.sendRedirect(req.getContextPath() + "/home");

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/login?error=oauth_db");
        }
    }

    // ==================== LẤY ACCESS TOKEN ====================
    private String getAccessToken(String provider, String code) throws IOException {
        String tokenUrl = "";
        Map<String, String> params = new HashMap<>();

        if ("google".equals(provider)) {
            tokenUrl = OAuthConstants.GOOGLE_TOKEN_URL;
            params.put("client_id", OAuthConstants.GOOGLE_CLIENT_ID);
            params.put("client_secret", OAuthConstants.GOOGLE_CLIENT_SECRET);
            params.put("redirect_uri", OAuthConstants.GOOGLE_REDIRECT_URI);
            params.put("code", code);
            params.put("grant_type", "authorization_code");
        } else if ("facebook".equals(provider)) {
            tokenUrl = OAuthConstants.FACEBOOK_TOKEN_URL;
            params.put("client_id", OAuthConstants.FACEBOOK_CLIENT_ID);
            params.put("client_secret", OAuthConstants.FACEBOOK_CLIENT_SECRET);
            params.put("redirect_uri", OAuthConstants.FACEBOOK_REDIRECT_URI);
            params.put("code", code);
        } else if ("github".equals(provider)) {
            tokenUrl = OAuthConstants.GITHUB_TOKEN_URL;
            params.put("client_id", OAuthConstants.GITHUB_CLIENT_ID);
            params.put("client_secret", OAuthConstants.GITHUB_CLIENT_SECRET);
            params.put("code", code);
            params.put("redirect_uri", OAuthConstants.GITHUB_REDIRECT_URI);
        }

        // Tạo query string
        StringBuilder urlParameters = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (urlParameters.length() > 0) urlParameters.append("&");
            urlParameters.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            urlParameters.append("=");
            urlParameters.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(tokenUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        if ("github".equals(provider)) {
            conn.setRequestProperty("Accept", "application/json");
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(urlParameters.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) return null;

        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        JsonNode json = mapper.readTree(response);

        if ("google".equals(provider) || "facebook".equals(provider)) {
            return json.has("access_token") ? json.get("access_token").asText() : null;
        } else if ("github".equals(provider)) {
            return json.has("access_token") ? json.get("access_token").asText() : null;
        }
        return null;
    }

    // ==================== LẤY THÔNG TIN USER ====================
    private Map<String, String> getUserInfo(String provider, String accessToken) throws IOException {
        String url = "";
        if ("google".equals(provider)) {
            url = OAuthConstants.GOOGLE_USERINFO_URL;
        } else if ("facebook".equals(provider)) {
            url = OAuthConstants.FACEBOOK_USERINFO_URL + "?fields=id,name,email&access_token=" + accessToken;
        } else if ("github".equals(provider)) {
            url = OAuthConstants.GITHUB_USERINFO_URL;
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        if ("github".equals(provider)) {
            conn.setRequestProperty("Accept", "application/json");
        }

        int code = conn.getResponseCode();
        if (code != 200) return null;

        String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        JsonNode json = mapper.readTree(response);

        Map<String, String> map = new HashMap<>();
        if ("google".equals(provider)) {
            map.put("email", json.has("email") ? json.get("email").asText() : null);
            map.put("name", json.has("name") ? json.get("name").asText() : null);
            map.put("id", json.has("id") ? json.get("id").asText() : null);
        } else if ("facebook".equals(provider)) {
            map.put("email", json.has("email") ? json.get("email").asText() : null);
            map.put("name", json.has("name") ? json.get("name").asText() : null);
            map.put("id", json.has("id") ? json.get("id").asText() : null);
        } else if ("github".equals(provider)) {
            map.put("email", json.has("email") ? json.get("email").asText() : null);
            map.put("name", json.has("name") ? json.get("name").asText() : json.get("login").asText());
            map.put("id", json.has("id") ? json.get("id").asText() : null);
        }
        return map;
    }
}
