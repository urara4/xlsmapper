package com.gh.mygreen.xlsmapper.cellconvert.converter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.gh.mygreen.xlsmapper.POIUtils;
import com.gh.mygreen.xlsmapper.Utils;
import com.gh.mygreen.xlsmapper.XlsMapperConfig;
import com.gh.mygreen.xlsmapper.XlsMapperException;
import com.gh.mygreen.xlsmapper.annotation.converter.XlsArrayConverter;
import com.gh.mygreen.xlsmapper.annotation.converter.XlsConverter;
import com.gh.mygreen.xlsmapper.cellconvert.AbstractCellConverter;
import com.gh.mygreen.xlsmapper.fieldprocessor.FieldAdaptor;


/**
 * {@link Set}型を変換するためのConverter。
 *
 * @version 1.0
 * @since 1.0
 * @author T.TSUCHIE
 *
 */
@SuppressWarnings("rawtypes")
public class SetCellConverter extends AbstractCellConverter<Set> {
    
    @SuppressWarnings("unchecked")
    @Override
    public Set toObject(final Cell cell, final FieldAdaptor adaptor, final XlsMapperConfig config)
            throws XlsMapperException {
        
        final ListCellConverter converter = new ListCellConverter();
        final XlsArrayConverter anno = converter.getLoadingAnnotation(adaptor);
        
        Class<?> itemClass = anno.itemClass();
        if(itemClass == Object.class) {
            itemClass = adaptor.getLoadingGenericClassType();
        }
        
        final List<?> list = converter.toObject(cell, adaptor, config);
        return new LinkedHashSet(list);
    }
    
    @Override
    public Cell toCell(final FieldAdaptor adaptor, final Set targetValue, final Sheet sheet, final int column, final int row,
            final XlsMapperConfig config) throws XlsMapperException {
        
        final ListCellConverter converter = new ListCellConverter();
        
        final XlsConverter converterAnno = adaptor.getSavingAnnotation(XlsConverter.class);
        final XlsArrayConverter anno = converter.getSavingAnnotation(adaptor);
        
        Class<?> itemClass = anno.itemClass();
        if(itemClass == Object.class) {
            itemClass = adaptor.getSavingGenericClassType();
        }
        
        final Cell cell = POIUtils.getCell(sheet, column, row);
        
        // セルの書式設定
        if(converterAnno != null) {
            POIUtils.wrapCellText(cell, converterAnno.forceWrapText());
            POIUtils.shrinkToFit(cell, converterAnno.forceShrinkToFit());
        }
        
        Set value = targetValue;
        
        // デフォルト値から値を設定する
        if(Utils.isEmpty(value) && Utils.hasDefaultValue(converterAnno)) {
            final List<?> list = converter.convertList(Utils.getDefaultValue(converterAnno), itemClass, converterAnno, anno);
            value = new LinkedHashSet(list);
        }
        
        if(Utils.isNotEmpty(value)) {
            final boolean trim = (converterAnno == null ? false : converterAnno.trim()); 
            final String cellValue = Utils.join(value, anno.separator(), anno.ignoreEmptyItem(), trim);
            cell.setCellValue(cellValue);
        } else {
            cell.setCellType(Cell.CELL_TYPE_BLANK);
        }
        
        return cell;
    }

}
