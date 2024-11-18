package no.vegvesen.ixn.federation.importexportclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ImportExportClientApplication implements CommandLineRunner {

    @Autowired
    ApplicationContext applicationContext;

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(ImportExportClientApplication.class, args)));
    }

    @Override
    public void run(String... args){
        new CommandLine(new ImportExportClient(), new PicocliSpringFactory(applicationContext)).execute(args);
    }
}
