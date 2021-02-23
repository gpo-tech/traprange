package com.giaybac.traprange.services;

import com.giaybac.traprange.entity.Table;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.giaybac.traprange.services.CliArgUtils.*;

public abstract class AbstractExtractor implements IExtractor {
    private static final Logger logger = LoggerFactory.getLogger(ManualExtractor.class);

    List<Integer> pages;
    List<Integer> exceptPages;
    List<Integer[]> exceptLines;
    String in;
    String out;
    PDFTableExtractor extractor;

    AbstractExtractor(String[] args) {
        pages = getPages(args);
        exceptPages = getExceptPages(args);
        exceptLines = getExceptLines(args);
        in = getIn(args);
        out = getOut(args);
        extractor = (new PDFTableExtractor()).setSource(in);
        prepare();
    }

    @Override
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
        //except lines
        List<Integer> exceptLineIdxes = new ArrayList<>();
        Multimap<Integer, Integer> exceptLineInPages = LinkedListMultimap.create();
        for (Integer[] exceptLine: exceptLines) {
            if (exceptLine.length == 1) {
                exceptLineIdxes.add(exceptLine[0]);
            } else if (exceptLine.length == 2) {
                int lineIdx = exceptLine[0];
                int pageIdx = exceptLine[1];
                exceptLineInPages.put(pageIdx, lineIdx);
            }
        }
        if (!exceptLineIdxes.isEmpty()) {
            extractor.exceptLine(Ints.toArray(exceptLineIdxes));
        }
        if (!exceptLineInPages.isEmpty()) {
            for (int pageIdx: exceptLineInPages.keySet()) {
                extractor.exceptLine(pageIdx, Ints.toArray(exceptLineInPages.get(pageIdx)));
            }
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
