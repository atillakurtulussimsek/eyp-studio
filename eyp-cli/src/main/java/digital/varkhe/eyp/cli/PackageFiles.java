package digital.varkhe.eyp.cli;

import digital.varkhe.eyp.core.opc.OpcPackage;
import digital.varkhe.eyp.core.opc.OpcReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

final class PackageFiles {
    static OpcPackage open(Path file) throws IOException, OpcReader.InvalidPackageException {
        try (InputStream in = Files.newInputStream(file)) {
            return OpcReader.read(in);
        }
    }

    private PackageFiles() {
    }
}
