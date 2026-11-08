<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký thành viên - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
    <style>
        /* Additional styles for registration page */
        .register-container {
            max-width: 500px;
            margin: 2rem auto;
            background: var(--card-bg);
            border-radius: var(--radius);
            padding: 2rem;
            border: 1px solid var(--border);
            box-shadow: 0 8px 20px rgba(0,0,0,0.3);
        }
        .register-container h2 {
            text-align: center;
            margin-bottom: 1.5rem;
            color: var(--orange);
            border-left: none;
            padding-left: 0;
        }
        .register-container .form-group {
            margin-bottom: 1.2rem;
        }
        .register-container label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: var(--text-primary);
        }
        .register-container input {
            width: 100%;
            padding: 0.8rem;
            background: #2a2a2a;
            border: 1px solid #3a3a3a;
            border-radius: 12px;
            color: white;
            font-size: 1rem;
            transition: 0.2s;
        }
        .register-container input:focus {
            outline: none;
            border-color: var(--orange);
            box-shadow: 0 0 5px rgba(255,87,34,0.5);
        }
        .register-container button {
            width: 100%;
            padding: 0.8rem;
            background: var(--orange);
            color: white;
            border: none;
            border-radius: 40px;
            font-size: 1rem;
            font-weight: bold;
            cursor: pointer;
            transition: var(--transition);
            margin-top: 0.5rem;
        }
        .register-container button:hover {
            background: var(--red);
            transform: translateY(-2px);
        }
        .error-msg {
            background: #d32f2f20;
            border-left: 4px solid var(--red);
            padding: 0.8rem;
            margin-bottom: 1rem;
            border-radius: 8px;
            color: #ff8a8a;
        }
        .success-msg {
            background: #4caf5020;
            border-left: 4px solid #4caf50;
            padding: 0.8rem;
            margin-bottom: 1rem;
            border-radius: 8px;
            color: #8bc34a;
        }
        .register-footer {
            text-align: center;
            margin-top: 1.5rem;
            color: var(--text-secondary);
        }
        .register-footer a {
            color: var(--orange);
            text-decoration: none;
        }
        .register-footer a:hover {
            text-decoration: underline;
        }
        @media (max-width: 768px) {
            .register-container {
                margin: 1rem;
                padding: 1.5rem;
            }
        }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="register-container">
    <h2>📝 Đăng ký thành viên mới</h2>
    
    <c:if test="${not empty error}">
        <div class="error-msg">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="success-msg">${success}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/register-member" method="post">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <div class="form-group">
            <label>Username</label>
            <input type="text" name="username" placeholder="Tên đăng nhập" required>
        </div>
        <div class="form-group">
            <label>Password</label>
            <input type="password" name="password" placeholder="Mật khẩu" required>
        </div>
        <div class="form-group">
            <label>Fullname</label>
            <input type="text" name="fullname" placeholder="Họ và tên" required>
        </div>
        <div class="form-group">
            <label>Email</label>
            <input type="email" name="email" placeholder="Địa chỉ email" required>
        </div>
        <button type="submit">Đăng ký</button>
    </form>
    <div class="register-footer">
        <p>Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
    </div>
</div>

<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>
