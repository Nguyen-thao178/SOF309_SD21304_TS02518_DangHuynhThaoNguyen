<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${product.name} - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-chat-cart1">
</head>
<body>
<%@ include file="header.jsp" %>
<div class="product-detail">
    <div class="detail-img">
        <img src="${product.imageUrl}" alt="${product.name}">
    </div>
    <div class="detail-info">
        <h1>${product.name}</h1>
        <p class="price">${product.price}₫</p>
        <p>⭐ Đã bán: ${product.soldCount}</p>
        <p>📦 Còn lại: ${product.stock}</p>
        <p>📝 Mô tả: ${product.description}</p>
        <form action="${pageContext.request.contextPath}/add-to-cart" method="post" class="inline-action">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="id" value="${product.id}">
            <button type="submit" class="btn btn-add-cart">🛒 Thêm vào giỏ</button>
        </form>
        <a href="${pageContext.request.contextPath}/menu" class="btn-outline">← Quay lại menu</a>
    </div>
</div>
<%@ include file="footer.jsp" %>
</body>
</html>
