package anz.vwap;

import anz.vwap.service.VWAPService;
import anz.vwap.service.VWAPServiceImpl;
import anz.vwap.service.csv.model.VWAPRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

import static java.lang.System.exit;

@SpringBootApplication
public class VWAPCalculator {

    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext context = SpringApplication.run(VWAPCalculator.class, args);
        VWAPService vwapService= context.getBean(VWAPServiceImpl.class);

        System.out.println("\nWelcome to VWAP calculator!\n");

        // Check the input CSV file
        if (args.length != 1) {
            System.out.println("Error: Please provide an input CSV file");
            System.out.println("Usage: calculate-vwap.cmd <input-file.csv>\n");
            exit(0);
        }
        long timeWindow = 60*60*1000;
        List<VWAPRecord> vwapRecords = vwapService.calculateVWAP(timeWindow, args[0]);

        if (!CollectionUtils.isEmpty(vwapRecords)) {
            vwapRecords.forEach(vwapRecord -> {
                System.out.println(vwapRecord.getTimeWindow() + " -> VWAP for " + vwapRecord.getCurrencyPair() + ": " + vwapRecord.getVwap());
            });
        }

        String vwapFile = "./vwap-" + new Date().getTime() + ".csv";
        // Write to a CSV file
        System.out.println("\nVWAP Records are also written to output file " + vwapFile + "\n");
        vwapService.writeVWAPRecords(vwapFile, vwapRecords);
    }

}
