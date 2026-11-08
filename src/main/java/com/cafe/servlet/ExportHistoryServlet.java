package com.cafe.servlet;

import com.cafe.dao.OrderDAO;
import com.cafe.model.Order;
import com.cafe.model.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RGBColor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/export-history")
public class ExportHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        boolean viewingAllOrders = "manager".equalsIgnoreCase(user.getRole());
        List<Order> orders = viewingAllOrders
                ? orderDAO.getAllOrders()
                : orderDAO.getOrdersByUserId(user.getId());

        resp.setContentType("application/pdf");
        String fileScope = viewingAllOrders ? "tat_ca" : user.getUsername();
        resp.setHeader("Content-Disposition", "attachment; filename=lich_su_thanh_toan_" + fileScope + ".pdf");

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, resp.getOutputStream());
            document.open();

            // ---- FONT hỗ trợ tiếng Việt (dùng Times New Roman, nhúng vào PDF) ----
            // Đường dẫn font Times New Roman trên Windows, Linux, macOS thường khác nhau.
            // Ta thử nhiều đường dẫn, nếu không có thì dùng font mặc định (có thể lỗi tiếng Việt nhưng không crash)
            String fontPath = null;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                fontPath = "C:/Windows/Fonts/times.ttf";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                fontPath = "/Library/Fonts/Times New Roman.ttf";
            } else { // Linux
                fontPath = "/usr/share/fonts/truetype/msttcorefonts/Times_New_Roman.ttf";
            }
            BaseFont baseFont;
            try {
                baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception e) {
                // Nếu không tìm thấy font, dùng font chuẩn (có thể lỗi tiếng Việt nhưng chương trình vẫn chạy)
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                System.err.println("Không tìm thấy font Times New Roman, dùng Helvetica. Tiếng Việt có thể bị lỗi.");
            }

            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font headerFont = new Font(baseFont, 12, Font.BOLD);
            Font normalFont = new Font(baseFont, 10, Font.NORMAL);
            Font smallFont = new Font(baseFont, 9, Font.NORMAL);

            // Tiêu đề
            Paragraph title = new Paragraph("LỊCH SỬ THANH TOÁN - CHIDORI COFFEE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Thông tin nhân viên
            document.add(new Paragraph("Nhân viên: " + user.getFullname() + " (" + user.getUsername() + ")", normalFont));
            document.add(new Paragraph("Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()), normalFont));
            document.add(Chunk.NEWLINE);

            // Bảng
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            float[] columnWidths = {0.8f, 1.2f, 2f, 1.5f, 1.5f};
            table.setWidths(columnWidths);

            // Header
            addCellHeader(table, "STT", headerFont);
            addCellHeader(table, "Mã đơn", headerFont);
            addCellHeader(table, "Ngày đặt", headerFont);
            addCellHeader(table, "Tổng tiền (VNĐ)", headerFont);
            addCellHeader(table, "Trạng thái", headerFont);

            // Dữ liệu
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            int stt = 1;
            for (Order o : orders) {
                addCell(table, String.valueOf(stt++), normalFont);
                addCell(table, String.valueOf(o.getId()), normalFont);
                addCell(table, sdf.format(o.getOrderDate()), normalFont);
                addCell(table, String.format("%,.0f", o.getTotalAmount()), normalFont);
                addCell(table, o.getTransactionTag(), normalFont);
            }
            document.add(table);

            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Tổng số đơn hàng: " + orders.size(), smallFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tạo PDF: " + e.getMessage());
        }
    }

    private void addCellHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(RGBColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }
}
