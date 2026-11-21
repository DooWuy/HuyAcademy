# Danh mục endpoint HuyAcademy

Đặc tả mục tiêu; chưa triển khai. Áp dụng [hợp đồng chung và phân quyền](API_references.md), [schema](database_references.md). Query filter không được tính thành endpoint mới. Danh sách dưới đây có 42 cặp method–path.

## Ký hiệu và dữ liệu

AUTH: tài khoản hoạt động có token hợp lệ. OWNER: chủ tài nguyên theo principal. TEACHER_OWNER: giảng viên được giao course chứa tài nguyên. Quyền ghi bài: ADMIN hoặc TEACHER_OWNER. Mọi kiểm tra owner thực hiện ở service.

ID trong path là số nguyên dương. Trường request không được liệt kê không được phép dùng để thay đổi dữ liệu đặc quyền. PUT trong tài liệu này cập nhật các trường được cung cấp, trường bỏ qua giữ nguyên; trường bắt buộc khi tạo không được đặt null khi sửa. Giá trị enum phân biệt hoa thường. Endpoint trạng thái/password/publish yêu cầu đủ các trường được chỉ định.

Các danh sách áp dụng page/size như tài liệu chung. Sort allowlist: users(username,createdAt), courses(title,price,createdAt), lessons(orderIndex), enrollments(enrollmentDate), notifications(createdAt), reviews(createdAt,rating). Mặc định enrollment theo enrollmentDate desc, lessons theo orderIndex asc; các danh sách khác createdAt desc, luôn thêm ID để ổn định.

## Auth và user

| Method | Path | Quyền | Request / kết quả |
|---|---|---|---|
| POST | /api/auth/login | PUBLIC | username,password → accessToken, tokenType=Bearer, expiresIn (giây), user |
| POST | /api/auth/verify | AUTH | Không body; kiểm tra token bearer → valid=true, userId, expiresAt |
| GET | /api/auth/me | AUTH | User hiện tại |
| POST | /api/auth/logout | AUTH | Không body; thu hồi token hiện tại → data null |
| GET | /api/users | ADMIN | Filter role, status=active/inactive; trang User |
| GET | /api/users/{userId} | ADMIN | User chi tiết |
| POST | /api/users | ADMIN | username,password,email,fullName,role; isActive mặc định true → User |
| PUT | /api/users/{userId} | OWNER hoặc ADMIN | email,fullName; ít nhất một trường → User |
| PUT | /api/users/{userId}/password | OWNER hoặc ADMIN | newPassword; oldPassword bắt buộc nếu tự đổi → null; vô hiệu hóa token cũ |
| PUT | /api/users/{userId}/role | ADMIN | role → User; không đổi role ADMIN khác; tự đổi còn chịu quy tắc ADMIN cuối cùng |
| PUT | /api/users/{userId}/status | ADMIN | isActive → User; khóa tài khoản vô hiệu hóa token |
| DELETE | /api/users/{userId} | ADMIN | Vô hiệu hóa tài khoản, giữ lịch sử → null; gọi lặp có cùng kết quả |

User response: id,username,email,fullName,role,isActive,createdAt,updatedAt. Không có hash/tokenVersion. Login sai thông tin hoặc tài khoản khóa trả thông báo chung 401. Username/email trùng trả 409. Không có endpoint register công khai.

## Course và lesson

| Method | Path | Quyền | Request / kết quả |
|---|---|---|---|
| GET | /api/courses | AUTH | search,teacherId,status; trang CourseSummary; STUDENT/TEACHER yêu cầu trạng thái khác PUBLISHED trả 403 |
| GET | /api/courses/{courseId} | AUTH | ADMIN mọi trạng thái; người khác chỉ PUBLISHED → CourseDetail |
| POST | /api/courses | ADMIN | title,teacherId; description,price,durationHours tùy chọn; luôn DRAFT → CourseDetail |
| PUT | /api/courses/{courseId} | ADMIN | title,description,teacherId,price,durationHours; ít nhất một trường; ARCHIVED chỉ đọc → CourseDetail |
| PUT | /api/courses/{courseId}/status | ADMIN | status; theo chuyển trạng thái đã quy định → CourseDetail |
| DELETE | /api/courses/{courseId} | ADMIN | Chỉ DRAFT chưa ghi danh → null |
| GET | /api/teaching/courses | TEACHER | Trang khóa được giao; filter status tùy chọn |
| GET | /api/teaching/courses/{courseId} | TEACHER_OWNER | CourseDetail mọi trạng thái, danh sách gồm cả bài nháp |
| GET | /api/courses/{courseId}/lessons | AUTH | Trang LessonSummary; ADMIN/TEACHER_OWNER thấy cả nháp; người khác chỉ khóa và bài PUBLISHED |
| GET | /api/lessons/{lessonId} | AUTH | Nội dung đầy đủ theo quyền học trong tài liệu chung → LessonDetail |
| POST | /api/courses/{courseId}/lessons | ADMIN hoặc TEACHER_OWNER | title,orderIndex; contentUrl,textContent tùy chọn; mặc định chưa xuất bản → LessonDetail |
| PUT | /api/lessons/{lessonId} | ADMIN hoặc TEACHER_OWNER | title,orderIndex,contentUrl,textContent; ít nhất một trường → LessonDetail |
| PUT | /api/lessons/{lessonId}/publish | ADMIN hoặc TEACHER_OWNER | isPublished → LessonDetail |
| DELETE | /api/lessons/{lessonId} | ADMIN hoặc TEACHER_OWNER | Theo giới hạn khóa đã ghi danh/đã archive → null |
| GET | /api/lessons/{lessonId}/content_preview | AUTH | Chỉ bài và khóa PUBLISHED → id,title,previewText |

CourseSummary: id,title,description,teacher{id,fullName},price,durationHours,status,createdAt,updatedAt. CourseDetail thêm lessons là metadata (id,title,orderIndex,isPublished), không nhúng nội dung đầy đủ; danh sách này chỉ bài xuất bản trừ luồng quản lý. Dùng endpoint lessons phân trang khi danh sách lớn. LessonSummary không chứa textContent/contentUrl. LessonDetail thêm courseId,textContent,contentUrl,createdAt,updatedAt.

Title trim, 1–255 ký tự; fullName trim, 1–100; description/textContent có thể null; contentUrl tối đa 500 ký tự, HTTP/HTTPS. Price BigDecimal tối đa 8 chữ số nguyên và 2 chữ số thập phân, không âm; durationHours > 0; orderIndex >= 1. Status/isPublished không sửa qua endpoint cập nhật thông tin thông thường. Chuyển lại đúng trạng thái hiện tại là no-op thành công.

## Enrollment và progress

| Method | Path | Quyền | Request / kết quả |
|---|---|---|---|
| GET | /api/enrollments | STUDENT | Trang enrollment của mình |
| POST | /api/enrollments | STUDENT | courseId → Enrollment; tạo notification xác nhận |
| GET | /api/enrollments/{enrollmentId} | STUDENT OWNER | Enrollment và danh sách tiến độ bài |
| PUT | /api/enrollments/{enrollmentId}/complete_lesson/{lessonId} | STUDENT OWNER | Không body; hoàn thành bài, trả Enrollment đã cập nhật |

Enrollment: id,course{id,title,status},status,enrollmentDate,completionDate,progressPercentage. Chi tiết thêm lessonProgress gồm lessonId,isCompleted,completedAt,lastAccessedAt. Client không truyền studentId, phần trăm hoặc trạng thái hoàn thành. Request trùng enrollment trả 409; lesson sai course trả 409; hoàn thành bài chưa xuất bản hoặc không sở hữu trả 403.

## Notification, review và report

| Method | Path | Quyền | Request / kết quả |
|---|---|---|---|
| GET | /api/notifications | AUTH | Filter isRead; trang thông báo của mình |
| PUT | /api/notifications/{notificationId}/read | AUTH OWNER | Không body → Notification đã đọc |
| POST | /api/notifications | ADMIN | userId,message; type,targetUrl tùy chọn → Notification |
| DELETE | /api/notifications/{notificationId} | ADMIN | Xóa → null |
| GET | /api/courses/{courseId}/reviews | AUTH | Quyền xem course như danh mục → trang Review |
| POST | /api/courses/{courseId}/reviews | STUDENT | rating,comment tùy chọn; phải COMPLETED → Review |
| PUT | /api/reviews/{reviewId} | OWNER hoặc ADMIN | rating/comment, ít nhất một trường → Review |
| DELETE | /api/reviews/{reviewId} | OWNER hoặc ADMIN | Xóa → null |
| GET | /api/reports/top_courses | ADMIN | Trang courseId,title,enrollmentCount; đếm ENROLLED/COMPLETED, giảm dần theo count rồi courseId tăng dần |
| GET | /api/reports/student_progress/{studentId} | ADMIN | studentId,fullName và trang courses chứa courseId,status,progressPercentage,completionDate |
| GET | /api/reports/teacher_courses_overview/{teacherId} | ADMIN | teacherId,fullName,totalCourses,publishedCourses,totalEnrollments,completedEnrollments |

Notification: id,userId,message,type,targetUrl,isRead,createdAt. Message trim, 1–5000 ký tự; type tối đa 50; targetUrl là đường dẫn nội bộ tối đa 500. Review: id,courseId,student{id,fullName},rating,comment,createdAt,updatedAt; rating số nguyên 1–5, comment tối đa 5000 ký tự. Không lộ email người đánh giá.

Báo cáo student_progress trả data gồm thông tin sinh viên và `courses:{items,pagination}`; endpoint này chỉ nhận page/size. teacher_courses_overview không phân trang, đếm khóa ở mọi trạng thái, enrollment chỉ ENROLLED/COMPLETED. ID báo cáo không tồn tại trả 404; user sai role mục tiêu trả 409. Top_courses gồm cả khóa chưa có enrollment; không nhận sort tùy ý.

## Thay đổi so với danh sách ban đầu

Gộp bốn dòng filter/search vào endpoint gốc; thêm hai endpoint quản lý course dành cho giảng viên; giữ các path complete_lesson/content_preview và báo cáo hiện có để hạn chế thay đổi không cần thiết. Các endpoint mới vẫn là đặc tả cần triển khai, không khẳng định đang tồn tại trong code.

