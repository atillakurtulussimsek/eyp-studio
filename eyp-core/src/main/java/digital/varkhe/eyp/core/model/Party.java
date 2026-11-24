package digital.varkhe.eyp.core.model;

import java.util.Objects;

/** One of CT_KurumKurulus, CT_TuzelSahis, CT_GercekSahis. */
public sealed interface Party {

    /** CT_KurumKurulus. {@code kkk} is the 14 hex char institution code (ST_KKK). */
    record PublicInstitution(String kkk, String name, ContactInfo contact, String unitKkk) implements Party {
        public PublicInstitution {
            Objects.requireNonNull(kkk, "kkk");
        }
    }

    /** CT_TuzelSahis. */
    record LegalEntity(Identifier id, String name, ContactInfo contact) implements Party {
        public LegalEntity {
            Objects.requireNonNull(id, "id");
        }
    }

    /** CT_GercekSahis. */
    record NaturalPerson(Person person, String tckn, String duty, ContactInfo contact) implements Party {
        public NaturalPerson {
            Objects.requireNonNull(person, "person");
        }
    }
}
