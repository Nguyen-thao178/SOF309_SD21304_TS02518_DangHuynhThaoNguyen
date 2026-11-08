<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-chat-cart1">
</head>
<body>
<%@ include file="header.jsp" %>
<div class="cart-container">
    <h2>🛒 Giỏ hàng của bạn</h2>
    <nav class="cart-tabs" aria-label="Giỏ hàng và đơn đã cọc">
        <a href="${pageContext.request.contextPath}/cart" class="cart-tab active">Giỏ hàng</a>
        <a href="${pageContext.request.contextPath}/deposit-orders" class="cart-tab">
            Đã Cọc
            <c:if test="${pendingDepositCount > 0}">
                <span class="tab-count">${pendingDepositCount}</span>
            </c:if>
        </a>
    </nav>
    <c:if test="${barcodeScannerEnabled && currentRole == 'staff'}">
    <section class="barcode-panel" aria-labelledby="barcode-title">
        <div>
            <h3 id="barcode-title">Quét mã vạch</h3>
            <p>Giữ con trỏ trong ô bên dưới, sau đó quét bằng máy đọc mã vạch. Mỗi lần quét sẽ thêm một sản phẩm.</p>
        </div>
        <form id="barcodeForm" action="${pageContext.request.contextPath}/cart/scan" method="post">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <label for="barcodeInput" class="sr-only">Mã vạch sản phẩm</label>
            <input type="text" id="barcodeInput" name="barcode" maxlength="16" inputmode="numeric"
                   autocomplete="off" autocapitalize="off" spellcheck="false"
                   placeholder="Quét barcode EAN-13 rồi nhấn Enter" autofocus required>
            <button type="submit" class="btn-primary">Quét / Thêm</button>
        </form>
        <div id="scanStatus" class="scan-status" role="status" aria-live="polite"></div>
    </section>
    </c:if>
    <c:choose>
        <c:when test="${empty sessionScope.cart or sessionScope.cart.size() == 0}">
            <p>Giỏ hàng trống. <a href="${pageContext.request.contextPath}/menu">Mua sắm ngay</a></p>
        </c:when>
        <c:otherwise>
            <table class="cart-table">
                <tr><th>Sản phẩm</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th></th></tr>
                <c:set var="total" value="0" />
                <c:forEach var="item" items="${sessionScope.cart}">
                    <tr>
                        <td>${item.product.name}</td>
                        <td><fmt:formatNumber value="${item.discountedPrice}" type="number"/>${appSettings.currency_symbol}</td>
                        <td>
                            <form action="${pageContext.request.contextPath}/update-cart" method="post" style="display:inline;">
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${item.product.id}">
                                <input type="number" name="quantity" value="${item.quantity}" min="1" max="${item.product.stock}" style="width:70px;">
                                <button type="submit" class="btn-outline" style="padding:0.2rem 0.5rem;">Cập nhật</button>
                            </form>
                         </td>
                        <td><fmt:formatNumber value="${item.discountedPrice * item.quantity}" type="number"/>${appSettings.currency_symbol}</td>
                        <td>
                            <form action="${pageContext.request.contextPath}/remove-cart" method="post"
                                  class="inline-action">
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${item.product.id}">
                                <button type="submit" class="remove-btn link-button">❌ Xóa</button>
                            </form>
                        </td>
                        <c:set var="total" value="${total + item.discountedPrice * item.quantity}" />
                    </tr>
                </c:forEach>
                <tr class="total-row"><td colspan="3"><strong>Tổng cộng:</strong></td>
                <td><strong><fmt:formatNumber value="${total}" type="number"/>${appSettings.currency_symbol}</strong></td><td></td></tr>
            </table>
            <div class="cart-actions" style="margin-top:1rem;">
                <a href="${pageContext.request.contextPath}/menu" class="btn">Tiếp tục mua</a>
                <a href="${pageContext.request.contextPath}/checkout" class="btn-primary">
                    ${currentRole == 'customer' || currentRole == 'member' ? 'Đặt hàng' : 'Thanh toán'}
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="footer.jsp" %>
<script>
    (() => {
        const form = document.getElementById('barcodeForm');
        if (!form) return;
        const input = document.getElementById('barcodeInput');
        const status = document.getElementById('scanStatus');
        const csrfToken = form.querySelector('input[name="_csrf"]')?.value || '';
        let submitting = false;

        const focusScanner = () => {
            if (!submitting) {
                input.focus();
                input.select();
            }
        };

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            const barcode = input.value.trim();
            if (!barcode || submitting) return;

            submitting = true;
            status.className = 'scan-status';
            status.textContent = 'Đang đọc mã ' + barcode + '…';

            try {
                const response = await fetch(form.action, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                        'Accept': 'application/json',
                        'X-CSRF-Token': csrfToken
                    },
                    body: new URLSearchParams({barcode, _csrf: csrfToken})
                });
                if (response.redirected) {
                    window.location.href = response.url;
                    return;
                }
                const result = await response.json();
                status.className = 'scan-status ' + (result.success ? 'scan-success' : 'scan-error');
                status.textContent = result.message;

                if (result.success) {
                    window.setTimeout(() => window.location.reload(), 250);
                    return;
                }
            } catch (error) {
                status.className = 'scan-status scan-error';
                status.textContent = 'Không thể xử lý mã vạch. Vui lòng thử lại.';
            }

            input.value = '';
            submitting = false;
            focusScanner();
        });

        input.addEventListener('keydown', (event) => {
            if ((event.key === 'Enter' || event.key === 'Tab') && input.value.trim()) {
                event.preventDefault();
                form.requestSubmit();
            }
        });

        document.addEventListener('click', (event) => {
            if (!event.target.closest('input, textarea, select, button, a')) focusScanner();
        });
        window.addEventListener('pageshow', focusScanner);
    })();
</script>
</body>
</html>
