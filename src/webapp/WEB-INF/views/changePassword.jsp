<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi mật khẩu - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
    <style>
        .change-password-container {
            max-width: 500px;
            margin: 60px auto;
            padding: 30px;
            background: var(--card-bg);
            border-radius: var(--radius);
            border: 1px solid var(--border);
        }
        .change-password-container h2 {
            text-align: center;
            color: var(--orange);
            margin-bottom: 20px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: 600;
        }
        .form-group input {
            width: 100%;
            padding: 10px 15px;
            background: #2a2a2a;
            border: 1px solid var(--border);
            border-radius: 12px;
            color: white;
        }
        .form-group input:focus {
            border-color: var(--orange);
            outline: none;
        }
        .btn-change {
            width: 100%;
            padding: 12px;
            background: var(--orange);
            border: none;
            border-radius: 40px;
            font-weight: bold;
            color: white;
            cursor: pointer;
            transition: var(--transition);
        }
        .btn-change:hover {
            background: var(--red);
        }
        .btn-back {
            display: inline-block;
            margin-top: 10px;
            padding: 10px 20px;
            background: transparent;
            border: 1px solid var(--border);
            border-radius: 40px;
            color: var(--text-secondary);
            text-decoration: none;
            text-align: center;
            width: 100%;
        }
        .btn-back:hover {
            background: var(--border);
        }
        .alert {
            padding: 12px 20px;
            border-radius: 10px;
            margin-bottom: 15px;
        }
        .alert-success {
            background: #4caf5020;
            border-left: 4px solid #4caf50;
            color: #8bc34a;
        }
        .alert-danger {
            background: #d32f2f20;
            border-left: 4px solid var(--red);
            color: #ff8a8a;
        }
        .toggle-password {
            cursor: pointer;
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--text-secondary);
        }
        .input-group {
            position: relative;
        }
        .password-strength .bar {
            height: 4px;
            border-radius: 4px;
            background: #dfe6e9;
            margin-top: 4px;
            transition: width 0.3s;
        }
        .password-strength .bar.weak { background: #ff6b6b; width: 25%; }
        .password-strength .bar.medium { background: #fdcb6e; width: 50%; }
        .password-strength .bar.strong { background: #00b894; width: 75%; }
        .password-strength .bar.very-strong { background: #00b894; width: 100%; }
    </style>
</head>
<body>
<%@ include file="header.jsp" %>

<div class="change-password-container">
    <h2>🔑 Đổi mật khẩu</h2>
    <p style="text-align:center; color:var(--text-secondary);">Bảo mật tài khoản của bạn</p>

    <c:if test="${not empty message}">
        <div class="alert alert-success">✅ ${message}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">❌ ${error}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/change-password" method="post">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <div class="form-group">
            <label for="oldPassword">Mật khẩu cũ</label>
            <div class="input-group">
                <input type="password" id="oldPassword" name="oldPassword" placeholder="Nhập mật khẩu cũ" required>
                <span class="toggle-password" onclick="togglePassword('oldPassword', this)">👁️</span>
            </div>
        </div>

        <div class="form-group">
            <label for="newPassword">Mật khẩu mới</label>
            <div class="input-group">
                <input type="password" id="newPassword" name="newPassword" placeholder="Ít nhất 6 ký tự" required oninput="checkPasswordStrength(this.value)">
                <span class="toggle-password" onclick="togglePassword('newPassword', this)">👁️</span>
            </div>
            <div class="password-strength" id="passwordStrength">
                <span id="strengthText" style="color:var(--text-secondary);">Nhập mật khẩu để kiểm tra độ mạnh</span>
                <div class="bar" id="strengthBar"></div>
            </div>
        </div>

        <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu mới</label>
            <div class="input-group">
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu mới" required>
                <span class="toggle-password" onclick="togglePassword('confirmPassword', this)">👁️</span>
            </div>
            <div id="confirmMessage" style="font-size:13px; margin-top:5px; color:var(--text-secondary);"></div>
        </div>

        <button type="submit" class="btn-change">💾 Cập nhật mật khẩu</button>
    </form>

    <a href="${homePath}" class="btn-back">⬅️ Quay lại trang chính</a>
</div>

<%@ include file="footer.jsp" %>

<script>
    function togglePassword(inputId, element) {
        const input = document.getElementById(inputId);
        if (input.type === 'password') {
            input.type = 'text';
            element.textContent = '🙈';
        } else {
            input.type = 'password';
            element.textContent = '👁️';
        }
    }

    function checkPasswordStrength(password) {
        const bar = document.getElementById('strengthBar');
        const text = document.getElementById('strengthText');
        let strength = 0;
        if (password.length >= 6) strength++;
        if (password.length >= 10) strength++;
        if (/[A-Z]/.test(password) && /[a-z]/.test(password)) strength++;
        if (/\d/.test(password)) strength++;
        if (/[^a-zA-Z0-9]/.test(password)) strength++;

        bar.className = 'bar';
        if (password.length === 0) {
            text.textContent = 'Nhập mật khẩu để kiểm tra độ mạnh';
            bar.style.width = '0%';
            return;
        }
        if (strength <= 1) { text.textContent = 'Yếu'; bar.classList.add('weak'); }
        else if (strength <= 3) { text.textContent = 'Trung bình'; bar.classList.add('medium'); }
        else if (strength <= 4) { text.textContent = 'Mạnh'; bar.classList.add('strong'); }
        else { text.textContent = 'Rất mạnh'; bar.classList.add('very-strong'); }
    }

    document.addEventListener('DOMContentLoaded', function() {
        const newPass = document.getElementById('newPassword');
        const confirmPass = document.getElementById('confirmPassword');
        const confirmMsg = document.getElementById('confirmMessage');
        function checkMatch() {
            if (confirmPass.value.length === 0) {
                confirmMsg.textContent = '';
                return;
            }
            if (newPass.value === confirmPass.value) {
                confirmMsg.textContent = '✓ Mật khẩu trùng khớp';
                confirmMsg.style.color = '#4caf50';
            } else {
                confirmMsg.textContent = '✗ Mật khẩu không khớp';
                confirmMsg.style.color = '#ff6b6b';
            }
        }
        newPass.addEventListener('input', checkMatch);
        confirmPass.addEventListener('input', checkMatch);
    });
</script>
</body>
</html>
