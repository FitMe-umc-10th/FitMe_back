package umc.fitme.global.timeTrace;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class TimeTraceApp {

    @Around("execution(* umc.fitme.domain..controller..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("START : {}", joinPoint.toString());

        try {
            return joinPoint.proceed();
        } finally {
            log.info("END: {}", joinPoint);
        }
    }
}
