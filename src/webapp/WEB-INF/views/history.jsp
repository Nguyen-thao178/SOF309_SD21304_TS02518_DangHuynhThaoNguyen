<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử giao dịch - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-chat-cart1">
</head>
<body>
<%@ include file="header.jsp" %>
<main class="history-container">
    <div class="page-heading">
        <div>
            <h2>📜 Lịch sử giao dịch</h2>
            <p>${viewingAllOrders
                    ? 'Theo dõi lịch sử giao dịch của toàn bộ nhân viên.'
                    : 'Theo dõi thanh toán trực tiếp và toàn bộ vòng đời đơn đặt cọc.'}</p>
        </div>
        <div>
            <c:if test="${not viewingAllOrders}">
                <a href="${pageContext.request.contextPath}/deposit-orders" class="btn">📅 Đơn Hàng Cọc</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/export-history" class="btn-primary">📄 Xuất PDF</a>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty orderList}">
            <p>${viewingAllOrders ? 'Hệ thống chưa có giao dịch nào.' : 'Bạn chưa có giao dịch nào.'}</p>
        </c:when>
        <c:otherwise>
            <div class="table-scroll">
                <table class="cart-table">
                    <thead>
                    <tr>
                        <th>Mã đơn</th>
                        <th>Ngày đặt</th>
                        <th>Ngày nhận</th>
                        <th>Tổng tiền</th>
                        <th>Đã cọc</th>
                        <th>Thanh toán</th>
                        <th>Loại giao dịch</th>
                        <th>Hóa đơn</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="order" items="${orderList}">
                        <tr>
                            <td>#${order.id}</td>
                            <td><fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty order.pickupDate}">
                                        <fmt:formatDate value="${order.pickupDate}" pattern="dd/MM/yyyy"/>
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                            <td><fmt:formatNumber value="${order.totalAmount}" type="number"/>₫</td>
                            <td>
                                <c:choose>
                                    <c:when test="${order.orderType == 'deposit'}">
                                        <fmt:formatNumber value="${order.depositAmount}" type="number"/>₫
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </td>
                            <td>${order.paymentMethod == 'vnpay' ? 'VNPay' : 'Tiền mặt'}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${order.transactionTag == 'Thanh toán trực tiếp'}">
                                        <span class="order-tag tag-direct">Thanh toán trực tiếp</span>
                                    </c:when>
                                    <c:when test="${order.transactionTag == 'Đã cọc nhưng không nhận hàng'}">
                                        <span class="order-tag tag-no-show">Đã cọc nhưng không nhận hàng</span>
                                    </c:when>
                                    <c:when test="${order.transactionTag == 'Đã cọc và đã nhận hàng'}">
                                        <span class="order-tag tag-picked-up">Đã cọc và đã nhận hàng</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="order-tag tag-deposit-pending">Đã cọc - Chờ nhận hàng</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a class="btn-outline"
                                   href="${pageContext.request.contextPath}/receipt?orderId=${order.id}">
                                    🖨 In
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>
<%@ include file="footer.jsp" %>
</body>
</html>
