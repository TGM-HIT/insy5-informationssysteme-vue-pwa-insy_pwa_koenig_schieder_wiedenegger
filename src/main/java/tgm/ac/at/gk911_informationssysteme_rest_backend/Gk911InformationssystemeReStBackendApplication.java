package tgm.ac.at.gk911_informationssysteme_rest_backend;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Gk911InformationssystemeReStBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Gk911InformationssystemeReStBackendApplication.class, args);
    }

    @Component
    public static class DatasourceUrlLogger {

        @Value("${spring.datasource.url}")
        private String datasourceUrl;

        @PostConstruct
        public void logDatasourceUrl() {
            System.out.println("Resolved spring.datasource.url = " + datasourceUrl);
        }
    }
}
