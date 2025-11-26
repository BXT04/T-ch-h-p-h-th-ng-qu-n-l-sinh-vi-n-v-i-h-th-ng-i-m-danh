#!/bin/bash

# Export students
docker exec student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db \
  -e "SELECT student_id, full_name, date_of_birth, gender, email, phone_number, address, class_id, enrollment_year, gpa, credits_completed, status, created_at FROM students;" \
  > ./exports/clean_students.csv

# Export classes
docker exec student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db \
  -e "SELECT * FROM classes;" \
  > ./exports/classes.csv

# Export statistics
docker exec student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db \
  -e "SELECT c.class_code, COUNT(s.id) as total, AVG(s.gpa) as avg_gpa FROM classes c LEFT JOIN students s ON c.id = s.class_id GROUP BY c.id;" \
  > ./exports/statistics.csv

echo "✅ Export completed! Check ./exports/ folder"


