<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${contact.id > 0 ? 'Sửa liên hệ' : 'Thêm liên hệ'} - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-rbac2">
    <style>
        .contact-form { max-width: 680px; margin: 2rem auto; padding: 1.5rem; }
        .contact-form label { display: block; margin: .9rem 0 .35rem; }
        .contact-form input, .contact-form select, .contact-form textarea { width: 100%; }
        .contact-form textarea { min-height: 96px; resize: vertical; }
        .form-actions { display: flex; gap: .75rem; margin-top: 1.25rem; align-items: center; }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/header.jsp" %>
<main class="contact-form card">
    <h2>${contact.id > 0 ? '✏️ Sửa thông tin liên hệ' : '➕ Thêm thông tin liên hệ'}</h2>
    <c:if test="${not empty error}">
        <div class="form-error" role="alert"><c:out value="${error}"/></div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/admin/contacts">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="${contact.id > 0 ? 'update' : 'create'}">
        <c:if test="${contact.id > 0}">
            <input type="hidden" name="id" value="${contact.id}">
        </c:if>

        <label for="name">Họ tên *</label>
        <input id="name" name="name" maxlength="120" value="<c:out value='${contact.name}'/>" required>

        <label for="position">Chức vụ *</label>
        <select id="position" name="position" required>
            <option value="owner" ${contact.position == 'owner' ? 'selected' : ''}>Chủ quán</option>
            <option value="manager" ${contact.position == 'manager' ? 'selected' : ''}>Quản lý</option>
            <option value="employee" ${contact.position == 'employee' ? 'selected' : ''}>Nhân viên</option>
            <option value="other" ${contact.position == 'other' ? 'selected' : ''}>Khác</option>
        </select>

        <label for="phone">Số điện thoại</label>
        <input id="phone" name="phone" maxlength="30" value="<c:out value='${contact.phone}'/>">

        <label for="email">Email</label>
        <input id="email" type="email" name="email" maxlength="254" value="<c:out value='${contact.email}'/>">

        <label for="address">Địa chỉ</label>
        <textarea id="address" name="address" maxlength="500"><c:out value="${contact.address}"/></textarea>

        <label for="notes">Ghi chú</label>
        <textarea id="notes" name="notes" maxlength="1000"><c:out value="${contact.notes}"/></textarea>

        <div class="form-actions">
            <button class="btn-primary" type="submit">💾 Lưu</button>
            <a class="btn" href="${pageContext.request.contextPath}/admin/contacts">Hủy</a>
        </div>
    </form>
</main>
<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>
