package com.giaybac.traprange.services;

import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableRow;
import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AutomaticPDFTableExtractor extends PDFTableExtractor {

    private final Logger logger = LoggerFactory.getLogger(AutomaticPDFTableExtractor.class);

    @Override
    public List<Table> extract() {
        logger.info("Starting automatic parsing.");
        int maxN = 10;
        List<Integer[]> list = new ArrayList<>();
        List<Integer> maxCols = new ArrayList<>();
        super.extract();
        List<Table> result = new ArrayList<>();
        for (int p = 0; p < document.getNumberOfPages(); p++) {
            this.setPage(p);
            for (int i = 0; i < maxN; i++) {
                for (int j = 0; j < maxN; j++) {
                    this.setSource(this.filePath);
                    logger.info("Starting with except lines {}", i);
                    list.add(new Integer[]{ i });
                    list.add(new Integer[]{ -1 * j });
                    this.pageNExceptedLinesMap = HashMultimap.create();
                    this.setExceptLines(list);
                    List<Table> res = super.extract();
                    int tmp = 0;
                    for (Table t: res) {
                        for (TableRow r: t.getRows()) {
                            if (r.getCells().size() > tmp) {
                                tmp = r.getCells().size();
                            }
                        }
                    }
                    maxCols.add(tmp);
                }
            }
            int excludeRows = 0;
            for (Integer n: maxCols) {
                if (excludeRows < n) {
                    excludeRows = n;
                }
            }
            this.setSource(this.filePath);
            List<Integer[]> exceptLines = new ArrayList<>();
            exceptLines.add(new Integer[]{ excludeRows });
            this.setExceptLines(exceptLines);
            List<Table> tables = super.extract();
            result.addAll(tables);
        }
        return result;
    }

}
