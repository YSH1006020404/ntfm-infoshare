package cn.les.ntfm.config;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

//@Aspect
//@Component
//@Configuration("__tx_advice_interceptor2__")
//@ConditionalOnBean({DataSource.class})
@ConditionalOnClass({DataSource.class})
public class MyTxAdviceInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${les.mybatis.tx.timeout:5}")
    private int TX_METHOD_TIMEOUT = 5;
    private String AOP_POINTCUT_EXPRESSION = "execution(* cn.les.framework.service.*.*(..)) || execution(* cn.les.framework.*.service.*.*(..)) || execution(* cn.les.*.*.service.*.*(..))||execution(* cn.les.*.service.*.*(..)) ";
    @Autowired
    private PlatformTransactionManager transactionManager;

    public MyTxAdviceInterceptor() {
    }

    @Bean
    public TransactionInterceptor txAdvice2() {
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
        readOnlyTx.setReadOnly(true);
        readOnlyTx.setPropagationBehavior(4);
        RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
        requiredTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        requiredTx.setPropagationBehavior(0);
        requiredTx.setTimeout(this.TX_METHOD_TIMEOUT);
        Map<String, TransactionAttribute> txMap = new HashMap();
        txMap.put("save*", requiredTx);
        txMap.put("update*", requiredTx);
        txMap.put("remove*", requiredTx);
        txMap.put("*", readOnlyTx);
        source.setNameMap(txMap);
        TransactionInterceptor txAdvice = new TransactionInterceptor(this.transactionManager, source);
        if (this.logger.isInfoEnabled()) {
            this.logger.info("事务管理器启动成功！");
        }

        return txAdvice;
    }

    @Bean
    public Advisor MytxAdviceAdvisor2() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(this.AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, this.txAdvice2());
    }
}
