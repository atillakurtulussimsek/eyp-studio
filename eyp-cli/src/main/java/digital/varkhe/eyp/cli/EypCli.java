package digital.varkhe.eyp.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/** Entry point. Exit codes: 0 ok, 1 validation errors, 2 usage or I/O error. */
@Command(name = "eyp", mixinStandardHelpOptions = true, version = "EYP Oluşturucu 0.1.0",
        description = "e-Yazışma Paketi (EYP) oluşturma, inceleme ve doğrulama aracı.",
        subcommands = {CreateCommand.class, InspectCommand.class, ValidateCommand.class})
public final class EypCli {

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String... args) {
        return new CommandLine(new EypCli()).setCaseInsensitiveEnumValuesAllowed(true).execute(args);
    }
}
