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

    /**
     * @return the skipProperties
     */
    public List<String> getSkipProperties() {
        return skipProperties;
    }

    /**
     * @param skipProperties the skipProperties to set
     */
    public void setSkipProperties(List<String> skipProperties) {
        if (null == skipProperties) {
            this.skipProperties = new ArrayList<String>();
        }
        this.skipProperties = skipProperties;
    }

    public List<String> parse() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        List<String> paths = new ArrayList<>();
        parseAndAddFilePaths(this.directoryUrl, "json", null, paths);
        return paths;
    }

    private Elements getList(String url) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        enableSSLSocket();
        Document doc = Jsoup.connect(url).ignoreContentType(true).get();
        return doc.select("a[href]");

    }

    private void parseAndAddFilePaths(String url, String extension, Element link, List<String> paths) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        if (null != link && link.attr(LINK_ELEMENT).contains(extension)) {
            paths.add(link.attr(LINK_ELEMENT));
            return;
        }

        Elements temp = null == link ? getList(url) : getList(link.attr(LINK_ELEMENT));

        for (Element element : temp) {
            if (canSkip(element.attr(LINK_ELEMENT))) {
                System.out.println("link:" + element.attr(LINK_ELEMENT));
                continue;
            }
            parseAndAddFilePaths(url, extension, element, paths);
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
        return this.skipProperties.contains(name);
    }
}
