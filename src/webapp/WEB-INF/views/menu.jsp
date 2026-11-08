<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menu - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-chat-cart1">
</head>
<body>
<%@ include file="header.jsp" %>
<div class="container">
    <%@ include file="sidebar.jsp" %>
    <main class="main-content">
        <h2>☕ Toàn bộ menu</h2>
        <c:if test="${not empty promoMap}">
    <div class="promo-section" style="margin-bottom: 2rem;">
        <h2>🎁 Khuyến mãi sốc - Giảm đến 30%</h2>
        <div class="product-grid">
            <c:forEach var="entry" items="${promoMap}">
                <c:set var="promoProduct" value="${productList.stream().filter(p -> p.id == entry.key).findFirst().orElse(null)}" />
                <c:if test="${not empty promoProduct}">
                    <div class="product-card promo-card" style="border: 2px solid var(--orange);">
                        <img src="${promoProduct.imageUrl}" alt="${promoProduct.name}">
                        <div class="product-info">
                            <h3>${promoProduct.name} <span style="background:red; font-size:0.7rem; padding:0.2rem 0.5rem; border-radius:20px;">-${entry.value}%</span></h3>
                            <p class="price">
                                <span style="text-decoration: line-through; font-size:0.9rem;"><fmt:formatNumber value="${promoProduct.price}" type="number"/>₫</span><br/>
                                <fmt:formatNumber value="${promoProduct.price * (100 - entry.value) / 100}" type="number"/>₫
                            </p>
                            <p>📦 Đã bán: ${promoProduct.soldCount} | 📊 Tồn: ${promoProduct.stock}</p>
                            <form action="${pageContext.request.contextPath}/add-to-cart" method="post" class="inline-action">
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${promoProduct.id}">
                                <button type="submit" class="btn btn-add-cart">➕ Thêm giỏ</button>
                            </form>
                        </div>
                    </div>
                </c:if>
            </c:forEach>
        </div>
    </div>
</c:if>
        <div class="product-grid">
            <c:forEach var="p" items="${productList}">
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
                        <p>📦 Đã bán: ${p.soldCount} | 📊 Tồn: ${p.stock}</p>
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
