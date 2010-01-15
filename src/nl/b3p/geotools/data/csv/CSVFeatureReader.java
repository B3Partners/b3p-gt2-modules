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
import java.util.Arrays;
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

    private static final Log log = LogFactory.getLog(CSVDataStore.class);
    private GeometryFactory gf;
    private SimpleFeatureType ft;
    private Map<String, String[]> metadata = new HashMap<String, String[]>();
    private SimpleFeature feature;
    private CsvInputStream inputstream;
    private int featureId = 0;
    private int column_x, column_y;
    private int remove_x, remove_y;
    private char seperator;

    public CSVFeatureReader(URL url, String typeName, String srs, boolean checkColumnCount, char seperator, int column_x, int column_y) throws IOException {
        CountingInputStream cis = new CountingInputStream(url.openStream());
        inputstream = new CsvInputStream(new InputStreamReader(cis));
        inputstream.setCheckColumnCount(checkColumnCount);
        inputstream.setSeparator(seperator);
        this.seperator = seperator;

        this.column_x = column_x;
        this.column_y = column_y;

        if (column_x < column_y) {
            remove_x = column_x;
            remove_y = column_y - 1;

        } else if (column_x > column_y) {
            remove_x = column_x - 1;
            remove_y = column_y;

        } else {
            throw new IOException("X column and Y column have the same value.");
        }

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

            if (columns.size() == 1) {
                String[] values = null;

                if (seperator == ',') {
                    values = columns.get(0).split(";");
                    inputstream.setSeparator(';');
                } else if (seperator == ';') {
                    values = columns.get(0).split(",");
                    inputstream.setSeparator(',');
                } else {
                    throw new IOException("Please specify a seperator value, columncount returned 1");
                }

                if (values.length == 1) {
                    // Column seperator not found
                    throw new IOException("Invalid septerator ',' & ';' didn't work");
                }
                columns = Arrays.asList(values);
            }

            ftb.setName(typeName);
            ftb.setCRS(crs);
            ftb.add("the_geom", Geometry.class);

            // Skip first two columns, expected these to be X and Y values
            for (int i = 0; i < columns.size(); i++) {
                if (i != column_x && i != column_y) {
                    ftb.add(columns.get(i), String.class);
                }
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

                Coordinate coordinate = new Coordinate(fixDecimals(field.get(column_x)), fixDecimals(field.get(column_y)));

                // Remove first two columns
                field.remove(remove_x);
                field.remove(remove_y);

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

    private static double fixDecimals(String value) {
        value = value.trim();
        if (value.contains(",")) {
            if (value.contains(".")) {
                value = value.replaceAll("[.]", "");
            }
            value = value.replaceAll("[,]", ".");
        }
        return Double.parseDouble(value);
    }
}
