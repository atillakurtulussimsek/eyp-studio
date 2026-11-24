package digital.varkhe.eyp.core.model;

/** CT_IletisimBilgisi. All fields optional. */
public record ContactInfo(String phone, String otherPhone, String email, String kepAddress, String fax,
                          String webAddress, String address, String city, String district, String country) {
}
