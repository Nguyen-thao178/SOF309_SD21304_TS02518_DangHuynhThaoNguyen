<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#0a0706">
    <title>Đăng nhập - Chidori Coffee</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
</head>
<body class="login-page login-centered-page">
<div class="login-reference-background" aria-hidden="true">
    <span class="login-ambient login-ambient-left"></span>
    <span class="login-ambient login-ambient-right"></span>
    <span class="login-horizon"></span>
    <span class="login-ring login-ring-one"></span>
    <span class="login-ring login-ring-two"></span>
    <span class="login-ring login-ring-three"></span>
    <span class="login-floor-glow"></span>
</div>

<header class="login-reference-header">
    <a class="login-reference-brand" href="${pageContext.request.contextPath}/"
       aria-label="Chidori Coffee">
        <span class="login-reference-logo">C</span>
        <span>Chidori <small>COFFEE</small></span>
    </a>
</header>

<main class="login-reference-stage">
    <section class="login-reference-card" aria-labelledby="loginTitle">
        <div class="login-card-icon" aria-hidden="true">
            <span>→</span>
        </div>

        <div class="login-reference-heading">
            <span>CHIDORI WORKSPACE</span>
            <h1 id="loginTitle">Đăng nhập tài khoản</h1>
            <p>Quản lý menu, barcode, đơn cọc và giao dịch<br>trong một không gian làm việc.</p>
        </div>

        <c:if test="${not empty error}">
            <div class="login-reference-error" role="alert">
                <span>!</span>
                <p>${error}</p>
            </div>
        </c:if>

        <form class="login-reference-form"
              action="${pageContext.request.contextPath}/login" method="post">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <label class="sr-only" for="username">Tên đăng nhập</label>
            <div class="login-reference-input">
                <span class="login-field-icon" aria-hidden="true">✦</span>
                <input id="username" type="text" name="username"
                       placeholder="Tên đăng nhập" autocomplete="username" required autofocus>
            </div>

            <label class="sr-only" for="password">Mật khẩu</label>
            <div class="login-reference-input">
                <span class="login-field-icon login-lock-icon" aria-hidden="true">◆</span>
                <input id="password" type="password" name="password"
                       placeholder="Mật khẩu" autocomplete="current-password" required>
                <button class="password-toggle" type="button" aria-label="Hiện mật khẩu"
                        aria-pressed="false">◉</button>
            </div>

            <div class="login-form-meta">
                <span>Truy cập dành cho nhân viên</span>
                <span>Quên mật khẩu? Liên hệ quản trị</span>
            </div>

            <button type="submit" class="login-reference-submit">
                <span>Bắt đầu làm việc</span>
                <i aria-hidden="true">→</i>
            </button>
        </form>

        <div class="login-reference-divider"><span>Hoặc đăng nhập với</span></div>

        <div class="login-reference-oauth">
            <a href="${pageContext.request.contextPath}/oauth-login?provider=google"
               aria-label="Đăng nhập bằng Google"><strong>G</strong></a>
            <a href="${pageContext.request.contextPath}/oauth-login?provider=facebook"
               aria-label="Đăng nhập bằng Facebook"><strong>f</strong></a>
            <a href="${pageContext.request.contextPath}/oauth-login?provider=github"
               aria-label="Đăng nhập bằng GitHub"><strong>⌘</strong></a>
        </div>

        <p class="login-reference-help">
            Chưa có tài khoản? <strong>Liên hệ quản trị viên</strong>
        </p>
    </section>
</main>

<p class="login-reference-caption">
    <span></span> Chidori Coffee Management System
</p>

<script src="${pageContext.request.contextPath}/assets/js/login.js?v=20260727-login2"></script>
</body>
</html>
