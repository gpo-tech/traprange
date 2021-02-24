package com.giaybac.traprange.services;

import com.giaybac.traprange.entity.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.giaybac.traprange.services.CliArgUtils.*;

public class ExtractingService {
    private static final Logger logger = LoggerFactory.getLogger(ExtractingService.class);

    List<Integer> pages;
    List<Integer> exceptPages;
    List<Integer[]> exceptLines;
    String in;
    String out;
    PDFTableExtractor extractor;

    public ExtractingService(String[] args) {
        pages = getPages(args);
        exceptPages = getExceptPages(args);
        exceptLines = getExceptLines(args);
        in = getIn(args);
        out = getOut(args);
        if (isAutomatic(args)) {
            extractor = new AutomaticPDFTableExtractor();
        } else {
            extractor = new PDFTableExtractor();
            extractor.setExceptLines(exceptLines);
        }
        extractor.setSource(in);
        prepare();
    }

    public void extractTables() {
        //begin parsing pdf file
        List<Table> tables = extractor.extract();
        storeResults(tables);
    }

    void prepare() {
        //page
        for (Integer page: pages) {
            extractor.addPage(page);
        }
        //except page
        for (Integer exceptPage: exceptPages) {
            extractor.exceptPage(exceptPage);
        }
    }

    void storeResults(List<Table> tables) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
            for (Table table: tables) {
                writer.write("Page: " + (table.getPageIdx() + 1) + "\n");
                writer.write(table.toHtml());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
