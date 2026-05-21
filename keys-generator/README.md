# Keys generator utility

This folder contains a command-line utility for generating a set of keys for an interchange based on a json description
of its trust chain.
After building the project, you can run it from a shell using the following command in the `/target` directory: `java -jar keys-generator-<version>.jar generate`. 
To get usage help, just add the `--help` flag to the command.

The file singlenode-keys.json is provided as an example of a simple structure of the structure for a single interchange 
with keys for a few predefined service providers. 
