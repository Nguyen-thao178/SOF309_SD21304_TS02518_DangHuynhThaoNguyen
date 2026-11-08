<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hóa đơn #${order.id} - Chidori Coffee</title>
    <style>
        :root { color-scheme: light; }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            padding: 24px 12px;
            color: #111;
            font-family: "Courier New", monospace;
            background: #d8d2ca;
        }
        .receipt {
            width: 80mm;
            min-height: 120mm;
            margin: 0 auto;
            padding: 7mm 5mm 6mm;
            background: #fffef9;
            box-shadow: 0 16px 45px rgba(0,0,0,.24);
        }
        .center { text-align: center; }
        .store-name {
            margin: 0 0 4px;
            font-family: Arial, sans-serif;
            font-size: 22px;
            font-weight: 900;
        }
        .store-info { margin: 2px 0; font-size: 11px; line-height: 1.35; }
        .receipt-title {
            margin: 10px 0 2px;
            font-family: Arial, sans-serif;
            font-size: 20px;
            font-weight: 900;
        }
        .receipt-type { margin: 0 0 8px; font-size: 12px; font-weight: bold; }
        .meta {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2px 8px;
            margin: 8px 0;
            font-size: 11px;
        }
        .meta span:nth-child(even) { text-align: right; }
        .divider { border-top: 1px dashed #111; margin: 6px 0; }
        table {
            width: 100%;
            border-collapse: collapse;
            table-layout: fixed;
            font-size: 11px;
        }
        th, td { padding: 4px 2px; vertical-align: top; }
        th { border-top: 1px solid #111; border-bottom: 1px solid #111; }
        tbody td { border-bottom: 1px dotted #777; }
        th:first-child, td:first-child { width: 40%; text-align: left; overflow-wrap: anywhere; }
        th:nth-child(2), td:nth-child(2) { width: 10%; text-align: center; }
        th:nth-child(3), td:nth-child(3),
        th:nth-child(4), td:nth-child(4) { width: 25%; text-align: right; }
        .summary { margin-top: 7px; font-size: 12px; }
        .summary-row {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            margin: 3px 0;
        }
        .grand-total {
            margin-top: 6px;
            padding-top: 5px;
            border-top: 2px solid #111;
            font-family: Arial, sans-serif;
            font-size: 19px;
            font-weight: 900;
        }
        .thanks { margin: 13px 0 2px; font-size: 11px; font-style: italic; }
        .receipt-actions {
            display: flex;
            width: 80mm;
            margin: 14px auto 0;
            gap: 8px;
        }
        .receipt-actions button, .receipt-actions a {
            flex: 1;
            padding: 10px;
            border: 0;
            border-radius: 8px;
            color: white;
            font: 600 13px Arial, sans-serif;
            text-align: center;
            text-decoration: none;
            cursor: pointer;
            background: #ff5722;
        }
        .receipt-actions a { background: #252525; }
        @page { size: 80mm auto; margin: 3mm; }
        @media print {
            body { padding: 0; background: white; }
            .receipt {
                width: 74mm;
                min-height: 0;
                margin: 0;
                padding: 2mm;
                box-shadow: none;
            }
            .receipt-actions { display: none !important; }
        }
    </style>
</head>
<body>
<article class="receipt">
    <header class="center">
        <h1 class="store-name"><c:out value="${appSettings.store_name}"/></h1>
        <p class="store-info"><c:out value="${appSettings.address}"/></p>
        <p class="store-info">ĐT: <c:out value="${appSettings.hotline}"/></p>
        <h2 class="receipt-title">HÓA ĐƠN BÁN HÀNG</h2>
        <p class="receipt-type">
            <c:choose>
                <c:when test="${paymentStage == 'deposit'}">PHIẾU ĐẶT CỌC</c:when>
                <c:when test="${paymentStage == 'balance'}">THANH TOÁN CÒN LẠI</c:when>
                <c:otherwise>THANH TOÁN TRỰC TIẾP</c:otherwise>
            </c:choose>
        </p>
    </header>

    <div class="meta">
        <span>Ngày: <fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy"/></span>
        <span>Số: <fmt:formatNumber value="${order.id}" pattern="00000000"/></span>
        <span>Thu ngân: ${cashier.fullname}</span>
        <span>In lúc: <fmt:formatDate value="${printedAt}" pattern="HH:mm:ss"/></span>
        <span>Giờ vào: <fmt:formatDate value="${order.orderDate}" pattern="HH:mm:ss"/></span>
        <span>${order.paymentMethod == 'vnpay' ? 'VNPay' : 'Tiền mặt'}</span>
    </div>

    <c:if test="${not empty order.pickupDate}">
        <div class="divider"></div>
        <p class="store-info"><strong>Ngày nhận hàng:</strong>
            <fmt:formatDate value="${order.pickupDate}" pattern="dd/MM/yyyy"/></p>
    </c:if>

    <table>
        <thead>
        <tr><th>Mặt hàng</th><th>SL</th><th>Giá</th><th>T.tiền</th></tr>
        </thead>
        <tbody>
        <c:forEach var="item" items="${receiptItems}">
            <tr>
                <td>${item.productName}</td>
                <td>${item.quantity}</td>
                <td><fmt:formatNumber value="${item.price}" pattern="#,##0"/></td>
                <td><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <section class="summary">
        <div class="summary-row">
            <span>Tổng đơn:</span>
            <strong><fmt:formatNumber value="${order.totalAmount}" pattern="#,##0"/>${appSettings.currency_symbol}</strong>
        </div>
        <c:if test="${order.orderType == 'deposit'}">
            <div class="summary-row">
                <span>Tiền cọc:</span>
                <strong><fmt:formatNumber value="${order.depositAmount}" pattern="#,##0"/>${appSettings.currency_symbol}</strong>
            </div>
            <c:if test="${paymentStage == 'deposit'}">
                <div class="summary-row">
                    <span>Còn lại khi nhận:</span>
                    <strong><fmt:formatNumber value="${remainingAmount}" pattern="#,##0"/>${appSettings.currency_symbol}</strong>
                </div>
            </c:if>
        </c:if>
        <div class="summary-row grand-total">
            <span>ĐÃ THU:</span>
            <span><fmt:formatNumber value="${paidNow}" pattern="#,##0"/>${appSettings.currency_symbol}</span>
        </div>
    </section>

    <footer class="center">
        <p class="thanks">Cảm ơn Quý khách. Hẹn gặp lại!</p>
        <p class="store-info">*** <c:out value="${appSettings.store_name}"/> ***</p>
    </footer>
</article>

<nav class="receipt-actions">
    <button type="button" onclick="window.print()">🖨 In lại hóa đơn</button>
    <a href="${pageContext.request.contextPath}/${order.orderType == 'deposit' ? 'deposit-orders' : 'history'}">
        Hoàn tất
    </a>
</nav>

<c:if test="${autoPrint}">
    <script>
        window.addEventListener('load', function () {
            window.setTimeout(function () { window.print(); }, 250);
        });
    </script>
</c:if>
</body>
</html>
