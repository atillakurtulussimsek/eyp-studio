package digital.varkhe.eyp.cli;

import digital.varkhe.eyp.core.opc.OpcPackage;
import digital.varkhe.eyp.core.opc.OpcReader;
import digital.varkhe.eyp.core.opc.Relationship;
import digital.varkhe.eyp.core.pkg.EypConstants;
import digital.varkhe.eyp.core.xml.SafeXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "incele", description = "Paketin bileşenlerini, ilişkilerini ve temel üstveri bilgisini listeler.")
final class InspectCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "PAKET", description = ".eyp dosyası")
    Path file;

    @Option(names = {"-x", "--disa-aktar"}, paramLabel = "KLASOR", description = "Üst yazı ve ekleri bu klasöre çıkarır.")
    Path exportDir;

    @Override
    public Integer call() throws Exception {
        OpcPackage pkg;
        try {
            pkg = PackageFiles.open(file);
        } catch (OpcReader.InvalidPackageException e) {
            System.out.println("Paket açılamadı: " + e.getMessage());
            return 2;
        }
        System.out.println("Bileşenler:");
        for (OpcPackage.Part p : pkg.parts()) {
            System.out.printf("  %-60s %10d  %s%n", p.name(), p.data().length, p.contentType());
        }
        System.out.println("İlişkiler:");
        for (Relationship r : pkg.relationships()) {
            System.out.printf("  %-28s %-22s -> %s%n", r.source(), r.id(), r.resolvedTarget());
        }
        pkg.part(EypConstants.PART_USTVERI).ifPresent(p -> {
            try {
                Document d = SafeXml.parse(p.data());
                System.out.println("Üstveri:");
                System.out.println("  BelgeId : " + text(d, "BelgeId"));
                System.out.println("  Konu    : " + text(d, "Konu"));
                System.out.println("  Güvenlik: " + text(d, "GuvenlikKodu"));
                System.out.println("  Dosya   : " + text(d, "DosyaAdi"));
            } catch (Exception e) {
                System.out.println("  Üstveri okunamadı: " + e.getMessage());
            }
        });
        boolean signed = !pkg.relationships(EypConstants.PART_PAKET_OZETI, EypConstants.REL_IMZA).isEmpty();
        boolean sealed = !pkg.relationships(EypConstants.PART_NIHAI_OZET, EypConstants.REL_MUHUR).isEmpty();
        System.out.println("İmza: " + (signed ? "VAR" : "YOK") + "   Mühür: " + (sealed ? "VAR" : "YOK"));

        if (exportDir != null) {
            Files.createDirectories(exportDir);
            for (String type : new String[]{EypConstants.REL_USTYAZI, EypConstants.REL_EK, EypConstants.REL_IMZASIZ_EK}) {
                for (Relationship r : pkg.relationships("/", type)) {
                    var part = pkg.part(r.resolvedTarget());
                    if (part.isEmpty()) continue;
                    String name = Path.of(part.get().name()).getFileName().toString();
                    Path out = exportDir.resolve(name).normalize();
                    if (!out.startsWith(exportDir.normalize())) continue;
                    Files.write(out, part.get().data());
                    System.out.println("Dışa aktarıldı: " + out);
                }
            }
        }
        return 0;
    }

    private static String text(Document d, String local) {
        NodeList nl = d.getElementsByTagNameNS(EypConstants.NS_TIPLER, local);
        return nl.getLength() == 0 ? "-" : ((Element) nl.item(0)).getTextContent().trim();
    }
}
