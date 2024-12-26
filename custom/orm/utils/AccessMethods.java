package custom.orm.utils;

public enum AccessMethods {
    GET("get"),
    SET("set");

    private final String value;

    // Constructor
    AccessMethods(String value) {
        this.value = value;
    }

    // Getter
    public String getValue() {
        return value;
    }
}
