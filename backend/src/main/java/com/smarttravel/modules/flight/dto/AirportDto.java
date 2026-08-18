package com.smarttravel.modules.flight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Airport location details")
public class AirportDto {

    @Schema(description = "3-letter IATA Airport code", example = "DEL")
    @NotBlank(message = "Airport code is required")
    @Size(min = 3, max = 3, message = "Airport code must be a 3-letter IATA code")
    private String code;

    @Schema(description = "Airport full name", example = "Indira Gandhi International Airport")
    @NotBlank(message = "Airport name is required")
    private String name;

    @Schema(description = "City name", example = "New Delhi")
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "Country name", example = "India")
    private String country;

    @Schema(description = "Terminal identifier", example = "T3")
    private String terminal;

    @Schema(description = "Gate identifier", example = "12A")
    private String gate;

    public AirportDto() {
    }

    public AirportDto(String code, String name, String city, String country, String terminal, String gate) {
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

        public AirportDto build() {
            return new AirportDto(code, name, city, country, terminal, gate);
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
}
