<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý liên hệ - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
    <style>
        .contact-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px,1fr)); gap: 1.5rem; margin: 2rem 0; }
        .contact-card { background: var(--card-bg); border-radius: 16px; padding: 1.2rem; border: 1px solid #2a2a2a; }
        .contact-card:hover { border-color: #ff5722; }
        .badge { background: #d32f2f; display: inline-block; padding: 0.2rem 0.8rem; border-radius: 30px; font-size: 0.7rem; margin-bottom: 0.8rem; }
        .contact-card h3 { color: #ff5722; margin-bottom: 0.5rem; }
        .contact-detail p { margin: 0.5rem 0; }
        .contact-actions { display: flex; gap: .75rem; margin-top: 1rem; align-items: center; }
    </style>
</head>
<body>
<%@ include file="header.jsp" %>
<div class="main-content" style="padding: 2rem;">
    <div class="page-heading">
        <div>
            <h2>📞 Quản lý thông tin liên hệ</h2>
            <p>Cập nhật danh bạ chủ quán, quản lý và nhân viên.</p>
        </div>
        <a class="btn-primary" href="${pageContext.request.contextPath}/admin/contacts?action=create">
            ➕ Thêm liên hệ
        </a>
    </div>
    <c:if test="${not empty param.success}">
        <div class="success-msg"><c:out value="${param.success}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="error-msg">${error}</div>
    </c:if>
    <div class="contact-grid">
        <c:forEach var="c" items="${contacts}">
            <div class="contact-card">
                <span class="badge">
                    <c:choose>
                        <c:when test="${c.position == 'owner'}">👑 Chủ quán</c:when>
                        <c:when test="${c.position == 'manager'}">📋 Quản lý</c:when>
                        <c:when test="${c.position == 'employee'}">👨‍🍳 Nhân viên</c:when>
                        <c:when test="${c.position == 'supplier'}">🚚 Nhà cung cấp</c:when>
                        <c:otherwise>${c.position}</c:otherwise>
                    </c:choose>
                </span>
                <h3><c:out value="${c.name}"/></h3>
                <div class="contact-detail">
                    <c:if test="${not empty c.phone}"><p>📞 <c:out value="${c.phone}"/></p></c:if>
                    <c:if test="${not empty c.email}"><p>✉️ <c:out value="${c.email}"/></p></c:if>
                    <c:if test="${not empty c.address}"><p>📍 <c:out value="${c.address}"/></p></c:if>
                    <c:if test="${not empty c.notes}"><p><small><c:out value="${c.notes}"/></small></p></c:if>
                </div>
                <div class="contact-actions">
                    <a class="btn-outline"
                       href="${pageContext.request.contextPath}/admin/contacts?action=edit&id=${c.id}">✏️ Sửa</a>
                    <form method="post" action="${pageContext.request.contextPath}/admin/contacts"
                          class="inline-action" onsubmit="return confirm('Xóa liên hệ này?');">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${c.id}">
                        <button class="link-button" type="submit">🗑️ Xóa</button>
                    </form>
                </div>
            </div>
        </c:forEach>
    </div>
</div>
<%@ include file="footer.jsp" %>
</body>
</html>
