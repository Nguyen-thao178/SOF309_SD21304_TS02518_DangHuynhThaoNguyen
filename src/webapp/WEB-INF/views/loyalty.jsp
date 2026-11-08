<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thành viên & Điểm thưởng - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260809-loyalty1">
</head>
<body>
<%@ include file="header.jsp" %>
<main class="loyalty-page">
    <c:if test="${not empty error}"><div class="loyalty-alert error">⚠ ${error}</div></c:if>
    <c:if test="${not empty success}"><div class="loyalty-alert success">✓ ${success}</div></c:if>

    <c:choose>
        <c:when test="${currentRole == 'staff'}">
            <section class="loyalty-hero staff-hero">
                <div><span class="section-kicker">CHIDORI MEMBERS</span>
                    <h1>Khách hàng <em>thành viên.</em></h1>
                    <p>Theo dõi hồ sơ, điểm khả dụng và tổng chi tiêu của toàn bộ hội viên.</p></div>
                <div class="hero-medallion"><b>${members.size()}</b><small>hội viên</small></div>
            </section>
            <section class="member-directory">
                <div class="directory-toolbar">
                    <div><h2>Danh sách thành viên</h2><p>Dữ liệu cập nhật trực tiếp từ giao dịch.</p></div>
                    <label>⌕ <input id="memberSearch" type="search" placeholder="Tìm tên, SĐT hoặc mã thành viên"></label>
                </div>
                <c:choose>
                    <c:when test="${empty members}"><div class="loyalty-empty">Chưa có khách hàng đăng ký thành viên.</div></c:when>
                    <c:otherwise>
                        <div class="member-table-wrap"><table class="member-table" id="memberTable">
                            <thead><tr><th>Khách hàng</th><th>Mã thành viên</th><th>Liên hệ</th><th>Điểm</th><th>Tổng chi tiêu</th><th>Ngày tham gia</th></tr></thead>
                            <tbody><c:forEach var="member" items="${members}"><tr>
                                <td><div class="member-identity"><span>${member.fullname.substring(0,1)}</span><div><strong><c:out value="${member.fullname}"/></strong><small>@<c:out value="${member.username}"/></small></div></div></td>
                                <td><span class="member-code"><c:out value="${member.membershipCode}"/></span></td>
                                <td><strong><c:out value="${member.phone}"/></strong><small><c:out value="${member.email}"/></small></td>
                                <td><b class="points-pill"><fmt:formatNumber value="${member.points}" type="number"/> điểm</b></td>
                                <td><strong><fmt:formatNumber value="${member.totalSpent}" type="number"/>${appSettings.currency_symbol}</strong></td>
                                <td><small>${member.joinedAt}</small></td>
                            </tr></c:forEach></tbody>
                        </table></div>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:when>

        <c:otherwise>
            <section class="loyalty-hero">
                <div><span class="section-kicker">CHIDORI REWARDS</span>
                    <h1>Mỗi tách cà phê,<br><em>một niềm vui nhỏ.</em></h1>
                    <p>Cứ <fmt:formatNumber value="${pointValue}" type="number"/>${appSettings.currency_symbol} thanh toán thành công, bạn nhận 1 điểm.</p></div>
                <div class="loyalty-balance-card">
                    <span>Điểm khả dụng</span><strong>${empty points ? 0 : points.points}</strong><small>≈ <fmt:formatNumber value="${voucherValue}" type="number"/>${appSettings.currency_symbol} voucher</small>
                </div>
            </section>

            <c:choose>
                <c:when test="${currentRole == 'customer'}">
                    <section class="member-onboarding">
                        <div class="onboarding-copy">
                            <span class="member-badge">MEMBERS ONLY</span><h2>Mở khóa voucher bằng điểm</h2>
                            <p>Điểm của bạn vẫn được tích sau khi thanh toán. Hoàn tất hồ sơ một lần để trở thành Member và dùng điểm giảm đến <b>80% mỗi đơn</b>.</p>
                            <ul><li>✓ Giữ nguyên toàn bộ chức năng Customer</li><li>✓ Điểm hiện có được bảo toàn</li><li>✓ Voucher áp dụng ngay tại checkout</li></ul>
                        </div>
                        <form method="post" class="member-form">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <div class="form-title"><span>✦</span><div><h3>Đăng ký khách hàng thành viên</h3><small>Thông tin được dùng để xác nhận hội viên.</small></div></div>
                            <label>Họ và tên<input name="fullname" maxlength="120" required value="<c:out value='${sessionScope.user.fullname}'/>"></label>
                            <div class="member-form-grid">
                                <label>Email<input type="email" name="email" maxlength="254" required value="<c:out value='${sessionScope.user.email}'/>"></label>
                                <label>Số điện thoại<input type="tel" name="phone" maxlength="20" required placeholder="0912 345 678"></label>
                            </div>
                            <label>Ngày sinh <small>(không bắt buộc)</small><input type="date" name="birthDate"></label>
                            <label>Địa chỉ<textarea name="address" maxlength="500" required placeholder="Địa chỉ nhận hàng hoặc liên hệ"></textarea></label>
                            <button class="btn-primary" type="submit">Trở thành Chidori Member <span>→</span></button>
                        </form>
                    </section>
                </c:when>
                <c:otherwise>
                    <section class="member-dashboard">
                        <article class="membership-card">
                            <div class="membership-top"><span>CHIDORI COFFEE</span><b>✦ MEMBER</b></div>
                            <div class="membership-chip">◫</div>
                            <h3><c:out value="${sessionScope.user.fullname}"/></h3>
                            <div class="membership-bottom"><span>${memberProfile.membershipCode}</span><small>THÀNH VIÊN TỪ ${memberProfile.joinedAt}</small></div>
                        </article>
                        <div class="member-metrics">
                            <article><span>🏆</span><div><small>Điểm khả dụng</small><strong>${empty points ? 0 : points.points}</strong></div></article>
                            <article><span>☕</span><div><small>Tổng chi tiêu</small><strong><fmt:formatNumber value="${empty points ? 0 : points.totalSpent}" type="number"/>${appSettings.currency_symbol}</strong></div></article>
                            <article><span>🎟</span><div><small>Giá trị voucher</small><strong><fmt:formatNumber value="${voucherValue}" type="number"/>${appSettings.currency_symbol}</strong></div></article>
                            <div class="voucher-rule"><b>Dùng voucher thế nào?</b><p>Chọn số điểm tại trang thanh toán. Hệ thống tự giới hạn mức giảm tối đa ${maxDiscountPercent}% và chỉ trừ điểm khi đơn VNPay được tạo thành công.</p><a href="${pageContext.request.contextPath}/menu">Chọn món ngay →</a></div>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
</main>
<%@ include file="footer.jsp" %>
<c:if test="${currentRole == 'staff'}"><script>
document.getElementById('memberSearch')?.addEventListener('input', event => {
    const needle = event.target.value.toLocaleLowerCase('vi');
    document.querySelectorAll('#memberTable tbody tr').forEach(row => {
        row.hidden = !row.textContent.toLocaleLowerCase('vi').includes(needle);
    });
});
</script></c:if>
</body>
</html>
