package java.Framework;

import org.json.simple.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.selebot.Exception.NoSuiteNameFoundException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuiteParser {




    /**
     * @param directory
     * @return give all the file inside a directory
     */
    public JSONArray getSuites(String directory) {

        JSONArray suiteFileList = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return suiteFileList;
    }


    /**
     * @param fileName : File path
     * @return whole file content as String buffer
     */
    public StringBuffer readSuiteFile(String fileName) {

        BufferedReader br = null;
        FileReader fr = null;

        StringBuffer suites = new StringBuffer();

        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                suites.append(sCurrentLine + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return suites;
    }


    public String[] getSuiteData(StringBuffer sb) {
        String allLines[] = sb.toString().split("[\\r\\n]+");
        return allLines;
    }


    /**
     * @param suite
     * @return
     * @Author : Ankit Mistry
     */
    public String getSuiteName(File suite) {
        String suiteName = "";
        String allLines[] = getSuiteData(readSuiteFile(suite.getAbsolutePath()));
        for (int i = 0; i < allLines.length; i++) {
            {
                if (allLines[i].toLowerCase().contains("suitename")) {
                    String sutie[] = allLines[i].split(":");
                    suiteName = sutie[1].trim();
                    break;
                }
            }
            if (suiteName.equals("")) {
                throw new NoSuiteNameFoundException("No suite name found");
            }
        }
        return suiteName;
    }







}
