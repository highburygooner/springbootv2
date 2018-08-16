package net.haige.dwl;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.FileNotFoundException;


@SpringBootApplication
@EnableSwagger2
@EnableWebMvc
public class AppStart {

    public static void main(String[] args) throws FileNotFoundException {

        SpringApplication.run(AppStart.class, args);
    }


}
