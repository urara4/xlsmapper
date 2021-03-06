package com.gh.mygreen.xlsmapper.expression;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.BeanNameResolver;
import javax.el.ELException;
import javax.el.ELProcessor;

import org.hibernate.validator.internal.engine.messageinterpolation.el.RootResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gh.mygreen.xlsmapper.ArgUtils;
import com.github.mygreen.expression.el.FormatterWrapper;


/**
 * 標準のEL式を使用するための実装。
 * <p>利用する際には、ELのライブラリが必要です。
 * 
 */
public class ExpressionLanguageELImpl implements ExpressionLanguage {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpressionLanguageELImpl.class);
    
    /** EL3.xが使用可能かどうか */
    boolean availabledEl3;
    {
        try {
            Class.forName("javax.el.ELProcessor");
            this.availabledEl3 = true;
        } catch (ClassNotFoundException e) {
            this.availabledEl3 = false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final String expression, final Map<String, ?> values) {
        
        ArgUtils.notEmpty(expression, "expression");
        ArgUtils.notEmpty(values, "values");
        
        if(availabledEl3) {
            return evaluateWithEL3(expression, values);
        } else {
            return evaluateWithEL2(expression, values);
        }
        
    }
    
    /**
     * EL3.xで評価する
     * @param expression
     * @param values
     * @return
     */
    Object evaluateWithEL3(final String expression, final Map<String, ?> values) {
        
        try {
            final ELProcessor elProc = new ELProcessor();
            
            final Map<String, Object> beans = new HashMap<String, Object>();
            for (final Entry<String, ? > entry : values.entrySet()) {
                if(isFormatter(entry.getKey(), entry.getValue())) {
                    // Formatterの場合は、ラップクラスを設定する。
                    beans.put(entry.getKey(), new FormatterWrapper((Formatter) entry.getValue()));
                } else {
                    beans.put(entry.getKey(), entry.getValue());
                }
            }
            
            elProc.getELManager().addBeanNameResolver(new LocalBeanNameResolver(beans));
            
            if(logger.isDebugEnabled()) {
                logger.debug("Evaluating EL expression: {}", expression);
            }
            
            return elProc.eval(expression);
        
        } catch (final ELException ex){
            throw new ExpressionEvaluationException(String.format("Evaluating [%s] script with EL failed.", expression), ex);
        }
    }
    
    /**
     * EL2.xで評価する
     * @param expression
     * @param values
     * @return
     */
    Object evaluateWithEL2(final String expression, final Map<String, ?> values) {
        
        try {
            final com.github.mygreen.expression.el.ELProcessor elProc = new com.github.mygreen.expression.el.ELProcessor();
            
            for (final Entry<String, ? > entry : values.entrySet()) {
                if(isFormatter(entry.getKey(), entry.getValue())) {
                    elProc.setVariable(entry.getKey(), new FormatterWrapper((Formatter) entry.getValue()));
                } else {
                    elProc.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            if(logger.isDebugEnabled()) {
                logger.debug("Evaluating EL expression: {}", expression);
            }
            
            return elProc.eval(expression);        
        } catch (final ELException ex){
            throw new ExpressionEvaluationException(String.format("Evaluating [%s] script with EL failed.", expression), ex);
        }
        
    }
    
    /**
     * {@link FormatterWrapper}で囲むべき値かどうか判定する。
     * @param key
     * @param value
     * @return
     */
    private boolean isFormatter(final String key, final Object value) {
        if(!RootResolver.FORMATTER.equals(key)) {
            return false;
        }
        
        if(value instanceof Formatter) {
            return true;
        }
        
        return false;
    }
    
    /**
     * EL3.0用の式中の変数のResolver。
     * ・存在しない場合はnullを返す。
     *
     */
    private class LocalBeanNameResolver extends BeanNameResolver {
        
        private final Map<String, Object> map;
        
        public LocalBeanNameResolver(final Map<String, ?> map) {
            this.map = new HashMap<>(map);
        }
        
        @Override
        public boolean isNameResolved(final String beanName){
            // 存在しない場合はnullを返すように、必ずtrueを設定する。
            return true;
        }
        
        @Override
        public Object getBean(final String beanName){
            return map.get( beanName );
        }
        
        @Override
        public void setBeanValue(String beanName, Object value){
            map.put(beanName, value );
        }

    }
    
}
