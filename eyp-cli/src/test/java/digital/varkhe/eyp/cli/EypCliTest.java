package digital.varkhe.eyp.cli;

import digital.varkhe.eyp.core.TestPackages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EypCliTest {

    private static final String JSON = """
            {
              "konu": "Sözleşme hk.",
              "guvenlikKodu": "YOK",
              "olusturan": {"tuzelSahis": {"id": "1234567890", "idTuru": "VKN", "adi": "Örnek Yazılım A.Ş.",
                            "iletisim": {"kepAdresi": "ornek@hs01.kep.tr"}}},
              "dagitimlar": [{"kurumKurulus": {"kkk": "ABCDEF01234567", "adi": "Örnek Bakanlığı"},
                              "ivedilik": "NRM", "dagitimTuru": "GRG"}],
              "ustYazi": "Üst Yazı.pdf",
              "ekler": [{"dosya": "ek 1.pdf", "ad": "Fatura listesi"},
                        {"dosya": "bilgi.pdf", "ad": "Bilgi notu", "imzali": false}],
              "ilgiler": [{"etiket": "a", "belgeNo": "E-12345", "tarih": "2026-10-01"}],
              "sdp": [{"kod": "045.02", "ad": "Sözleşmeler"}],
              "dogrulamaAdresi": "https://dogrulama.example.com/"
            }
            """;

    private record Result(int code, String out) {
    }

    private static Result run(String... args) {
        PrintStream old = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true, StandardCharsets.UTF_8));
        try {
            return new Result(EypCli.run(args), buf.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    void createInspectValidateRoundTrip(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("belge.json"), JSON);
        Files.write(dir.resolve("Üst Yazı.pdf"), TestPackages.COVER_LETTER);
        Files.write(dir.resolve("ek 1.pdf"), TestPackages.EK1);
        Files.write(dir.resolve("bilgi.pdf"), TestPackages.EK2);

        Result create = run("olustur", dir.resolve("belge.json").toString(), "-o", dir.resolve("out").toString());
        assertEquals(0, create.code(), create.out());
        List<Path> eyps;
        try (var s = Files.list(dir.resolve("out"))) {
            eyps = s.filter(p -> p.toString().endsWith(".eyp")).toList();
        }
        assertEquals(1, eyps.size());
        assertTrue(eyps.getFirst().getFileName().toString().matches("[0-9A-F-]{36}\\.eyp"));

        Result validate = run("dogrula", eyps.getFirst().toString());
        assertEquals(0, validate.code(), validate.out());
        assertTrue(validate.out().contains("EYP-IMZA-YOK"), validate.out());

        Result inspect = run("incele", eyps.getFirst().toString(), "-x", dir.resolve("x").toString());
        assertEquals(0, inspect.code(), inspect.out());
        assertTrue(inspect.out().contains("/UstYazi/UstYazi.pdf"), inspect.out());
        assertTrue(Files.exists(dir.resolve("x/UstYazi.pdf")));
        assertTrue(Files.exists(dir.resolve("x/ek1.pdf")));
        assertTrue(Files.exists(dir.resolve("x/bilgi.pdf")));
    }

    @Test
    void missingRequiredFieldIsReported(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("belge.json"), JSON.replace("\"dogrulamaAdresi\": \"https://dogrulama.example.com/\"", "\"x\": 1"));
        Files.write(dir.resolve("Üst Yazı.pdf"), TestPackages.COVER_LETTER);
        Files.write(dir.resolve("ek 1.pdf"), TestPackages.EK1);
        Files.write(dir.resolve("bilgi.pdf"), TestPackages.EK2);
        assertEquals(2, run("olustur", dir.resolve("belge.json").toString()).code());
    }

    @Test
    void legalEntityDefaultsToTenDigitVkn(@TempDir Path dir) throws Exception {
        Files.write(dir.resolve("Üst Yazı.pdf"), TestPackages.COVER_LETTER);
        Files.write(dir.resolve("ek 1.pdf"), TestPackages.EK1);
        Files.write(dir.resolve("bilgi.pdf"), TestPackages.EK2);
        Files.writeString(dir.resolve("belge.json"), JSON.replace("\"idTuru\": \"VKN\", ", "").replace("1234567890", "12345"));
        assertEquals(2, run("olustur", dir.resolve("belge.json").toString()).code());
        Files.writeString(dir.resolve("belge.json"), JSON.replace("\"idTuru\": \"VKN\", ", ""));
        assertEquals(0, run("olustur", dir.resolve("belge.json").toString(), "-o", dir.resolve("out").toString()).code());
        try (var s = Files.list(dir.resolve("out"))) {
            Path eyp = s.findFirst().orElseThrow();
            try (var z = new java.util.zip.ZipFile(eyp.toFile())) {
                String ustveri = new String(z.getInputStream(z.getEntry("Ustveri/Ustveri.xml")).readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(ustveri.contains("schemeID=\"VKN\">1234567890<"), ustveri);
            }
        }
    }

    @Test
    void invalidPackageExitCode(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("bad.eyp"), "not a zip");
        assertEquals(2, run("dogrula", dir.resolve("bad.eyp").toString()).code());
    }
}
