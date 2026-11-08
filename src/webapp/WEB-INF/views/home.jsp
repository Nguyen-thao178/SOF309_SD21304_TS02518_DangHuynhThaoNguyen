<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chidori Coffee - Trang chủ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-chat-cart1">
</head>
<body>
<%@ include file="header.jsp" %>
<div class="container">
    <%@ include file="sidebar.jsp" %>
    <main class="main-content">
        <div class="slider-container">
            <c:choose>
                <c:when test="${not empty featuredProducts}">
                    <c:forEach var="sliderProduct" items="${featuredProducts}" varStatus="slideStatus">
                        <div class="slide ${slideStatus.first ? 'active' : ''}">
                            <img src="${sliderProduct.imageUrl}"
                                 alt="${sliderProduct.name}">
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="slide active">
                        <img src="${pageContext.request.contextPath}/assets/images/caphesua.jpeg"
                             alt="Cà phê sữa">
                    </div>
                </c:otherwise>
            </c:choose>
            <button class="prev">&#10094;</button>
            <button class="next">&#10095;</button>
        </div>
        <h2>Sản phẩm nổi bật</h2>
        <div class="product-grid">
            <c:forEach var="p" items="${featuredProducts}">
                <div class="product-card">
                    <img src="${p.imageUrl}" alt="${p.name}">
                    <div class="product-info">
                        <h3>${p.name}</h3>
                        <p class="price">
                            <c:if test="${maxDiscount > 0}">
                                <span style="text-decoration: line-through; font-size:0.9rem; color:gray;">
                                    <fmt:formatNumber value="${p.price}" type="number"/>₫
                                </span><br/>
                            </c:if>
                            <fmt:formatNumber value="${p.price * (100 - maxDiscount) / 100}" type="number"/>₫
                        </p>
                        <p>📦 Đã bán: ${p.soldCount} | 📊 Còn: ${p.stock}</p>
                        <form action="${pageContext.request.contextPath}/add-to-cart" method="post" class="inline-action">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${p.id}">
                            <button type="submit" class="btn btn-add-cart">➕ Thêm giỏ</button>
                        </form>
                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="btn-outline">🔍 Chi tiết</a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </main>
</div>
<%@ include file="footer.jsp" %>
</body>
</html>
