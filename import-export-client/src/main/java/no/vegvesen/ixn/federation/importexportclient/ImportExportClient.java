package no.vegvesen.ixn.federation.importexportclient;

import no.vegvesen.ixn.federation.importexportclient.commands.Export;
import no.vegvesen.ixn.federation.importexportclient.commands.Import;
import picocli.CommandLine.*;

@Command(name = "importexportclient",
        description = "Import/export client",
        showAtFileInUsageHelp = true,
        defaultValueProvider = PropertiesDefaultProvider.class,
        subcommands = {
                Export.class,
                Import.class
        },
        mixinStandardHelpOptions = true)
public class ImportExportClient {
}
