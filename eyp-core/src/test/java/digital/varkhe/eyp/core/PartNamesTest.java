package digital.varkhe.eyp.core;

import digital.varkhe.eyp.core.opc.PartNames;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartNamesTest {

    @Test
    void foldsTurkishLettersIndependentOfLocale() {
        assertEquals("UstYaziIIsiGuc.pdf", PartNames.sanitizeFileName("Üst Yazı İIşı Güç.pdf"));
        assertEquals("ek_1-son.PDF", PartNames.sanitizeFileName("ek_1-son.PDF"));
    }

    @Test
    void rejectsNamesWithoutExtension() {
        assertThrows(IllegalArgumentException.class, () -> PartNames.sanitizeFileName("ğüş"));
        assertThrows(IllegalArgumentException.class, () -> PartNames.sanitizeFileName("../.."));
    }

    @Test
    void relativizeAndNormalize() {
        assertEquals("../Imzalar/ImzaCades.imz", PartNames.relativize("/PaketOzeti/PaketOzeti.xml", "/Imzalar/ImzaCades.imz"));
        assertEquals("/Imzalar/ImzaCades.imz", PartNames.normalize("/PaketOzeti/../Imzalar/ImzaCades.imz"));
        assertThrows(IllegalArgumentException.class, () -> PartNames.normalize("/../x"));
    }

    @Test
    void partKeyIsLocaleIndependent() {
        Locale old = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("/ustveri/ustveri.xml", PartNames.key("/USTVERI/USTVERI.XML"));
        } finally {
            Locale.setDefault(old);
        }
    }
}
