package nl.b3p.geotools.data.csv;

import java.io.IOException;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author Gertjan Al, B3Partners
 */
public class CSVDataStore extends AbstractFileDataStore {

    private static final Log log = LogFactory.getLog(CSVDataStore.class);
    private URL url;
    private String typename;
    private FeatureReader featureReader;
    private String srs;
    private char seperator;
    private int column_x, column_y;
    private boolean checkColumnCount;
    private String encoding;

    public CSVDataStore(URL url, String srs, boolean checkColumnCount, char seperator, int column_x, int column_y, String encoding) throws IOException {
        this.url = url;
        this.typename = getURLTypeName(url);
        this.srs = srs;
        this.seperator = seperator;
        this.column_x = column_x;
        this.column_y = column_y;
        this.checkColumnCount = checkColumnCount;
        this.encoding = encoding;
    }

    public String[] getTypeNames() throws IOException {
        return new String[]{getURLTypeName(url)};
    }

    static String getURLTypeName(URL url) throws IOException {
        String file = url.getFile();
        if (file.length() == 0) {
            return "unknown_csv";
        } else {
            int i = file.lastIndexOf('/');
            if (i != -1) {
                file = file.substring(i + 1);
            }
            if (file.toLowerCase().endsWith(".csv")) {
                file = file.substring(0, file.length() - 4);
            }
            return file;
        }
    }

    public SimpleFeatureType getSchema(String typeName) throws IOException {
        /* only one type */
        return getSchema();
    }

    @Override
    public SimpleFeatureType getSchema() throws IOException {
        return (SimpleFeatureType) getFeatureReader().getFeatureType();
    }

    public FeatureReader getFeatureReader(String typeName) throws IOException {
        /* only one type */
        return getFeatureReader();
    }

    @Override
    public FeatureReader getFeatureReader() throws IOException {
        if (featureReader == null) { //|| !featureReader.hasNext()) {
            try {
                featureReader = new CSVFeatureReader(url, typename, srs, checkColumnCount, seperator, column_x, column_y, encoding);
            } catch (Exception e) {
                throw new IOException("CSV parse exception" + e.getLocalizedMessage());
            }
        }
        return featureReader;
    }

    @Override
    protected int getCount(Query query) throws IOException {
        //if (query.equals(Query.ALL)) {
            // always returns the count of all features in the csv-file.
            getFeatureReader();
            int count = 0;
            while (featureReader.hasNext()) {
                featureReader.next(); // not necessary, but more future-proof.
                count++;
            }
            featureReader.close();
            featureReader = null;
            return count;
        //}
        //return super.getCount(query);
    }
}
