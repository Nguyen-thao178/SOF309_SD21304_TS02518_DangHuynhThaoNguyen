<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<script>
    window.contextPath = "${pageContext.request.contextPath}";
    window.CHIDORI_CSRF = "${sessionScope.csrfToken}";
</script>
<c:set var="currentRole" value="${sessionScope.user.role}"/>
<c:set var="headerCartQuantity" value="0"/>
<c:forEach var="headerCartItem" items="${sessionScope.cart}">
    <c:set var="headerCartQuantity" value="${headerCartQuantity + headerCartItem.quantity}"/>
</c:forEach>
<header>
    <div class="top-bar">
        <div class="logo">
            <a href="${homePath}">☕ <c:out value="${appSettings.store_name}"/></a>
        </div>
        <c:if test="${currentRole == 'staff' || currentRole == 'customer' || currentRole == 'member'}">
            <form class="search-form" action="${pageContext.request.contextPath}/search" method="get">
                <input type="text" name="keyword" placeholder="Tìm món, thức uống..." required>
                <button type="submit">🔍</button>
            </form>
        </c:if>
        <div class="user-actions">
            <button id="themeToggle" class="theme-toggle" type="button"
                    aria-label="Chuyển giao diện sáng/tối">🌙</button>
            <c:if test="${not empty sessionScope.user}">
                <span>🧑‍💼 ${sessionScope.user.fullname} (${sessionScope.user.role})</span>
                <c:if test="${currentRole == 'staff' || currentRole == 'customer'
                        || currentRole == 'member' || currentRole == 'manager'}">
                    <a href="${pageContext.request.contextPath}/history">📜 Lịch sử</a>
                </c:if>
                <c:if test="${currentRole == 'staff' || currentRole == 'customer' || currentRole == 'member'}">
                    <a href="${pageContext.request.contextPath}/deposit-orders">📅 Đơn Hàng Cọc</a>
                </c:if>
                <c:if test="${currentRole == 'staff' || currentRole == 'customer' || currentRole == 'member'}">
                    <a href="${pageContext.request.contextPath}/loyalty">🎖️ <c:choose><c:when test="${currentRole == 'staff'}">Khách thành viên</c:when><c:when test="${currentRole == 'customer'}">Đăng ký thành viên</c:when><c:otherwise>Điểm & Voucher</c:otherwise></c:choose></a>
                </c:if>
                <a href="${pageContext.request.contextPath}/change-password">🔑 Đổi mật khẩu</a>
                <c:if test="${currentRole == 'staff' || currentRole == 'customer' || currentRole == 'member'}">
                    <a href="${pageContext.request.contextPath}/cart" id="headerCartLink">🛒 Giỏ
                        <span id="headerCartCount" class="header-cart-count ${headerCartQuantity == 0 ? 'is-empty' : ''}">${headerCartQuantity}</span>
                    </a>
                </c:if>
                <form action="${pageContext.request.contextPath}/logout" method="post" class="inline-action">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <button type="submit" class="link-button">🚪 Đăng xuất</button>
                </form>
            </c:if>
        </div>
    </div>
    <nav class="navbar">
        <c:if test="${currentRole == 'staff' || currentRole == 'customer' || currentRole == 'member'}">
            <a href="${pageContext.request.contextPath}/home">🏠 Trang chủ</a>
            <a href="${pageContext.request.contextPath}/menu">📋 Menu</a>
            <a href="${pageContext.request.contextPath}/promotion">🎁 Khuyến mãi</a>
        </c:if>
        <c:if test="${currentRole == 'admin'}">
            <a href="${pageContext.request.contextPath}/admin/report">📊 Báo cáo</a>
            <a href="${pageContext.request.contextPath}/admin/contacts">📞 Quản lý liên hệ</a>
            <a href="${pageContext.request.contextPath}/admin/settings">⚙️ Cài đặt</a>
        </c:if>
        <c:if test="${currentRole == 'manager'}">
            <a href="${pageContext.request.contextPath}/admin/users">👥 Quản lý nhân viên</a>
            <a href="${pageContext.request.contextPath}/admin/products">📦 Quản lý menu</a>
            <a href="${pageContext.request.contextPath}/admin/report">📊 Báo cáo</a>
        </c:if>
    </nav>
    <div class="breadcrumb">
        <c:set var="uri" value="${pageContext.request.requestURI}"/>
        <a href="${homePath}">Trang chính</a> &gt;
        <c:choose>
            <c:when test="${uri.contains('/menu')}">Menu</c:when>
            <c:when test="${uri.contains('/cart')}">Giỏ hàng</c:when>
            <c:when test="${uri.contains('/checkout')}">Thanh toán</c:when>
            <c:when test="${uri.contains('/history')}">Lịch sử đơn</c:when>
            <c:when test="${uri.contains('/deposit-orders')}">Đơn Hàng Cọc</c:when>
            <c:when test="${uri.contains('/promotion')}">Khuyến mãi</c:when>
            <c:when test="${uri.contains('/contact')}">Liên hệ</c:when>
            <c:when test="${uri.contains('/loyalty')}">Điểm thưởng</c:when>
            <c:when test="${uri.contains('/admin')}">Quản trị</c:when>
            <c:when test="${uri.contains('/change-password')}">Đổi mật khẩu</c:when>
            <c:otherwise>Trang hiện tại</c:otherwise>
        </c:choose>
    </div>
</header>
