/**
* Copyright (C) 2016, GIAYBAC
*
* Released under the MIT license
*/
package com.giaybac.traprange;

import com.giaybac.traprange.services.AutomaticExtractor;
import com.giaybac.traprange.services.IExtractor;
import com.giaybac.traprange.services.ManualExtractor;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.giaybac.traprange.services.CliArgUtils.printHelp;

/**
 *
 * @author thoqbk
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    /**
     * -in: source <br/>
     * -out: target  <br/>
     * -el: except lines. Ex: 1,2,3-1,6@8 #line 6 in page 8  <br/>
     * -p: page  <br/>
     * -ep: except page <br/>
     * -auto: automatic table recognition
     * -h: help
     *
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator.configure(Application.class.getResource("/com/giaybac/traprange/log4j.properties"));
        if (args.length == 1 && "-h".equals(args[0])) {
            printHelp();
        } else {
            resolveExtractor(args).extractTables();
        }
    }

    private static IExtractor resolveExtractor(String[] args) {
        if (Arrays.asList(args).contains("-auto")) {
            logger.debug("Running AutomaticExtractor");
            return new AutomaticExtractor(args);
        }
        logger.debug("Running ManualExtractor");
        return new ManualExtractor(args);
    }
}
