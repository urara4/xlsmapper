package com.gh.mygreen.xlsmapper.annotation;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.gh.mygreen.xlsmapper.TestUtils.*;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gh.mygreen.xlsmapper.AnnotationInvalidException;
import com.gh.mygreen.xlsmapper.XlsMapper;
import com.gh.mygreen.xlsmapper.annotation.LabelledCellType;
import com.gh.mygreen.xlsmapper.annotation.XlsHint;
import com.gh.mygreen.xlsmapper.annotation.XlsLabelledCell;
import com.gh.mygreen.xlsmapper.annotation.XlsSheet;
import com.gh.mygreen.xlsmapper.annotation.converter.XlsDateConverter;
import com.gh.mygreen.xlsmapper.cellconvert.TypeBindException;
import com.gh.mygreen.xlsmapper.fieldprocessor.CellNotFoundException;
import com.gh.mygreen.xlsmapper.fieldprocessor.processor.LabelledCellProcessor;
import com.gh.mygreen.xlsmapper.validation.SheetBindingErrors;


/**
 * {@link LabelledCellProcessor}のテスタ
 * アノテーション{@link XlsLabelledCell}のテスタ。
 * @version 1.0
 * @since 0.5
 * @author T.TSUCHIE
 *
 */
public class AnnoLabelledCellTest {
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * 読み込みテスト - 通常のテスト
     */
    @Test
    public void test_load_labelled_cell_normal() throws Exception {
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LabelledCell.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(NormalSheet.class);
            
            NormalSheet sheet = mapper.load(in, NormalSheet.class, errors);
            
            assertThat(sheet.posRight,is("右側の値です。"));
            assertThat(sheet.posLeft,is("左側の値です。"));
            assertThat(sheet.posBottom,is("下側の値です。"));
            
            assertThat(sheet.foundNo,is(nullValue()));
            
            assertThat(cellFieldError(errors, cellAddress(sheet.positions.get("wrongFormat"))).isTypeBindFailure(), is(true));
            
            assertThat(sheet.header, is(toUtilDate(toTimestamp("2015-05-09 00:00:00.000"))));
            assertThat(sheet.headerSkip, is(toUtilDate(toTimestamp("2015-04-02 00:00:00.000"))));
            assertThat(sheet.headerRange, is(toUtilDate(toTimestamp("2015-06-13 00:00:00.000"))));
            
            assertThat(sheet.address1,is("右側の値です。"));
            assertThat(sheet.address2,is("下側の値です。"));
            
            assertThat(sheet.blank, is(nullValue()));
            
        }
    }
    
    /**
     * 読み込みテスト - バインドエラー
     */
    @Test(expected=TypeBindException.class)
    public void test_load_labelled_cell_bind_error() throws Exception {
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(false);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LabelledCell.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(NormalSheet.class);
            
            NormalSheet sheet = mapper.load(in, NormalSheet.class, errors);
            
            fail();
            
        }
    }
    
    /**
     * 読み込みテスト - ラベルで指定したセルが見つからない。
     */
    @Test(expected=CellNotFoundException.class)
    public void test_load_labelled_cell_notFoundCell() throws Exception {
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(false);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LabelledCell.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(NotFounceLabelCellSheet.class);
            
            NotFounceLabelCellSheet sheet = mapper.load(in, NotFounceLabelCellSheet.class, errors);
            
            fail();
            
        }
    }
    
    /**
     * 読み込みのテスト - 不正なアノテーション - 見出しセルのアドレスの書式が不正
     */
    @Test(expected=AnnotationInvalidException.class)
    public void test_load_labelled_cell_invalid_annotation1() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(false);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LabelledCell.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(InvalidAnnoSheet1.class);
            
            InvalidAnnoSheet1 sheet = mapper.load(in, InvalidAnnoSheet1.class, errors);
            
            fail();
            
        }
        
    }
    
    /**
     * 読み込みのテスト - 不正なアノテーション - 見出しセルのアドレスのインデックスが範囲外
     */
    @Test(expected=AnnotationInvalidException.class)
    public void test_load_labelled_cell_invalid_annotation2() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(false);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LabelledCell.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(InvalidAnnoSheet2.class);
            
            InvalidAnnoSheet2 sheet = mapper.load(in, InvalidAnnoSheet2.class, errors);
            
            fail();
            
        }
        
    }
    
    /**
     * 読み込みのテスト - メソッドにアノテーションを付与
     * @since 1.0
     */
    @Test
    public void test_load_labelled_cell_methodAnno() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LabelledCell.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(MethodAnnoSheet.class);
            
            MethodAnnoSheet sheet = mapper.load(in, MethodAnnoSheet.class, errors);
            
            assertThat(sheet.posRight,is("右側の値です。"));
            assertThat(sheet.posLeft,is("左側の値です。"));
            assertThat(sheet.posBottom,is("下側の値です。"));
            
            assertThat(sheet.foundNo,is(nullValue()));
            
            assertThat(cellFieldError(errors, cellAddress(sheet.wrongFormatPosition)).isTypeBindFailure(), is(true));
            
            assertThat(sheet.header, is(toUtilDate(toTimestamp("2015-05-09 00:00:00.000"))));
            assertThat(sheet.headerSkip, is(toUtilDate(toTimestamp("2015-04-02 00:00:00.000"))));
            assertThat(sheet.headerRange, is(toUtilDate(toTimestamp("2015-06-13 00:00:00.000"))));
            
            assertThat(sheet.address1,is("右側の値です。"));
            assertThat(sheet.address2,is("下側の値です。"));
            
            assertThat(sheet.blank, is(nullValue()));
            
        }
        
    }
    
    /**
     * 読み込みのテスト - 通常のデータ
     */
    @Test
    public void test_save_labelled_cell_normal() throws Exception {
        
        // テストデータの作成
        final NormalSheet outSheet = new NormalSheet();
        
        outSheet.posRight("右側です。")
            .posLeft("左側の値です。")
            .posBottom("下側の値です。")
            .foundNo(123)
            .wrongFormat(123.456)
            .header(toUtilDate(toTimestamp("2015-06-07 08:09:10.000")))
            .headerSkip(toUtilDate(toTimestamp("2012-03-04 05:06:07.000")))
            .headerRange(toUtilDate(toTimestamp("2011-02-03 04:05:06.000")))
            .address1("アドレス指定です。\n右側。")
            .address2("アドレス指定です。\n左側。")
            ;
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        File outFile = new File("src/test/out/anno_LabelledCell.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_LabelledCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            
            SheetBindingErrors errors = new SheetBindingErrors(NormalSheet.class);
            
            NormalSheet sheet = mapper.load(in, NormalSheet.class, errors);
            
            assertThat(sheet.positions, is(outSheet.positions));
            assertThat(sheet.labels, is(outSheet.labels));
            
            assertThat(sheet.posRight, is(outSheet.posRight));
            assertThat(sheet.posLeft, is(outSheet.posLeft));
            assertThat(sheet.posBottom, is(outSheet.posBottom));
            
            assertThat(sheet.foundNo,is(nullValue()));
            assertThat(sheet.wrongFormat, is(outSheet.wrongFormat));
            
            assertThat(sheet.header, is(outSheet.header));
            assertThat(sheet.headerSkip, is(outSheet.headerSkip));
            assertThat(sheet.headerRange, is(outSheet.headerRange));
            
            assertThat(sheet.address1, is(outSheet.address1));
            assertThat(sheet.address2, is(outSheet.address2));
            
            assertThat(sheet.blank, is(outSheet.blank));
            
            
        }
        
    }
    
    /**
     * 読み込みのテスト - メソッドにアノテーションを付与
     * @since 1.0
     */
    @Test
    public void test_save_labelled_cell_methoAnno() throws Exception {
        
        // テストデータの作成
        final MethodAnnoSheet outSheet = new MethodAnnoSheet();
        
        outSheet.posRight("右側です。")
            .posLeft("左側の値です。")
            .posBottom("下側の値です。")
            .foundNo(123)
            .wrongFormat(123.456)
            .header(toUtilDate(toTimestamp("2015-06-07 08:09:10.000")))
            .headerSkip(toUtilDate(toTimestamp("2012-03-04 05:06:07.000")))
            .headerRange(toUtilDate(toTimestamp("2011-02-03 04:05:06.000")))
            .address1("アドレス指定です。\n右側。")
            .address2("アドレス指定です。\n左側。")
            ;
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        File outFile = new File("src/test/out/anno_LabelledCell.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_LabelledCell_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            
            SheetBindingErrors errors = new SheetBindingErrors(MethodAnnoSheet.class);
            
            MethodAnnoSheet sheet = mapper.load(in, MethodAnnoSheet.class, errors);
            
            // 位置情報の指定
            assertThat(sheet.posRightPositon, is(outSheet.posRightPositon));
            assertThat(sheet.posLeftPosition, is(outSheet.posLeftPosition));
            assertThat(sheet.posBottomPosition, is(outSheet.posBottomPosition));
            assertThat(sheet.foundNoPosition, is(outSheet.foundNoPosition));
            assertThat(sheet.wrongFormatPosition, is(outSheet.wrongFormatPosition));
            assertThat(sheet.headerPosition, is(outSheet.headerPosition));
            assertThat(sheet.headerSkipPosition, is(outSheet.headerSkipPosition));
            assertThat(sheet.headerRangePosition, is(outSheet.headerRangePosition));
            assertThat(sheet.address1Position, is(outSheet.address1Position));
            assertThat(sheet.address2Position, is(outSheet.address2Position));
            assertThat(sheet.blankPosition, is(outSheet.blankPosition));
            
            
            // ラベル情報
            assertThat(sheet.posRightLabel, is(outSheet.posRightLabel));
            assertThat(sheet.posLeftLabel, is(outSheet.posLeftLabel));
            assertThat(sheet.posBottomLabel, is(outSheet.posBottomLabel));
            assertThat(sheet.foundNoLabel, is(outSheet.foundNoLabel));
            assertThat(sheet.wrongFormatLabel, is(outSheet.wrongFormatLabel));
            assertThat(sheet.headerLabel, is(outSheet.headerLabel));
            assertThat(sheet.headerSkipLabel, is(outSheet.headerSkipLabel));
            assertThat(sheet.headerRangeLabel, is(outSheet.headerRangeLabel));
            assertThat(sheet.address1, is(outSheet.address1));
            assertThat(sheet.address2, is(outSheet.address2));
            
            // 値
            assertThat(sheet.posRight, is(outSheet.posRight));
            assertThat(sheet.posLeft, is(outSheet.posLeft));
            assertThat(sheet.posBottom, is(outSheet.posBottom));
            
            assertThat(sheet.foundNo,is(nullValue()));
            assertThat(sheet.wrongFormat, is(outSheet.wrongFormat));
            
            assertThat(sheet.header, is(outSheet.header));
            assertThat(sheet.headerSkip, is(outSheet.headerSkip));
            assertThat(sheet.headerRange, is(outSheet.headerRange));
            
            assertThat(sheet.address1, is(outSheet.address1));
            assertThat(sheet.address2, is(outSheet.address2));
            
            assertThat(sheet.blank, is(outSheet.blank));
            
            
        }
        
    }
    
    @XlsSheet(name="LabelledCell(通常)")
    private static class NormalSheet {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        /**
         * 位置のテスト - 右側
         */
        @XlsLabelledCell(label="位置（右側）", type=LabelledCellType.Right)
        private String posRight;
        
        /**
         * 位置のテスト - 左側
         */
        @XlsLabelledCell(label="位置（左側）", type=LabelledCellType.Left)
        private String posLeft;
        
        /**
         * 位置のテスト - 下側
         */
        @XlsLabelledCell(label="位置（下側）", type=LabelledCellType.Bottom)
        private String posBottom;
        
        /**
         * ラベルが見つからない 
         */
        @XlsLabelledCell(label="見つからない", type=LabelledCellType.Right, optional=true)
        private Integer foundNo;
        
        /**
         * 不正なフォーマット
         */
        @XlsLabelledCell(label="不正なフォーマット", type=LabelledCellType.Right)
        private Double wrongFormat;
        
        /**
         * ヘッダーラベル指定
         */
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Bottom, headerLabel="見出し１")
        private Date header;
        
        /**
         * ヘッダーラベル指定 - skip指定
         */
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Right, headerLabel="見出し２", skip=2)
        private Date headerSkip;
        
        /**
         * ヘッダーラベル指定 - range指定
         */
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Left, headerLabel="見出し３", range=2)
        private Date headerRange;
        
        /**
         * アドレス指定 - labelAddress
         */
        @XlsLabelledCell(labelAddress="B26", type=LabelledCellType.Right)
        private String address1;
        
        /**
         * アドレス指定 - labelColumn, labelRow
         */
        @XlsLabelledCell(labelColumn=1, labelRow=25, type=LabelledCellType.Bottom)
        private String address2;
        
        /**
         * 値が空の場合
         */
        @XlsLabelledCell(label="値が空の場合", type=LabelledCellType.Right)
        private String blank;
        
        public NormalSheet posRight(String posRight) {
            this.posRight = posRight;
            return this;
        }
        
        public NormalSheet posLeft(String posLeft) {
            this.posLeft = posLeft;
            return this;
        }
        
        public NormalSheet posBottom(String posBottom) {
            this.posBottom = posBottom;
            return this;
        }
        
        public NormalSheet foundNo(Integer foundNo) {
            this.foundNo = foundNo;
            return this;
        }
        
        public NormalSheet wrongFormat(Double wrongFormat) {
            this.wrongFormat = wrongFormat;
            return this;
        }
        
        public NormalSheet header(Date header) {
            this.header = header;
            return this;
        }
        
        public NormalSheet headerSkip(Date headerSkip) {
            this.headerSkip = headerSkip;
            return this;
        }
        
        public NormalSheet headerRange(Date headerRange) {
            this.headerRange = headerRange;
            return this;
        }
        
        public NormalSheet address1(String address1) {
            this.address1 = address1;
            return this;
        }
        
        public NormalSheet address2(String address2) {
            this.address2 = address2;
            return this;
        }
        
        public NormalSheet blank(String blank) {
            this.blank = blank;
            return this;
        }
    }
    
    /**
     * ラベルで指定したセルが見つからない場合
     */
    @XlsSheet(name="LabelledCell(通常)")
    private static class NotFounceLabelCellSheet {
        
        /**
         * ラベルが見つからない 
         */
        @XlsLabelledCell(label="身つからない", type=LabelledCellType.Right, optional=false)
        private Integer foundNo;
        
    }
    
    /**
     * アノテーションが不正 - ラベルのアドレスの書式が不正
     *
     */
    @XlsSheet(name="LabelledCell(通常)")
    private static class InvalidAnnoSheet1 {
        
        /**
         * アドレス指定 - labelAddress
         */
        @XlsLabelledCell(labelAddress="aaa", type=LabelledCellType.Right)
        private String address1;
        
    }
    
    /**
     * アノテーションが不正 - ラベルのアドレスの範囲が不正
     *
     */
    @XlsSheet(name="LabelledCell(通常)")
    private static class InvalidAnnoSheet2 {
        
        /**
         * アドレス指定 - labelColumn, labelRow
         */
        @XlsLabelledCell(labelColumn=-1, labelRow=-1, type=LabelledCellType.Bottom)
        private String address2;
        
    }
    
    /**
     * メソッドによるアノテーションの付与
     * @since 1.0
     *
     */
    @XlsSheet(name="LabelledCell(メソッドにアノテーションを付与)")
    private static class MethodAnnoSheet {
        
        /**
         * 位置のテスト - 右側
         */
        private String posRight;
        
        /**
         * 位置のテスト - 左側
         */
        private String posLeft;
        
        /**
         * 位置のテスト - 下側
         */
        private String posBottom;
        
        /**
         * ラベルが見つからない 
         */
        private Integer foundNo;
        
        /**
         * 不正なフォーマット
         */
        private Double wrongFormat;
        
        /**
         * ヘッダーラベル指定
         */
        private Date header;
        
        /**
         * ヘッダーラベル指定 - skip指定
         */
        private Date headerSkip;
        
        /**
         * ヘッダーラベル指定 - range指定
         */
        private Date headerRange;
        
        /**
         * アドレス指定 - labelAddress
         */
        private String address1;
        
        /**
         * アドレス指定 - labelColumn, labelRow
         */
        private String address2;
        
        /**
         * 値が空の場合
         */
        private String blank;
        
        @XlsLabelledCell(label="位置（右側）", type=LabelledCellType.Right)
        public String getPosRight() {
            return posRight;
        }
        
        @XlsLabelledCell(label="位置（右側）", type=LabelledCellType.Right)
        public void setPosRight(String posRight) {
            this.posRight = posRight;
        }
        
        @XlsLabelledCell(label="位置（左側）", type=LabelledCellType.Left)
        public String getPosLeft() {
            return posLeft;
        }
        
        @XlsLabelledCell(label="位置（左側）", type=LabelledCellType.Left)
        public void setPosLeft(String posLeft) {
            this.posLeft = posLeft;
        }
        
        @XlsLabelledCell(label="位置（下側）", type=LabelledCellType.Bottom)
        public String getPosBottom() {
            return posBottom;
        }
        
        @XlsLabelledCell(label="位置（下側）", type=LabelledCellType.Bottom)
        public void setPosBottom(String posBottom) {
            this.posBottom = posBottom;
        }
        
        @XlsLabelledCell(label="見つからない", type=LabelledCellType.Right, optional=true)
        public Integer getFoundNo() {
            return foundNo;
        }
        
        @XlsLabelledCell(label="見つからない", type=LabelledCellType.Right, optional=true)
        public void setFoundNo(Integer foundNo) {
            this.foundNo = foundNo;
        }
        
        @XlsLabelledCell(label="不正なフォーマット", type=LabelledCellType.Right)
        public Double getWrongFormat() {
            return wrongFormat;
        }
        
        @XlsLabelledCell(label="不正なフォーマット", type=LabelledCellType.Right)
        public void setWrongFormat(Double wrongFormat) {
            this.wrongFormat = wrongFormat;
        }
        
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Bottom, headerLabel="見出し１")
        public Date getHeader() {
            return header;
        }
        
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Bottom, headerLabel="見出し１")
        public void setHeader(Date header) {
            this.header = header;
        }
        
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Right, headerLabel="見出し２", skip=2)
        public Date getHeaderSkip() {
            return headerSkip;
        }
        
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Right, headerLabel="見出し２", skip=2)
        public void setHeaderSkip(Date headerSkip) {
            this.headerSkip = headerSkip;
        }
        
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Left, headerLabel="見出し３", range=2)
        public Date getHeaderRange() {
            return headerRange;
        }
        
        @XlsLabelledCell(label="ラベル名", type=LabelledCellType.Left, headerLabel="見出し３", range=2)
        public void setHeaderRange(Date headerRange) {
            this.headerRange = headerRange;
        }
        
        @XlsLabelledCell(labelAddress="B26", type=LabelledCellType.Right)
        public String getAddress1() {
            return address1;
        }
        
        @XlsLabelledCell(labelAddress="B26", type=LabelledCellType.Right)
        public void setAddress1(String address1) {
            this.address1 = address1;
        }
        
        @XlsLabelledCell(labelColumn=1, labelRow=25, type=LabelledCellType.Bottom)
        public String getAddress2() {
            return address2;
        }
        
        @XlsLabelledCell(labelColumn=1, labelRow=25, type=LabelledCellType.Bottom)
        public void setAddress2(String address2) {
            this.address2 = address2;
        }
        
        @XlsLabelledCell(label="値が空の場合", type=LabelledCellType.Right)
        public String getBlank() {
            return blank;
        }
        
        @XlsLabelledCell(label="値が空の場合", type=LabelledCellType.Right)
        public void setBlank(String blank) {
            this.blank = blank;
        }
        
        
        // 位置情報
        private Point posRightPositon;
        
        private Point posLeftPosition;
        
        private Point posBottomPosition;
        
        private Point foundNoPosition;
        
        private Point wrongFormatPosition;
        
        private Point headerPosition;
        
        private Point headerSkipPosition;
        
        private Point headerRangePosition;
        
        private Point address1Position;
        
        private Point address2Position;
        
        private Point blankPosition;
        
        public void setPosRightPositon(Point posRightPositon) {
            this.posRightPositon = posRightPositon;
        }
        
        public void setPosLeftPosition(Point posLeftPosition) {
            this.posLeftPosition = posLeftPosition;
        }
        
        public void setPosBottomPosition(Point posBottomPosition) {
            this.posBottomPosition = posBottomPosition;
        }
        
        public void setFoundNoPosition(Point foundNoPosition) {
            this.foundNoPosition = foundNoPosition;
        }
        
        public void setWrongFormatPosition(Point wrongFormatPosition) {
            this.wrongFormatPosition = wrongFormatPosition;
        }
        
        public void setHeaderPosition(Point headerPosition) {
            this.headerPosition = headerPosition;
        }
        
        public void setHeaderSkipPosition(Point headerSkipPosition) {
            this.headerSkipPosition = headerSkipPosition;
        }
        
        public void setHeaderRangePosition(Point headerRangePosition) {
            this.headerRangePosition = headerRangePosition;
        }
        
        public void setAddress1Position(Point address1Position) {
            this.address1Position = address1Position;
        }
        
        public void setAddress2Position(Point address2Position) {
            this.address2Position = address2Position;
        }
        
        public void setBlankPosition(Point blankPosition) {
            this.blankPosition = blankPosition;
        }
        
        // ラベル情報
        private String posRightLabel;
        
        private String posLeftLabel;
        
        private String posBottomLabel;
        
        private String foundNoLabel;
        
        private String wrongFormatLabel;
        
        private String headerLabel;
        
        private String headerSkipLabel;
        
        private String headerRangeLabel;
        
        private String address1Label;
        
        private String address2Label;
        
        private String blankLabel;
        
        public void setPosRightLabel(String posRightLabel) {
            this.posRightLabel = posRightLabel;
        }
        
        public void setPosLeftLabel(String posLeftLabel) {
            this.posLeftLabel = posLeftLabel;
        }
        
        public void setPosBottomLabel(String posBottomLabel) {
            this.posBottomLabel = posBottomLabel;
        }
        
        public void setFoundNoLabel(String foundNoLabel) {
            this.foundNoLabel = foundNoLabel;
        }
        
        public void setWrongFormatLabel(String wrongFormatLabel) {
            this.wrongFormatLabel = wrongFormatLabel;
        }
        
        public void setHeaderLabel(String headerLabel) {
            this.headerLabel = headerLabel;
        }
        
        public void setHeaderSkipLabel(String headerSkipLabel) {
            this.headerSkipLabel = headerSkipLabel;
        }
        
        public void setHeaderRangeLabel(String headerRangeLabel) {
            this.headerRangeLabel = headerRangeLabel;
        }
        
        public void setAddress1Label(String address1Label) {
            this.address1Label = address1Label;
        }
        
        public void setAddress2Label(String address2Label) {
            this.address2Label = address2Label;
        }
        
        public void setBlankLabel(String blankLabel) {
            this.blankLabel = blankLabel;
        }

        
        //// 値の設定用のメソッド
        
        public MethodAnnoSheet posRight(String posRight) {
            this.posRight = posRight;
            return this;
        }
        
        public MethodAnnoSheet posLeft(String posLeft) {
            this.posLeft = posLeft;
            return this;
        }
        
        public MethodAnnoSheet posBottom(String posBottom) {
            this.posBottom = posBottom;
            return this;
        }
        
        public MethodAnnoSheet foundNo(Integer foundNo) {
            this.foundNo = foundNo;
            return this;
        }
        
        public MethodAnnoSheet wrongFormat(Double wrongFormat) {
            this.wrongFormat = wrongFormat;
            return this;
        }
        
        public MethodAnnoSheet header(Date header) {
            this.header = header;
            return this;
        }
        
        public MethodAnnoSheet headerSkip(Date headerSkip) {
            this.headerSkip = headerSkip;
            return this;
        }
        
        public MethodAnnoSheet headerRange(Date headerRange) {
            this.headerRange = headerRange;
            return this;
        }
        
        public MethodAnnoSheet address1(String address1) {
            this.address1 = address1;
            return this;
        }
        
        public MethodAnnoSheet address2(String address2) {
            this.address2 = address2;
            return this;
        }
        
        public MethodAnnoSheet blank(String blank) {
            this.blank = blank;
            return this;
        }


    }

}
