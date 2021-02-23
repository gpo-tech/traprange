package com.giaybac.traprange.services;

import com.giaybac.traprange.entity.Table;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.giaybac.traprange.services.CliArgUtils.*;

public class ManualExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ManualExtractor.class);

    public static void extractTables(String[] args) {
        try {
            List<Integer> pages = getPages(args);
            List<Integer> exceptPages = getExceptPages(args);
            List<Integer[]> exceptLines = getExceptLines(args);
            String in = getIn(args);
            String out = getOut(args);

            PDFTableExtractor extractor = (new PDFTableExtractor())
                    .setSource(in);
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
            //begin parsing pdf file
            List<Table> tables = extractor.extract();

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
                for (Table table: tables) {
                    writer.write("Page: " + (table.getPageIdx() + 1) + "\n");
                    writer.write(table.toHtml());
                }
            }
        } catch (Exception e) {
            logger.error(null, e);
        }
    }
}
