package com.giaybac.traprange.services;

import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableRow;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AutomaticPDFTableExtractor extends PDFTableExtractor {

    private final Logger logger = LoggerFactory.getLogger(AutomaticPDFTableExtractor.class);

    @Override
    public List<Table> extract() {
        logger.info("Starting automatic parsing.");
        int maxN = 10;
        int start = 9;
        int end = 10;// document.getNumberOfPages();
//        List<Integer[]> list = new ArrayList<>();
//        Map<Integer, List<Integer[]>> maxCols = new HashMap<>();
//        super.extract();
//        List<Table> result = new ArrayList<>();
//        for (int p = start; p < end; p++) {
//            this.setPage(p);
//            for (int i = 0; i < maxN; i++) {
//                list.add(new Integer[]{ i });
//                List<Integer[]> fromBottom = new ArrayList<>();
////                for (int j = 0; j < maxN; j++) {
//                    this.setSource(this.filePath);
//                    logger.info("Starting with except lines {}", i);
////                    fromBottom.add(new Integer[]{ -1 * j });
//                    this.pageNExceptedLinesMap = HashMultimap.create();
//                    this.setExceptLines(list);
//                    List<Table> res = super.extract();
//                    int tmp = 0;
//                    for (Table t: res) {
//                        t.cleanEmptyRows();
//                        for (TableRow r: t.getRows()) {
//                            if (r.getCells().size() > tmp) {
//                                tmp = r.getCells().size();
//                            }
//                        }
//                    }
//                    if (!maxCols.containsKey(tmp)) {
//                        List<Integer[]> currentExcludedRows = new ArrayList<>(list);
//                        currentExcludedRows.addAll(fromBottom);
//                        maxCols.put(tmp, currentExcludedRows);
//                    }
////                }
//            }
//            int excludeRows = 0;
//            for (Integer n: maxCols.keySet()) {
//                if (excludeRows < n) {
//                    excludeRows = n;
//                }
//            }
//            this.setSource(this.filePath);
//            this.setExceptLines(maxCols.get(excludeRows));
////            this.setExceptLines(
////                    Arrays.asList(
////                            new Integer[] { 0 },
////                            new Integer[] { 1 },
////                            new Integer[] { 2 },
////                            new Integer[] { 3 },
////                            new Integer[] { 4 },
////                            new Integer[] { 5 }
////                    )
////            );
//            List<Table> tables = super.extract();
//            result.addAll(tables);
//        }
        List<TextPosition> texts = getText(start);
        return createModel(texts, 8);
    }


    private List<Table> createModel(List<TextPosition> texts, Integer exceptRows) {
        List<Table> retVal = new ArrayList<>();
        List<Range<Integer>> rows = getRows(texts);
        Integer topOffset = 0;
        if (exceptRows != null) {
            topOffset = rows.get(Math.min(exceptRows, rows.size())).lowerEndpoint();
        }
        List<Range<Integer>> columns = getColumns(texts, topOffset)
                .stream()
                .filter(r -> r.upperEndpoint() - r.lowerEndpoint() > 10)
                .collect(Collectors.toList());
        return retVal;
    }

    private List<TextPosition> getText(int pageId) {
        this.setSource(this.filePath);
        try {
            this.document = this.password !=null ? PDDocument.load(inputStream, this.password) : PDDocument.load(inputStream);
            return extractTextPositions(pageId);//sorted by .getY() ASC
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Parse pdf file fail", ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (this.document != null) {
                try {
                    this.document.close();
                } catch (IOException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private List<Range<Integer>> getRows(List<TextPosition> pageContent) {
        TrapRangeBuilder lineTrapRangeBuilder = new TrapRangeBuilder();
        for (TextPosition textPosition : pageContent) {
            Range<Integer> lineRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            //add to builder
            lineTrapRangeBuilder.addRange(lineRange);
        }
        return lineTrapRangeBuilder.build();
    }

    private List<Range<Integer>> getColumns(List<TextPosition> pageContent, Integer topOffset) {
        TrapRangeBuilder rangesBuilder = new TrapRangeBuilder();
        for (TextPosition text : pageContent) {
            if (text.getY() < topOffset)
                continue;
            Range<Integer> range = Range.closed((int) text.getX(), (int) (text.getX() + text.getWidth()));
            rangesBuilder.addRange(range);
        }
        return rangesBuilder.build();
    }

}
