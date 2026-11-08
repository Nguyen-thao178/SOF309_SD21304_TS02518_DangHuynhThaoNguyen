<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cài đặt hệ thống - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
    <style>
        .settings-page { max-width: 1080px; margin: 2rem auto; padding: 0 1rem 3rem; }
        .settings-hero { display:flex; justify-content:space-between; gap:1.5rem; align-items:end;
            padding:1.8rem; border:1px solid var(--border); border-radius:24px;
            background:linear-gradient(135deg, rgba(255,87,34,.16), var(--card-bg)); }
        .settings-hero h1 { margin:.3rem 0; font-size:clamp(1.8rem,4vw,3rem); }
        .settings-hero p { margin:0; color:var(--text-secondary); }
        .settings-badge { padding:.55rem .9rem; border-radius:999px; color:#ff8a65;
            background:rgba(255,87,34,.12); border:1px solid rgba(255,87,34,.35); white-space:nowrap; }
        .settings-message { margin:1rem 0; padding:1rem 1.2rem; border-radius:14px; }
        .settings-message.success { color:#8ee3a1; background:rgba(46,125,50,.18); border:1px solid rgba(76,175,80,.4); }
        .settings-message.error { color:#ff9a9a; background:rgba(198,40,40,.18); border:1px solid rgba(244,67,54,.4); }
        .settings-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1rem; margin-top:1rem; }
        .settings-card { padding:1.35rem; border:1px solid var(--border); border-radius:20px; background:var(--card-bg); }
        .settings-card.full { grid-column:1/-1; }
        .settings-card h2 { margin:0 0 .3rem; font-size:1.1rem; border:0; }
        .settings-card > p { margin:0 0 1rem; color:var(--text-secondary); font-size:.9rem; }
        .settings-fields { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:.9rem; }
        .field { display:flex; flex-direction:column; gap:.4rem; }
        .field.full { grid-column:1/-1; }
        .field label { font-weight:700; }
        .field small { color:var(--text-secondary); }
        .field input, .field select, .field textarea { width:100%; padding:.82rem .9rem; color:var(--text-primary);
            background:var(--input-bg, #222); border:1px solid var(--border); border-radius:12px; font:inherit; }
        .field textarea { min-height:78px; resize:vertical; }
        .field input:focus, .field select:focus, .field textarea:focus { outline:0; border-color:var(--orange); box-shadow:0 0 0 3px rgba(255,87,34,.13); }
        .settings-actions { position:sticky; bottom:1rem; display:flex; justify-content:flex-end; gap:.8rem;
            margin-top:1rem; padding:1rem; border:1px solid var(--border); border-radius:18px;
            background:color-mix(in srgb, var(--card-bg) 92%, transparent); backdrop-filter:blur(14px); }
        .settings-actions button { min-width:220px; }
        @media(max-width:760px) { .settings-grid,.settings-fields{grid-template-columns:1fr}.settings-card.full,.field.full{grid-column:auto}.settings-hero{align-items:start;flex-direction:column}.settings-actions button{width:100%} }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/header.jsp" %>

<main class="settings-page">
    <section class="settings-hero">
        <div>
            <span class="section-kicker">SYSTEM CONTROL</span>
            <h1>Cài đặt hệ thống</h1>
            <p>Các thay đổi được lưu đồng thời và áp dụng ngay trên website.</p>
        </div>
        <span class="settings-badge">🔒 Chỉ Admin</span>
    </section>

    <c:if test="${not empty param.success}">
        <div class="settings-message success" role="status">✓ <c:out value="${param.success}"/></div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="settings-message error" role="alert">⚠ <c:out value="${param.error}"/></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/admin/settings" method="post">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <div class="settings-grid">
            <section class="settings-card full">
                <h2>☕ Thông tin thương hiệu</h2>
                <p>Hiển thị ở header, footer, hóa đơn và chatbot.</p>
                <div class="settings-fields">
                    <div class="field"><label for="store_name">Tên cửa hàng</label><input id="store_name" name="store_name" maxlength="100" value="${fn:escapeXml(settings.store_name)}" required></div>
                    <div class="field"><label for="hotline">Hotline</label><input id="hotline" name="hotline" maxlength="30" value="${fn:escapeXml(settings.hotline)}" required></div>
                    <div class="field full"><label for="store_tagline">Mô tả ngắn</label><input id="store_tagline" name="store_tagline" maxlength="200" value="${fn:escapeXml(settings.store_tagline)}" required></div>
                    <div class="field full"><label for="address">Địa chỉ</label><textarea id="address" name="address" maxlength="300" required>${fn:escapeXml(settings.address)}</textarea></div>
                </div>
            </section>

            <section class="settings-card">
                <h2>🕒 Hoạt động cửa hàng</h2>
                <p>Giờ mở cửa và kênh mạng xã hội.</p>
                <div class="settings-fields">
                    <div class="field full"><label for="weekday_hours">Ngày trong tuần</label><input id="weekday_hours" name="weekday_hours" maxlength="80" value="${fn:escapeXml(settings.weekday_hours)}" required></div>
                    <div class="field full"><label for="weekend_hours">Cuối tuần</label><input id="weekend_hours" name="weekend_hours" maxlength="80" value="${fn:escapeXml(settings.weekend_hours)}" required></div>
                    <div class="field full"><label for="social_links">Mạng xã hội</label><input id="social_links" name="social_links" maxlength="200" value="${fn:escapeXml(settings.social_links)}"></div>
                </div>
            </section>

            <section class="settings-card">
                <h2>💳 Giao dịch &amp; điểm thưởng</h2>
                <p>Áp dụng cho đơn mới sau khi lưu.</p>
                <div class="settings-fields">
                    <div class="field"><label for="deposit_percent">Tiền cọc (%)</label><input type="number" id="deposit_percent" name="deposit_percent" min="1" max="90" value="${settings.deposit_percent}" required></div>
                    <div class="field"><label for="loyalty_vnd_per_point">Số tiền / 1 điểm</label><input type="number" id="loyalty_vnd_per_point" name="loyalty_vnd_per_point" min="100" max="1000000" step="100" value="${settings.loyalty_vnd_per_point}" required></div>
                    <div class="field"><label for="currency">Đơn vị hiển thị</label><select id="currency" name="currency"><option value="VND" ${settings.currency == 'VND' ? 'selected' : ''}>VND (₫)</option><option value="USD" ${settings.currency == 'USD' ? 'selected' : ''}>USD ($)</option><option value="EUR" ${settings.currency == 'EUR' ? 'selected' : ''}>EUR (€)</option></select><small>Chỉ đổi ký hiệu, không quy đổi giá.</small></div>
                    <div class="field"><label for="barcode_scanner_enabled">Máy quét barcode</label><select id="barcode_scanner_enabled" name="barcode_scanner_enabled"><option value="true" ${settings.barcode_scanner_enabled == 'true' ? 'selected' : ''}>Đang bật</option><option value="false" ${settings.barcode_scanner_enabled == 'false' ? 'selected' : ''}>Tạm tắt</option></select></div>
                </div>
            </section>
        </div>

        <div class="settings-actions">
            <button type="submit" class="btn-primary">💾 Lưu và áp dụng cài đặt</button>
        </div>
    </form>
</main>

<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>
