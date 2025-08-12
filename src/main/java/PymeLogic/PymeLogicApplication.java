package PymeLogic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class PymeLogicApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(PymeLogicApplication.class);

    public static void main(String[] args) {
        logger.info("Iniciando aplicación PymeLogic...");
        ApplicationContext context = SpringApplication.run(PymeLogicApplication.class, args);
        logger.info("Aplicación PymeLogic iniciada correctamente");
        String[] beanNames = context.getBeanDefinitionNames();
        logger.info("Beans cargados: " + beanNames.length);
    }

}
