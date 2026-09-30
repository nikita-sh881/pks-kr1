package rentcar;

import rentcar.exception.BusinessException;
import rentcar.exception.EntityNotFoundException;
import rentcar.model.Car;
import rentcar.model.CarStatus;
import rentcar.model.Client;
import rentcar.model.Rental;
import rentcar.model.RentalStatus;
import rentcar.service.RentCarService;
import rentcar.util.ExcelExporter;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner SC = new Scanner(System.in);
    private static final RentCarService service = new RentCarService();

    public static void main(String[] args) {
        try {
            DatabaseManager.initializeDatabase();
        } catch (BusinessException e) {
            System.out.println("Не удалось инициализировать БД: " + e.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Выберите действие: ");
            try {
                switch (choice) {
                    case 1 -> clientsMenu();
                    case 2 -> carsMenu();
                    case 3 -> rentalsMenu();
                    case 4 -> searchMenu();
                    case 5 -> filterMenu();
                    case 6 -> sortMenu();
                    case 7 -> service.printStatistics();
                    case 8 -> exportMenu();
                    case 9 -> showAllTables();
                    case 0 -> running = false;
                    default -> System.out.println("Нет такого пункта.");
                }
            } catch (EntityNotFoundException | BusinessException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        System.out.println("Выход. До свидания!");
    }

    private static void printMainMenu() {
        System.out.println("\n=========== ПРОКАТ АВТОМОБИЛЕЙ ===========");
        System.out.println("1. Клиенты");
        System.out.println("2. Автомобили");
        System.out.println("3. Аренды");
        System.out.println("4. Поиск");
        System.out.println("5. Фильтрация");
        System.out.println("6. Сортировка");
        System.out.println("7. Статистика");
        System.out.println("8. Экспорт данных");
        System.out.println("9. Показать таблицы БД");
        System.out.println("0. Выход");
        System.out.println("==========================================");
    }

    private static void clientsMenu() {
        System.out.println("\n--- КЛИЕНТЫ ---");
        System.out.println("1. Список");
        System.out.println("2. Найти по ID");
        System.out.println("3. Добавить");
        System.out.println("4. Удалить");
        System.out.println("0. Назад");
        int c = readInt("> ");
        switch (c) {
            case 1 -> service.getAllClients().forEach(System.out::println);
            case 2 -> System.out.println(service.findClientById(readInt("ID: ")));
            case 3 -> {
                String name = readLine("ФИО: ");
                String passport = readLine("Паспорт: ");
                String phone = readLine("Телефон: ");
                service.addClient(name, passport, phone);
                System.out.println("Клиент добавлен.");
            }
            case 4 -> {
                service.deleteClient(readInt("ID: "));
                System.out.println("Удалено.");
            }
            case 0 -> { }
            default -> System.out.println("Нет такого пункта.");
        }
    }

    private static void carsMenu() {
        System.out.println("\n--- АВТОМОБИЛИ ---");
        System.out.println("1. Список");
        System.out.println("2. Найти по ID");
        System.out.println("3. Добавить");
        System.out.println("4. Изменить статус");
        System.out.println("5. Удалить");
        System.out.println("0. Назад");
        int c = readInt("> ");
        switch (c) {
            case 1 -> service.getAllCars().forEach(System.out::println);
            case 2 -> System.out.println(service.findCarById(readInt("ID: ")));
            case 3 -> {
                String brand = readLine("Марка: ");
                String model = readLine("Модель: ");
                String plate = readLine("Госномер: ");
                int year = readInt("Год: ");
                double price = readDouble("Цена за сутки: ");
                CarStatus status = readCarStatus();
                service.addCar(brand, model, plate, year, price, status);
                System.out.println("Автомобиль добавлен.");
            }
            case 4 -> {
                int id = readInt("ID: ");
                CarStatus status = readCarStatus();
                service.updateCarStatus(id, status);
                System.out.println("Статус обновлён.");
            }
            case 5 -> {
                service.deleteCar(readInt("ID: "));
                System.out.println("Удалено.");
            }
            case 0 -> { }
            default -> System.out.println("Нет такого пункта.");
        }
    }

    private static void rentalsMenu() {
        System.out.println("\n--- АРЕНДЫ ---");
        System.out.println("1. Список");
        System.out.println("2. Найти по ID");
        System.out.println("3. Создать");
        System.out.println("4. Завершить");
        System.out.println("5. Удалить");
        System.out.println("0. Назад");
        int c = readInt("> ");
        switch (c) {
            case 1 -> service.getAllRentals().forEach(System.out::println);
            case 2 -> System.out.println(service.findRentalById(readInt("ID: ")));
            case 3 -> {
                int clientId = readInt("ID клиента: ");
                int carId = readInt("ID автомобиля: ");
                LocalDate start = readDate("Дата начала (YYYY-MM-DD): ");
                LocalDate end = readDate("Дата окончания (YYYY-MM-DD): ");
                service.createRental(clientId, carId, start, end);
                System.out.println("Аренда создана.");
            }
            case 4 -> {
                service.completeRental(readInt("ID аренды: "));
                System.out.println("Аренда завершена.");
            }
            case 5 -> {
                service.deleteRental(readInt("ID: "));
                System.out.println("Удалено.");
            }
            case 0 -> { }
            default -> System.out.println("Нет такого пункта.");
        }
    }

    private static void searchMenu() {
        System.out.println("\n--- ПОИСК ---");
        System.out.println("1. Клиенты по ФИО");
        System.out.println("2. Автомобили по марке");
        System.out.println("0. Назад");
        int c = readInt("> ");
        switch (c) {
            case 1 -> {
                String q = readLine("Часть ФИО: ");
                List<Client> res = service.searchClientsByName(q);
                if (res.isEmpty()) System.out.println("Ничего не найдено.");
                else res.forEach(System.out::println);
            }
            case 2 -> {
                String q = readLine("Часть марки: ");
                List<Car> res = service.searchCarsByBrand(q);
                if (res.isEmpty()) System.out.println("Ничего не найдено.");
                else res.forEach(System.out::println);
            }
            case 0 -> { }
            default -> System.out.println("Нет такого пункта.");
        }
    }

    private static void filterMenu() {
        System.out.println("\n--- ФИЛЬТРАЦИЯ ---");
        System.out.println("1. Автомобили по статусу");
        System.out.println("2. Аренды по статусу");
        System.out.println("0. Назад");
        int c = readInt("> ");
        switch (c) {
            case 1 -> {
                CarStatus s = readCarStatus();
                service.filterCarsByStatus(s).forEach(System.out::println);
            }
            case 2 -> {
                RentalStatus s = readRentalStatus();
                service.filterRentalsByStatus(s).forEach(System.out::println);
            }
            case 0 -> { }
            default -> System.out.println("Нет такого пункта.");
        }
    }

    private static void sortMenu() {
        System.out.println("\n--- СОРТИРОВКА ---");
        System.out.println("1. Автомобили: цена ↑");
        System.out.println("2. Автомобили: цена ↓");
        System.out.println("3. Аренды по дате начала");
        System.out.println("0. Назад");
        int c = readInt("> ");
        switch (c) {
            case 1 -> service.sortCarsByPrice(true).forEach(System.out::println);
            case 2 -> service.sortCarsByPrice(false).forEach(System.out::println);
            case 3 -> service.sortRentalsByStartDate().forEach(System.out::println);
            case 0 -> { }
            default -> System.out.println("Нет такого пункта.");
        }
    }

    private static void showAllTables() {
        System.out.println("\n>>> CLIENTS");
        service.getAllClients().forEach(System.out::println);
        System.out.println("\n>>> CARS");
        service.getAllCars().forEach(System.out::println);
        System.out.println("\n>>> RENTALS");
        service.getAllRentals().forEach(System.out::println);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SC.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно целое число.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SC.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно число.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SC.nextLine().trim();
            try {
                return LocalDate.parse(line);
            } catch (Exception e) {
                System.out.println("Ошибка: формат даты — YYYY-MM-DD.");
            }
        }
    }

    private static CarStatus readCarStatus() {
        while (true) {
            System.out.print("Статус (AVAILABLE/RENTED/MAINTENANCE): ");
            String line = SC.nextLine().trim().toUpperCase();
            try {
                return CarStatus.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: такого статуса нет.");
            }
        }
    }

    private static void exportMenu() {
        System.out.println("\n--- ЭКСПОРТ ДАННЫХ ---");
        System.out.println("1. Клиенты в clients.xlsx");
        System.out.println("2. Автомобили в cars.xlsx");
        System.out.println("3. Аренды в rentals.xlsx");
        System.out.println("0. Назад");
        int c = readInt("> ");
        switch (c) {
            case 1 -> ExcelExporter.exportClients(service.getAllClients(), "clients.xlsx");
            case 2 -> ExcelExporter.exportCars(service.getAllCars(), "cars.xlsx");
            case 3 -> ExcelExporter.exportRentals(service.getAllRentals(), "rentals.xlsx");
            case 0 -> { }
            default -> System.out.println("Нет такого пункта.");
        }
    }
    
    private static RentalStatus readRentalStatus() {
        while (true) {
            System.out.print("Статус (CREATED/ACTIVE/COMPLETED/CANCELLED): ");
            String line = SC.nextLine().trim().toUpperCase();
            try {
                return RentalStatus.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: такого статуса нет.");
            }
        }
    }
    
}
