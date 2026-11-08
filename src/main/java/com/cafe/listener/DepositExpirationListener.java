package com.cafe.listener;

import com.cafe.dao.OrderDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class DepositExpirationListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "chidori-deposit-expiration");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                int expired = new OrderDAO().expireOverdueDeposits();
                if (expired > 0) {
                    event.getServletContext().log("Đã chuyển " + expired + " đơn cọc sang Không nhận hàng.");
                }
            } catch (Exception e) {
                event.getServletContext().log("Lỗi kiểm tra đơn cọc quá hạn", e);
            }
        }, 1, 60, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
