# ĐẶC TẢ NGHIỆP VỤ WEB CÁ NHÂN (SPECIFICATIONS)

## 1. Thông tin dự án

- **Tên đề tài:** Chidori Coffee Management System – Hệ thống bán hàng và quản lý quán cà phê.
- **Sinh viên:** Đặng Huỳnh Thảo Nguyên – TS02518 – SD21304.
- **Công nghệ:** Java 17, Jakarta Servlet 6.0, JSP 3.1, JSTL 3, JDBC, Microsoft SQL Server; Maven đóng gói WAR, triển khai trên Tomcat 10.1; HTML/CSS/JavaScript.
- **URL gốc local:** `http://localhost:8080/chidori`. Ghép các đường dẫn bên dưới sau URL gốc; ví dụ `http://localhost:8080/chidori/login`.
- **Mục đích:** Đáp ứng bước 1.4 của assignment SOF309, cung cấp ngữ cảnh để xây dựng Test Plan và Test Case.
- **Ngày rà soát:** 08/11/2026.

**Căn cứ và quy ước:** Đặc tả dựa trên Servlet, Filter, Service, DAO, Model, JSP, JavaScript, cấu hình Maven/web và tài liệu/test trong repository. Các bảng mô tả hành vi hiện có; quy tắc chỉ có ở giao diện được ghi riêng. Những yêu cầu đề xuất ở mục 7 chưa phải chức năng đã triển khai. Đây là kết quả rà soát mã, không phải báo cáo các ca kiểm thử đã chạy. Không suy diễn giới hạn cột hay ràng buộc DB khi chưa có schema thực tế.

## 2. Người dùng và phân quyền

| Đối tượng / role | Phạm vi nghiệp vụ | Trang sau đăng nhập |
| --- | --- | --- |
| Chưa đăng nhập | Đăng nhập, đăng ký tài khoản, khởi tạo OAuth | `/login` |
| `customer` | Xem món, giỏ hàng, đặt cọc qua VNPay; lịch sử/hóa đơn của mình; đăng ký thành viên | `/home` |
| `member` | Quyền customer; hồ sơ, điểm thưởng và đổi điểm thành voucher | `/home` |
| `staff` | Bán hàng, quét barcode; trực tiếp/đặt cọc bằng tiền mặt/VNPay; thu phần còn lại của đơn cọc khách; xem danh sách thành viên | `/home` |
| `manager` | CRUD sản phẩm, quản lý tài khoản staff; toàn bộ lịch sử/hóa đơn; xuất lịch sử; báo cáo | `/history` |
| `admin` | Báo cáo, in lịch sử chatbot, danh bạ liên hệ và cài đặt hệ thống | `/admin/report` |

- Quyền không tự kế thừa: admin hiện không được quản lý sản phẩm/tài khoản staff; manager không được dùng giỏ hàng/thanh toán.
- Tất cả role đã đăng nhập được đổi mật khẩu, dùng chatbot và đăng xuất.
- URL được bảo vệ: chưa đăng nhập chuyển tới `/login`; đã đăng nhập nhưng sai role trả HTTP 403.
- Session hết hạn sau 30 phút không hoạt động; cookie session có `HttpOnly`, theo dõi session bằng cookie.
- POST thông thường yêu cầu `_csrf` hoặc header `X-CSRF-Token` khớp session; thiếu/sai trả 403. Callback OAuth/VNPay được miễn CSRF và có cơ chế xác thực riêng.

Nguồn: `security/RoleAccessPolicy.java`, `filter/AuthFilter.java`, `utils/CsrfUtil.java`, `src/webapp/WEB-INF/web.xml`.

## 3. URL màn hình và điểm xử lý

### 3.1. Tài khoản và bán hàng

| URL tương đối | Phương thức | Màn hình / chức năng | Quyền |
| --- | --- | --- | --- |
| `/`, `/index.jsp` | GET | Chuyển tới đăng nhập | Công khai |
| `/login` | GET, POST | Form và xử lý đăng nhập | Công khai |
| `/register-member` | GET, POST | Đăng ký tài khoản khách hàng | Công khai |
| `/oauth-login?provider=google` hoặc `facebook` | GET | Chuyển tới nhà cung cấp OAuth | Công khai |
| `/oauth-callback` | GET | Nhận provider, code, state | Callback công khai |
| `/logout` | POST | Hủy session, về đăng nhập; GET trả 405 nếu qua được bộ lọc quyền | Đã đăng nhập |
| `/change-password` | GET, POST | Đổi mật khẩu | Đã đăng nhập |
| `/home` | GET | Món nổi bật, khuyến mãi, món bán chạy | customer, member, staff |
| `/menu?category=Coffee` | GET | Danh sách món, lọc danh mục | customer, member, staff |
| `/search?keyword=...` | GET | Tìm món theo tên | customer, member, staff |
| `/product?id=...` | GET | Chi tiết sản phẩm | customer, member, staff |
| `/promotion` | GET | Khuyến mãi đang hiệu lực | customer, member, staff |
| `/cart` | GET | Giỏ hàng trong session | customer, member, staff |
| `/add-to-cart` | POST | Thêm một đơn vị sản phẩm theo id | customer, member, staff |
| `/update-cart` | POST | Cập nhật id, quantity | customer, member, staff |
| `/remove-cart` | POST | Xóa dòng giỏ hàng theo id | customer, member, staff |
| `/cart/scan` | POST | Quét/nhập barcode, trả JSON | staff; máy quét phải được bật |
| `/checkout` | GET, POST | Form thanh toán; tạo đơn, trả JSON | customer, member, staff, theo giới hạn role |
| `/vnpay-pay?paymentId=...` | GET | Chuyển tới VNPay cho giao dịch chờ | Chủ giao dịch; staff có ngoại lệ thu phần còn lại đơn cọc khách |
| `/vnpay-return` | GET | Hóa đơn hoặc màn hình kết quả thanh toán | Callback công khai |
| `/vnpay-ipn` | GET | Xác nhận giao dịch VNPay | Callback công khai |
| `/history` | GET | Lịch sử của mình; manager xem tất cả | customer, member, staff, manager |
| `/export-history` | GET | Tải lịch sử PDF | Cùng phạm vi history |
| `/receipt?orderId=...` | GET | Hóa đơn; hỗ trợ stage=balance, autoprint=1 | Chủ đơn; manager xem tất cả; staff xem thêm đơn cọc khách |
| `/deposit-orders` | GET, POST | Xem đơn cọc; thu phần còn lại/xác nhận nhận hàng | GET: customer, member, staff; POST: staff |
| `/loyalty` | GET, POST | Hồ sơ, điểm hoặc danh sách thành viên | GET: customer, member, staff; POST đăng ký: customer |
| `/chat` | POST | Chatbot tư vấn, hỗ trợ thêm món, trả JSON | Đã đăng nhập; thao tác giỏ phụ thuộc role |

### 3.2. Quản trị

| URL tương đối | Phương thức / tham số | Chức năng | Quyền |
| --- | --- | --- | --- |
| `/admin/products` | GET | Danh sách sản phẩm | manager |
| `/admin/products?action=create` | GET | Form thêm sản phẩm | manager |
| `/admin/products?action=edit&id=...` | GET | Form sửa sản phẩm | manager |
| `/admin/products` | POST action=create/update/delete | Lưu hoặc xóa; lưu thực tế phân biệt thêm/sửa theo id | manager |
| `/admin/users` | GET | Danh sách nhân viên | manager |
| `/admin/users?action=create` | GET | Form thêm nhân viên | manager |
| `/admin/users?action=edit&id=...` | GET | Form sửa nhân viên | manager |
| `/admin/users` | POST action=create/update/delete | CRUD tài khoản staff | manager |
| `/admin/contacts` | GET; action=create/edit, id khi sửa | Danh bạ hoặc form liên hệ | admin |
| `/admin/contacts` | POST action=create/update/delete | CRUD liên hệ | admin |
| `/admin/settings` | GET, POST | Xem/lưu cài đặt cửa hàng | admin |
| `/admin/report?date=yyyy-MM-dd` | GET | Báo cáo và lịch sử chatbot | admin, manager |
| `/admin/report/chat-history/print?date=yyyy-MM-dd` | GET | In lịch sử chatbot theo ngày | admin, manager |
| `/create-admin` | GET, POST | Endpoint cũ đã vô hiệu hóa trong Servlet (410); bộ lọc quyền có thể chặn trước | Không phải form tạo admin công khai |

**Đối chiếu đề bài:** Không có ánh xạ `/register` và `/products`. Đường dẫn thực tế là `/register-member`, `/menu`, `/product?id=...`, `/admin/products`. Các JSP trong `WEB-INF/views` được mở thông qua Servlet.

## 4. Trường dữ liệu và validation

### 4.1. Đăng nhập – `/login`

| Trường | Ràng buộc |
| --- | --- |
| username | Bắt buộc, không chỉ chứa khoảng trắng; form required. Servlet chưa giới hạn độ dài hoặc regex ký tự. |
| password | Bắt buộc, không chỉ chứa khoảng trắng; form required; so khớp hash trong DB, không kiểm tra độ mạnh tại bước đăng nhập. |
| _csrf | Token hợp lệ của session. |

- Thiếu dữ liệu: “Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.”
- Sai thông tin: “Sai tên đăng nhập hoặc mật khẩu!”; lỗi SQL: “Lỗi hệ thống, vui lòng thử lại sau.”
- Thành công thay session ID, lưu user vào session, chuyển theo role ở mục 2. GET login khi đã đăng nhập cũng chuyển theo role.

### 4.2. Đăng ký tài khoản – `/register-member`

| Trường | Bắt buộc | Validation thực tế |
| --- | --- | --- |
| username | Có | Không null/rỗng sau trim để kiểm tra; không trùng qua findByUsername. Chưa giới hạn độ dài/cấm ký tự đặc biệt. Giá trị lưu vẫn là chuỗi nhận được, không trim trước lưu. |
| password | Có | Chuỗi gốc tối thiểu 8 ký tự; chưa giới hạn tối đa, chưa bắt buộc hoa/thường/số/ký tự đặc biệt. Máy chủ chưa loại riêng mật khẩu toàn dấu cách dài từ 8 ký tự. |
| fullname | Có | Không rỗng sau trim để kiểm tra; chưa có min/max cụ thể hay regex ký tự. |
| email | Có | Regex `^[A-Za-z0-9+_.-]+@(.+)$`; không được tài khoản khác sử dụng. Giao diện type=email có thể chặn chặt hơn máy chủ. |
| _csrf | Có | Token hợp lệ. |

- Form và Servlet đăng ký hiện không có confirmPassword.
- Tài khoản mới có role customer; mật khẩu được băm trước khi lưu.
- Thành công forward tới giao diện đăng nhập với “Đăng ký thành công! Vui lòng đăng nhập.”; không tự đăng nhập, URL có thể vẫn là register-member.
- Lỗi: username rỗng/trùng, mật khẩu dưới 8, họ tên rỗng, email sai/trùng hoặc lưu DB thất bại.
- ValidationUtil có regex username 3–50 và hàm kiểm tra mật khẩu mạnh nhưng luồng này không gọi; không dùng các hàm đó làm yêu cầu đã triển khai.

### 4.3. Đổi mật khẩu – `/change-password`

| Trường | Ràng buộc |
| --- | --- |
| oldPassword | Bắt buộc, không blank; phải đúng mật khẩu hiện tại trong DB. |
| newPassword | Bắt buộc, không blank; tối thiểu 8 ký tự chuỗi gốc; chưa bắt buộc ký tự đặc biệt hoặc giới hạn tối đa. |
| confirmPassword | Bắt buộc, phải bằng chính xác newPassword. |

Thành công lưu hash mới và hiển thị “Đổi mật khẩu thành công!”. Chưa có quy tắc cấm dùng lại mật khẩu cũ trong Servlet.

### 4.4. CRUD sản phẩm – `/admin/products`

| Trường | Kiểu / bắt buộc | Ràng buộc hiện tại |
| --- | --- | --- |
| id | Số nguyên khi sửa/xóa | > 0. Khi lưu, thiếu/rỗng hiểu là thêm mới id=0. GET sửa ID sai trả 400; không tìm thấy trả 404. |
| name | Chuỗi, có | Trim, không rỗng; chưa giới hạn độ dài, cấm ký tự đặc biệt hoặc kiểm tra tên trùng ở tầng ứng dụng. |
| category | Chuỗi, có | Trim, không rỗng. Form cho chọn Coffee, Tea, Pastry; máy chủ chưa kiểm tra danh sách cho phép. |
| barcode | Chuỗi, không | Rỗng thành null. Nếu nhập phải EAN-13 đúng 13 chữ số và check digit, không trùng sản phẩm khác; loại trừ chính ID đang sửa. Chuẩn hóa bỏ ký tự điều khiển, trim, bỏ tiền tố máy quét dạng ]E0. Form maxlength=16. |
| price | Số thực, có | Double.parseDouble; từ chối âm, hiện chấp nhận 0. Form step=1000; máy chủ chưa kiểm tra bội số 1.000 hoặc tính hữu hạn NaN/Infinity. |
| description | Chuỗi, không | Chưa có min/max hoặc quy tắc ký tự riêng. |
| stock | Số nguyên, có | >= 0; sai định dạng/âm bị từ chối. |
| soldCount | Số nguyên, có | >= 0; sai định dạng/âm bị từ chối. |
| imageUrl | Chuỗi, có | Trim, không rỗng; chưa kiểm tra cú pháp URL/giao thức/đuôi ảnh. |
| action, _csrf | Điều khiển | action=delete xử lý xóa; thao tác lưu phân biệt theo id; POST cần CSRF. |

Lưu thành công chuyển về danh sách với success=1. Dữ liệu số/text sai có thể trả 400; barcode sai/trùng hiển thị lại form. Xóa có xác nhận trên giao diện; DAO có thể từ chối xóa, ví dụ do giao dịch liên quan. Quy tắc “giá > 0” và “tên không trùng” của mẫu đề bài chưa được đảm bảo bởi mã hiện tại.

### 4.5. CRUD nhân viên – `/admin/users`

| Trường | Ràng buộc |
| --- | --- |
| id | Số nguyên > 0 khi sửa/xóa; bản ghi không tồn tại trả 404. |
| username | Bắt buộc sau trim; không trùng khi tạo. Khi sửa form readonly, máy chủ giữ username cũ. Chưa có min/max hay regex riêng. |
| password | Tạo mới: tối thiểu 8 ký tự. Sửa: để trống/blank để giữ mật khẩu; nhập mới không blank phải >= 8. Chưa bắt buộc ký tự đặc biệt. |
| fullname | Bắt buộc sau trim; chưa có giới hạn độ dài. |
| email | Bắt buộc, trim; regex `^[A-Za-z0-9+_.-]+@[^\s@]+$`; không thuộc tài khoản khác. |
| role | Chỉ staff được manager gán. |
| action | create/update/delete; hành động không hợp lệ trả 400. |

Manager chỉ sửa/xóa staff, không sửa/xóa ngang cấp/cấp trên và không tự xóa tài khoản đang đăng nhập. Giao diện xóa có xác nhận. Thành công chuyển về danh sách với success=1.

### 4.6. Danh bạ liên hệ – `/admin/contacts`

Các chuỗi được trim; rỗng chuyển null trước validation.

| Trường | Bắt buộc | Ràng buộc |
| --- | --- | --- |
| id | Khi sửa/xóa | Số nguyên > 0. |
| name | Có | Không rỗng, tối đa 120 ký tự. |
| position | Có | owner, manager, employee hoặc other. |
| phone | Không | Tối đa 30 ký tự; chưa kiểm tra định dạng điện thoại/chỉ chứa số. |
| email | Không | Tối đa 254 ký tự; nếu nhập, regex `^[A-Za-z0-9+_.-]+@[^\s@]+$`. |
| address | Không | Tối đa 500 ký tự. |
| notes | Không | Tối đa 1.000 ký tự. |

### 4.7. Đăng ký thành viên – POST `/loyalty`

Khác đăng ký tài khoản công khai: phải đăng nhập bằng customer. Thành công tạo hồ sơ, chuyển role DB/session thành member; mã thành viên dạng CDR + ID đệm tối thiểu 7 chữ số.

| Trường | Bắt buộc | Ràng buộc |
| --- | --- | --- |
| fullname | Có | Sau trim: 2–120 ký tự. |
| email | Có | Regex `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`; form tối đa 254, Servlet chưa kiểm tra giới hạn này. |
| phone | Có | Regex `^(?:\+84|0)[0-9]{9,10}$`: prefix 0 hoặc +84, sau đó 9–10 chữ số; không có dấu cách giữa số. Form maxlength=20. |
| birthDate | Không | Ngày hợp lệ yyyy-MM-dd, không sau ngày hiện tại. |
| address | Có | Sau trim: 5–500 ký tự. |

Email trùng có thể bị ràng buộc DB từ chối; cần xác minh schema thực tế. Staff xem/tìm danh sách theo tên, số điện thoại hoặc mã, không được gửi form đăng ký này.

### 4.8. Giỏ hàng, barcode và tìm kiếm

| Trường / tình huống | Quy tắc |
| --- | --- |
| id khi thêm | Phân tích số nguyên; sản phẩm phải tồn tại, còn hàng. Mỗi lần thêm 1, gộp sản phẩm trùng, không vượt tồn kho. |
| id khi cập nhật | Số nguyên > 0; sai định dạng trả 400. |
| quantity | Số nguyên. Form min=1, max=tồn kho; máy chủ xóa dòng nếu <=0, giới hạn về tồn kho trong đối tượng giỏ nếu vượt. |
| barcode khi quét | Không rỗng; EAN-13/check digit đúng sau chuẩn hóa; phải tồn tại và còn hàng; staff và máy quét bật. |
| keyword | Chuỗi tìm tên sản phẩm; form required, Servlet chưa giới hạn độ dài hay validation riêng. |
| category | Tham số lọc menu; giao diện có Coffee, Tea, Pastry. |
| id ở trang product | Thiếu/sai định dạng/không tìm thấy chuyển về menu. |

Quét trả JSON: 200 thành công; 400 mã rỗng/sai; 404 không có sản phẩm; 409 hết hàng/vượt kho; 500 lỗi DB; 403 sai quyền/tắt máy quét. Giỏ nằm trong session; kiểm tra tồn kho tại giỏ không thay thế kiểm tra DB lúc tạo đơn.

### 4.9. Thanh toán và đơn cọc

| Trường | Ràng buộc |
| --- | --- |
| orderType | direct hoặc deposit; customer/member chỉ deposit; staff cả hai. |
| paymentMethod | cash hoặc vnpay; customer/member chỉ vnpay; staff cả hai. |
| pickupDate | Bắt buộc với deposit; ngày hợp lệ sau ngày hiện tại (từ ngày mai); không bắt buộc với direct. |
| redeemPoints | Thiếu/blank mặc định 0; nếu có phải số nguyên >=0. Chỉ member dùng; không vượt số dư hoặc mức giảm 80% đơn. |
| orderId nhận hàng | Phân tích số nguyên; DB phải tìm được đơn cọc khách đang chờ nhận, chưa quá hạn; chưa có giao dịch phần còn lại pending/paid. |
| balancePaymentMethod | cash hoặc vnpay; chỉ staff thu phần còn lại. |
| paymentId | Phân tích số nguyên dài; giao dịch phải đang chờ và thuộc phạm vi được thanh toán. |

- Giỏ rỗng không được tạo đơn. Tạo đơn kiểm tra tồn kho và dùng transaction DB; giá/tổng được tính từ giỏ phía máy chủ.
- Giá dòng = discountedPrice × quantity. Ưu tiên khuyến mãi riêng sản phẩm; nếu không có, dùng phần trăm lớn nhất của khuyến mãi chung đang hiệu lực. Hiệu lực: status=active, ngày hiện tại nằm trong khoảng bắt đầu/kết thúc, tính cả hai đầu.
- Tổng sau voucher = tổng các dòng − điểm dùng × giá trị một điểm. Mặc định một điểm tương ứng 1.000 theo cấu hình.
- Điểm dùng tối đa = min(số dư, floor(tổng trước voucher × 80% / giá trị một điểm)). Gửi quá giới hạn bị từ chối, không tự chấp nhận một số điểm khác.
- Tiền cọc = round(tổng sau voucher × tỷ lệ cọc), mặc định 30%; phần còn lại = tổng − cọc.
- Tiền mặt thành công chuyển hóa đơn, xóa giỏ. VNPay tạo giao dịch chờ, giữ hàng, xóa giỏ, chuyển tới cổng thanh toán; chưa cấu hình VNPay thì báo lỗi.
- Callback VNPay kiểm tra HMAC-SHA512, mã giao dịch, số tiền bằng round(payment.amount × 100), mã phản hồi/trạng thái. Callback lặp của giao dịch đã thanh toán không được thu hoặc trừ kho lần nữa.
- Trạng thái chính: pending (đợi trả tiền), completed (đơn trực tiếp hoàn tất), deposit_pending (đã cọc/chờ nhận), picked_up (đã nhận); DAO có xử lý giao dịch thất bại.
- Quá ngày nhận mà chưa nhận: chuyển không nhận hàng và hoàn kho theo logic DB. Bộ lập lịch chạy sau 1 phút, lặp 60 giây; history/deposit-orders cũng kích hoạt kiểm tra. Cần thủ tục dbo.usp_expire_overdue_deposits trong DB để thực thi đầy đủ.

### 4.10. Cài đặt hệ thống – `/admin/settings`

POST phải có đủ mọi khóa dưới đây, kể cả khóa cho phép rỗng. Chuỗi được trim.

| Trường | Ràng buộc máy chủ |
| --- | --- |
| store_name | 2–100 ký tự. |
| store_tagline | 2–200 ký tự. |
| hotline | 3–30 ký tự, regex `[0-9+(). -]{3,30}`: số, dấu +, ngoặc, chấm, cách, gạch ngang. |
| address | 5–300 ký tự. |
| weekday_hours, weekend_hours | Mỗi trường 3–80 ký tự; chưa phân tích cú pháp giờ. |
| social_links | Có thể rỗng, tối đa 200; chưa kiểm tra URL. |
| currency | VND/USD/EUR, chuẩn hóa hoa; chỉ đổi ký hiệu, không quy đổi giá. |
| deposit_percent | Số nguyên 1–90. |
| loyalty_vnd_per_point | Số nguyên 100–1.000.000. Form step=100; máy chủ chưa yêu cầu bội số 100. |
| barcode_scanner_enabled | true/false; không phân biệt hoa/thường khi kiểm tra. |

### 4.11. Hóa đơn, báo cáo, chatbot và OAuth

- **Hóa đơn:** orderId sai định dạng trả 400; không tồn tại/ngoài phạm vi được xem trả 404. stage=balance hiển thị phần còn lại, autoprint=1 kích hoạt in trình duyệt.
- **Lịch sử/PDF:** customer/member/staff xem đơn của mình; manager xem toàn bộ. Staff có thêm quyền xem hóa đơn đơn cọc khách qua receipt.
- **Báo cáo:** date dạng yyyy-MM-dd; thiếu/sai dùng ngày hiện tại. Có doanh thu ngày chọn, doanh thu tháng hiện tại/theo ngày của tháng hiện tại, top 5 sản phẩm và lịch sử chatbot.
- **Chat:** message bắt buộc sau trim, tối đa 500 ký tự; hỗ trợ phản hồi cục bộ/Gemini và fallback. Lệnh thêm món tuân theo quyền giỏ và tồn kho; phản hồi tư vấn không phải xác nhận thanh toán.
- **OAuth:** Google/Facebook cần cấu hình; code phải có, state phải khớp session. Callback thiếu/sai state không phải đăng nhập hợp lệ.

## 5. Dữ liệu nghiệp vụ chính

| Nhóm | Thuộc tính chủ yếu |
| --- | --- |
| User | id, username, passwordHash, fullname, email, role. |
| Product | id, name, price, description, stock, soldCount, imageUrl, category, barcode. |
| CartItem | product, quantity, discountedPrice; lưu trong session. |
| Promotion / PromotionItem | Phần trăm giảm, ngày bắt đầu/kết thúc, status; khuyến mãi riêng gắn productId. |
| Order / chi tiết | userId, ngày lập, tổng, trạng thái, loại đơn, phương thức, cọc, ngày nhận/trạng thái nhận; sản phẩm, số lượng, giá từng dòng. |
| Payment | orderId, giai đoạn thanh toán, phương thức, số tiền, trạng thái, mã tham chiếu. |
| Thành viên | userId, mã thành viên, phone, birthDate, address; points, totalSpent, thời điểm cập nhật. |
| Contact | name, position, phone, email, address, notes. |
| Cài đặt / chatbot | Khóa–giá trị cấu hình; người dùng, tin nhắn, phản hồi, nguồn và thời gian. |

Model chủ yếu lưu dữ liệu, không phải bằng chứng mọi trường đã được validation. Giới hạn vật lý của cột phải xác minh với SQL Server.

## 6. Điều kiện và định hướng kiểm thử

### 6.1. Điều kiện môi trường

1. WAR chứa mã Java và src/webapp, được triển khai tại context /chidori trên Tomcat tương thích Servlet 6.
2. SQL Server mặc định CafeDB ở localhost:1433; cấu hình bằng CAFE_DB_URL, CAFE_DB_USER, CAFE_DB_PASSWORD hoặc cơ chế cấu hình local trong mã. Không lưu mật khẩu thật vào tài liệu/test.
3. Cần bảng, dữ liệu và thủ tục phục vụ đơn cọc, thành viên, barcode. Repository hiện không có file .sql để xác nhận schema/migration đầy đủ.
4. Có tài khoản kiểm thử từng role, sản phẩm đủ hàng/hết hàng/đã giao dịch. Không giả định tài khoản mẫu admin trong test tồn tại ở DB.
5. OAuth/VNPay/Gemini cần cấu hình riêng. Ngày nhận/ngày sinh theo ngày máy chủ; hiệu lực khuyến mãi theo ngày SQL Server.

### 6.2. Nhóm ca kiểm thử cần thiết

| Nhóm | Dữ liệu biên / tình huống |
| --- | --- |
| Tài khoản | Trống/blank; sai thông tin; đúng từng role; mật khẩu 7/8/9 ký tự; username/email trùng; xác nhận mật khẩu sai khi đổi. |
| Sản phẩm | Giá âm/0/dương/NaN/Infinity; sai kiểu số; stock âm/0; tên trùng; barcode sai độ dài/check digit/trùng; thiếu dữ liệu; xóa sản phẩm có giao dịch. |
| Phân quyền | Truy cập trực tiếp URL/POST sai role, sai/thiếu CSRF, thay ID để xem tài nguyên người khác. |
| Giỏ | Thêm trùng/hết hàng/vượt kho; quantity 0/âm/chữ; giỏ rỗng; quét mã không tồn tại. |
| Đơn cọc/thanh toán | Customer gửi direct/cash; ngày hôm qua/hôm nay/ngày mai; điểm vượt số dư/80%; callback sai chữ ký/số tiền/lặp. |
| Thành viên/liên hệ | Biên min/max trường; phone sai prefix/có khoảng trắng; ngày sinh tương lai; email sai/trùng. |
| Cài đặt/báo cáo/chat | Tỷ lệ cọc 0/1/90/91; giá trị điểm 99/100/1.000.000/1.000.001; ngày sai; message 0/500/501 ký tự. |

Đây là định hướng thiết kế test, chưa phải kết quả Pass/Fail. TEST_PLAN.md, TEST_CASES.md và test Java hiện chứa nội dung mẫu, cần đồng bộ URL/dữ liệu/kết quả mong đợi trước sử dụng.

## 7. Khoảng trống cần hoàn thiện hoặc chốt yêu cầu

| Mã | Phát hiện | Hướng chốt yêu cầu |
| --- | --- | --- |
| GAP-01 | Đăng ký chưa giới hạn username 5–20/3–50, cấm ký tự đặc biệt hoặc có confirmPassword. | Nếu áp dụng, bổ sung đồng bộ form/máy chủ; không coi ví dụ là chức năng đã có. |
| GAP-02 | Mật khẩu tối thiểu 8, chưa bắt buộc ký tự đặc biệt; ValidationUtil có chính sách khác và không được gọi ở luồng đăng ký/đổi. | Thống nhất chính sách dùng chung; kiểm thử POST trực tiếp. |
| GAP-03 | Giá 0 được chấp nhận, chưa chặn NaN/Infinity, chưa kiểm tra tên sản phẩm trùng. | Đề xuất giá hữu hạn >0, tên duy nhất nếu chọn làm tiêu chí nghiệp vụ. |
| GAP-04 | Thiếu min/max cho nhiều trường; nhiều regex email; category/imageUrl chỉ kiểm tra không rỗng. | Chốt độ dài theo schema, danh mục cho phép và cú pháp URL; test tiếng Việt/ký tự đặc biệt/chuỗi dài. |
| GAP-05 | HTML và máy chủ khác nhau về step giá/điểm, maxlength email thành viên, quantity <=0. | Tách kỳ vọng UI và POST trực tiếp; thống nhất nếu sửa. |
| GAP-06 | Chưa có DDL/migration trong repository, DAO phụ thuộc thủ tục DB. | Xác minh schema, unique, transaction và thủ tục hết hạn trước nghiệm thu tích hợp. |
| GAP-07 | Test mẫu dùng URL thiếu /chidori, dữ liệu/đích chuyển hướng mẫu; UI test đang Disabled. | Cập nhật test sau khi chốt đặc tả; Pass ghi sẵn không phải bằng chứng đã chạy. |

## 8. Nguồn đối chiếu

- **URL/luồng:** src/main/java/com/cafe/servlet và dao/LoyaltyServlet.java.
- **Form:** src/webapp/WEB-INF/views; JavaScript trong src/webapp/assets/js và các JSP.
- **Validation:** MemberRegisterServlet, LoginServlet, ChangePasswordServlet, ProductManagementServlet, UserManagementServlet, ContactServlet, LoyaltyServlet, CheckoutServlet, BarcodeUtil, CartService, LoyaltyVoucherService, SystemSettingsService.
- **Quyền/session:** RoleAccessPolicy, AuthFilter, CsrfUtil, WEB-INF/web.xml.
- **Giao dịch/dữ liệu:** dao, model, payment/VNPayCallbackService.java, listener/DepositExpirationListener.java.
- **Môi trường/test mẫu:** pom.xml, config, src/test/java, .github/workflows/test.yml, README.md, docs/TEST_PLAN.md, docs/TEST_CASES.md.
