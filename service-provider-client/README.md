# Service Provider Client

This is a command-line client meant to be used to interact with the interchange.

For help with any command, type ```serviceprovicerclient <command> --help```

This tool supports auto-completion in bash and zsh, to use, first create a completion 
file using the command ```java -cp service-provider-client-1.0.23-SNAPSHOT.jar picocli.AutoComplete no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication```

The auto-completion file assumes the name of the command is ```serviceproviderclient```

To create a command-completion ready client, you have to firstly generate the file as described above, and the new file 
will be created in the folder the command is called from.

To get autocomplete working, first create an alias for the command, then source the completion file, for example

```alias serviceproviderclient='java -jar <absolute-path-to-jar>/service-provider-client-1.0.23-SNAPSHOT.jar```'
Then 
```. <path to completion file>``` (notice the dot in the beginning)
