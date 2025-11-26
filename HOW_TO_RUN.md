CHẠY LẠI TOÀN BỘ (Từ đầu hoàn toàn)
Bước 1: Dọn dẹp hoàn toàn
powershell# Stop tất cả containers
docker-compose -f docker/docker-compose.yml down

# Xóa volumes (xóa toàn bộ data cũ)
docker volume prune -f

# Xóa target folder
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue

Bước 2: Khởi động lại Docker containers
powershell# Start containers
docker-compose -f docker/docker-compose.yml up -d

# Chờ 10 giây để containers khởi động
Start-Sleep -Seconds 10

# Verify containers đang chạy
docker ps
Kết quả mong đợi: 3 containers running (rabbitmq, mysql-raw, mysql-clean)

Bước 3: Import database schemas
powershell# Import Clean DB schema
Get-Content .\sql\clean_schema.sql | docker exec -i student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db

# Verify tables
docker exec -it student_mysql_clean mysql -uclean_user -pclean_pass -e "SHOW TABLES FROM student_clean_db;"

Bước 4: Compile code
powershellmvn clean compile

Bước 5: Generate messy CSV data (nếu chưa có)
powershellmvn exec:java -Dexec.mainClass="com.student.integration.generator.MessyDataGenerator"
Output: ./data/generated/students_messy_20k.csv

Bước 6: Chạy Full Pipeline
powershellmvn exec:java -Dexec.mainClass="com.student.integration.FullPipelineTest"
Chờ 3-5 phút để hoàn thành.

Bước 7: Verify kết quả
powershell# Check students count
docker exec -it student_mysql_clean mysql -uclean_user -pclean_pass -e "SELECT COUNT(*) FROM student_clean_db.students;"

# Check sample data
docker exec -it student_mysql_clean mysql -uclean_user -pclean_pass -e "SELECT student_id, full_name, email, gpa FROM student_clean_db.students LIMIT 10;"




====================================================================
#XUAT FILE DU LIEU SACH CUOI CUNG
# Tạo folder exports
mkdir exports -ErrorAction SilentlyContinue

# Export students
docker exec student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db -e "SELECT student_id, full_name, date_of_birth, gender, email, phone_number, address, class_id, enrollment_year, gpa, credits_completed, status FROM students;" | Out-File -Encoding UTF8 ./exports/clean_students.csv

# Export classes
docker exec student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db -e "SELECT * FROM classes;" | Out-File -Encoding UTF8 ./exports/classes.csv

# Export statistics
docker exec student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db -e "SELECT c.class_code, COUNT(s.id) as total, AVG(s.gpa) as avg_gpa FROM classes c LEFT JOIN students s ON c.id = s.class_id GROUP BY c.id;" | Out-File -Encoding UTF8 ./exports/statistics.csv