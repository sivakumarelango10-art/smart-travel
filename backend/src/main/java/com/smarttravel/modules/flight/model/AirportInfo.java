package com.smarttravel.modules.flight.model;

import java.util.Objects;

/**
 * Embedded airport details for departure and arrival locations.
 */
public class AirportInfo {

    private String code;
    private String name;
    private String city;
    private String country;
    private String terminal;
    private String gate;

    public AirportInfo() {
    }

    public AirportInfo(String code, String name, String city, String country, String terminal, String gate) {
        this.code = code != null ? code.toUpperCase().trim() : null;
        this.name = name;
        this.city = city;
        this.country = country;
        this.terminal = terminal;
        this.gate = gate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String name;
        private String city;
        private String country;
        private String terminal;
        private String gate;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder terminal(String terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder gate(String gate) {
            this.gate = gate;
            return this;
        }

        public AirportInfo build() {
            return new AirportInfo(code, name, city, country, terminal, gate);
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.toUpperCase().trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirportInfo that = (AirportInfo) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(name, that.name) &&
                Objects.equals(city, that.city) &&
                Objects.equals(country, that.country) &&
                Objects.equals(terminal, that.terminal) &&
                Objects.equals(gate, that.gate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, city, country, terminal, gate);
    }

    @Override
    public String toString() {
        return "AirportInfo{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", terminal='" + terminal + '\'' +
                ", gate='" + gate + '\'' +
                '}';
    }
}
