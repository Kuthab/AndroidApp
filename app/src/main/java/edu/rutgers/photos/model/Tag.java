package edu.rutgers.photos.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        PERSON, LOCATION;

        public String label() {
            switch (this) {
                case PERSON: return "person";
                case LOCATION: return "location";
                default: return name().toLowerCase(Locale.ROOT);
            }
        }
    }

    private final Type type;
    private final String value;

    public Tag(Type type, String value) {
        if (type == null) throw new IllegalArgumentException("type");
        if (value == null) throw new IllegalArgumentException("value");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("empty");
        this.type = type;
        this.value = trimmed;
    }

    public Type getType() { return type; }

    public String getValue() { return value; }

    public boolean matchesValue(String query) {
        if (query == null) return false;
        return value.toLowerCase(Locale.ROOT)
                .startsWith(query.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag t = (Tag) o;
        return type == t.type
                && value.toLowerCase(Locale.ROOT).equals(t.value.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return type.label() + ": " + value;
    }
}
