<%@ page pageEncoding="UTF-8" %>
<footer>
    <div class="footer-content">
        <div class="footer-section">
            <h4><c:out value="${appSettings.store_name}"/></h4>
            <p><c:out value="${appSettings.store_tagline}"/></p>
            <p>📞 Hotline: <c:out value="${appSettings.hotline}"/></p>
            <p>📍 <c:out value="${appSettings.address}"/></p>
        </div>
        <div class="footer-section">
            <h4>Giờ mở cửa</h4>
            <p><c:out value="${appSettings.weekday_hours}"/></p>
            <p><c:out value="${appSettings.weekend_hours}"/></p>
        </div>
        <div class="footer-section">
            <h4>Theo dõi</h4>
            <p><c:out value="${appSettings.social_links}"/></p>
        </div>
    </div>
    <div class="copyright">
        &copy; 2026 <c:out value="${appSettings.store_name}"/> System. All rights reserved.
    </div>
</footer>
<!-- Chidori Assistant -->
<button class="chatbot-icon" id="chatIcon" type="button"
        aria-label="Mở trợ lý Chidori" aria-controls="chatWindow" aria-expanded="false">
    <span class="chatbot-icon-mark">✦</span>
    <span class="chatbot-icon-label">Hỏi Chidori</span>
    <i aria-hidden="true"></i>
</button>

<aside class="chat-window" id="chatWindow" aria-label="Trợ lý Chidori" aria-hidden="true">
    <div class="chat-glow" aria-hidden="true"></div>
    <header class="chat-header">
        <div class="chat-assistant-avatar" aria-hidden="true">
            <span>C</span><i></i>
        </div>
        <div class="chat-assistant-info">
            <span class="chat-eyebrow">CHIDORI CONCIERGE</span>
            <strong>Chidori Assistant</strong>
            <small id="chatProviderStatus"><i></i> Sẵn sàng hỗ trợ</small>
        </div>
        <button class="chat-header-action" id="clearChat" type="button"
                aria-label="Xóa cuộc trò chuyện" title="Xóa cuộc trò chuyện">↻</button>
        <button class="chat-header-action" id="closeChat" type="button"
                aria-label="Đóng chat" title="Đóng">×</button>
    </header>

    <div class="chat-body" id="chatBody" role="log" aria-live="polite">
        <section class="chat-welcome" id="chatWelcome">
            <span class="chat-welcome-icon">☕</span>
            <span class="chat-welcome-kicker">Xin chào từ Chidori</span>
            <h3>Mình có thể giúp gì cho bạn?</h3>
            <p>Hỏi nhanh về menu, giá món, thêm món vào giỏ và đơn hàng đã cọc.</p>
        </section>
        <div class="chat-message bot-message">
            <span class="message-avatar">C</span>
            <div>
                <div class="message-bubble">Chào bạn! Mình là trợ lý riêng của Chidori Coffee. Chọn một chủ đề bên dưới hoặc gửi câu hỏi nhé.</div>
                <span class="message-meta">Chidori Assistant · vừa xong</span>
            </div>
        </div>
        <div class="chat-suggestions" id="chatSuggestions" aria-label="Câu hỏi gợi ý">
            <c:if test="${currentRole == 'staff' || currentRole == 'customer' || currentRole == 'member'}">
                <button type="button" data-chat-suggestion="Thêm cà phê sữa vào giỏ">
                    <span>🛒</span><strong>Thêm vào giỏ</strong><small>Đặt món bằng chat</small>
                </button>
            </c:if>
            <button type="button" data-chat-suggestion="Cho mình xem menu và giá món">
                <span>☕</span><strong>Menu &amp; giá</strong><small>Xem món đang bán</small>
            </button>
            <button type="button" data-chat-suggestion="Chính sách đặt cọc và nhận hàng thế nào?">
                <span>◷</span><strong>Đơn đặt cọc</strong><small>Lịch nhận và thanh toán</small>
            </button>
            <button type="button" data-chat-suggestion="Quán mở cửa lúc mấy giờ và ở đâu?">
                <span>⌖</span><strong>Thông tin quán</strong><small>Địa chỉ và giờ mở cửa</small>
            </button>
        </div>
    </div>

    <div class="chat-typing" id="chatTyping" hidden>
        <span class="message-avatar">C</span>
        <div><i></i><i></i><i></i></div>
        <small>Chidori đang soạn câu trả lời…</small>
    </div>

    <div class="chat-composer">
        <div class="chat-footer">
            <textarea id="chatInput" maxlength="500" rows="1"
                      aria-label="Tin nhắn cho Chidori Assistant"
                      placeholder="Nhập câu hỏi của bạn..." autocomplete="off"></textarea>
            <button id="sendChat" type="button" aria-label="Gửi tin nhắn">
                <span>➤</span>
            </button>
        </div>
        <div class="chat-composer-meta">
            <span><i></i> Chỉ tư vấn dịch vụ Chidori</span>
            <span id="chatCharacterCount">0/500</span>
        </div>
    </div>
</aside>
<script src="${pageContext.request.contextPath}/assets/js/main.js?v=20260809-chat-cart2"></script>
