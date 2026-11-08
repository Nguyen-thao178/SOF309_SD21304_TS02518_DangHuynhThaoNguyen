<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch sử Chatbox ${reportDate} - Chidori Coffee</title>
    <style>
        * { box-sizing: border-box; }
        body {
            max-width: 1100px;
            margin: 0 auto;
            padding: 28px;
            color: #171310;
            font: 14px/1.5 Arial, sans-serif;
            background: #f4f0ec;
        }
        .sheet {
            padding: 30px;
            border: 1px solid #ddd3cc;
            border-radius: 16px;
            background: #fff;
            box-shadow: 0 16px 45px rgba(55, 29, 19, .08);
        }
        .print-header {
            display: flex;
            padding-bottom: 20px;
            align-items: flex-end;
            justify-content: space-between;
            border-bottom: 3px solid #f05a2a;
        }
        .brand { color: #e64b23; font-size: 22px; font-weight: 800; }
        h1 { margin: 6px 0 0; font-size: 28px; }
        .meta { color: #756961; text-align: right; }
        .summary {
            display: flex;
            margin: 20px 0;
            padding: 12px 16px;
            justify-content: space-between;
            border-radius: 10px;
            background: #fff1e9;
        }
        table { width: 100%; border-collapse: collapse; }
        th, td {
            padding: 11px 10px;
            vertical-align: top;
            border: 1px solid #ded7d2;
            text-align: left;
        }
        th { color: #8c341d; background: #f8eee8; }
        .conversation { min-width: 360px; }
        .question, .answer {
            margin: 0 0 7px;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }
        .answer { margin-bottom: 0; color: #4f4742; }
        .label { color: #df4d24; font-size: 11px; font-weight: 800; }
        .provider {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 999px;
            color: #8d391e;
            font-size: 11px;
            background: #ffe6d8;
        }
        .empty { padding: 45px; color: #776d67; text-align: center; }
        .toolbar {
            display: flex;
            margin-bottom: 16px;
            justify-content: flex-end;
            gap: 8px;
        }
        .toolbar button, .toolbar a {
            padding: 10px 16px;
            border: 0;
            border-radius: 10px;
            color: white;
            text-decoration: none;
            cursor: pointer;
            background: #f05a2a;
        }
        .toolbar a { color: #3f3530; background: #e6dfda; }
        @page { size: A4 landscape; margin: 12mm; }
        @media print {
            body { max-width: none; padding: 0; background: white; }
            .sheet { padding: 0; border: 0; box-shadow: none; }
            .toolbar { display: none; }
            tr { break-inside: avoid; }
        }
    </style>
</head>
<body>
<div class="toolbar">
    <a href="${pageContext.request.contextPath}/admin/report?date=${reportDate}">← Quay lại báo cáo</a>
    <button type="button" onclick="window.print()">🖨 In lịch sử chat</button>
</div>

<main class="sheet">
    <header class="print-header">
        <div>
            <div class="brand">CHIDORI COFFEE</div>
            <h1>Lịch sử Chatbox</h1>
        </div>
        <div class="meta">
            <div>Ngày báo cáo: <strong>${reportDate}</strong></div>
            <div>Người in: <c:out value="${printedBy}"/></div>
            <div>In lúc: <span id="printedAt"></span></div>
        </div>
    </header>

    <div class="summary">
        <strong>Tổng số cuộc hỏi đáp</strong>
        <strong>${chatHistory.size()}</strong>
    </div>

    <c:choose>
        <c:when test="${empty chatHistory}">
            <div class="empty">Không có lịch sử chatbox trong ngày ${reportDate}.</div>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr>
                    <th style="width: 55px">#</th>
                    <th style="width: 150px">Thời gian</th>
                    <th style="width: 170px">Nhân viên</th>
                    <th>Nội dung hội thoại</th>
                    <th style="width: 95px">Nguồn</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="chat" items="${chatHistory}" varStatus="loop">
                    <tr>
                        <td>${loop.count}</td>
                        <td><fmt:formatDate value="${chat.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                        <td>
                            <strong><c:out value="${chat.fullname}"/></strong><br>
                            <small>@<c:out value="${chat.username}"/></small>
                        </td>
                        <td class="conversation">
                            <p class="question"><span class="label">CÂU HỎI</span><br><c:out value="${chat.question}"/></p>
                            <p class="answer"><span class="label">TRẢ LỜI</span><br><c:out value="${chat.answer}"/></p>
                        </td>
                        <td><span class="provider">${chat.provider == 'gemini' ? 'Gemini' : 'Nội bộ'}</span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
<script>
    document.getElementById('printedAt').textContent =
        new Intl.DateTimeFormat('vi-VN', {dateStyle: 'short', timeStyle: 'medium'}).format(new Date());
</script>
</body>
</html>
