# Service Provider Client

This is a command-line client meant to be used to interact with the interchange.

For help with any command, type ```serviceprovicerclient <command> --help```

This tool supports auto-completion in bash and zsh, to use, first create a completion 
file using the command ```java -cp service-provider-client-1.0.23-SNAPSHOT.jar picocli.AutoComplete no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication```

The auto-completion file assumes the name of the command is ```serviceproviderclient```

To create a command-completion ready client, you have to firstly generate the file as described above, and the new file 
will be created in the folder the command is called from.

To get autocomplete working, first create an alias for the command, then source the completion file, for example

```alias serviceproviderclient='java -jar <absolute-path-to-jar>/service-provider-client-1.0.23-SNAPSHOT.jar'```
Then 
```. <path to completion file>``` (notice the dot in the beginning). This will create an alias for the serviceproviderclient
command, and give autocomplete for the different switches and subcommands when pressing tab twice in the shell.

## Tip

The command has a few options that will rarely change, like the keystore and truststore locations and passwords.
This can be either added to the alias (but that will make the password visible when running ```alias```), or, even better
use an "at-file", a file with the switches you want to have a default value on one line.
In the case of using an at-file, the alias would be
```alias serviceproviderclient='java -jar <absolute-path-to-jar>/service-provider-client-1.0.23-SNAPSHOT.jar' @<path-to-my-at-file>```,
and you cound have settings in the file, for example
```--user=myUser --keystorepath=/path/to/my/keystore.p12 --keystorepassword=mySuperDuperStrongPassword```

