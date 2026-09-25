package domain;

import java.util.Objects;

public final class Address {
    private final String district;
    private final String detailLine;

    public Address(String district, String detailLine) {
        if (district == null || district.isBlank()) throw new IllegalArgumentException("District cannot be empty");
        if (detailLine == null || detailLine.isBlank()) throw new IllegalArgumentException("Detail line cannot be empty");
        this.district = district;
        this.detailLine = detailLine;
    }

    public String getDistrict() { return district; }
    public String getDetailLine() { return detailLine; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return district.equalsIgnoreCase(address.district) && detailLine.equalsIgnoreCase(address.detailLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(district.toLowerCase(), detailLine.toLowerCase());
    }

    @Override
    public String toString() {
        return detailLine + ", " + district;
    }
}