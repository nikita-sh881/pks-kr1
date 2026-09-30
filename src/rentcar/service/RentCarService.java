package rentcar.service;

import rentcar.DatabaseManager;
import rentcar.exception.BusinessException;
import rentcar.exception.EntityNotFoundException;
import rentcar.model.Car;
import rentcar.model.CarStatus;
import rentcar.model.Client;
import rentcar.model.Rental;
import rentcar.model.RentalStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RentCarService {

    public List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Client(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("passport"),
                        rs.getString("phone")));
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка чтения клиентов: " + e.getMessage());
        }
        return list;
    }

    public void addClient(String fullName, String passport, String phone) {
        if (fullName == null || fullName.isBlank()) {
            throw new BusinessException("ФИО клиента обязательно.");
        }
        if (passport == null || passport.isBlank()) {
            throw new BusinessException("Паспорт обязателен.");
        }
        String sql = "INSERT INTO clients (full_name, passport, phone) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, passport);
            ps.setString(3, phone);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось добавить клиента: " + e.getMessage());
        }
    }

    public Client findClientById(int id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Client(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("passport"),
                            rs.getString("phone"));
                }
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка поиска клиента: " + e.getMessage());
        }
        throw new EntityNotFoundException("Клиент с ID " + id + " не найден.");
    }

    public void deleteClient(int id) {
        findClientById(id);
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось удалить клиента: " + e.getMessage());
        }
    }

    // ==================== АВТОМОБИЛИ ====================

    public List<Car> getAllCars() {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT * FROM cars ORDER BY id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCar(rs));
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка чтения автомобилей: " + e.getMessage());
        }
        return list;
    }

    public Car findCarById(int id) {
        String sql = "SELECT * FROM cars WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCar(rs);
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка поиска автомобиля: " + e.getMessage());
        }
        throw new EntityNotFoundException("Автомобиль с ID " + id + " не найден.");
    }

    public void addCar(String brand, String model, String plate,
                       int year, double price, CarStatus status) {
        if (brand == null || brand.isBlank()) {
            throw new BusinessException("Марка обязательна.");
        }
        if (price <= 0) {
            throw new BusinessException("Цена за сутки должна быть положительной.");
        }
        String sql = """
                INSERT INTO cars (brand, model, license_plate, year, price_per_day, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, brand);
            ps.setString(2, model);
            ps.setString(3, plate);
            ps.setInt(4, year);
            ps.setDouble(5, price);
            ps.setString(6, status.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось добавить автомобиль: " + e.getMessage());
        }
    }

    public void updateCarStatus(int id, CarStatus newStatus) {
        Car car = findCarById(id);
        if (car.getStatus() == CarStatus.RENTED && newStatus == CarStatus.MAINTENANCE) {
            throw new BusinessException("Нельзя отправить на ТО автомобиль, который сейчас в аренде.");
        }
        String sql = "UPDATE cars SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось обновить статус: " + e.getMessage());
        }
    }

    public void deleteCar(int id) {
        findCarById(id);
        String sql = "DELETE FROM cars WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось удалить автомобиль: " + e.getMessage());
        }
    }

    private Car mapCar(ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("id"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getString("license_plate"),
                rs.getInt("year"),
                rs.getDouble("price_per_day"),
                CarStatus.valueOf(rs.getString("status")));
    }

    public List<Rental> getAllRentals() {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT * FROM rentals ORDER BY id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRental(rs));
        } catch (SQLException e) {
            throw new BusinessException("Ошибка чтения аренд: " + e.getMessage());
        }
        return list;
    }

    public Rental findRentalById(int id) {
        String sql = "SELECT * FROM rentals WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRental(rs);
            }
        } catch (SQLException e) {
            throw new BusinessException("Ошибка поиска аренды: " + e.getMessage());
        }
        throw new EntityNotFoundException("Аренда с ID " + id + " не найдена.");
    }

    public void createRental(int clientId, int carId, LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new BusinessException("Дата окончания не может быть раньше даты начала.");
        }
        Client client = findClientById(clientId);
        Car car = findCarById(carId);

        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new BusinessException("Автомобиль недоступен (текущий статус: "
                    + car.getStatus() + ").");
        }

        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) days = 1;
        double total = days * car.getPricePerDay();

        String insert = """
                INSERT INTO rentals (client_id, car_id, start_date, end_date, total_price, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String updateCar = "UPDATE cars SET status = 'RENTED' WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insert);
                 PreparedStatement up = conn.prepareStatement(updateCar)) {

                ps.setInt(1, client.getId());
                ps.setInt(2, car.getId());
                ps.setDate(3, Date.valueOf(start));
                ps.setDate(4, Date.valueOf(end));
                ps.setDouble(5, total);
                ps.setString(6, RentalStatus.ACTIVE.name());
                ps.executeUpdate();

                up.setInt(1, car.getId());
                up.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("Не удалось создать аренду: " + e.getMessage());
        }
    }

    public void completeRental(int rentalId) {
        Rental rental = findRentalById(rentalId);
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new BusinessException("Завершить можно только активную аренду.");
        }
        String updateRental = "UPDATE rentals SET status = 'COMPLETED' WHERE id = ?";
        String updateCar = "UPDATE cars SET status = 'AVAILABLE' WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(updateRental);
                 PreparedStatement up = conn.prepareStatement(updateCar)) {
                ps.setInt(1, rentalId);
                ps.executeUpdate();
                up.setInt(1, rental.getCarId());
                up.executeUpdate();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("Не удалось завершить аренду: " + e.getMessage());
        }
    }

    public void deleteRental(int id) {
        findRentalById(id);
        String sql = "DELETE FROM rentals WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException("Не удалось удалить аренду: " + e.getMessage());
        }
    }

    private Rental mapRental(ResultSet rs) throws SQLException {
        return new Rental(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getInt("car_id"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getDouble("total_price"),
                RentalStatus.valueOf(rs.getString("status")));
    }

    public List<Car> searchCarsByBrand(String query) {
        return getAllCars().stream()
                .filter(c -> c.getBrand().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public List<Client> searchClientsByName(String query) {
        return getAllClients().stream()
                .filter(c -> c.getFullName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public List<Car> filterCarsByStatus(CarStatus status) {
        return getAllCars().stream()
                .filter(c -> c.getStatus() == status)
                .toList();
    }

    public List<Rental> filterRentalsByStatus(RentalStatus status) {
        return getAllRentals().stream()
                .filter(r -> r.getStatus() == status)
                .toList();
    }

    public List<Car> sortCarsByPrice(boolean ascending) {
        Comparator<Car> cmp = Comparator.comparingDouble(Car::getPricePerDay);
        if (!ascending) cmp = cmp.reversed();
        return getAllCars().stream().sorted(cmp).toList();
    }

    public List<Rental> sortRentalsByStartDate() {
        return getAllRentals().stream()
                .sorted(Comparator.comparing(Rental::getStartDate))
                .toList();
    }

    public void printStatistics() {
        List<Client> clients = getAllClients();
        List<Car> cars = getAllCars();
        List<Rental> rentals = getAllRentals();

        long activeRentals = rentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.ACTIVE).count();
        long availableCars = cars.stream()
                .filter(c -> c.getStatus() == CarStatus.AVAILABLE).count();
        double avgPrice = rentals.stream()
                .mapToDouble(Rental::getTotalPrice).average().orElse(0);
        double totalIncome = rentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED)
                .mapToDouble(Rental::getTotalPrice).sum();

        System.out.println("\n=========== СТАТИСТИКА ===========");
        System.out.println("Всего клиентов:          " + clients.size());
        System.out.println("Всего автомобилей:       " + cars.size());
        System.out.println("Всего аренд:             " + rentals.size());
        System.out.println("Активных аренд:          " + activeRentals);
        System.out.println("Доступных автомобилей:   " + availableCars);
        System.out.printf("Средняя стоимость аренды: %.2f руб%n", avgPrice);
        System.out.printf("Доход по завершённым:    %.2f руб%n", totalIncome);
        System.out.println("==================================");
    }
}
