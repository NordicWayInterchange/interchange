# Service Provider Client

This is a command-line client meant to be used to interact with the interchange. The command can be used from both Windows and Linux 
command lines, but the following documentation assumes bash or zsh shells. 

The actual client requires Java 21 installed to run.

## Command alias

For easier execution of the command, add the line below to your .bashrc or .bash_aliases file:
```alias serviceproviderclient='java -jar <absolute-path-to-jar>/service-provider-client-1.0.33-SNAPSHOT.jar'```

For some more help on bash and aliases, see [W3Scools tutorial on Bash alias](https://www.w3schools.com/bash/bash_alias.php)


For help with any subcommand, type ```serviceprovicerclient <command> --help```

## Auto-completion
This tool supports auto-completion in bash and zsh. To use, first create a completion 
file using the command ```java -cp service-provider-client-1.0.33-SNAPSHOT.jar picocli.AutoComplete no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication```
The new file will be created in the folder the command is called from.

The auto-completion file assumes the name of the command is ```serviceproviderclient```, as in the alias shown above.

Then 
```. <path to completion file>``` (notice the dot in the beginning). This will create an alias for the serviceproviderclient
command, and give autocomplete for the different switches and subcommands when pressing tab twice in the shell.


## Generating keys

In order to generate the key and trust stores for using the Interchange, you have to generate keys and certificate in the portal.
Enter the country code and the organisation name, and click "Generate certificate". This will start the download of three files.
Move these three files to a known location, and you can use these with the service provider client by using the ```--cacert=root...crt.pem```, 
``--cert=chain...crt.pem`` and ``--key=...key.pem`` arguments. 

## Tip

The command has a few options that will rarely change, like the keystore and truststore locations and passwords.
This can be either added to the alias (but that will make the password visible when running ```alias```), or, even better
use an "at-file", a file with the switches you want to have a default value on one line.
In the case of using an at-file, the alias would be
```alias serviceproviderclient='java -jar <absolute-path-to-jar>/service-provider-client-1.0.33-SNAPSHOT.jar' @<path-to-my-at-file>```,
and you could have settings in the file, for example
```--user=myUser --key=/path/to/my/key.pem ...```

