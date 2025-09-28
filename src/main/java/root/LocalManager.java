package root;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/local")
@Profile("local")
public class LocalManager {
    final Bank bank;

    public LocalManager(Bank bank) {
        this.bank = bank;
    }

    @PostMapping("/fetch")
    public void refresh() {
        bank.update();
    }
}
