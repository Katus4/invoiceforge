package ch.invoiceforge.core.model;

public class Company {
    private String name;
    private Address address;
    private String email;
    private String phone;
    private String vatNumber;
    private String iban;
    private String paymentTerms;
    private String logoPath;

    public Company() {
    }

    public Company(String name, String street, String cityLine) {
        this.name = name;
        this.address = new Customer(name, street, cityLine).getAddress();
    }

    public Company(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public Company setName(String name) {
        this.name = name;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public Company setAddress(Address address) {
        this.address = address;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Company setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Company setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public Company setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
        return this;
    }

    public String getIban() {
        return iban;
    }

    public Company setIban(String iban) {
        this.iban = iban;
        return this;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public Company setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
        return this;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public Company setLogoPath(String logoPath) {
        this.logoPath = logoPath;
        return this;
    }
}
