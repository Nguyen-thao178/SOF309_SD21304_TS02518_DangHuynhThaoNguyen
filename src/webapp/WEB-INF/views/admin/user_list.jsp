<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý nhân viên - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
</head>
<body>
<%@ include file="/WEB-INF/views/header.jsp" %>
<div class="main-content" style="padding: 2rem;">
    <h2>👥 Quản lý nhân viên</h2>
    <div style="margin-bottom: 1rem;">
        <a href="${pageContext.request.contextPath}/admin/users?action=create" class="btn-primary">➕ Thêm mới</a>
    </div>
    <table class="cart-table" style="width:100%;">
        <thead>
            <tr><th>ID</th><th>Username</th><th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Hành động</th></tr>
        </thead>
        <tbody>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td>${u.id}</td>
                    <td>${u.username}</td>
                    <td>${u.fullname}</td>
                    <td>${u.email}</td>
                    <td>${u.role}</td>
                    <td>
                        <a href="?action=edit&id=${u.id}">✏️ Sửa</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/users"
                              class="inline-action"
                              onsubmit="return confirm('Xóa nhân viên này?');">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${u.id}">
                            <button type="submit" class="link-button">🗑️ Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>
