package com.cafe.servlet;

import com.cafe.dao.ChatHistoryDAO;
import com.cafe.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/admin/report/chat-history/print")
public class ChatHistoryPrintServlet extends HttpServlet {
    private final ChatHistoryDAO chatHistoryDAO = new ChatHistoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !("admin".equalsIgnoreCase(user.getRole())
                || "manager".equalsIgnoreCase(user.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        LocalDate reportDate;
        try {
            reportDate = LocalDate.parse(request.getParameter("date"));
        } catch (DateTimeParseException | NullPointerException exception) {
            reportDate = LocalDate.now();
        }

        request.setAttribute("reportDate", reportDate.toString());
        request.setAttribute("chatHistory", chatHistoryDAO.findByDate(reportDate));
        request.setAttribute("printedBy", user.getFullname());
        request.getRequestDispatcher("/WEB-INF/views/admin/chat_history_print.jsp")
                .forward(request, response);
    }
}
