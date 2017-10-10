package nl.b3p.geotools.data.csv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStore;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * @author Gertjan Al, B3Partners
 * @author mprins
 */
public class CSVDataStore implements FileDataStore {

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

    @Override
    public List<Name> getNames() throws IOException {
        return Arrays.asList((Name) new NameImpl(getSchema().getTypeName()));
    }

    @Override
    public SimpleFeatureType getSchema(Name name) throws IOException {
        return getSchema();
    }

    @Override
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

    /**
     * filtering is not supported.
     */
    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query, Transaction t) throws IOException {
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

    protected List<Name> createTypeNames() throws IOException {
        Name typeName = new NameImpl(this.typename);
        return Collections.singletonList(typeName);
    }


    @Override
    public SimpleFeatureSource getFeatureSource() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SimpleFeatureSource getFeatureSource(String string) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SimpleFeatureSource getFeatureSource(Name name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createSchema(SimpleFeatureType t) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void updateSchema(SimpleFeatureType sft) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void updateSchema(Name name, SimpleFeatureType t) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void updateSchema(String string, SimpleFeatureType sft) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void removeSchema(Name name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void removeSchema(String string) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(Filter filter, Transaction t) throws IOException {
        throw new UnsupportedOperationException("Functie niet ondersteund voor alleen-lezen databron.");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(Transaction t) throws IOException {
        throw new UnsupportedOperationException("Functie niet ondersteund voor alleen-lezen databron.");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(Transaction t) throws IOException {
        throw new UnsupportedOperationException("Functie niet ondersteund voor alleen-lezen databron.");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String string, Filter filter, Transaction t) throws IOException {
        throw new UnsupportedOperationException("Functie niet ondersteund voor alleen-lezen databron.");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String string, Transaction t) throws IOException {
        throw new UnsupportedOperationException("Functie niet ondersteund voor alleen-lezen databron.");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(String string, Transaction t) throws IOException {
        throw new UnsupportedOperationException("Functie niet ondersteund voor alleen-lezen databron.");
    }

    @Override
    public LockingManager getLockingManager() {
        throw new UnsupportedOperationException("Functie niet ondersteund voor alleen-lezen databron.");
    }

    @Override
    public ServiceInfo getInfo() {
        DefaultServiceInfo serviceInfo = new DefaultServiceInfo();
        serviceInfo.setTitle("CSV DataStore");
        try {
            serviceInfo.setSource(this.url.toURI());
        } catch (URISyntaxException ex) {

        }
        return serviceInfo;
    }

    @Override
    public void dispose() {
        try {
            this.featureReader.close();
        } catch (IOException ex) {
            // ignore
        }
        this.featureReader = null;
    }

}
