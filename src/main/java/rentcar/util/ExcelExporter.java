package rentcar.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import rentcar.exception.BusinessException;
import rentcar.model.Car;
import rentcar.model.Client;
import rentcar.model.Rental;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public static void exportClients(List<Client> clients, String path) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Clients");
            String[] headers = {"ID", "ФИО", "Паспорт", "Телефон"};
            createHeader(wb, sheet, headers);

            int rowIdx = 1;
            for (Client c : clients) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getFullName());
                row.createCell(2).setCellValue(c.getPassport());
                row.createCell(3).setCellValue(c.getPhone());
            }
            autoSize(sheet, headers.length);
            save(wb, path);
        } catch (IOException e) {
            throw new BusinessException("Ошибка экспорта клиентов: " + e.getMessage());
        }
    }

    public static void exportCars(List<Car> cars, String path) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Cars");
            String[] headers = {"ID", "Марка", "Модель", "Госномер", "Год", "Цена/сутки", "Статус"};
            createHeader(wb, sheet, headers);

            int rowIdx = 1;
            for (Car c : cars) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getBrand());
                row.createCell(2).setCellValue(c.getModel());
                row.createCell(3).setCellValue(c.getLicensePlate());
                row.createCell(4).setCellValue(c.getYear());
                row.createCell(5).setCellValue(c.getPricePerDay());
                row.createCell(6).setCellValue(c.getStatus().name());
            }
            autoSize(sheet, headers.length);
            save(wb, path);
        } catch (IOException e) {
            throw new BusinessException("Ошибка экспорта автомобилей: " + e.getMessage());
        }
    }

    public static void exportRentals(List<Rental> rentals, String path) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Rentals");
            String[] headers = {"ID", "Клиент ID", "Авто ID", "Дата начала",
                    "Дата окончания", "Сумма", "Статус"};
            createHeader(wb, sheet, headers);

            int rowIdx = 1;
            for (Rental r : rentals) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getClientId());
                row.createCell(2).setCellValue(r.getCarId());
                row.createCell(3).setCellValue(r.getStartDate().toString());
                row.createCell(4).setCellValue(r.getEndDate().toString());
                row.createCell(5).setCellValue(r.getTotalPrice());
                row.createCell(6).setCellValue(r.getStatus().name());
            }
            autoSize(sheet, headers.length);
            save(wb, path);
        } catch (IOException e) {
            throw new BusinessException("Ошибка экспорта аренд: " + e.getMessage());
        }
    }

    private static void createHeader(Workbook wb, Sheet sheet, String[] headers) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private static void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void save(Workbook wb, String path) throws IOException {
        try (FileOutputStream out = new FileOutputStream(path)) {
            wb.write(out);
        }
        System.out.println("Файл сохранён: " + path);
    }
}
