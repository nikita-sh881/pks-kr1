package rentcar.model;

import java.time.LocalDate;

public class Rental {
    private int id;
    private int clientId;
    private int carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;
    private RentalStatus status;

    public Rental(int id, int clientId, int carId, LocalDate startDate,
                  LocalDate endDate, double totalPrice, RentalStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public int getCarId() { return carId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalPrice() { return totalPrice; }
    public RentalStatus getStatus() { return status; }

    public void setStatus(RentalStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("ID=%d | клиент=%d | авто=%d | %s..%s | %.2f руб | %s",
                id, clientId, carId, startDate, endDate, totalPrice, status);
    }
}
