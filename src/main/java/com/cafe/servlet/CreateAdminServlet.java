package com.cafe.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Legacy endpoint retained only to avoid an ambiguous 404 on old bookmarks.
 * Administrator bootstrap is performed by the database migration/rebuild.
 */
@WebServlet("/create-admin")
public class CreateAdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendError(HttpServletResponse.SC_GONE,
                "Endpoint tạo admin mặc định đã bị vô hiệu hóa.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendError(HttpServletResponse.SC_GONE,
                "Hãy tạo quản trị viên qua migration bảo mật.");
    }
}
