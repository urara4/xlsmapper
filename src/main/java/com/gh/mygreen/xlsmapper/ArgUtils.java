package com.gh.mygreen.xlsmapper;

import java.util.Collection;
import java.util.Map;


/**
 * 引数チェックに関するユーティリティクラス。
 * 
 * @author T.TSUCHIE
 *
 */
public class ArgUtils {
    
    /**
     * 値がnullでないかどうか検証する。
     * @param arg 検証対象の値
     * @param name 検証対象の引数の名前
     * @throws IllegalArgumentException if arg == null.
     */
    public static void notNull(final Object arg, final String name) {
        if(arg == null) {
            throw new IllegalArgumentException(String.format("%s should not be null.", name));
        }
    }
    
    /**
     * 文字列が空 or nullでないかどうか検証する。
     * @param arg 検証対象の値
     * @param name 検証対象の引数の名前
     * @throws IllegalArgumentException if arg == null or arg == "".
     */
    public static void notEmpty(final String arg, final String name) {
        if(arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s should not be empty.", name));
        }
    }
    
    /**
     * 配列のサイズが0または、nullでないかどうか検証する。
     * @param arg
     * @param name
     * @throws IllegalArgumentException if arg == null or arg.length == "".
     */
    public static void notEmpty(final Object[] arg, final String name) {
        if(arg == null || arg.length == 0) {
            throw new IllegalArgumentException(String.format("%s should has length ararys.", name));
        }
    }
    
    /**
     * Collection(リスト、セット)のサイズが0または、nullでないかどうか検証する。
     * @param arg 検証対象の値
     * @param name 検証対象の引数の名前
     * @throws IllegalArgumentException if arg == null or arg.size() == 0.
     */
    public static void notEmpty(final Collection<?> arg, final String name) {
        if(arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s should not be empty.", name));
        }
    }
    
    /**
     * マップのサイズが0または、nullでないかどうか検証する。
     * @param arg 検証対象の値
     * @param name 検証対象の引数の名前
     * @throws IllegalArgumentException if arg == null or arg.size() == 0.
     */
    public static void notEmpty(final Map<?, ?> arg, final String name) {
        if(arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s should not be empty.", name));
        }
    }
    
    /**
     * 引数が'arg' > 'min' の関係か検証する。
     * @param arg 検証対象の値
     * @param min 最小値
     * @param name 検証対象の引数の名前
     * @throws IllegalArgumentException if arg == null
     * @throws IllegalArgumentException if arg < min.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Comparable> void notMin(final T arg, final T min, final String name) {
        
        if(arg == null) {
            throw new IllegalArgumentException(String.format("%s should not be null.", name));
        }
        
        if(arg.compareTo(min) < 0) {
            throw new IllegalArgumentException(String.format("%s cannot be smaller than %s", name, min.toString()));
        }
        
    }
    
    /**
     * 引数が'arg' < 'max' の関係か検証する。
     * @param arg 検証対象の値
     * @param max 最大値
     * @param name 検証対象の引数の名前
     * @throws IllegalArgumentException if arg == null
     * @throws IllegalArgumentException if arg > max.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Comparable> void notMax(final T arg, final T max, final String name) {
        
        if(arg == null) {
            throw new IllegalArgumentException(String.format("%s should not be null.", name));
        }
        
        if(arg.compareTo(max) > 0) {
            throw new IllegalArgumentException(String.format("%s cannot be greater than %s", name, max.toString()));
        }
        
    }
    
}
