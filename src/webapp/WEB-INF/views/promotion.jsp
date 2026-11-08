<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khuyến mãi thành viên - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-chat-cart1">
</head>
<body>
<%@ include file="header.jsp" %>
<div class="main-content" style="padding: 2rem;">
    <h2>🎉 Ưu đãi đặc biệt dành cho thành viên</h2>
    <c:choose>
        <c:when test="${empty promotions}">
            <p>Hiện chưa có chương trình khuyến mãi nào.</p>
        </c:when>
        <c:otherwise>
            <div class="product-grid">
                <c:forEach var="promo" items="${promotions}">
                    <div class="product-card promo-card" style="border: 2px solid #ff5722;">
                        <img src="${promo.imageUrl}" alt="${promo.title}">
                        <div class="product-info">
                            <h3>${promo.title}</h3>
                            <p class="price">Giảm ${promo.discountPercent}%</p>
                            <p>${promo.description}</p>
                            <p><small>📅 ${promo.startDate} → ${promo.endDate}</small></p>
                            <a href="${pageContext.request.contextPath}/menu" class="btn-primary">🛒 Mua ngay</a>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="footer.jsp" %>
</body>
</html>
