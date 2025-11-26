# Student Data Integration System

🎓 Hệ thống tích hợp dữ liệu sinh viên sử dụng Message Queue Architecture

## 📋 Mô tả

Đồ án xây dựng ETL Pipeline để xử lý và validate dữ liệu sinh viên từ file CSV, sử dụng RabbitMQ message broker và MySQL database. Hệ thống hỗ trợ xử lý realtime với khả năng tự động phát hiện và xử lý file mới.

## ✨ Tính năng chính

- ✅ **ETL Pipeline hoàn chỉnh** - Extract, Transform, Load
- ✅ **Validation Framework** - 7 quy tắc validation toàn diện
- ✅ **Realtime Processing** - File Watcher tự động xử lý file mới
- ✅ **Message Queue** - RabbitMQ cho xử lý bất đồng bộ
- ✅ **Error Handling** - Phân loại và log errors chi tiết
- ✅ **Batch Processing** - Xử lý hàng chục nghìn records

## 🏗️ Kiến trúc
```
CSV File → Producer → RabbitMQ → Validator → Transformer → MySQL
                         ↓
                    Error Queue
```

## 🛠️ Công nghệ

- **Backend:** Java 17
- **Message Broker:** RabbitMQ 3.12
- **Database:** MySQL 8.0
- **Build Tool:** Maven 3.9
- **Containerization:** Docker Compose
- **Libraries:** Jackson, Apache Commons CSV, HikariCP

## 📊 Kết quả

- **Throughput:** 100-150 messages/second
- **Dataset:** 20,000+ records
- **Validation Rules:** 7 rules
- **Processing Time:** ~5 minutes for 20,000 records
- **Success Rate:** 39.5% valid (by design with messy data)

## 🚀 Hướng dẫn chạy

### Yêu cầu

- Java JDK 17+
- Maven 3.8+
- Docker Desktop
- 8GB RAM

### Cài đặt

1. **Clone repository:**
```bash
git clone https://github.com/BXT04/T-ch-h-p-h-th-ng-qu-n-l-sinh-vi-n-v-i-h-th-ng-i-m-danh
cd student-integration-system
```

2. **Khởi động Docker containers:**
```bash
docker-compose -f docker/docker-compose.yml up -d
```

3. **Import database schema:**
```bash
# PowerShell
Get-Content .\sql\clean_schema.sql | docker exec -i student_mysql_clean mysql -uclean_user -pclean_pass student_clean_db
```

4. **Compile project:**
```bash
mvn clean compile
```

5. **Generate test data:**
```bash
mvn exec:java -Dexec.mainClass="com.student.integration.generator.MessyDataGenerator"
```

6. **Chạy Full Pipeline:**
```bash
mvn exec:java -Dexec.mainClass="com.student.integration.FullPipelineTest"
```

### Chạy Realtime File Watcher
```bash
mvn exec:java -Dexec.mainClass="com.student.integration.watcher.FileWatcherTest"
```

Sau đó drop CSV files vào `./data/input/` để tự động xử lý.

## 📁 Cấu trúc thư mục
```
student-integration-system/
├── docker/              # Docker Compose configuration
├── sql/                 # Database schemas
├── data/                # CSV files
├── src/main/java/       # Source code
│   ├── config/          # Configuration classes
│   ├── consumer/        # Message consumers
│   ├── producer/        # Message producers
│   ├── model/           # Data models
│   ├── validator/       # Validation framework
│   ├── transformer/     # Data transformation
│   ├── loader/          # Database loading
│   └── watcher/         # File watching
└── pom.xml              # Maven dependencies
```

## 🎨 Design Patterns

- **Chain of Responsibility** - Validation framework
- **Singleton** - Configuration management
- **Template Method** - Base consumer
- **Producer-Consumer** - Message processing

## 📊 Monitoring

- **RabbitMQ UI:** http://localhost:15672 (admin/admin123)
- **MySQL Clean DB:** localhost:3308 (clean_user/clean_pass)

## 📝 License

MIT License

## 👤 Tác giả

**Bùi Xuân Thức**
- Email: buixuanthuc2020@gmail.com
- GitHub: https://github.com/BXT04/T-ch-h-p-h-th-ng-qu-n-l-sinh-vi-n-v-i-h-th-ng-i-m-danh

## 🙏 Acknowledgments

- Đồ án tốt nghiệp - Đại học Duy Tân
- Giảng viên hướng dẫn: Phạm An Bình
