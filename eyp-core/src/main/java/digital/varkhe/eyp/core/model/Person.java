package digital.varkhe.eyp.core.model;

import java.util.Objects;

/** CT_Kisi. */
public record Person(String firstName, String lastName, String middleName, String title, String prefix) {
    public Person {
        Objects.requireNonNull(firstName, "firstName");
        Objects.requireNonNull(lastName, "lastName");
    }

    public static Person of(String firstName, String lastName) {
        return new Person(firstName, lastName, null, null, null);
    }
}
