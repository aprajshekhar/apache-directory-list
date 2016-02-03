package org.ap.httpdirectoryparse.apache;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author randalap
 */
public class Parser {

    protected List<String> skipProperties;
    private String directoryUrl;
    private List<String> extensions;

    public Parser(List<String> skipProperties, String directoryUrl, List<String> extensions) {
        this.skipProperties = skipProperties;
        this.directoryUrl = directoryUrl;
        this.extensions = extensions;

    }

    public Parser(String directoryUrl) {
        this.directoryUrl = directoryUrl;
    }

    /**
     * @return the skipProperties
     */
    public List<String> getSkipProperties() {
        if (null == skipProperties) {
            this.skipProperties = new ArrayList<String>();
        }
        return skipProperties;
    }

    /**
     * @param skipProperties the skipProperties to set
     */
    public void setSkipProperties(List<String> skipProperties) {

        this.skipProperties = skipProperties;
    }

    /**
     * Parses the html at the URL passed recursively and returns the paths(urls) containing the extensions in extensions
     * list
     *
     * @return list containing he paths(urls) containing the extensions in extensions list
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public List<String> parse() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        List<String> paths = new ArrayList<>();
        System.out.println("skip: " + skipProperties);
        parseAndAddFilePaths(this.directoryUrl, null, paths);
        return paths;
    }

    protected Elements getList(String url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        enableSSLSocket();
        Document doc = Jsoup.connect(url).ignoreContentType(true).get();
        return doc.select("a[href]");

    }

    private void parseAndAddFilePaths(String url, Element link, List<String> paths) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        if (null != link && hasExtension(link.attr(LINK_ELEMENT))) {
            System.out.println("adding link: " + link.attr(LINK_ELEMENT));
            paths.add(link.attr(LINK_ELEMENT));
            return;
        }

        Elements temp = null == link ? getList(url) : getList(link.attr(LINK_ELEMENT));

        for (Element element : temp) {

            if (canSkip(element.text()) || canSkip(element.attr(LINK_ELEMENT))) {
                continue;
            }
            System.out.println("currently accessing: " + element.attr(LINK_ELEMENT));
            parseAndAddFilePaths(null, element, paths);
        }

    }
    private static final String LINK_ELEMENT = "abs:href";

    private void enableSSLSocket() throws KeyManagementException, NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }

        });

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    private boolean canSkip(String name) {

        for (String skipProperty : skipProperties) {
            if (name.trim().contains(skipProperty)) {
                System.out.println("skipped name: " + name);
                return true;
            }
        }
        return false;
    }

    /**
     * @return the extensions
     */
    public List<String> getExtensions() {
        if (null == extensions) {
            this.extensions = new ArrayList<>();
        }
        return extensions;
    }

    /**
     * @param extensions the extensions to set
     */
    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    private boolean hasExtension(String extension) {
        for (String extensionName : extensions) {
            if (extension.contains(extensionName)) {
                return true;
            }
        }
        return false;
    }
}
