document.addEventListener('DOMContentLoaded', function() {
    var contextPath = window.contextPath || '';

    // ==================== AMBIENT COFFEE BACKGROUND ====================
    function initCoffeeAmbient() {
        if (document.querySelector('.chidori-ambient')) return;

        var ambient = document.createElement('div');
        ambient.className = 'chidori-ambient';
        ambient.setAttribute('aria-hidden', 'true');

        var particles = document.createElement('div');
        particles.className = 'coffee-particles';
        ambient.appendChild(particles);
        document.body.prepend(ambient);

        var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        var compactScreen = window.matchMedia('(max-width: 700px)').matches;
        var particleCount = reduceMotion ? 0 : (compactScreen ? 12 : 24);

        for (var index = 0; index < particleCount; index++) {
            var bean = document.createElement('span');
            bean.className = 'coffee-particle';
            bean.style.setProperty('--bean-x', (Math.random() * 100).toFixed(2) + 'vw');
            bean.style.setProperty('--bean-size', (10 + Math.random() * 13).toFixed(1) + 'px');
            bean.style.setProperty('--bean-duration', (12 + Math.random() * 16).toFixed(2) + 's');
            bean.style.setProperty('--bean-delay', (-Math.random() * 28).toFixed(2) + 's');
            bean.style.setProperty('--bean-drift', (-70 + Math.random() * 140).toFixed(1) + 'px');
            bean.style.setProperty('--bean-spin', (240 + Math.random() * 620).toFixed(0) + 'deg');
            bean.style.setProperty('--bean-opacity', (0.16 + Math.random() * 0.34).toFixed(2));
            particles.appendChild(bean);
        }

        if (window.matchMedia('(pointer: fine)').matches) {
            var targetX = window.innerWidth / 2;
            var targetY = window.innerHeight * 0.28;
            var currentX = targetX;
            var currentY = targetY;
            var framePending = false;

            function renderSpotlight() {
                currentX += (targetX - currentX) * 0.18;
                currentY += (targetY - currentY) * 0.18;
                ambient.style.setProperty('--spot-x', currentX.toFixed(1) + 'px');
                ambient.style.setProperty('--spot-y', currentY.toFixed(1) + 'px');
                framePending = Math.abs(targetX - currentX) > 0.2 || Math.abs(targetY - currentY) > 0.2;
                if (framePending) window.requestAnimationFrame(renderSpotlight);
            }

            window.addEventListener('pointermove', function(event) {
                targetX = event.clientX;
                targetY = event.clientY;
                if (!framePending) {
                    framePending = true;
                    window.requestAnimationFrame(renderSpotlight);
                }
            }, { passive: true });
        }
    }

    initCoffeeAmbient();

    // ==================== SLIDER ====================
    var slideIndex = 0;
    var slides = document.querySelectorAll('.slide');
    if (slides.length) {
        slides[0].classList.add('active');
        setInterval(function() {
            slides.forEach(function(s) { s.classList.remove('active'); });
            slideIndex = (slideIndex + 1) % slides.length;
            slides[slideIndex].classList.add('active');
        }, 4000);
    }

    var prevBtn = document.querySelector('.prev');
    var nextBtn = document.querySelector('.next');
    if (prevBtn) {
        prevBtn.addEventListener('click', function() {
            slides.forEach(function(s) { s.classList.remove('active'); });
            slideIndex = (slideIndex - 1 + slides.length) % slides.length;
            slides[slideIndex].classList.add('active');
        });
    }
    if (nextBtn) {
        nextBtn.addEventListener('click', function() {
            slides.forEach(function(s) { s.classList.remove('active'); });
            slideIndex = (slideIndex + 1) % slides.length;
            slides[slideIndex].classList.add('active');
        });
    }

    // ==================== CHATBOT (AI) ====================
    var chatIcon = document.getElementById('chatIcon');
    var chatWindow = document.getElementById('chatWindow');
    var closeChat = document.getElementById('closeChat');
    var sendBtn = document.getElementById('sendChat');
    var chatInput = document.getElementById('chatInput');
    var chatBody = document.getElementById('chatBody');
    var chatTyping = document.getElementById('chatTyping');
    var chatProviderStatus = document.getElementById('chatProviderStatus');
    var chatCharacterCount = document.getElementById('chatCharacterCount');
    var clearChat = document.getElementById('clearChat');
    var initialChatMarkup = chatBody ? chatBody.innerHTML : '';
    var sendingMessage = false;

    function setChatOpen(open) {
        if (!chatWindow || !chatIcon) return;
        chatWindow.classList.toggle('is-open', open);
        chatWindow.setAttribute('aria-hidden', String(!open));
        chatIcon.setAttribute('aria-expanded', String(open));
        chatIcon.setAttribute('aria-label', open ? 'Đóng trợ lý Chidori' : 'Mở trợ lý Chidori');
        if (open && chatInput) {
            window.setTimeout(function() { chatInput.focus(); }, 160);
        }
    }

    if (chatIcon) {
        chatIcon.addEventListener('click', function() {
            setChatOpen(!chatWindow.classList.contains('is-open'));
        });
    }
    if (closeChat) {
        closeChat.addEventListener('click', function() { setChatOpen(false); });
    }
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && chatWindow && chatWindow.classList.contains('is-open')) {
            setChatOpen(false);
        }
    });

    function currentTimeLabel() {
        return new Intl.DateTimeFormat('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        }).format(new Date());
    }

    function addMessage(text, isUser, provider) {
        var msgDiv = document.createElement('div');
        msgDiv.className = 'chat-message ' + (isUser ? 'user-message' : 'bot-message');

        if (!isUser) {
            var avatar = document.createElement('span');
            avatar.className = 'message-avatar';
            avatar.textContent = 'C';
            msgDiv.appendChild(avatar);
        }

        var messageContent = document.createElement('div');
        var bubble = document.createElement('div');
        bubble.className = 'message-bubble';
        bubble.textContent = text;
        messageContent.appendChild(bubble);

        var meta = document.createElement('span');
        meta.className = 'message-meta';
        meta.textContent = isUser
            ? 'Bạn · ' + currentTimeLabel()
            : (provider === 'gemini' ? 'Gemini · '
                : (provider === 'cart' ? 'Giỏ hàng · ' : 'Chidori · ')) + currentTimeLabel();
        messageContent.appendChild(meta);
        msgDiv.appendChild(messageContent);
        chatBody.appendChild(msgDiv);
        chatBody.scrollTop = chatBody.scrollHeight;
    }

    function setSending(sending) {
        sendingMessage = sending;
        if (sendBtn) sendBtn.disabled = sending;
        if (chatInput) chatInput.disabled = sending;
        if (chatTyping) chatTyping.hidden = !sending;
        if (chatProviderStatus) {
            chatProviderStatus.innerHTML = sending
                ? '<i></i> Đang tìm câu trả lời…'
                : '<i></i> Sẵn sàng hỗ trợ';
        }
        if (sending && chatBody) chatBody.scrollTop = chatBody.scrollHeight;
    }

    function animateChatCartAddition(cartAction) {
        if (!cartAction || !cartAction.added) return;

        var cartLink = document.getElementById('headerCartLink');
        var cartCount = document.getElementById('headerCartCount');
        if (cartCount) {
            cartCount.textContent = String(cartAction.cartQuantity || 0);
            cartCount.classList.toggle('is-empty', !cartAction.cartQuantity);
        }

        var source = document.getElementById('sendChat') || chatWindow;
        var target = cartLink || chatIcon;
        if (!source || !target || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            if (cartLink) {
                cartLink.classList.add('cart-received');
                window.setTimeout(function() { cartLink.classList.remove('cart-received'); }, 650);
            }
            return;
        }

        var sourceRect = source.getBoundingClientRect();
        var targetRect = target.getBoundingClientRect();
        var flyer = document.createElement('div');
        flyer.className = 'chat-cart-flyer';
        flyer.setAttribute('aria-hidden', 'true');

        if (cartAction.productImage) {
            var image = document.createElement('img');
            image.alt = '';
            image.src = cartAction.productImage.startsWith(contextPath + '/')
                ? cartAction.productImage
                : (cartAction.productImage.startsWith('/')
                    ? contextPath + cartAction.productImage
                    : cartAction.productImage);
            image.addEventListener('error', function() {
                image.remove();
                flyer.textContent = '☕';
            }, {once: true});
            flyer.appendChild(image);
        } else {
            flyer.textContent = '☕';
        }
        document.body.appendChild(flyer);

        var startX = sourceRect.left + sourceRect.width / 2 - 24;
        var startY = sourceRect.top + sourceRect.height / 2 - 24;
        var endX = targetRect.left + targetRect.width / 2 - 24;
        var endY = targetRect.top + targetRect.height / 2 - 24;
        flyer.style.left = startX + 'px';
        flyer.style.top = startY + 'px';

        var animation = flyer.animate([
            {transform: 'translate3d(0,0,0) scale(.72)', opacity: 0},
            {transform: 'translate3d(0,-30px,0) scale(1.08)', opacity: 1, offset: .2},
            {transform: 'translate3d(' + ((endX - startX) * .58) + 'px,'
                    + ((endY - startY) * .45 - 55) + 'px,0) scale(.9)', opacity: 1, offset: .62},
            {transform: 'translate3d(' + (endX - startX) + 'px,'
                    + (endY - startY) + 'px,0) scale(.28)', opacity: .15}
        ], {duration: 920, easing: 'cubic-bezier(.2,.72,.24,1)', fill: 'forwards'});

        animation.finished.finally(function() {
            flyer.remove();
            if (cartLink) {
                cartLink.classList.add('cart-received');
                window.setTimeout(function() { cartLink.classList.remove('cart-received'); }, 650);
            }
        });
    }

    function sendToAI(message) {
        setSending(true);
        fetch(contextPath + '/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                'Accept': 'application/json',
                'X-CSRF-Token': window.CHIDORI_CSRF || ''
            },
            body: new URLSearchParams({message: message})
        })
        .then(function(response) {
            if (!response.ok) throw new Error('HTTP ' + response.status);
            return response.json();
        })
        .then(function(data) {
            addMessage(data.message || 'Mình chưa thể trả lời lúc này.', false, data.provider);
            animateChatCartAddition(data.cartAction);
            if (chatProviderStatus) {
                chatProviderStatus.innerHTML = data.provider === 'gemini'
                    ? '<i></i> Gemini đang hoạt động'
                    : (data.provider === 'cart'
                        ? '<i></i> Đã cập nhật giỏ hàng'
                        : '<i></i> Chidori đang hoạt động');
            }
        })
        .catch(function() {
            addMessage('Mình chưa kết nối được máy chủ. Bạn vui lòng thử lại sau nhé.', false, 'local');
            if (chatProviderStatus) {
                chatProviderStatus.innerHTML = '<i class="is-error"></i> Mất kết nối';
            }
        })
        .finally(function() {
            sendingMessage = false;
            if (sendBtn) sendBtn.disabled = false;
            if (chatInput) chatInput.disabled = false;
            if (chatTyping) chatTyping.hidden = true;
            if (chatInput) chatInput.focus();
        });
    }

    function submitChat(message) {
        var msg = (message || (chatInput ? chatInput.value : '')).trim();
        if (!msg || sendingMessage) return;
        addMessage(msg, true);
        chatInput.value = '';
        chatInput.style.height = '';
        if (chatCharacterCount) chatCharacterCount.textContent = '0/500';
        document.getElementById('chatWelcome')?.remove();
        document.getElementById('chatSuggestions')?.remove();
        sendToAI(msg);
    }

    if (sendBtn && chatInput && chatBody) {
        sendBtn.addEventListener('click', function() { submitChat(); });
        chatInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                submitChat();
            }
        });
        chatInput.addEventListener('input', function() {
            chatInput.style.height = 'auto';
            chatInput.style.height = Math.min(chatInput.scrollHeight, 96) + 'px';
            if (chatCharacterCount) {
                chatCharacterCount.textContent = chatInput.value.length + '/500';
            }
        });
        document.querySelectorAll('[data-chat-suggestion]').forEach(function(button) {
            button.addEventListener('click', function() {
                setChatOpen(true);
                submitChat(button.dataset.chatSuggestion);
            });
        });
    }
    if (clearChat && chatBody) {
        clearChat.addEventListener('click', function() {
            if (sendingMessage) return;
            chatBody.innerHTML = initialChatMarkup;
            chatBody.scrollTop = 0;
            chatBody.querySelectorAll('[data-chat-suggestion]').forEach(function(button) {
                button.addEventListener('click', function() {
                    submitChat(button.dataset.chatSuggestion);
                });
            });
            if (chatProviderStatus) {
                chatProviderStatus.innerHTML = '<i></i> Sẵn sàng hỗ trợ';
            }
            if (chatInput) chatInput.focus();
        });
    }

    // ==================== ANIMATION THÊM GIỎ ====================
    var addBtns = document.querySelectorAll('.btn-add-cart');
    addBtns.forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            var ripple = document.createElement('div');
            ripple.textContent = '➕';
            ripple.style.position = 'fixed';
            ripple.style.left = e.clientX + 'px';
            ripple.style.top = e.clientY + 'px';
            ripple.style.fontSize = '24px';
            ripple.style.pointerEvents = 'none';
            ripple.style.zIndex = '9999';
            ripple.style.transition = 'all 0.6s ease';
            ripple.style.opacity = '1';
            document.body.appendChild(ripple);
            setTimeout(function() {
                ripple.style.transform = 'translate(30px, -80px)';
                ripple.style.opacity = '0';
            }, 10);
            setTimeout(function() { ripple.remove(); }, 700);
        });
    });

    // ==================== SCROLL REVEAL ====================
    var observer = new IntersectionObserver(function(entries) {
        entries.forEach(function(entry) {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });
    document.querySelectorAll('.product-card, .widget, .cart-container, .product-detail').forEach(function(el) {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        observer.observe(el);
    });

    // ==================== THEME TOGGLE (Light/Dark) ====================
    var themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        var currentTheme = localStorage.getItem('theme') || 'dark';
        if (currentTheme === 'light') document.documentElement.classList.add('light');
        themeToggle.textContent = currentTheme === 'light' ? '🌙' : '☀️';
        themeToggle.setAttribute('aria-label', currentTheme === 'light'
            ? 'Chuyển sang giao diện tối' : 'Chuyển sang giao diện sáng');
        themeToggle.setAttribute('title', themeToggle.getAttribute('aria-label'));
        themeToggle.addEventListener('click', function() {
            var isLight = document.documentElement.classList.toggle('light');
            localStorage.setItem('theme', isLight ? 'light' : 'dark');
            themeToggle.textContent = isLight ? '🌙' : '☀️';
            themeToggle.setAttribute('aria-label', isLight
                ? 'Chuyển sang giao diện tối' : 'Chuyển sang giao diện sáng');
            themeToggle.setAttribute('title', themeToggle.getAttribute('aria-label'));
        });
    }
});
document.addEventListener('error', function (event) {
    const image = event.target;
    if (!(image instanceof HTMLImageElement) || image.dataset.fallbackApplied) return;
    if (!image.closest('.product-card, .product-detail, .cart-table')) return;
    image.dataset.fallbackApplied = 'true';
    image.src = (window.contextPath || '') + '/assets/images/caphesua.jpeg';
}, true);
