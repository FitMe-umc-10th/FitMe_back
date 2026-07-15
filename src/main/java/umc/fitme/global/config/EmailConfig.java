package umc.fitme.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean auth;
    private final boolean starttlsEnable;
    private final boolean starttlsRequired;
    private final int timeout;

    public EmailConfig(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password,
            @Value("${spring.mail.auth}") boolean auth,
            @Value("${spring.mail.starttls.enable}") boolean starttlsEnable,
            @Value("${spring.mail.starttls.required}") boolean starttlsRequired,
            @Value("${spring.mail.timeout}") int timeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.auth = auth;
        this.starttlsEnable = starttlsEnable;
        this.starttlsRequired = starttlsRequired;
        this.timeout = timeout;
    }
}
