package com.giaybac.traprange.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CliArgUtils {

    private static final Logger logger = LoggerFactory.getLogger(CliArgUtils.class);
    public static void printHelp() {
        StringBuilder help = new StringBuilder();
        help.append("Argument list: \n")
                .append("\t-in: (required) absolute pdf location path. Ex: \"/Users/thoqbk/table.pdf\"\n")
                .append("\t-out: (required) absolute output file. Ex: \"/Users/thoqbk/table.html\"\n")
                .append("\t-el: skip lines. For example, to skip lines 1,2,3 and -1 (last line) in all pages and line 4 in page 8, the value should be: \"1,2,3,-1,4@8\"\n")
                .append("\t-p: only parse these pages. Ex: 1,2,3\n")
                .append("\t-ep: all pages except these pages. Ex: 1,2\n")
                .append("\t-auto: detect automatic each table start and end.\n")
                .append("\t-h: help\n")
                .append("---");
        logger.info(help.toString());
    }

    public static List<Integer> getPages(String[] args) {
        return getInts(args, "p");
    }

    public static List<Integer> getExceptPages(String[] args) {
        return getInts(args, "ep");
    }

    public static List<Integer> getInts(String[] args, String name) {
        List<Integer> retVal = new ArrayList<>();
        String intsInString = getArg(args, name);
        if (intsInString != null) {
            String[] intInStrings = intsInString.split(",");
            for (String intInString : intInStrings) {
                try {
                    retVal.add(Integer.parseInt(intInString.trim()));
                } catch (Exception e) {
                    throw new RuntimeException("Invalid argument (-" + name + "): " + intsInString, e);
                }
            }
        }
        return retVal;
    }

    public static List<Integer[]> getExceptLines(String[] args) {
        List<Integer[]> retVal = new ArrayList<>();
        String exceptLinesInString = getArg(args, "el");
        if(exceptLinesInString == null){
            return retVal;
        }
        //ELSE:
        String[] exceptLineStrings = exceptLinesInString.split(",");
        for (String exceptLineString : exceptLineStrings) {
            if (exceptLineString.contains("@")) {
                String[] exceptLineItems = exceptLineString.split("@");
                if (exceptLineItems.length != 2) {
                    throw new RuntimeException("Invalid except lines argument (-el): " + exceptLinesInString);
                } else {
                    try {
                        int lineIdx = Integer.parseInt(exceptLineItems[0].trim());
                        int pageIdx = Integer.parseInt(exceptLineItems[1].trim());
                        retVal.add(new Integer[]{lineIdx, pageIdx});
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid except lines argument (-el): " + exceptLinesInString, e);
                    }
                }
            } else {
                try {
                    int lineIdx = Integer.parseInt(exceptLineString.trim());
                    retVal.add(new Integer[]{lineIdx});
                } catch (Exception e) {
                    throw new RuntimeException("Invalid except lines argument (-el): " + exceptLinesInString, e);
                }
            }
        }
        return retVal;
    }

    public static String getOut(String[] args) {
        String retVal = getArg(args, "out", null);
        if (retVal == null) {
            throw new RuntimeException("Missing output location");
        }
        return retVal;
    }

    public static String getIn(String[] args) {
        String retVal = getArg(args, "in", null);
        if (retVal == null) {
            throw new RuntimeException("Missing input file");
        }
        return retVal;
    }

    public static String getArg(String[] args, String name, String defaultValue) {
        int argIdx = -1;
        for (int idx = 0; idx < args.length; idx++) {
            if (("-" + name).equals(args[idx])) {
                argIdx = idx;
                break;
            }
        }
        if (argIdx == -1) {
            return defaultValue;
        } else if (argIdx < args.length - 1) {
            return args[argIdx + 1].trim();
        } else {
            throw new RuntimeException("Missing argument value. Argument name: " + name);
        }
    }

    public static String getArg(String[] args, String name) {
        return getArg(args, name, null);
    }

    public static boolean isAutomatic(String[] args) {
        return Arrays.asList(args).contains("-auto");
    }
}
