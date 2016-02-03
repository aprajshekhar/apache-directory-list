package org.ap.httpdirectoryparse.examples;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.ap.httpdirectoryparse.apache.Parser;

/**
 *
 * @author randalap
 */
public class ApacheDirectoryListParserExample {

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        Parser parser = new Parser("http(s) url");
        parser.getExtensions().add(".json");
        parser.getExtensions().add(".plk");
        parser.getSkipProperties().add("Parent Directory");
        parser.getSkipProperties().add("Description");
        parser.getSkipProperties().add("Name");
        parser.getSkipProperties().add("Last modified");
        parser.getSkipProperties().add("Size");
        parser.getSkipProperties().add("blobs");
        parser.getSkipProperties().add("manifests");
        parser.getSkipProperties().add("tags");

        List<String> paths = parser.parse();
        for (String path : paths) {
            System.err.println(" path after parse: " + path);
        }
    }

}
