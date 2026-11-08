document.addEventListener('DOMContentLoaded', function () {
    var page = document.querySelector('.login-page');
    var password = document.getElementById('password');
    var toggle = document.querySelector('.password-toggle');
    var targetX = window.innerWidth / 2;
    var targetY = window.innerHeight / 2;
    var currentX = targetX;
    var currentY = targetY;

    function renderSpotlight() {
        currentX += (targetX - currentX) * 0.14;
        currentY += (targetY - currentY) * 0.14;
        page.style.setProperty('--pointer-x', currentX + 'px');
        page.style.setProperty('--pointer-y', currentY + 'px');
        window.requestAnimationFrame(renderSpotlight);
    }

    if (page && window.matchMedia('(pointer: fine)').matches) {
        window.addEventListener('pointermove', function (event) {
            targetX = event.clientX;
            targetY = event.clientY;
        }, {passive: true});
        window.requestAnimationFrame(renderSpotlight);
    }

    if (toggle && password) {
        toggle.addEventListener('click', function () {
            var showPassword = password.type === 'password';
            password.type = showPassword ? 'text' : 'password';
            toggle.setAttribute('aria-pressed', String(showPassword));
            toggle.setAttribute('aria-label', showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
        });
    }
});
