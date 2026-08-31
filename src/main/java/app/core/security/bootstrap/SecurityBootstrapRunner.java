package app.core.security.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SecurityBootstrapRunner implements ApplicationRunner {

    private final SecurityBootstrapService service;

    public SecurityBootstrapRunner(SecurityBootstrapService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        service.bootstrapIfEnabled();
    }
}
