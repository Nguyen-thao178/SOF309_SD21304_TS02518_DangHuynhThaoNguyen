# KẾ HOẠCH KIỂM THỬ (TEST PLAN)

## 1. Thông tin và mục tiêu

- **Dự án:** Chidori Coffee Management System.
- **Sinh viên:** Đặng Huỳnh Thảo Nguyên – TS02518 – SD21304.
- **Phiên bản thiết kế:** 1.0 – 08/11/2026.
- **Căn cứ:** [SPECIFICATIONS.md](SPECIFICATIONS.md), đặc biệt mục 2–4 và các GAP tại mục 7.
- **Bộ ca:** [TEST_CASES.md](TEST_CASES.md). **Nhật ký AI:** [01_test_design_ai.md](ai-logs/01_test_design_ai.md).
- **Mục tiêu:** Kiểm tra validation và kết quả nghiệp vụ ở Controller/Service/Filter; xác minh luồng thao tác và validation trên trình duyệt; phát hiện sai quyền, sai số tiền, sai kho/điểm và khác biệt giữa giao diện với máy chủ.
- Đây là thiết kế kiểm thử; chưa triển khai script mới, chưa chạy test và chưa xác nhận CI thành công.

## 2. Phạm vi

**Trong phạm vi:** đăng nhập/đăng ký/đổi mật khẩu/đăng xuất; CRUD sản phẩm, nhân viên, liên hệ; thành viên; menu/tìm kiếm/khuyến mãi; giỏ/barcode; checkout, voucher, đơn cọc, hóa đơn/lịch sử/PDF; cài đặt/báo cáo/chat; phân quyền/CSRF/session; hợp đồng callback OAuth/VNPay.

**Ngoài phạm vi:** tải lớn, hiệu năng chuyên sâu, kiểm thử xâm nhập toàn diện, chứng nhận dịch vụ OAuth/VNPay/Gemini của bên thứ ba, giao dịch tiền thật, tương thích mọi thiết bị/trình duyệt, thay đổi mã nghiệp vụ hoặc schema.

**Giới hạn:** không có schema/migration SQL đầy đủ trong repository. Các ca cần thủ tục hết hạn, khóa duy nhất hoặc giao dịch DB chỉ có thể chạy khi chuẩn bị DB test tương ứng; không giả định DB production đúng theo mã. GAP chưa được phê duyệt không trở thành yêu cầu mới.

## 3. Chiến lược theo tầng

| Tầng | Cách thực hiện | Kết quả cần kiểm tra |
| --- | --- | --- |
| Controller/Filter | JUnit 5, request/response/session và DAO/provider được kiểm soát; gọi GET/POST trực tiếp, không phụ thuộc HTML | Status, forward/redirect, message, session, tham số DAO, số lần gọi; không ghi khi sai |
| Service | ParameterizedTest + MethodSource/CsvSource cho logic thuần, không thay thế bằng hàm giả lập nghiệp vụ | ValidationResult, barcode/check digit, voucher/net total, chính sách role |
| Tích hợp | Tomcat + SQL Server test, stub hoặc sandbox gateway; kiểm tra transaction/callback | Dữ liệu trước/sau, rollback, kho/điểm, tính lặp an toàn, quyền sở hữu |
| UI | Selenium WebDriver trên Chrome; Page Object Model, explicit wait | Nội dung, URL đúng, form validity, menu/quyền, thông báo, dữ liệu sau reload |
| Smoke | P0 chọn lọc: login từng role, quyền, CRUD sản phẩm hợp lệ, giỏ, cash checkout, CSRF | Luồng thiết yếu không hỏng |
| Regression | Toàn bộ ca đã sẵn sàng sau sửa lỗi, ưu tiên P0 rồi P1 | Không tái xuất hiện lỗi và không ảnh hưởng chức năng khác |

JUnit/Mockito hoặc công cụ mock tương đương là phương án triển khai đề xuất; repository hiện chưa có đầy đủ bộ mock/test theo thiết kế này. Không dùng test mẫu authenticate giả làm bằng chứng Controller thật đúng.

## 4. Kỹ thuật thiết kế

### 4.1. Phân vùng tương đương (EP)

| Đầu vào | Phân vùng hợp lệ | Phân vùng không hợp lệ / khác biệt |
| --- | --- | --- |
| Trường bắt buộc | Có giá trị sau trim | Thiếu tham số, rỗng, toàn khoảng trắng |
| Đăng nhập | User tồn tại + password đúng, mỗi role | Không tồn tại, sai password, lỗi DAO |
| Trùng dữ liệu | Username/email mới; sửa giữ email/barcode của chính mình | Trùng user/sản phẩm khác |
| Số nguyên | Đúng kiểu và trong miền | Chữ, số thập phân, ngoài miền; không gộp mọi lỗi thành một ca |
| Barcode | EAN-13 đúng check digit, chưa trùng | Rỗng khi quét, sai số chữ số, sai check digit, không tồn tại, trùng |
| Quyền | Role được phép, tài nguyên thuộc phạm vi | Chưa đăng nhập, sai role, ID của người khác |
| Thanh toán | Role × orderType × paymentMethod hợp lệ | Giá trị ngoài enum, customer direct/cash, giỏ rỗng |
| Callback | Chữ ký/mã giao dịch/số tiền đúng | Sai/thiếu chữ ký, mã không tồn tại, lệch số tiền, lặp |
| UI so với server | Dữ liệu qua form | Dữ liệu vượt ràng buộc HTML gửi trực tiếp chỉ kiểm tra ở Controller |

### 4.2. Phân tích giá trị biên (BVA)

Dùng min−1, min, min+1 và max−1, max, max+1 khi có cả hai biên; miền chỉ có min thì kiểm tra ba điểm quanh min và một giá trị danh nghĩa. Mỗi lượt chỉ thay một trường, giữ các trường còn lại hợp lệ.

| Quy tắc | Giá trị kiểm tra |
| --- | --- |
| Password min8 | 7,8,9; riêng null/rỗng/blank |
| Giá/tồn kho/đã bán hiện tại >=0 | -1,0,1; giá danh nghĩa30000; không tự đổi tiêu chí thành >0 |
| ID >0 | -1,0,1 và ID_MISSING; ID=1 phải được seed hoặc mock là tồn tại |
| Họ tên member 2–120 | 1,2,3,119,120,121 |
| Địa chỉ member 5–500 | 4,5,6,499,500,501 |
| Độ dài contact có max | 0,1 khi bắt buộc; max−1,max,max+1 |
| Phone member | Prefix 0/+84 × 8,9,10,11 chữ số sau prefix |
| Ngày nhận >T | T−1,T,T+1,T+2 |
| Ngày sinh <=T | T−1,T,T+1; thêm ngày không tồn tại |
| Quantity, S=5 | -1,0,1,4,5,6; UI và server kỳ vọng khác ở0/6 |
| Điểm, tổng100000/giá trị1000 | -1,0,1,79,80,81; số dư30 kiểm tra29,30,31 |
| Cọc1–90% | 0,1,2,89,90,91 |
| Giá trị điểm100–1000000 | 99,100,101,999999,1000000,1000001 |
| Tin nhắn max500 | 0,1,499,500,501 |
| Số tiền callback30000×100 | 2999999,3000000,3000001 |

Không thiết kế biên tối đa giả cho trường chưa có giới hạn trong đặc tả. Dùng chuỗi ASCII để độ dài Java/DOM đo được rõ ràng; ca tiếng Việt kiểm tra riêng khả năng bảo toàn nội dung.

### 4.3. Tổ hợp phân quyền bắt buộc (ROLE_MATRIX)

Mỗi ô là một lượt độc lập cho từng endpoint trong nhóm; POST luôn có CSRF đúng để đo quyền, trừ ca CSRF riêng. A=được qua filter, D=403. Không session: GET protected → redirect login; POST thiếu session/token →403 trước kiểm tra quyền.

| Endpoint/phương thức | customer | member | staff | manager | admin |
| --- | --- | --- | --- | --- | --- |
| GET home/menu/promotion/search/product/cart; POST add-to-cart/update-cart/remove-cart | A | A | A | D | D |
| GET/POST checkout; GET vnpay-pay | A | A | A | D | D |
| POST cart/scan | D | D | A | D | D |
| GET history/export-history/receipt | A | A | A | A | D |
| GET deposit-orders | A | A | A | D | D |
| POST deposit-orders | D | D | A | D | D |
| GET loyalty | A | A | A | D | D |
| POST loyalty | A | D | D | D | D |
| GET/POST admin/products; GET/POST admin/users | D | D | D | A | D |
| GET admin/report; GET admin/report/chat-history/print | D | D | D | A | A |
| GET/POST admin/contacts; GET/POST admin/settings | D | D | D | D | A |
| GET/POST change-password; POST chat/logout | A | A | A | A | A |

A không đảm bảo nghiệp vụ thành công: dùng ID/dữ liệu fixture hợp lệ để đi tiếp; checkout còn giới hạn direct/deposit và cash/vnpay; receipt còn kiểm tra chủ sở hữu. Các endpoint công khai login/register-member/oauth-login/oauth-callback/vnpay-return/vnpay-ipn/assets được kiểm tra riêng.

## 5. Môi trường, dữ liệu và cách ly

- Java17, Maven, Tomcat10.1, SQL Server test với CafeDB/schema được xác nhận; URL mặc định http://localhost:8080/chidori.
- Chrome/ChromeDriver tương thích; CI có thể headless. Đặt URL bằng cấu hình thay vì hard-code /login thiếu context.
- Dùng tài khoản từng role, DB riêng và dữ liệu có tiền tố qa_; mật khẩu/secret lấy từ biến môi trường, không ghi giá trị thật vào báo cáo.
- Fixture chi tiết tại TEST_CASES mục 1. Seed/reset trước mỗi lượt; không phụ thuộc thứ tự ca. Các ca boundary được chấp nhận phải reset để tránh lỗi trùng che mất validation.
- Đồng bộ ngày ứng dụng/DB; T là ngày máy chủ tại thời điểm chạy, ghi ngày thực tế vào kết quả; không hard-code ngày mai theo ngày thiết kế.
- Gateway dùng stub ký callback bằng secret test hoặc VNPay sandbox. Cần proxy/provider giả cho lỗi OAuth/Gemini; không dựa mạng thật cho unit test.
- Chụp snapshot đơn/payment/kho/điểm/hash/cấu hình trước và sau ca liên quan. Cleanup chỉ dữ liệu test tạo, phục hồi cấu hình; không xóa dữ liệu người dùng.

## 6. Điều kiện vào, ra, dừng và tiếp tục

**Đầu vào:** đặc tả và kỳ vọng hiện trạng được duyệt; build deploy được; fixture/schema có sẵn; đúng role/CSRF; gateway stub/sandbox và test DB sẵn sàng với ca tích hợp.

**Đầu ra:** tất cả ca trong phạm vi đã chạy hoặc có lý do Blocked/N/A được chấp thuận; P0 phải Pass; không còn lỗi nghiêm trọng làm sai quyền, thu tiền/kho/điểm hoặc mất dữ liệu; các ca thất bại có defect liên kết; mọi bộ dữ liệu BVA đã được ghi kết quả riêng. Không tuyên bố hoàn thành toàn bộ nếu còn Blocked chưa được chấp thuận.

**Dừng:** môi trường sai dữ liệu, kết nối nhầm DB thật, mất kiểm soát giao dịch sandbox, login/fixture hỏng khiến kết quả không tin cậy. Giữ log, khôi phục môi trường và chạy lại ca ảnh hưởng sau khi sửa.

**Blocked:** thiếu SQL procedure/schema, tài khoản/secret sandbox hoặc công cụ gây lỗi có kiểm soát. **N/A:** thao tác UI không tồn tại hoặc trình duyệt không thể nhập trạng thái đó; phải giải thích và liên kết ca Controller tương ứng, không ghi Pass.

## 7. Ưu tiên, quản lý lỗi và bằng chứng

- P0: xác thực/phân quyền/CSRF; tiền, kho, đơn và giao dịch quan trọng. P1: validation phụ, CRUD hỗ trợ, báo cáo/UI.
- Phân biệt priority của ca với severity của lỗi: Critical (truy cập trái quyền/sai tiền/mất dữ liệu), Major (luồng chính hỏng), Minor (hiển thị/luồng phụ).
- Ghi: ID ca + dataset, môi trường/build, tiền điều kiện thực tế, bước tái hiện, expected/actual, HTTP/JSON/redirect, snapshot DB và ảnh UI; che mật khẩu/token/secrets.
- Trạng thái: Chưa chạy, Pass, Fail, Blocked, N/A. Ban đầu tất cả Chưa chạy; không sao chép Pass từ tài liệu mẫu.
- Ca Characterization ghi lại hành vi hiện tại/GAP; không tự đánh Fail vì yêu cầu mới chưa duyệt. NaN/Infinity chỉ kiểm tra nhánh validation bằng mock; không gửi dữ liệu đó vào DB dùng chung.

## 8. Lộ trình thực hiện

1. Chuẩn bị fixture, DB test, mapping role và hợp đồng mock.
2. Triển khai/chạy Controller/Service parameterized tests, ưu tiên P0.
3. Chạy smoke UI, sau đó BVA/EP UI và tích hợp giao dịch.
4. Ghi lỗi, sửa, retest và regression; cập nhật actual/evidence theo từng dataset.
5. Đưa kiểm tra độc lập vào CI; UI/tích hợp chỉ chạy khi server/DB đã được cấp. Workflow mẫu mvn clean test hiện chưa tự cấp môi trường web/DB đầy đủ.

## 9. Truy vết

| Đặc tả | Nhóm ca Controller | Nhóm UI |
| --- | --- | --- |
| §2–3 quyền/route | CTL-SEC | UI-AUTH, UI-FLOW |
| §4.1 login | CTL-LOGIN | UI-AUTH |
| §4.2 register | CTL-REG | UI-AUTH |
| §4.3 password | CTL-PWD | UI-AUTH |
| §4.4 sản phẩm | CTL-PROD | UI-PROD |
| §4.5 staff | CTL-USER | UI-FLOW |
| §4.6 contact | CTL-CONTACT | UI-FLOW |
| §4.7 member | CTL-MEMBER | UI-FLOW |
| §4.8 cart/barcode/search | CTL-CART, CTL-MISC | UI-FLOW |
| §4.9 checkout/payment | CTL-PAY | UI-FLOW |
| §4.10 settings | CTL-SET | UI-FLOW |
| §4.11 report/chat/OAuth | CTL-MISC | UI-FLOW |
| §7 GAP | Characterization trong CTL-REG/PWD/PROD, các ca UI/server khác nhau | Không tự áp quy tắc mới |

OAuth thành công và callback ký đúng cần stub/provider fixture; kiểm tra thành công đầy đủ khi tích hợp được cấp, không đánh Pass chỉ vì mock trả thành công.
