package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.service.commands.ImportExportCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;

@SpringBootApplication(scanBasePackages = "no.vegvesen.ixn")
public class ImportExportApplication implements CommandLineRunner {


    ApplicationContext applicationContext;

    public static void main(String[] args) {
        //SpringApplication application = new SpringApplication(ImportExportApplication.class);
        //application.setDefaultProperties();
        SpringApplication.run(ImportExportApplication.class, args);
    }


    @Override
    public void run(String... args) {
        new CommandLine(new ImportExportCommand(), new PicocliSpringFactory(applicationContext)).execute(args);
    }
}
