<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn Hàng Cọc - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-chat-cart1">
</head>
<body>
<%@ include file="header.jsp" %>
<main class="deposit-orders-container">
    <div class="page-heading deposit-page-heading">
        <div>
            <span class="section-kicker">DEPOSIT WORKSPACE</span>
            <h2><c:choose><c:when test="${staffDepositView}">Đơn cọc <em>của khách hàng.</em></c:when><c:otherwise>Đơn cọc <em>của tôi.</em></c:otherwise></c:choose></h2>
            <p><c:choose><c:when test="${staffDepositView}">Theo dõi lịch nhận và xử lý các đơn Customer đã đặt cọc.</c:when><c:otherwise>Bạn chỉ nhìn thấy các đơn đặt cọc thuộc tài khoản của mình.</c:otherwise></c:choose></p>
        </div>
        <a href="${pageContext.request.contextPath}/cart" class="btn deposit-back-btn">← Về giỏ hàng</a>
    </div>

    <c:if test="${param.received == '1'}">
        <div class="notice-success">Đã xác nhận khách nhận hàng thành công.</div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="form-error">Không thể cập nhật đơn cọc. Vui lòng tải lại và thử lại.</div>
    </c:if>

    <section class="deposit-calendar-card">
        <div class="calendar-toolbar">
            <div>
                <span class="calendar-label">LỊCH NHẬN HÀNG</span>
                <h3 id="calendarTitle"></h3>
            </div>
            <div class="calendar-controls">
                <button type="button" id="previousMonth" class="btn-outline" aria-label="Tháng trước">←</button>
                <button type="button" id="todayMonth" class="btn-outline">Hôm nay</button>
                <button type="button" id="nextMonth" class="btn-outline" aria-label="Tháng sau">→</button>
            </div>
        </div>
        <div class="calendar-weekdays">
            <span>CN</span><span>T2</span><span>T3</span><span>T4</span>
            <span>T5</span><span>T6</span><span>T7</span>
        </div>
        <div id="depositCalendar" class="deposit-calendar"></div>
    </section>

    <section class="deposit-list-section">
        <div class="deposit-list-heading">
            <div><span class="calendar-label">DANH SÁCH</span><h3>Chi tiết đơn cọc</h3></div>
            <span class="deposit-total-count">${depositOrders.size()} đơn</span>
        </div>
        <c:choose>
            <c:when test="${empty depositOrders}">
                <p>Chưa có đơn đặt cọc.</p>
            </c:when>
            <c:otherwise>
                <div class="deposit-order-grid">
                    <c:forEach var="order" items="${depositOrders}">
                        <fmt:formatDate value="${order.pickupDate}" pattern="yyyy-MM-dd" var="pickupDateIso"/>
                        <article class="deposit-order-card"
                                 data-order-id="${order.id}"
                                 data-pickup-date="${pickupDateIso}"
                                 data-status="${order.status}">
                            <div class="deposit-order-header">
                                <div><small>MÃ ĐƠN</small><strong>#${order.id}</strong></div>
                                <c:choose>
                                    <c:when test="${order.status == 'deposit_pending'}">
                                        <span class="order-tag tag-deposit-pending">Chờ nhận</span>
                                    </c:when>
                                    <c:when test="${order.status == 'picked_up'}">
                                        <span class="order-tag tag-picked-up">Đã nhận</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="order-tag tag-no-show">Không nhận hàng</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <c:if test="${staffDepositView}">
                                <p class="deposit-method"><span>Khách hàng</span>
                                    <strong><c:out value="${order.customerName}"/> (@<c:out value="${order.customerUsername}"/>)</strong>
                                </p>
                            </c:if>
                            <div class="deposit-pickup-date">
                                <span>📅</span>
                                <div><small>NGÀY NHẬN HÀNG</small>
                                    <strong><fmt:formatDate value="${order.pickupDate}" pattern="dd/MM/yyyy"/></strong>
                                </div>
                            </div>
                            <div class="deposit-money-grid">
                                <div><small>TỔNG ĐƠN</small><strong><fmt:formatNumber value="${order.totalAmount}" type="number"/>₫</strong></div>
                                <div><small>ĐÃ CỌC</small><strong><fmt:formatNumber value="${order.depositAmount}" type="number"/>₫</strong></div>
                            </div>
                            <p class="deposit-method"><span>Hình thức cọc</span><strong>${order.paymentMethod == 'vnpay' ? 'VNPay' : 'Tiền mặt'}</strong></p>

                            <a class="btn-outline deposit-print-link"
                               href="${pageContext.request.contextPath}/receipt?orderId=${order.id}">
                                🖨 In phiếu cọc
                            </a>

                            <c:if test="${staffDepositView && order.status == 'deposit_pending'}">
                                <form action="${pageContext.request.contextPath}/deposit-orders" method="post"
                                      onsubmit="return confirm('Xác nhận khách đã nhận hàng và thanh toán phần còn lại?');">
                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="orderId" value="${order.id}">
                                    <fieldset class="balance-payment-method">
                                        <legend>Thanh toán phần còn lại</legend>
                                        <label>
                                            <input type="radio" name="balancePaymentMethod"
                                                   value="cash" checked>
                                            Tiền mặt
                                        </label>
                                        <label>
                                            <input type="radio" name="balancePaymentMethod"
                                                   value="vnpay">
                                            VNPay
                                        </label>
                                    </fieldset>
                                    <button type="submit" class="btn-primary">✓ Xác nhận đã nhận hàng</button>
                                </form>
                            </c:if>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<%@ include file="footer.jsp" %>
<script>
    (() => {
        const calendar = document.getElementById('depositCalendar');
        if (!calendar) return;

        const title = document.getElementById('calendarTitle');
        const cards = [...document.querySelectorAll('.deposit-order-card')];
        const events = cards.map(card => ({
            id: card.dataset.orderId,
            date: card.dataset.pickupDate,
            status: card.dataset.status
        })).filter(event => event.date);
        let visibleMonth = new Date();
        visibleMonth.setDate(1);

        function renderCalendar() {
            const year = visibleMonth.getFullYear();
            const month = visibleMonth.getMonth();
            title.textContent = 'Tháng ' + (month + 1) + '/' + year;
            calendar.innerHTML = '';

            const firstWeekday = new Date(year, month, 1).getDay();
            const daysInMonth = new Date(year, month + 1, 0).getDate();
            for (let blank = 0; blank < firstWeekday; blank++) {
                calendar.appendChild(document.createElement('div'));
            }

            for (let day = 1; day <= daysInMonth; day++) {
                const cell = document.createElement('div');
                cell.className = 'calendar-day';
                const dateKey = year + '-' + String(month + 1).padStart(2, '0')
                    + '-' + String(day).padStart(2, '0');
                cell.innerHTML = '<strong>' + day + '</strong>';

                events.filter(event => event.date === dateKey).forEach(event => {
                    const badge = document.createElement('button');
                    badge.type = 'button';
                    badge.className = 'calendar-event ' + event.status;
                    badge.textContent = '#' + event.id;
                    badge.addEventListener('click', () => {
                        document.querySelector('[data-order-id="' + event.id + '"]')
                            ?.scrollIntoView({behavior: 'smooth', block: 'center'});
                    });
                    cell.appendChild(badge);
                });
                calendar.appendChild(cell);
            }
        }

        document.getElementById('previousMonth').addEventListener('click', () => {
            visibleMonth.setMonth(visibleMonth.getMonth() - 1);
            renderCalendar();
        });
        document.getElementById('nextMonth').addEventListener('click', () => {
            visibleMonth.setMonth(visibleMonth.getMonth() + 1);
            renderCalendar();
        });
        document.getElementById('todayMonth').addEventListener('click', () => {
            visibleMonth = new Date();
            visibleMonth.setDate(1);
            renderCalendar();
        });
        renderCalendar();
    })();
</script>
</body>
</html>
