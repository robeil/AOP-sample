package bank.advise;


import bank.dao.AccountDAO;
import bank.domain.Account;
import bank.domain.Customer;
import bank.logging.ILogger;
import bank.logging.Logger;
import bank.service.AccountService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Aspect
@Configuration
public class TraceAdvice {

    @Autowired
    private ILogger logger;

    @After("execution(* bank.dao.AccountDAO.*(..))")
    public void traceAfterMethod(JoinPoint joinpoint) {
        Object object = joinpoint.getTarget();
//        AccountDAO account = (AccountDAO) object;
        LocalTime localTime = LocalTime.now();

        System.out.println( LocalDateTime.now().getMonth() + " " +
                LocalDateTime.now().getDayOfMonth() + ", " +
                localTime + " PM " +
                object.getClass());
//        System.out.println("Info : " + joinpoint.getSignature().getName() + " with parameters accountNumber " + accountNumber + " ," +
//                " customerName = " );

        logger.log(LocalDateTime.now() +" PM " + joinpoint.getSignature().getName() + object.getClass());
    }

    @Around("execution(* bank.service.AccountService.*(..)) && args(accountNumber, amount)")
    public Object invoke(ProceedingJoinPoint call, String accountNumber, double amount) throws Throwable {

        Object object = call.getTarget();
        Account accountService = (Account) object;
        Customer customer = (Customer) object;
        long startTime = System.currentTimeMillis();
        Object requiredTime = call.proceed();
        long endTime = System.currentTimeMillis();
        long totalTimeRequired = endTime - startTime;

        logger.log("ClassName: " + call.getSignature().getDeclaringTypeName()+" " +
                " MethodName: " +call.getSignature().getName() +
                ": time taken for Execution is : ===> " + totalTimeRequired + " milliseconds.");
        return requiredTime;
    }

    @After("execution(* bank.jms.JMSSender.sendJMSMessage(..)) && args(message)")
    public void traceAfterEmailSent(JoinPoint joinpoint, String message) {

        logger.log("ClassName = " + joinpoint.getSignature().getDeclaringTypeName() +
                " methodName = " + joinpoint.getSignature().getName() +
                " messageSent = " + message );
    }


    @Bean
    public Logger getLogger(){
        return new Logger();
    }
}
