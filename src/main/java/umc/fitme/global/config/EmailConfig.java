package umc.fitme.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean auth;
    private final boolean starttlsEnable;
    private final boolean starttlsRequired;
    private final int connectionTimeout;
    private final int writeTimeout;
    private final int responseTimeout;


    public EmailConfig(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password,
            @Value("${spring.mail.properties.mail.smtp.auth}") boolean auth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable}") boolean starttlsEnable,
            @Value("${spring.mail.properties.mail.smtp.starttls.require}") boolean starttlsRequired,
            @Value("${spring.mail.properties.mail.smtp.response-timeout}") int responseTimeout,
            @Value("${spring.mail.properties.mail.smtp.connection-timeout}") int connectionTimeout,
            @Value("${spring.mail.properties.mail.smtp.write-timeout}") int writeTimeout) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.auth = auth;
        this.starttlsEnable = starttlsEnable;
        this.starttlsRequired = starttlsRequired;
        this.responseTimeout = responseTimeout;
        this.connectionTimeout = connectionTimeout;
        this.writeTimeout = writeTimeout;
    }

    // 해당 빈을 통해 메일을 보낼 수 있다.
    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setJavaMailProperties(getMailProperties());

        return mailSender;
    }

    private Properties getMailProperties(){
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", String.valueOf(auth));
        props.setProperty("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));
        props.setProperty("mail.smtp.starttls.required", String.valueOf(starttlsRequired));
        props.setProperty("mail.smtp.response-timeout", String.valueOf(responseTimeout));
        props.setProperty("mail.smtp.connection-timeout", String.valueOf(connectionTimeout));
        props.setProperty("mail.smtp.write-timeout", String.valueOf(writeTimeout));

        return props;
    }
}
