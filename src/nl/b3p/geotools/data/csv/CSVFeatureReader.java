/*
 * $Id: CSVFeatureReader.java 9066 2008-09-30 15:01:19Z Richard $
 */
package nl.b3p.geotools.data.csv;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import nl.b3p.commons.csv.CsvFormatException;
import nl.b3p.commons.csv.CsvInputStream;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author Gertjan Al, B3Partners
 */
public class CSVFeatureReader implements FeatureReader {

    public static final int POINT_X = 0;
    public static final int POINT_Y = 1;
    private static final Log log = LogFactory.getLog(CSVDataStore.class);
    private GeometryFactory gf;
    private SimpleFeatureType ft;
    private Map<String, String[]> metadata = new HashMap<String, String[]>();
    private SimpleFeature feature;
    private CsvInputStream inputstream;
    private int featureId = 0;

    public CSVFeatureReader(URL url, String typeName, String srs) throws IOException {
        CountingInputStream cis = new CountingInputStream(url.openStream());
        inputstream = new CsvInputStream(new InputStreamReader(cis));

        createFeatureType(typeName, srs);
        gf = new GeometryFactory();
    }

    private void createFeatureType(String typeName, String srs) throws DataSourceException {
        CoordinateReferenceSystem crs = null;
        String[] csMetadata = metadata.get("coordinatesystem");
        if (csMetadata != null) {
            String wkt = csMetadata[0];
            try {
                /* parse WKT */
                CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
                crs = crsFactory.createFromWKT(wkt);
            } catch (Exception e) {
                throw new DataSourceException("Error parsing CoordinateSystem WKT: \"" + wkt + "\"");
            }
        }

        /* override srs when provided */
        if (srs != null) {
            try {
                crs = CRS.decode(srs);
            } catch (Exception e) {
                throw new DataSourceException("Error parsing CoordinateSystem srs: \"" + srs + "\"");
            }
        }

        try {

            List<String> columns = inputstream.readRecordAsList();
            SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();

            ftb.setName(typeName);
            ftb.setCRS(crs);
            ftb.add("the_geom", Geometry.class);

            // Skip first two columns, expected these to be X and Y values
            for (int i = 2; i < columns.size(); i++) {
                ftb.add(columns.get(i), String.class);
            }

            ft = ftb.buildFeatureType();

        } catch (Exception e) {
            throw new DataSourceException("Error creating SimpleFeatureType", e);
        }
    }

    public SimpleFeatureType getFeatureType() {
        return ft;
    }

    public SimpleFeature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        return feature;
    }

    public boolean hasNext() throws IOException {
        List<String> field;
        try {
            if ((field = inputstream.readRecordAsList()) == null) {
                return false;
            } else {
                SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);

                Coordinate coordinate = new Coordinate(Double.parseDouble(field.get(POINT_X)), Double.parseDouble(field.get(POINT_Y)));

                field.remove(0);
                field.remove(0);

                sfb.add(gf.createPoint(coordinate));
                sfb.addAll(field);
                feature = sfb.buildFeature(Integer.toString(featureId++));

                return true;
            }
        } catch (CsvFormatException e) {
            throw new IOException(e);
        }
    }

    public void close() throws IOException {
    }
}
