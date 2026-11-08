<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${user == null ? "Thêm nhân viên" : "Sửa nhân viên"} - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
    <style>
        .form-container { max-width: 500px; margin: 2rem auto; }
        .form-container select { width: 100%; padding: 0.8rem; background: #2a2a2a; border: 1px solid #3a3a3a; border-radius: 12px; color: white; }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/header.jsp" %>
<div class="form-container">
    <h2>${user == null ? "➕ Thêm nhân viên mới" : "✏️ Cập nhật nhân viên"}</h2>
    <c:if test="${not empty error}">
        <div class="form-error" role="alert">${error}</div>
    </c:if>
    <form method="post">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="${user == null ? 'create' : 'update'}">
        <c:if test="${user != null}">
            <input type="hidden" name="id" value="${user.id}">
        </c:if>

        <label>Username:</label>
        <input type="text" name="username" value="${user.username}" ${user != null ? 'readonly' : ''} required>

        <label>Password:</label>
        <input type="password" name="password" placeholder="${user != null ? 'Để trống nếu không đổi' : 'Bắt buộc'}">

        <label>Họ tên:</label>
        <input type="text" name="fullname" value="${user.fullname}" required>

        <label>Email:</label>
        <input type="email" name="email" value="${user.email}" required>

        <label>Vai trò:</label>
        <select name="role" required>
            <option value="staff" ${user.role == 'staff' ? 'selected' : ''}>Nhân viên</option>
        </select>

        <button type="submit" class="btn-primary">💾 Lưu</button>
        <a href="${pageContext.request.contextPath}/admin/users" class="btn">⬅️ Quay lại</a>
    </form>
</div>
<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>
