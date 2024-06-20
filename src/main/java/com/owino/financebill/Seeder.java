package com.owino.financebill;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Seeder {
    public static String updateJsonWithVotingData(){
        String jsonFilePath = "mps_raw.json";
        String votingDataFilePath = "first_voting_data.txt";
        String updatedJsonFilePath = "updated_members.json";
        JSONArray members = readJsonArrayFromFile(jsonFilePath);
        List<String> votingData = readVotingDataFromFile(votingDataFilePath);
        for (String record : votingData) {
            String[] parts = record.split(" - ");
            if (parts.length == 2) {
                String mpName = parts[0].trim();
                String vote = parts[1].trim();
                updateJsonWithVote(members, mpName, vote);
            }
        }
        writeJsonArrayToFile(members, updatedJsonFilePath);
        return convertJsonToMarkdownTable(members);
    }
    @SuppressWarnings("unchecked")
    private static void updateJsonWithVote(JSONArray members, String mpName, String vote) {
        for (Object obj : members) {
            JSONObject member = (JSONObject) obj;
            String rawName = (String) member.get("name");
            if (rawName.isEmpty()) continue;
            String jsonName = cleanName(rawName)
                    .toUpperCase()
                    .replace(",","")
                    .replace("HON","")
                    .replace("HON.","")
                    .replace("(DR.)","")
                    .replace("(AMB.)","");
            String txtName = cleanName(mpName)
                    .toUpperCase()
                    .replace(",","")
                    .replace("HON","")
                    .replace("HON.","")
                    .replace("(DR.)","")
                    .replace("(AMB.)","");
            if (jsonName.contains(txtName) || txtName.contains(jsonName) || checkIfTwoSimilarWords(txtName,jsonName)) {
                member.put("vote", vote.toUpperCase());
                return;
            }
        }
    }
    private static JSONArray readJsonArrayFromFile(String filePath) {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = new JSONArray();
        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                jsonArray = (JSONArray) obj;
            }
        } catch (IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }
    private static List<String> readVotingDataFromFile(String filePath) {
        List<String> votingData = null;
        try {
            votingData = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return votingData;
    }
    private static void writeJsonArrayToFile(JSONArray jsonArray, String filePath) {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(prettyPrintJSON(jsonArray.toJSONString()));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String cleanName(String mpName) {
        return mpName.replaceAll("\\d+", "")
                .replaceAll("\\.\\s*", " ")
                .trim();
    }
    public static boolean checkIfTwoSimilarWords(String str1, String str2) {
        Set<String> wordsSet1 = new HashSet<>();
        Set<String> wordsSet2 = new HashSet<>();
        for (String word : str1.split("\\s+")) {
            wordsSet1.add(word);
        }
        for (String word : str2.split("\\s+")) {
            wordsSet2.add(word);
        }
        wordsSet1.retainAll(wordsSet2);
        return wordsSet1.size() >= 2;
    }
    public static String prettyPrintJSON(String jsonString) {
        StringBuilder prettyJSONBuilder = new StringBuilder();
        int indentLevel = 0;
        boolean inQuote = false;
        for (char charFromJson : jsonString.toCharArray()) {
            switch (charFromJson) {
                case '"':
                    inQuote = !inQuote;
                    prettyJSONBuilder.append(charFromJson);
                    break;
                case ' ':
                    if (inQuote) {
                        prettyJSONBuilder.append(charFromJson);
                    }
                    break;
                case '{':
                case '[':
                    prettyJSONBuilder.append(charFromJson);
                    if (!inQuote) {
                        prettyJSONBuilder.append('\n');
                        indentLevel++;
                        appendIndent(prettyJSONBuilder, indentLevel);
                    }
                    break;
                case '}':
                case ']':
                    if (!inQuote) {
                        prettyJSONBuilder.append('\n');
                        indentLevel--;
                        appendIndent(prettyJSONBuilder, indentLevel);
                    }
                    prettyJSONBuilder.append(charFromJson);
                    break;
                case ',':
                    prettyJSONBuilder.append(charFromJson);
                    if (!inQuote) {
                        prettyJSONBuilder.append('\n');
                        appendIndent(prettyJSONBuilder, indentLevel);
                    }
                    break;
                case ':':
                    prettyJSONBuilder.append(charFromJson);
                    if (!inQuote) {
                        prettyJSONBuilder.append(' ');
                    }
                    break;
                default:
                    prettyJSONBuilder.append(charFromJson);
            }
        }
        return prettyJSONBuilder.toString();
    }
    private static void appendIndent(StringBuilder stringBuilder, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            stringBuilder.append("    "); // Indent with 4 spaces
        }
    }
    public static String convertJsonToMarkdownTable(JSONArray jsonArray) {
        StringBuilder table = new StringBuilder();
        table.append("| Image | Name | County | Constituency | Party | Vote |\n");
        table.append("| --- | --- | --- | --- | --- | --- |\n");

        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            table.append("| ![](").append(jsonObject.getOrDefault("image", "")).append(") | ")
                    .append(jsonObject.getOrDefault("name", "")).append(" | ")
                    .append(jsonObject.getOrDefault("county", "")).append(" | ")
                    .append(jsonObject.getOrDefault("constituency", "")).append(" | ")
                    .append(jsonObject.getOrDefault("party", "")).append(" | ")
                    .append(jsonObject.getOrDefault("vote", "")).append(" |\n");
        }

        return table.toString();
    }
}
