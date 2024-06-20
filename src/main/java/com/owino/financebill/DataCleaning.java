package com.owino.financebill;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.nio.charset.StandardCharsets;
@Slf4j
public class DataCleaning {
    static void convertToCSV(String sourceFile, String outputCSVFileName) {
        try (InputStream inputStream = DataCleaning.class.getClassLoader().getResourceAsStream(sourceFile)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                 FileWriter writer = new FileWriter(outputCSVFileName)) {
                writer.append("Member,Vote\n");
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" - ");
                    if (parts.length == 2) {
                        String memberName = parts[0].trim();
                        String vote = parts[1].trim();
                        writer.append(String.format("%s,%s\n", memberName, vote));
                    } else {
                        System.err.println("Skipping line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            log.debug("error while converting raw txt data to csv {}", e.getLocalizedMessage());
        }
    }
    static void generateBasicResults(String sourceFile){
        String outputFilePath = "README.md";
        try (InputStream inputStream = DataCleaning.class.getClassLoader().getResourceAsStream(sourceFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             FileWriter writer = new FileWriter(outputFilePath)) {
            int yesCount = 0;
            int noCount = 0;
            int absentCount = 0;
            int totalCount = 0;
            StringBuilder rawDataTable = new StringBuilder();
            rawDataTable.append("| Member | Vote |\n");
            rawDataTable.append("|--------|------|\n");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length == 2) {
                    String memberName = parts[0].trim();
                    String vote = parts[1].trim();
                    rawDataTable.append(String.format("| %s | %s |\n", memberName, vote));
                    switch (vote) {
                        case "YES":
                            yesCount++;
                            break;
                        case "NO":
                            noCount++;
                            break;
                        case "ABSENT":
                            absentCount++;
                            break;
                        default:
                            log.debug("Unexpected vote value: " + vote);
                            break;
                    }
                    ++totalCount;
                } else {
                    log.debug("Skipping line: " + line);
                }
            }
            double yesPercentage = (double) yesCount / totalCount * 100.0;
            double noPercentage = (double) noCount / totalCount * 100.0;
            double absentPercentage = (double) absentCount / totalCount * 100.0;
            writer.write("# THE FINANCE BILL, 2024  ## SECOND READING ## VOTING RESULTS\n");
            writer.write("A Bill for\n");
            writer.write("AN ACT of Parliament to amend the law relating to various taxes and duties\n");

            writer.write("# Voting Analysis\n\n");
            writer.write("Total Votes: " + totalCount + "\n\n");
            writer.write("### Vote Breakdown:\n");
            writer.write("- Yes Votes: " + yesCount + " (" + String.format("%.2f", yesPercentage) + "%)\n");
            writer.write("- No Votes: " + noCount + " (" + String.format("%.2f", noPercentage) + "%)\n");
            writer.write("- Absent Votes: " + absentCount + " (" + String.format("%.2f", absentPercentage) + "%)\n\n");
            writer.write("## Raw Voting Data\n\n");
            writer.write(rawDataTable.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void appendUpdatedTableToMarkDown(String markdownTable){
        String outputFilePath = "README.md";
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.append(markdownTable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
