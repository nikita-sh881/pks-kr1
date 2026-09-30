package rentcar;

import rentcar.exception.BusinessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String HOST = "localhost";
    private static final String PORT = "5432";
    private static final String ADMIN_DB = "postgres";
    private static final String APP_DB = "rentcar_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres"; 

    private static final String ADMIN_URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + ADMIN_DB;
    private static final String APP_URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + APP_DB;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new BusinessException(
                    "Не найден драйвер PostgreSQL.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(APP_URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        createDatabaseIfNotExists();
        createTables();
        seedDataIfEmpty();
    }

    private static void createDatabaseIfNotExists() {
        String checkSql = "SELECT 1 FROM pg_database WHERE datname = '" + APP_DB + "'";
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement st = conn.createStatement();
             var rs = st.executeQuery(checkSql)) {

            if (!rs.next()) {
                st.executeUpdate("CREATE DATABASE " + APP_DB);
                System.out.println("База данных " + APP_DB + " создана.");
            } else {
                System.out.println("База данных " + APP_DB + " уже существует.");
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка при создании базы данных: " + e.getMessage());
        }
    }

    private static void createTables() {
        String clientsTable = """
                CREATE TABLE IF NOT EXISTS clients (
                    id SERIAL PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    passport VARCHAR(20) NOT NULL UNIQUE,
                    phone VARCHAR(20) NOT NULL
                )
                """;

        String carsTable = """
                CREATE TABLE IF NOT EXISTS cars (
                    id SERIAL PRIMARY KEY,
                    brand VARCHAR(50) NOT NULL,
                    model VARCHAR(50) NOT NULL,
                    license_plate VARCHAR(20) NOT NULL UNIQUE,
                    year INT NOT NULL CHECK (year >= 1950),
                    price_per_day NUMERIC(10,2) NOT NULL CHECK (price_per_day > 0),
                    status VARCHAR(20) NOT NULL
                )
                """;

        String rentalsTable = """
                CREATE TABLE IF NOT EXISTS rentals (
                    id SERIAL PRIMARY KEY,
                    client_id INT NOT NULL REFERENCES clients(id),
                    car_id INT NOT NULL REFERENCES cars(id),
                    start_date DATE NOT NULL,
                    end_date DATE NOT NULL,
                    total_price NUMERIC(10,2) NOT NULL CHECK (total_price >= 0),
                    status VARCHAR(20) NOT NULL,
                    CHECK (end_date >= start_date)
                )
                """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute(clientsTable);
            st.execute(carsTable);
            st.execute(rentalsTable);
        } catch (SQLException e) {
            throw new BusinessException("Ошибка при создании таблиц: " + e.getMessage());
        }
    }

    private static void seedDataIfEmpty() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM clients")) {

            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }

            st.executeUpdate("""
                INSERT INTO clients (full_name, passport, phone) VALUES
                ('Иванов Иван Иванович', '4501 123456', '+7-900-111-11-11'),
                ('Петров Пётр Петрович', '4502 234567', '+7-900-222-22-22'),
                ('Сидорова Анна Сергеевна', '4503 345678', '+7-900-333-33-33'),
                ('Кузнецов Дмитрий Олегович', '4504 456789', '+7-900-444-44-44'),
                ('Смирнова Ольга Ивановна', '4505 567890', '+7-900-555-55-55')
                """);

            st.executeUpdate("""
                INSERT INTO cars (brand, model, license_plate, year, price_per_day, status) VALUES
                ('Toyota', 'Camry', 'A123BC77', 2020, 4500.00, 'AVAILABLE'),
                ('Kia', 'Rio', 'B456CD77', 2019, 2500.00, 'AVAILABLE'),
                ('BMW', 'X5', 'C789EF77', 2021, 8000.00, 'RENTED'),
                ('Hyundai', 'Solaris', 'D012GH77', 2018, 2200.00, 'AVAILABLE'),
                ('Mercedes', 'E200', 'E345IJ77', 2022, 9500.00, 'MAINTENANCE'),
                ('Lada', 'Vesta', 'F678KL77', 2021, 1800.00, 'AVAILABLE'),
                ('Volkswagen', 'Polo', 'G901MN77', 2020, 2400.00, 'RENTED'),
                ('Skoda', 'Octavia', 'H234OP77', 2021, 3200.00, 'AVAILABLE'),
                ('Nissan', 'Qashqai', 'I567QR77', 2019, 3800.00, 'AVAILABLE'),
                ('Audi', 'A6', 'J890ST77', 2022, 9000.00, 'AVAILABLE')
                """);

            st.executeUpdate("""
                INSERT INTO rentals (client_id, car_id, start_date, end_date, total_price, status) VALUES
                (1, 3, '2025-01-10', '2025-01-15', 40000.00, 'ACTIVE'),
                (2, 1, '2025-01-05', '2025-01-08', 13500.00, 'COMPLETED'),
                (3, 7, '2025-01-12', '2025-01-14', 4800.00, 'ACTIVE'),
                (4, 2, '2025-01-01', '2025-01-03', 5000.00, 'COMPLETED'),
                (5, 4, '2025-01-15', '2025-01-20', 11000.00, 'CREATED'),
                (1, 8, '2024-12-20', '2024-12-25', 16000.00, 'COMPLETED'),
                (2, 9, '2024-12-15', '2024-12-18', 11400.00, 'COMPLETED'),
                (3, 10, '2025-01-20', '2025-01-25', 45000.00, 'CREATED'),
                (4, 6, '2025-01-08', '2025-01-10', 3600.00, 'CANCELLED'),
                (5, 1, '2025-01-18', '2025-01-22', 18000.00, 'CREATED')
                """);

            System.out.println("Тестовые данные добавлены.");
        } catch (SQLException e) {
            throw new BusinessException("Ошибка при заполнении данных: " + e.getMessage());
        }
    }
}
