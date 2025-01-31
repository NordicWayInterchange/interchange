package no.vegvesen.ixn.federation.service.commands;


import picocli.CommandLine;

@CommandLine.Command(
        name = "importexportclient",
        showAtFileInUsageHelp = true,
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        subcommands = {
                Import.class,
                Export.class
        },
        mixinStandardHelpOptions = true
)
public class ImportExportCommand {
}
