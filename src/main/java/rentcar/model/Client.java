package rentcar.model;

public class Client {
    private int id;
    private String fullName;
    private String passport;
    private String phone;

    public Client(int id, String fullName, String passport, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.passport = passport;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPassport() { return passport; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return String.format("ID=%d | %s | паспорт %s | тел. %s",
                id, fullName, passport, phone);
    }
}
