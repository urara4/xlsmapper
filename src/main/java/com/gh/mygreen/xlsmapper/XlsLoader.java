package com.gh.mygreen.xlsmapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gh.mygreen.xlsmapper.annotation.XlsPostLoad;
import com.gh.mygreen.xlsmapper.annotation.XlsPreLoad;
import com.gh.mygreen.xlsmapper.annotation.XlsSheet;
import com.gh.mygreen.xlsmapper.fieldprocessor.FieldAdaptor;
import com.gh.mygreen.xlsmapper.fieldprocessor.LoadingFieldProcessor;
import com.gh.mygreen.xlsmapper.validation.SheetBindingErrors;
import com.gh.mygreen.xlsmapper.xml.AnnotationReader;
import com.gh.mygreen.xlsmapper.xml.XmlLoader;
import com.gh.mygreen.xlsmapper.xml.bind.XmlInfo;


/**
 * ExcelのシートをJavaBeanにマッピングするクラス。
 * 
 * @author T.TSUCHIE
 *
 */
public class XlsLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(XlsLoader.class); 
    
    private XlsMapperConfig config;
    
    public XlsLoader(final XlsMapperConfig config) {
        this.config = config;
    }
    
    public XlsLoader() {
        this(new XlsMapperConfig());
    }
    
    /**
     * Excelファイルの１シートを読み込み、任意のクラスにマッピングする。
     * @param xlsIn 読み込みもとのExcelファイルのストリーム。
     * @param clazz マッピング先のクラスタイプ。
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     * @throws IllegalArgumentException xlsIn == null.
     * @throws IllegalArgumentException clazz == null.
     * 
     */
    public <P> P load(final InputStream xlsIn, final Class<P> clazz) throws XlsMapperException, IOException {
        
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        return load(xlsIn, clazz, null, null);
    }
    
    /**
     * Excelファイルの１シートを読み込み、任意のクラスにマッピングする。
     * @param xlsIn 読み込みもとのExcelファイルのストリーム。
     * @param clazz マッピング先のクラスタイプ。
     * @param xmlIn アノテーションの定義をしているXMLファイルの入力。指定しない場合は、nullを指定する。
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     * @throws IllegalArgumentException xlsIn == null.
     * @throws IllegalArgumentException clazz == null.
     */
    public <P> P load(final InputStream xlsIn, final Class<P> clazz, final InputStream xmlIn) throws XlsMapperException, IOException {
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        return load(xlsIn, clazz, xmlIn, null);
    }
    
    /**
     * Excelファイルの１シートを読み込み、任意のクラスにマッピングする。
     * @param xlsIn 読み込みもとのExcelファイルのストリーム。
     * @param clazz マッピング先のクラスタイプ。
     * @param errors エラー内容
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     * @throws IllegalArgumentException xlsIn == null.
     * @throws IllegalArgumentException clazz == null.
     * 
     */
    public <P> P load(final InputStream xlsIn, final Class<P> clazz, final SheetBindingErrors errors) throws XlsMapperException, IOException {
        
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        return load(xlsIn, clazz, null, errors);
    }
    
    /**
     * Excelファイルの１シートを読み込み、任意のクラスにマッピングする。
     * @param xlsIn 読み込みもとのExcelファイルのストリーム。
     * @param clazz マッピング先のクラスタイプ。
     * @param xmlIn アノテーションの定義をしているXMLファイルの入力。指定しない場合は、nullを指定する。
     * @param errors マッピング時のエラー情報。指定しない場合は、nulを指定する。
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     * @throws IllegalArgumentException xlsIn == null.
     * @throws IllegalArgumentException clazz == null.
     */
    public <P> P load(final InputStream xlsIn, final Class<P> clazz, final InputStream xmlIn, 
            final SheetBindingErrors errors)
            throws XlsMapperException, IOException {
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        XmlInfo xmlInfo = null;
        if(xmlIn != null) {
            xmlInfo = XmlLoader.load(xmlIn);
        }
        
        final LoadingWorkObject work = new LoadingWorkObject();
        
        final AnnotationReader annoReader = new AnnotationReader(xmlInfo);
        work.setAnnoReader(annoReader);
        
        if(errors != null) {
            work.setErrors(errors);
        } else {
            work.setErrors(new SheetBindingErrors(clazz));
        }
        
        final XlsSheet sheetAnno = clazz.getAnnotation(XlsSheet.class);
        if(sheetAnno == null) {
            throw new AnnotationInvalidException("Cannot finld annoation '@XlsSheet'", sheetAnno);
        }
        
        final Workbook book;
        try {
            book = WorkbookFactory.create(xlsIn);
            
        } catch (InvalidFormatException e) {
            throw new XlsMapperException("fail load Excel File", e);
        }
        
        try {
            final org.apache.poi.ss.usermodel.Sheet[] xlsSheet = findSheet(book, sheetAnno);
            return loadSheet(xlsSheet[0], clazz, work);
        } catch(SheetNotFoundException e) {
            if(config.isIgnoreSheetNotFound()){
                logger.warn("skip loading by not-found sheet.", e);
                return null;
            } else {
                throw e;
            }
        }
    }
    
    /**
     * Excelファイルの複数シートを読み込み、任意のクラスにマップする。
     * @param xlsIn
     * @param clazz
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     */
    public <P> P[] loadMultiple(final InputStream xlsIn, final Class<P> clazz) throws XlsMapperException, IOException {
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        return loadMultiple(xlsIn, clazz, null, null);
    }
    
    /**
     * Excelファイルの複数シートを読み込み、任意のクラスにマップする。
     * @param xlsIn
     * @param clazz
     * @param xmlIn
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     */
    public <P> P[] loadMultiple(final InputStream xlsIn, final Class<P> clazz, final InputStream xmlIn) throws XlsMapperException, IOException {
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        return loadMultiple(xlsIn, clazz, xmlIn, null);
    }
    
    /**
     * Excelファイルの複数シートを読み込み、任意のクラスにマップする。
     * @param xlsIn
     * @param clazz
     * @param errorsContainer
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     */
    public <P> P[] loadMultiple(final InputStream xlsIn, final Class<P> clazz,
            final SheetBindingErrorsContainer errorsContainer) throws XlsMapperException, IOException {
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        return loadMultiple(xlsIn, clazz, null, errorsContainer);
    }
    
    /**
     * XMLによるマッピングを指定し、Excelファイルの複数シートを読み込み、任意のクラスにマップする。
     * @param xlsIn
     * @param clazz
     * @param xmlIn
     * @param errorsContainer
     * @return
     * @throws XlsMapperException 
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
    public <P> P[] loadMultiple(final InputStream xlsIn, final Class<P> clazz, final InputStream xmlIn,
            final SheetBindingErrorsContainer errorsContainer) throws XlsMapperException, IOException {
        
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notNull(clazz, "clazz");
        
        XmlInfo xmlInfo = null;
        if(xmlIn != null) {
            xmlInfo = XmlLoader.load(xmlIn);
        }
        
        final AnnotationReader annoReader = new AnnotationReader(xmlInfo);
        
        final XlsSheet sheetAnno = clazz.getAnnotation(XlsSheet.class);
        if(sheetAnno == null) {
            throw new AnnotationInvalidException("Cannot finld annoation '@XlsSheet'", sheetAnno);
        }
        
        final SheetBindingErrorsContainer container;
        if(errorsContainer != null) {
            container = errorsContainer;
        } else {
            container = new SheetBindingErrorsContainer(clazz);
        }
        
        final Workbook book;
        try {
            book = WorkbookFactory.create(xlsIn);
            
        } catch (InvalidFormatException e) {
            throw new XlsMapperException("fail load Excel File", e);
        }
        
        final List<P> list = new ArrayList<P>();
        
        if(sheetAnno.number() == -1 && sheetAnno.name().isEmpty() && sheetAnno.regex().isEmpty()) {
            // 読み込むシートの条件が指定されていない場合、全て読み込む
            int sheetNum = book.getNumberOfSheets();
            for(int i=0; i < sheetNum; i++) {
                final org.apache.poi.ss.usermodel.Sheet sheet = book.getSheetAt(i);
                
                final LoadingWorkObject work = new LoadingWorkObject();
                work.setAnnoReader(annoReader);
                work.setErrors(container.findBindingResult(i));
                list.add(loadSheet(sheet, clazz, work));
            }
            
        } else {
            // 読み込むシートの条件が指定されている場合
            try {
                final org.apache.poi.ss.usermodel.Sheet[] xlsSheet = findSheet(book, sheetAnno);
                for(org.apache.poi.ss.usermodel.Sheet sheet : xlsSheet) {
                    
                    final LoadingWorkObject work = new LoadingWorkObject();
                    work.setAnnoReader(annoReader);
                    work.setErrors(container.findBindingResult(list.size()));
                    list.add(loadSheet(sheet, clazz, work));
                }
                
            } catch(SheetNotFoundException e) {
                if(config.isIgnoreSheetNotFound()){
                    logger.warn("skip loading by not-found sheet.", e);
                } else {
                    throw e;
                }
            }
            
        }
        
        return list.toArray((P[])Array.newInstance(clazz, list.size()));
    }
    
    public Object[] loadMultiple(final InputStream xlsIn, final Class<?>[] classes) throws XlsMapperException {
        return loadMultiple(xlsIn, classes, null, null);
    }
    
    public Object[] loadMultiple(final InputStream xlsIn, final Class<?>[] classes, final InputStream xmlIn) throws XlsMapperException {
        return loadMultiple(xlsIn, classes, xmlIn, null);
    }
    
    public Object[] loadMultiple(final InputStream xlsIn, final Class<?>[] classes,
            final SheetBindingErrorsContainer errorsContainer) throws XlsMapperException {
        return loadMultiple(xlsIn, classes, null, errorsContainer);
    }
    
    public Object[] loadMultiple(final InputStream xlsIn, final Class<?>[] classes, final InputStream xmlIn,
            SheetBindingErrorsContainer errorsContainer) throws XlsMapperException {
        
        ArgUtils.notNull(xlsIn, "xlsIn");
        ArgUtils.notEmpty(classes, "clazz");
        
        XmlInfo xmlInfo = null;
        if(xmlIn != null) {
            xmlInfo = XmlLoader.load(xmlIn);
        }
        
        final AnnotationReader annoReader = new AnnotationReader(xmlInfo);
        
        final SheetBindingErrorsContainer container;
        if(errorsContainer != null) {
            container = errorsContainer;
        } else {
            container = new SheetBindingErrorsContainer(classes);
        }
        
        final Workbook book;
        try {
            book = WorkbookFactory.create(xlsIn);
            
        } catch (InvalidFormatException | IOException e) {
            throw new XlsMapperException("fail load Excel File", e);
        }
        
        final List<Object> list = new ArrayList<Object>();
        for(Class<?> clazz : classes) {
            final XlsSheet sheetAnno = clazz.getAnnotation(XlsSheet.class);
            if(sheetAnno == null) {
                throw new AnnotationInvalidException("Cannot finld annoation '@XlsSheet'", sheetAnno);
            }
            
            try {
                final org.apache.poi.ss.usermodel.Sheet[] xlsSheet = findSheet(book, sheetAnno);
                for(org.apache.poi.ss.usermodel.Sheet sheet: xlsSheet) {
                    
                    final LoadingWorkObject work = new LoadingWorkObject();
                    work.setAnnoReader(annoReader);
                    work.setErrors(container.findBindingResult(list.size()));
                    list.add(loadSheet(sheet, clazz, work));
                    
                } 
            } catch(SheetNotFoundException ex){
                if(!config.isIgnoreSheetNotFound()){
                    logger.warn("skip loading by not-found sheet.", ex);
                    throw ex;
                }
            }
            
        }
        
        return list.toArray();
    }
    
    /**
     * シートを読み込み、任意のクラスにマッピングする。
     * @param sheet シート情報
     * @param clazz マッピング先のクラスタイプ。
     * @param work 
     * @return
     * @throws Exception 
     * 
     */
    @SuppressWarnings({"rawtypes"})
    private <P> P loadSheet(final org.apache.poi.ss.usermodel.Sheet sheet, final Class<P> clazz,
            final LoadingWorkObject work) throws XlsMapperException {
        
        // 値の読み込み対象のJavaBeanオブジェクトの作成
        final P beanObj = config.createBean(clazz);
        
        work.getErrors().setSheetName(sheet.getSheetName());
        
        final List<FieldAdaptorProxy> adaptorProxies = new ArrayList<>();
        
        // @PreLoad用のメソッドの取得と実行
        for(Method method : clazz.getMethods()) {
            
            final XlsPreLoad preProcessAnno = work.getAnnoReader().getAnnotation(beanObj.getClass(), method, XlsPreLoad.class);
            if(preProcessAnno != null) {
                Utils.invokeNeedProcessMethod(method, beanObj, sheet, config, work.getErrors());
            }
        }
        
        // public メソッドの処理
        for(Method method : clazz.getMethods()) {
            method.setAccessible(true);
            
            for(Annotation anno : work.getAnnoReader().getAnnotations(clazz, method)) {
                final LoadingFieldProcessor processor = config.getFieldProcessorRegistry().getLoadingProcessor(anno);
                if(Utils.isSetterMethod(method) && processor != null) {
                    final FieldAdaptor adaptor = new FieldAdaptor(clazz, method, work.getAnnoReader());
                    adaptorProxies.add(new FieldAdaptorProxy(anno, processor, adaptor));
                    break;
                    
                } else if(anno instanceof XlsPostLoad) {
                    work.addNeedPostProcess(new NeedProcess(beanObj, method));
                    break;
                }
            }
            
        }
        
        // public / private / protected / default フィールドの処理
        for(Field field : clazz.getDeclaredFields()) {
            
            field.setAccessible(true);
            final FieldAdaptor adaptor = new FieldAdaptor(clazz, field, work.getAnnoReader());
            
            // メソッドを重複している場合は排除する。
            if(adaptorProxies.contains(adaptor)) {
                continue;
            }
            
            for(Annotation anno : work.getAnnoReader().getAnnotations(clazz, field)) {
                final LoadingFieldProcessor processor = config.getFieldProcessorRegistry().getLoadingProcessor(anno);
                if(processor != null) {
                    adaptorProxies.add(new FieldAdaptorProxy(anno, processor, adaptor));
                    break;
                }
            }
        }
        
        // 順番を並び替えて保存処理を実行する
        Collections.sort(adaptorProxies, HintOrderComparator.createForLoading());
        for(FieldAdaptorProxy adaptorProxy : adaptorProxies) {
            adaptorProxy.loadProcess(sheet, beanObj, config, work);
        }
        
        //@PostLoadが付与されているメソッドの実行
        for(NeedProcess need : work.getNeedPostProcesses()) {
            Utils.invokeNeedProcessMethod(need.getMethod(), need.getTarget(), sheet, config, work.getErrors());
        }
        
        return beanObj;
    }
    
    /**
     * {@code @XlsSheet}の値をもとにして、シートを取得する。
     * @param book Excelのワークブック。
     * @param sheetAnno アノテーション{@link XlsSheet}
     * @return Excelのシート情報。複数ヒットする場合は、該当するものを全て返す。
     * @throws SheetNotFoundException
     * @throws AnnotationInvalidException
     */
    private org.apache.poi.ss.usermodel.Sheet[] findSheet(final Workbook book, final XlsSheet sheetAnno) throws SheetNotFoundException, AnnotationInvalidException {
        
        if(sheetAnno.name().length() > 0) {
            // シート名から取得する。
            final org.apache.poi.ss.usermodel.Sheet xlsSheet = book.getSheet(sheetAnno.name());
            if(xlsSheet == null) {
                throw new SheetNotFoundException(sheetAnno.name());
            }
            return new org.apache.poi.ss.usermodel.Sheet[]{ xlsSheet };
            
        } else if(sheetAnno.number() >= 0) {
            // シート番号から取得する
            if(sheetAnno.number() >= book.getNumberOfSheets()) {
                throw new SheetNotFoundException(sheetAnno.number(), book.getNumberOfSheets());
            }
            
            return new org.apache.poi.ss.usermodel.Sheet[]{ book.getSheetAt(sheetAnno.number()) }; 
            
        } else if(sheetAnno.regex().length() > 0) {
            // シート名（正規表現）をもとにして、取得する。
            final Pattern pattern = Pattern.compile(sheetAnno.regex());
            final List<org.apache.poi.ss.usermodel.Sheet> matches = new ArrayList<>();
            for(int i=0; i < book.getNumberOfSheets(); i++) {
                final org.apache.poi.ss.usermodel.Sheet xlsSheet = book.getSheetAt(i);
                if(pattern.matcher(xlsSheet.getSheetName()).matches()) {
                    matches.add(xlsSheet);
                }
            }
            
            if(matches.isEmpty()) {
                throw new SheetNotFoundException(sheetAnno.regex());
            }
            
            return matches.toArray(new org.apache.poi.ss.usermodel.Sheet[matches.size()]);
        }
        
        throw new AnnotationInvalidException("@XlsSheet requires name or number or regex parameter.", sheetAnno);
        
    }
    
    public XlsMapperConfig getConfig() {
        return config;
    }
    
    public void setConfig(XlsMapperConfig config) {
        this.config = config;
    }
}
