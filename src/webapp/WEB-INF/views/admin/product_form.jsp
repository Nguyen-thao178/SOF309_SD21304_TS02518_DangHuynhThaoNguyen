<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${product == null ? "Thêm mới" : "Cập nhật"} sản phẩm - Chidori Coffee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=20260803-role-permissions1">
</head>
<body>
<%@ include file="/WEB-INF/views/header.jsp" %>
<div class="form-container" style="max-width: 600px;">
    <h2>${product == null ? "➕ Thêm sản phẩm mới" : "✏️ Cập nhật sản phẩm"}</h2>
    <c:if test="${not empty error}">
        <div class="form-error" role="alert">${error}</div>
    </c:if>
    <form action="${pageContext.request.contextPath}/admin/products" method="post">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="${product == null ? 'create' : 'update'}">
        <c:if test="${product != null}">
            <input type="hidden" name="id" value="${product.id}">
        </c:if>

        <label>Tên sản phẩm:</label>
        <input type="text" name="name" value="${product.name}" required>

        <label>Danh mục:</label>
        <select name="category" required>
            <option value="Coffee" ${product.category == 'Coffee' ? 'selected' : ''}>Coffee</option>
            <option value="Tea" ${product.category == 'Tea' ? 'selected' : ''}>Tea</option>
            <option value="Pastry" ${product.category == 'Pastry' ? 'selected' : ''}>Pastry</option>
        </select>

        <label for="barcode">Mã vạch:</label>
        <input type="text" id="barcode" name="barcode" value="${product.barcode}"
               inputmode="numeric" maxlength="16" autocomplete="off"
               placeholder="Quét hoặc nhập 13 chữ số EAN-13">
        <small class="field-hint">Hỗ trợ EAN-13 có check digit hợp lệ; mã phải là duy nhất.</small>

        <label>Giá (VNĐ):</label>
        <input type="number" name="price" step="1000" value="${product.price}" required>

        <label>Mô tả:</label>
        <textarea name="description" rows="3">${product.description}</textarea>

        <label>Số lượng tồn kho:</label>
        <input type="number" name="stock" value="${product.stock}" required>

        <label>Số lượng đã bán:</label>
        <input type="number" name="soldCount" value="${product.soldCount}" required>

        <label>URL hình ảnh:</label>
        <input type="text" name="imageUrl" value="${product.imageUrl}" placeholder="https://..." required>

        <button type="submit" class="btn-primary">💾 Lưu sản phẩm</button>
        <a href="${pageContext.request.contextPath}/admin/products" class="btn">⬅️ Quay lại</a>
    </form>
</div>
<%@ include file="/WEB-INF/views/footer.jsp" %>
</body>
</html>
