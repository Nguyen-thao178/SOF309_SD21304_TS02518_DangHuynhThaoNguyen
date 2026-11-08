<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-loyalty1">
</head>
<body>
<%@ include file="header.jsp" %>
<main class="checkout-container">
    <div class="checkout-hero">
        <div>
            <span class="section-kicker">CHIDORI CHECKOUT</span>
            <h2>Thanh toán <em>thật nhẹ nhàng.</em></h2>
            <p>Chọn thanh toán ngay hoặc giữ món bằng một khoản cọc.</p>
        </div>
        <div class="checkout-secure"><span>✓</span> Giao dịch an toàn</div>
    </div>

    <c:choose>
        <c:when test="${empty sessionScope.cart}">
            <div class="checkout-empty">
                <span>☕</span>
                <h3>Giỏ hàng đang trống</h3>
                <a class="btn-primary" href="${pageContext.request.contextPath}/menu">Khám phá menu</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="checkout-layout">
                <section class="checkout-order">
                    <div class="checkout-section-title">
                        <span>01</span>
                        <div><h3>Thông tin đơn hàng</h3><p>Xác nhận khách hàng và các món đã chọn.</p></div>
                    </div>
                    <div class="customer-card">
                        <span class="customer-avatar">${sessionScope.user.fullname.substring(0, 1)}</span>
                        <div>
                            <strong>${sessionScope.user.fullname}</strong>
                            <small>${sessionScope.user.email}</small>
                        </div>
                        <i>Đã xác thực</i>
                    </div>

                    <div class="checkout-table-wrap">
                        <table class="cart-table checkout-table">
                            <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>SL</th>
                                <th>Đơn giá</th>
                                <th>Thành tiền</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:set var="total" value="0"/>
                            <c:forEach var="item" items="${sessionScope.cart}">
                                <tr>
                                    <td><strong>${item.product.name}</strong></td>
                                    <td><span class="quantity-pill">${item.quantity}</span></td>
                                    <td><fmt:formatNumber value="${item.discountedPrice}" type="number"/>${appSettings.currency_symbol}</td>
                                    <td><strong><fmt:formatNumber value="${item.discountedPrice * item.quantity}" type="number"/>${appSettings.currency_symbol}</strong></td>
                                </tr>
                                <c:set var="total" value="${total + item.discountedPrice * item.quantity}"/>
                            </c:forEach>
                            </tbody>
                            <tfoot>
                            <tr class="total-row">
                                <td colspan="3">Tổng giá trị đơn</td>
                                <td><strong><fmt:formatNumber value="${total}" type="number"/>${appSettings.currency_symbol}</strong></td>
                            </tr>
                            </tfoot>
                        </table>
                    </div>
                </section>

                <section class="checkout-options">
                    <form id="checkoutForm">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                        <c:if test="${memberVoucherEnabled}">
                            <section class="checkout-voucher-card">
                                <div class="voucher-heading">
                                    <span>🎟️</span>
                                    <div><strong>Dùng điểm thành viên</strong>
                                        <small>Bạn có <b>${loyaltyPoints} điểm</b> · giảm tối đa 80% đơn</small></div>
                                </div>
                                <div class="voucher-control">
                                    <input id="redeemPoints" name="redeemPoints" type="number"
                                           value="0" min="0" max="${loyaltyPoints}" step="1">
                                    <button id="useMaximumPoints" type="button">Dùng tối đa</button>
                                </div>
                                <p id="voucherPreview">Chưa áp dụng voucher.</p>
                            </section>
                        </c:if>
                        <fieldset>
                            <legend><span>02</span> Hình thức giao dịch</legend>
                            <c:if test="${not customerDepositOnly}">
                                <label class="choice-card">
                                    <input type="radio" name="orderType" value="direct" checked>
                                    <i class="choice-icon">⚡</i>
                                    <span>
                                        <strong>Thanh toán liền</strong>
                                        <small>Thanh toán toàn bộ và nhận hàng ngay.</small>
                                    </span>
                                    <b class="choice-check">✓</b>
                                </label>
                            </c:if>
                            <label class="choice-card">
                                <input type="radio" name="orderType" value="deposit"
                                       <c:if test="${customerDepositOnly}">checked</c:if>>
                                <i class="choice-icon">◷</i>
                                <span>
                                    <strong>Đặt cọc</strong>
                                    <small>Cọc ${depositPercent}% để giữ hàng và chọn ngày nhận.</small>
                                </span>
                                <b class="choice-check">✓</b>
                            </label>
                        </fieldset>

                        <div id="pickupDateGroup" class="conditional-field"
                             <c:if test="${not customerDepositOnly}">hidden</c:if>>
                            <label for="pickupDate"><strong>📅 Ngày dự kiến nhận hàng</strong></label>
                            <input type="date" id="pickupDate" name="pickupDate" min="${minPickupDate}"
                                   <c:if test="${customerDepositOnly}">required</c:if>>
                            <p><span>Tiền cọc (${depositPercent}%)</span>
                                <strong id="depositAmount" class="deposit-highlight">
                                    <fmt:formatNumber value="${total * depositPercent / 100}" type="number"/>${appSettings.currency_symbol}
                                </strong>
                            </p>
                            <small>ⓘ Quá ngày nhận, đơn chuyển sang “Không nhận hàng” và sản phẩm được hoàn kho.</small>
                        </div>

                        <fieldset>
                            <legend><span>03</span> Phương thức thanh toán</legend>
                            <c:if test="${not customerDepositOnly}">
                                <label class="choice-card">
                                    <input type="radio" name="paymentMethod" value="cash" checked>
                                    <i class="choice-icon">₫</i>
                                    <span>
                                        <strong>Tiền mặt</strong>
                                        <small>Thu tiền trực tiếp tại quầy.</small>
                                    </span>
                                    <b class="choice-check">✓</b>
                                </label>
                            </c:if>
                            <label class="choice-card">
                                <input type="radio" name="paymentMethod" value="vnpay"
                                       <c:if test="${customerDepositOnly}">checked</c:if>>
                                <i class="choice-icon vnpay-icon">V</i>
                                <span>
                                    <strong>VNPay</strong>
                                    <small>
                                        ${customerDepositOnly ? 'Phương thức thanh toán duy nhất cho tài khoản Customer' : 'Thanh toán toàn bộ hoặc tiền cọc'} qua VNPay
                                        <c:if test="${vnpaySandbox}"> sandbox.</c:if>
                                    </small>
                                </span>
                                <b class="choice-check">✓</b>
                            </label>
                        </fieldset>

                        <c:if test="${vnpaySandbox}">
                            <div class="vnpay-sandbox-note" id="vnpaySandboxNote" hidden>
                                <strong>🧪 Chế độ VNPay Sandbox</strong>
                                <p>Không quét QR bằng ứng dụng ngân hàng thật. Hệ thống sẽ chuyển thẳng tới NCB test.</p>
                                <dl>
                                    <div><dt>Số thẻ</dt><dd>9704198526191432198</dd></div>
                                    <div><dt>Chủ thẻ</dt><dd>NGUYEN VAN A</dd></div>
                                    <div><dt>Ngày phát hành</dt><dd>07/15</dd></div>
                                    <div><dt>OTP</dt><dd>123456</dd></div>
                                </dl>
                            </div>
                        </c:if>

                        <div id="paymentSummary" class="payment-summary"></div>
                        <button type="submit" id="submitCheckout" class="btn-primary">
                            <span>Xác nhận giao dịch</span><i>→</i>
                        </button>
                        <p class="checkout-assurance">🔒 Thông tin giao dịch được bảo vệ an toàn</p>
                        <div id="paymentMessage" role="status" aria-live="polite"></div>
                    </form>
                </section>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="footer.jsp" %>
<script>
    (() => {
        const form = document.getElementById('checkoutForm');
        if (!form) return;

        const total = Number('${total}');
        const depositRate = Number('${depositPercent}') / 100;
        const pickupGroup = document.getElementById('pickupDateGroup');
        const pickupInput = document.getElementById('pickupDate');
        const summary = document.getElementById('paymentSummary');
        const submitButton = document.getElementById('submitCheckout');
        const message = document.getElementById('paymentMessage');
        const sandboxNote = document.getElementById('vnpaySandboxNote');
        const money = new Intl.NumberFormat('vi-VN');
        const currencySymbol = '${appSettings.currency_symbol}';
        const redeemInput = document.getElementById('redeemPoints');
        const maximumButton = document.getElementById('useMaximumPoints');
        const voucherPreview = document.getElementById('voucherPreview');
        const pointValue = Number('${loyaltyPointValue}');
        const availablePoints = Number('${loyaltyPoints}');
        const maximumPoints = Math.max(0, Math.min(availablePoints,
            Math.floor(total * 0.8 / Math.max(1, pointValue))));

        function voucher() {
            if (!redeemInput) return {points: 0, discount: 0, net: total};
            const points = Math.max(0, Math.min(maximumPoints,
                Number.parseInt(redeemInput.value || '0', 10) || 0));
            redeemInput.value = points;
            const discount = points * pointValue;
            if (voucherPreview) voucherPreview.innerHTML = points > 0
                ? 'Đã dùng <b>' + points + ' điểm</b>, giảm <b>' + money.format(discount) + currencySymbol + '</b>.'
                : 'Chưa áp dụng voucher.';
            return {points, discount, net: total - discount};
        }

        function refreshOptions() {
            const orderType = form.elements.orderType.value;
            const paymentMethod = form.elements.paymentMethod.value;
            const isDeposit = orderType === 'deposit';
            const applied = voucher();
            const amount = isDeposit ? Math.round(applied.net * depositRate) : applied.net;

            pickupGroup.hidden = !isDeposit;
            pickupInput.required = isDeposit;
            if (sandboxNote) sandboxNote.hidden = paymentMethod !== 'vnpay';
            summary.innerHTML =
                '<strong>Số tiền cần thanh toán: ' + money.format(amount) + currencySymbol + '</strong>' +
                (applied.discount > 0 ? '<small>Giá gốc ' + money.format(total) + currencySymbol +
                    ' · Voucher -' + money.format(applied.discount) + currencySymbol + '</small>' : '') +
                '<small>' + (isDeposit ? 'Tiền cọc giữ hàng' : 'Toàn bộ giá trị đơn') +
                ' · ' + (paymentMethod === 'cash' ? 'Tiền mặt' : 'VNPay') + '</small>';
            const depositAmount = document.getElementById('depositAmount');
            if (depositAmount) depositAmount.textContent = money.format(Math.round(applied.net * depositRate)) + currencySymbol;
            submitButton.querySelector('span').textContent =
                isDeposit ? 'Xác nhận đặt cọc' : 'Xác nhận thanh toán';
        }

        form.addEventListener('change', refreshOptions);
        if (redeemInput) redeemInput.addEventListener('input', refreshOptions);
        if (maximumButton) maximumButton.addEventListener('click', () => {
            redeemInput.value = maximumPoints;
            refreshOptions();
        });
        refreshOptions();

        form.addEventListener('submit', async event => {
            event.preventDefault();
            submitButton.disabled = true;
            message.className = '';
            message.textContent = 'Đang xử lý giao dịch…';

            try {
                const response = await fetch('${pageContext.request.contextPath}/checkout', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'},
                    body: new URLSearchParams(new FormData(form))
                });
                const result = await response.json();
                message.className = result.success ? 'scan-success' : 'scan-error';
                message.textContent = result.message;
                if (result.success && result.redirectUrl) {
                    window.setTimeout(() => window.location.href = result.redirectUrl, 500);
                    return;
                }
            } catch (error) {
                message.className = 'scan-error';
                message.textContent = 'Không thể kết nối máy chủ. Vui lòng thử lại.';
            }
            submitButton.disabled = false;
        });
    })();
</script>
</body>
</html>
