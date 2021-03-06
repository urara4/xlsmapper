package com.gh.mygreen.xlsmapper.annotation;

import static com.gh.mygreen.xlsmapper.TestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gh.mygreen.xlsmapper.IsEmptyBuilder;
import com.gh.mygreen.xlsmapper.XlsMapper;
import com.gh.mygreen.xlsmapper.XlsMapperConfig;
import com.gh.mygreen.xlsmapper.validation.SheetBindingErrors;


/**
 * ライフサイクル用のアノテーション{@link XlsPreLoad}, {@link XlsPostLoad},{@link XlsPreSave},{@link XlsPostSave}のテスタ
 * 
 * @since 0.5
 * @author T.TSUCHIE
 *
 */
public class AnnoLifeCycleTest {
    
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
     * 読み込みのテスト - 単純な表
     */
    @Test
    public void test_load_lc_simple() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LifeCycle.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(SimpleSheet.class);
            
            SimpleSheet sheet = mapper.load(in, SimpleSheet.class, errors);
            
            assertThat(sheet.executeInitLoad, is(true));
            assertThat(sheet.executeDestroyLoad, is(true));
            assertThat(sheet.executeInitSave, is(false));
            assertThat(sheet.executeDestroySave, is(false));
            
            assertThat(sheet.name, is("A"));
            
            if(sheet.hRecords != null) {
                assertThat(sheet.hRecords, hasSize(2));
                for(Record record : sheet.hRecords) {
                    assertRecord(record, errors);
                }
            }
            
            if(sheet.vRecords != null) {
                assertThat(sheet.vRecords, hasSize(3));
                for(Record record : sheet.vRecords) {
                    assertRecord(record, errors);
                }
            }
            
            
        }
    }
    
    /**
     * 読み込みのテスト - 繰り返しの表
     */
    @Test
    public void test_load_lc_iterate() throws Exception {
        
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        try(InputStream in = new FileInputStream("src/test/data/anno_LifeCycle.xlsx")) {
            SheetBindingErrors errors = new SheetBindingErrors(IteratableSheet.class);
            
            IteratableSheet sheet = mapper.load(in, IteratableSheet.class, errors);
            
            assertThat(sheet.executeInitLoad, is(true));
            assertThat(sheet.executeDestroyLoad, is(true));
            assertThat(sheet.executeInitSave, is(false));
            assertThat(sheet.executeDestroySave, is(false));
            
            if(sheet.tables != null) {
                assertThat(sheet.tables, hasSize(2));
                for(Table table : sheet.tables) {
                    assertTable(table, errors);
                }
            }
            
            
        }
    }
    
    /**
     * 読み込み用のレコードの値の検証
     */
    private void assertRecord(final Record record, final SheetBindingErrors errors) {
        
        assertThat(record.executeInitLoad, is(true));
        assertThat(record.executeDestroyLoad, is(true));
        assertThat(record.executeInitSave, is(false));
        assertThat(record.executeDestroySave, is(false));
        
        if(record.no == 1) {
            assertThat(record.name, is("阿部一郎"));
            
        } else if(record.no == 2) {
            assertThat(record.name, is("泉太郎"));
            
        } else if(record.no == 3) {
            assertThat(record.name, is("山田太郎"));
        }
        
    }
    
    /**
     * 読み込み用の表の値の検証
     */
    private void assertTable(final Table table, final SheetBindingErrors errors) {
        
        assertThat(table.executeInitLoad, is(true));
        assertThat(table.executeDestroyLoad, is(true));
        assertThat(table.executeInitSave, is(false));
        assertThat(table.executeDestroySave, is(false));
        
        if(table.name.equals("1年2組")) {
            assertThat(table.hRecords, hasSize(2));
            
            for(Record record : table.hRecords) {
                assertRecord(record, errors);
            }
            
        } else if(table.name.equals("2年3組")) {
            assertThat(table.hRecords, hasSize(3));
            
            for(Record record : table.hRecords) {
                assertRecord(record, errors);
            }
        }
    }
    
    /**
     * 書き込みのテスト - 単純な表
     */
    @Test
    public void test_save_lc_simple() throws Exception {
        
        
        // テストデータの作成
        SimpleSheet outSheet = new SimpleSheet();
        
        outSheet.name = "A";
        outSheet.addHoritonzal(new Record().name("阿部一郎"));
        outSheet.addHoritonzal(new Record().name("泉太郎"));
        
        outSheet.addVertical(new Record().name("阿部一郎"));
        outSheet.addVertical(new Record().name("泉太郎"));
        outSheet.addVertical(new Record().name("山田太郎"));
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        File outFile = new File("src/test/out/anno_LifeCycle_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_LifeCycle_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            
            SheetBindingErrors errors = new SheetBindingErrors(SimpleSheet.class);
            
            SimpleSheet sheet = mapper.load(in, SimpleSheet.class, errors);
            
            assertThat(outSheet.executeInitLoad, is(false));
            assertThat(outSheet.executeDestroyLoad, is(false));
            assertThat(outSheet.executeInitSave, is(true));
            assertThat(outSheet.executeDestroySave, is(true));
            
            
            if(sheet.hRecords != null) {
                assertThat(sheet.hRecords, hasSize(outSheet.hRecords.size()));
                
                for(int i=0; i < sheet.hRecords.size(); i++) {
                    assertRecord(sheet.hRecords.get(i), outSheet.hRecords.get(i), errors);
                }
                
            }
            
            if(sheet.vRecords != null) {
                assertThat(sheet.vRecords, hasSize(outSheet.vRecords.size()));
                
                for(int i=0; i < sheet.vRecords.size(); i++) {
                    assertRecord(sheet.vRecords.get(i), outSheet.vRecords.get(i), errors);
                }
                
            }
        }
        
    }
    
    /**
     * 書き込みのテスト - 繰り返しの表
     */
    @Test
    public void test_save_lc_iterate() throws Exception {
        
        
        // テストデータの作成
        IteratableSheet outSheet = new IteratableSheet();
        
        outSheet.add(new Table().name("A")
                .addHoritonzal(new Record().name("阿部一郎"))
                .addHoritonzal(new Record().name("泉太郎"))
                );
        
        outSheet.add(new Table().name("B")
                .addHoritonzal(new Record().name("阿部一郎"))
                .addHoritonzal(new Record().name("泉太郎"))
                .addHoritonzal(new Record().name("山田太郎"))
                );
        
        // ファイルへの書き込み
        XlsMapper mapper = new XlsMapper();
        mapper.getConig().setSkipTypeBindFailure(true);
        
        File outFile = new File("src/test/out/anno_LifeCycle_out.xlsx");
        try(InputStream template = new FileInputStream("src/test/data/anno_LifeCycle_template.xlsx");
                OutputStream out = new FileOutputStream(outFile)) {
            
            mapper.save(template, out, outSheet);
        }
        
        // 書き込んだファイルを読み込み値の検証を行う。
        try(InputStream in = new FileInputStream(outFile)) {
            
            SheetBindingErrors errors = new SheetBindingErrors(IteratableSheet.class);
            
            IteratableSheet sheet = mapper.load(in, IteratableSheet.class, errors);
            
            assertThat(outSheet.executeInitLoad, is(false));
            assertThat(outSheet.executeDestroyLoad, is(false));
            assertThat(outSheet.executeInitSave, is(true));
            assertThat(outSheet.executeDestroySave, is(true));
            
            
            if(sheet.tables != null) {
                assertThat(sheet.tables, hasSize(outSheet.tables.size()));
                
                for(int i=0; i < sheet.tables.size(); i++) {
                    assertTable(sheet.tables.get(i), outSheet.tables.get(i), errors);
                }
                
            }
        }
        
    }
    
    /**
     * 書き込んだレコードを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertRecord(final Record inRecord, final Record outRecord, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertRecord::%s no=%d\n",
                this.getClass().getSimpleName(), inRecord.getClass().getSimpleName(), inRecord.no);
        
        assertThat(outRecord.executeInitLoad, is(false));
        assertThat(outRecord.executeDestroyLoad, is(false));
        assertThat(outRecord.executeInitSave, is(true));
        assertThat(outRecord.executeDestroySave, is(true));
        
        assertThat(inRecord.no, is(outRecord.no));
        assertThat(inRecord.name, is(outRecord.name));
    }
    
    /**
     * 書き込んだテーブルを検証するための
     * @param inRecord
     * @param outRecord
     * @param errors
     */
    private void assertTable(final Table inTable, final Table outTable, final SheetBindingErrors errors) {
        
        System.out.printf("%s - assertTable::%s name=%s\n",
                this.getClass().getSimpleName(), inTable.getClass().getSimpleName(), inTable.name);
        
        assertThat(outTable.executeInitLoad, is(false));
        assertThat(outTable.executeDestroyLoad, is(false));
        assertThat(outTable.executeInitSave, is(true));
        assertThat(outTable.executeDestroySave, is(true));
        
        
        assertThat(inTable.name, is(outTable.name));
        
        if(inTable.hRecords != null) {
            assertThat(inTable.hRecords, hasSize(outTable.hRecords.size()));
            
            for(int i=0; i < inTable.hRecords.size(); i++) {
                assertRecord(inTable.hRecords.get(i), outTable.hRecords.get(i), errors);
            }
        }
        
    }
    
    @XlsSheet(name="単純な表")
    private static class SimpleSheet {
        
        @XlsLabelledCell(label="クラス名", type=LabelledCellType.Right)
        private String name;
        
        @XlsHorizontalRecords(tableLabel="横方向", terminal=RecordTerminal.Border, skipEmptyRecord=true,
                overRecord=OverRecordOperate.Insert, remainedRecord=RemainedRecordOperate.Delete)
        private List<Record> hRecords;
        
        @XlsVerticalRecords(tableLabel="縦方向", terminal=RecordTerminal.Border, skipEmptyRecord=true,
                overRecord=OverRecordOperate.Copy, remainedRecord=RemainedRecordOperate.Clear)
        private List<Record> vRecords;
        
        public SimpleSheet addHoritonzal(final Record record) {
            
            if(hRecords == null) {
                this.hRecords = new ArrayList<>();
            }
            
            this.hRecords.add(record);
            record.no(hRecords.size());
            
            return this;
            
        }
        
        public SimpleSheet addVertical(final Record record) {
            
            if(vRecords == null) {
                this.vRecords = new ArrayList<>();
            }
            
            this.vRecords.add(record);
            record.no(vRecords.size());
            
            return this;
            
        }
        
        private boolean executeInitLoad;
        private boolean executeDestroyLoad;
        private boolean executeInitSave;
        private boolean executeDestroySave;
        
        @XlsPreLoad
        public void initLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitLoad = true;
        }
        
        @XlsPostLoad
        public void destroyLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroyLoad = true;
            
        }
        
        @XlsPreSave
        public void initSave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitSave = true;
            
        }
        
        @XlsPostSave
        public void destroySave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroySave = true;
            
        }
        
    }
    
    @XlsSheet(name="繰り返しの表")
    private static class IteratableSheet {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsIterateTables(tableLabel="横方向", bottom=2)
        private List<Table> tables;
        
        public IteratableSheet add(final Table table) {
            
            if(tables == null) {
                this.tables = new ArrayList<>();
            }
            
            this.tables.add(table);
            
            return this;
            
        }
        
        private boolean executeInitLoad;
        private boolean executeDestroyLoad;
        private boolean executeInitSave;
        private boolean executeDestroySave;
        
        @XlsPreLoad
        public void initLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitLoad = true;
        }
        
        @XlsPostLoad
        public void destroyLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroyLoad = true;
            
        }
        
        @XlsPreSave
        public void initSave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitSave = true;
            
        }
        
        @XlsPostSave
        public void destroySave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroySave = true;
            
        }
    }
    
    private static class Table {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsLabelledCell(label="クラス名", type=LabelledCellType.Right)
        private String name;
        
        @XlsHorizontalRecords(tableLabel="横方向", terminal=RecordTerminal.Border, skipEmptyRecord=true,
                overRecord=OverRecordOperate.Insert, remainedRecord=RemainedRecordOperate.Delete)
        private List<Record> hRecords;
        
        public Table name(final String name) {
            this.name = name;
            return this;
        }
        
        public Table addHoritonzal(final Record record) {
            
            if(hRecords == null) {
                this.hRecords = new ArrayList<>();
            }
            
            this.hRecords.add(record);
            record.no(hRecords.size());
            
            return this;
            
        }
        
        private boolean executeInitLoad;
        private boolean executeDestroyLoad;
        private boolean executeInitSave;
        private boolean executeDestroySave;
        
        @XlsPreLoad
        public void initLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitLoad = true;
        }
        
        @XlsPostLoad
        public void destroyLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroyLoad = true;
            
        }
        
        @XlsPreSave
        public void initSave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitSave = true;
            
        }
        
        @XlsPostSave
        public void destroySave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroySave = true;
            
        }
        
    }
    
    private static class Record {
        
        private Map<String, Point> positions;
        
        private Map<String, String> labels;
        
        @XlsColumn(columnName="No.")
        private int no;
        
        @XlsColumn(columnName="氏名")
        private String name;
        
        @XlsIsEmpty
        public boolean isEmpty() {
            return IsEmptyBuilder.reflectionIsEmpty(this, "positions", "labels", "no",
                    "executeInitLoad", "executeDestroyLoad", "executeInitSave", "executeDestroySave");
        }
        
        public Record no(int no) {
            this.no = no;
            return this;
        }
        
        public Record name(String name) {
            this.name = name;
            return this;
        }
        
        private boolean executeInitLoad;
        private boolean executeDestroyLoad;
        private boolean executeInitSave;
        private boolean executeDestroySave;
        
        @XlsPreLoad
        public void initLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitLoad = true;
        }
        
        @XlsPostLoad
        public void destroyLoad(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroyLoad = true;
            
        }
        
        @XlsPreSave
        public void initSave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeInitSave = true;
            
        }
        
        @XlsPostSave
        public void destroySave(final Sheet sheet, final XlsMapperConfig config, final SheetBindingErrors errors) {
            assertThat(sheet, is(not(nullValue())));
            assertThat(config, is(not(nullValue())));
            assertThat(errors, is(not(nullValue())));
            
            this.executeDestroySave = true;
            
        }
        
    }
    
}
