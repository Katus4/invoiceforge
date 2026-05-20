package ch.invoiceforge.core.model;

public class Customer {
    private String name;
    private Address address;
    private String email;
    private String phone;

    public Customer() {
    }

    public Customer(String name, String street, String cityLine) {
        this.name = name;
        this.address = parseAddress(street, cityLine);
    }

    public Customer(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public Customer setName(String name) {
        this.name = name;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public Customer setAddress(Address address) {
        this.address = address;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Customer setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Customer setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    private static Address parseAddress(String street, String cityLine) {
        if (cityLine == null || cityLine.isBlank()) {
            return new Address().setStreet(street);
        }
        String[] parts = cityLine.trim().split("\\s+", 2);
        if (parts.length == 2 && parts[0].matches("\\d{4,6}")) {
            return new Address(street, parts[0], parts[1]);
        }
        return new Address().setStreet(street).setCity(cityLine);
    }
}
