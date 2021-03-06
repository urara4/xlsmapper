package com.gh.mygreen.xlsmapper.annotation.converter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.DecimalFormat;
import java.util.Currency;



/**
 * 数値型に対するConverter。
 * 
 * @version 0.5
 * @author T.TSUCHIE
 *
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XlsNumberConverter {
    
    /**
     * 数値の書式のパターン値を指定します。{@link DecimalFormat}で指定可能な書式を指定します。
     * @return
     */
    String pattern() default "";
    
    /**
     * 通過を指定します。{@link Currency}で処理可能なコード(ISO 4217のコード)で指定します。
     * <p>formatting {@link Currency} Code(ISO 4217 Code)
     * @return
     */
    String currency() default "";
    
    /**
     * ロケールの指定を行います。指定しない場合、デフォルトのロケールで処理されます。
     * <p>例. 'ja_JP', 'ja'
     */
    String locale() default "";
    
    /**
     * 有効桁数を指定します。
     * <p>Excelの場合、有効桁数は15桁であるため、デフォルト値は15です。
     * <p>0以下の値を設定すると、桁数の指定を省略したことになります。
     * @since 0.5
     * @return
     */
    int precision() default 15;    
}
