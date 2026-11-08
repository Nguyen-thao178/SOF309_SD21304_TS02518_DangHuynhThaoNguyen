# 01 – NHẬT KÝ AI THIẾT KẾ KIỂM THỬ

## 1. Thông tin phiên làm việc

- **Ngày:** 08/11/2026.
- **Dự án:** Chidori Coffee Management System.
- **Sinh viên:** Đặng Huỳnh Thảo Nguyên – TS02518 – SD21304.
- **Vai trò yêu cầu AI:** QA Lead.
- **Đầu vào:** [SPECIFICATIONS.md](../SPECIFICATIONS.md) hiện tại trong repository; đối chiếu mã nguồn liên quan khi cần xác định phản hồi.
- **Đầu ra:** [TEST_PLAN.md](../TEST_PLAN.md) và [TEST_CASES.md](../TEST_CASES.md).
- **Phạm vi phiên này:** Thiết kế tài liệu; chưa viết/chạy bộ test tự động mới và chưa có kết quả Pass/Fail thực nghiệm.

## 2. Câu lệnh prompt của người dùng

Nội dung hiển thị của prompt (chuẩn hóa khoảng trắng từ định dạng tin nhắn):

> Bạn là QA Lead. Dưới đây là đặc tả nghiệp vụ dự án Web của tôi: SPECIFICATIONS.md. Hãy áp dụng kỹ thuật Phân vùng tương đương và Phân tích giá trị biên để thiết kế Test Plan và Bộ Test Case chi tiết cho Controller và UI lần lượt vào TEST_PLAN.md và TEST_CASES.md sau đó lưu câu lệnh prompt vào `docs/ai-logs/01_test_design_ai.md`.

## 3. Cách xử lý và kết quả AI tạo

1. Đọc đặc tả, tài liệu kiểm thử mẫu và kiểm tra một số xử lý Controller để xác định HTTP/forward/redirect, validation và CSRF.
2. Chia tầng Controller/Service/Filter, tích hợp SQL/gateway và UI; không lấy validation HTML thay thế kiểm tra máy chủ.
3. Áp dụng EP cho rỗng/blank/thiếu, đúng/sai định dạng, trùng dữ liệu, role/quyền sở hữu, enum và trạng thái giao dịch.
4. Áp dụng BVA cho min/max độ dài, password7/8/9, số âm/0/1, stock/quantity, ngày sinh/ngày nhận, giới hạn điểm80%, cọc1–90 và giá trị điểm100–1000000.
5. Tạo kế hoạch với phạm vi, môi trường, fixture, ma trận role, tiêu chí vào/ra, ưu tiên, quản lý lỗi và truy vết.
6. Tạo **201 ca/họ ca có mã duy nhất**, gồm **163 Controller/Service/Filter/tích hợp** và **38 UI**. Mỗi bộ giá trị tham số phải chạy độc lập và có mã lượt D01, D02…; 201 không phải số lượt chạy sau khi bung tham số.
7. Mỗi dòng có ref đặc tả, mức ưu tiên, kỹ thuật/loại, tiền điều kiện, bước, dữ liệu, expected và trạng thái Chưa chạy; có mẫu ghi actual/evidence/defect cho từng dataset.

## 4. Các quyết định cần người thực hiện kiểm thử lưu ý

- Dùng URL đúng context /chidori; đăng ký là /register-member và CRUD sản phẩm là /admin/products.
- Không biến GAP thành yêu cầu đã có: giá0 hiện qua validation; mật khẩu đăng ký tối thiểu8 nhưng chưa bắt buộc ký tự đặc biệt; chưa có confirmPassword trong đăng ký.
- Tách ca Characterization khỏi tiêu chí mới chưa phê duyệt; NaN/Infinity chỉ kiểm tra bằng mock.
- Các ca tích hợp phụ thuộc schema/procedure SQL, dữ liệu từng role và gateway test; thiếu điều kiện phải ghi Blocked, không ghi Pass.
- Fixture là dữ liệu tổng hợp cần seed, không phải cam kết tài khoản tồn tại. Không ghi mật khẩu, CSRF hoặc secret thật vào bằng chứng.

## 5. Kiểm tra tài liệu và phần sinh viên bổ sung

- AI kiểm tra số lượng/ID ca, đủ cột và trạng thái ban đầu, liên kết tài liệu và lỗi khoảng trắng.
- **Sinh viên tự chỉnh sửa:** Chưa có thông tin trong phiên này; không ghi nhận thay cho sinh viên.
- **Cần thực hiện tiếp:** Duyệt kỳ vọng, xác nhận DB, chuẩn bị fixture, triển khai script, chạy ca và điền actual/status/evidence/defect theo kết quả thật.
