package ch.invoiceforge.core.model;

import java.util.Objects;

public class Address {
    private String street;
    private String postalCode;
    private String city;
    private String country;

    public Address() {
    }

    public Address(String street, String postalCode, String city) {
        this(street, postalCode, city, null);
    }

    public Address(String street, String postalCode, String city, String country) {
        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public Address setStreet(String street) {
        this.street = street;
        return this;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Address setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public String getCity() {
        return city;
    }

    public Address setCity(String city) {
        this.city = city;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public Address setCountry(String country) {
        this.country = country;
        return this;
    }

    public String formatSingleLine() {
        String locality = Objects.toString(postalCode, "") + (city == null ? "" : " " + city);
        String result = Objects.toString(street, "");
        if (!locality.isBlank()) {
            result = result.isBlank() ? locality.trim() : result + ", " + locality.trim();
        }
        if (country != null && !country.isBlank()) {
            result = result.isBlank() ? country : result + ", " + country;
        }
        return result;
    }
}
