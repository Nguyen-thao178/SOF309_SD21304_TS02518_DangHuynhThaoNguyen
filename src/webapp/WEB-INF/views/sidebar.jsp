<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar">
    <div class="widget">
        <h3>📂 Danh mục</h3>
        <ul>
            <li><a href="${pageContext.request.contextPath}/menu?category=Coffee">☕ Cà phê</a></li>
            <li><a href="${pageContext.request.contextPath}/menu?category=Tea">🍵 Trà</a></li>
            <li><a href="${pageContext.request.contextPath}/menu?category=Pastry">🥐 Bánh ngọt</a></li>
        </ul>
    </div>
    <div class="widget">
        <h3>🔥 Top bán chạy</h3>
        <c:choose>
            <c:when test="${empty topSelling}">
                <p>Chưa có dữ liệu</p>
            </c:when>
            <c:otherwise>
                <ul>
                    <c:forEach var="p" items="${topSelling}">
                        <li>
                            <a href="${pageContext.request.contextPath}/product?id=${p.id}">
                                ${p.name} - ${p.soldCount} đã bán
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </div>
</aside>