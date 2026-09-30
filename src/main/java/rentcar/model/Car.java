package rentcar.model;

public class Car {
    private int id;
    private String brand;
    private String model;
    private String licensePlate;
    private int year;
    private double pricePerDay;
    private CarStatus status;

    public Car(int id, String brand, String model, String licensePlate,
               int year, double pricePerDay, CarStatus status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.licensePlate = licensePlate;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.status = status;
    }

    public int getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getLicensePlate() { return licensePlate; }
    public int getYear() { return year; }
    public double getPricePerDay() { return pricePerDay; }
    public CarStatus getStatus() { return status; }

    public void setStatus(CarStatus status) { this.status = status; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    @Override
    public String toString() {
        return String.format("ID=%d | %s %s | %s | %d | %.2f руб/сутки | %s",
                id, brand, model, licensePlate, year, pricePerDay, status);
    }
}
