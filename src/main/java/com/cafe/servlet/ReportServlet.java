package com.cafe.servlet;

import com.cafe.dao.ChatHistoryDAO;
import com.cafe.dao.ReportDAO;
import com.cafe.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@WebServlet("/admin/report")
public class ReportServlet extends HttpServlet {
    private ReportDAO reportDAO = new ReportDAO();
    private ChatHistoryDAO chatHistoryDAO = new ChatHistoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || !("admin".equalsIgnoreCase(user.getRole())
                || "manager".equalsIgnoreCase(user.getRole()))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chỉ Admin hoặc Quản lý được phép xem báo cáo.");
            return;
        }

        LocalDate reportDate;
        try {
            reportDate = LocalDate.parse(req.getParameter("date"));
        } catch (DateTimeParseException | NullPointerException exception) {
            reportDate = LocalDate.now();
        }
        String dateParam = reportDate.toString();
        double revenue = reportDAO.getTotalRevenueByDate(dateParam);
        Map<String, Integer> topProducts = reportDAO.getTopProducts(5);
        // Lấy doanh thu tháng hiện tại
        LocalDate now = LocalDate.now();
        double monthlyRevenue = reportDAO.getTotalRevenueByMonth(now.getYear(), now.getMonthValue());
        Map<String, Double> dailyRevenue = reportDAO.getDailyRevenue(now.getYear(), now.getMonthValue());

        req.setAttribute("revenue", revenue);
        req.setAttribute("topProducts", topProducts);
        req.setAttribute("reportDate", dateParam);
        req.setAttribute("monthlyRevenue", monthlyRevenue);
        req.setAttribute("dailyRevenue", dailyRevenue);
        req.setAttribute("currentYear", now.getYear());
        req.setAttribute("currentMonth", now.getMonthValue());
        req.setAttribute("chatHistory", chatHistoryDAO.findByDate(reportDate));

        req.getRequestDispatcher("/WEB-INF/views/admin/report.jsp").forward(req, resp);
    }
}
