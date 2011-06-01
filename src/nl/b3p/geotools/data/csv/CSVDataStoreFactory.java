package nl.b3p.geotools.data.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 * @author Gertjan Al, B3Partners
 */
public class CSVDataStoreFactory implements FileDataStoreFactorySpi {

    public static final DataStoreFactorySpi.Param PARAM_URL = new Param("url", URL.class, "url to a .csv file");
    public static final DataStoreFactorySpi.Param PARAM_SRS = new Param("srs", String.class, "override srs");
    public static final DataStoreFactorySpi.Param PARAM_CHECKCOLUMNCOUNT = new Param("check_column_count", Boolean.class, "Check if columncount of a field is valid");
    public static final DataStoreFactorySpi.Param PARAM_SEPERATOR = new Param("seperator", String.class, "value seperator");
    public static final DataStoreFactorySpi.Param PARAM_COLUMN_X = new Param("column_x", Integer.class, "X column to use for creating a point geometry");
    public static final DataStoreFactorySpi.Param PARAM_COLUMN_Y = new Param("column_y", Integer.class, "y column to use for creating a point geometry");

    public String getDisplayName() {
        return "CSV File";
    }

    public String getDescription() {
        return "Comma Seperated Values";
    }

    public String[] getFileExtensions() {
        return new String[]{".csv"};
    }

    /**
     * @return true if the file of the f parameter exists
     */
    public boolean canProcess(URL f) {
        return f.getFile().toLowerCase().endsWith(".csv");
    }

    /**
     * @return true if srs can be resolved
     */
    public boolean canProcess(String srs) throws NoSuchAuthorityCodeException, FactoryException {
        return CRS.decode(srs) != null;
    }

    /**
     * @return true if the file in the url param exists
     */
    public boolean canProcess(Map params) {
        boolean result = false;
        if (params.containsKey(PARAM_URL.key)) {
            try {
                URL url = (URL) PARAM_URL.lookUp(params);
                result = canProcess(url);
            } catch (IOException ioe) {
                /* return false on any exception */
            }
        }
        if (result && params.containsKey(PARAM_SRS.key)) {
            try {
                String srs = (String) PARAM_SRS.lookUp(params);
                result = canProcess(srs);
            } catch (NoSuchAuthorityCodeException ex) {
                /* return false on any exception */
            } catch (FactoryException ex) {
                /* return false on any exception */
            } catch (IOException ioe) {
                /* return false on any exception */
            }
        }
        return result;
    }

    /*
     * Always returns true, no additional libraries needed
     */
    public boolean isAvailable() {
        return true;
    }

    public Param[] getParametersInfo() {
        return new Param[]{PARAM_URL};
    }

    public Map getImplementationHints() {
        /* XXX do we need to put something in this map? */
        return Collections.EMPTY_MAP;
    }

    public String getTypeName(URL url) throws IOException {
        return CSVDataStore.getURLTypeName(url);
    }

    public FileDataStore createDataStore(URL url) throws IOException {
        Map params = new HashMap();
        params.put(PARAM_URL.key, url);

        boolean isLocal = url.getProtocol().equalsIgnoreCase("file");
        if (isLocal && !(new File(url.getFile()).exists())) {
            throw new UnsupportedOperationException("Specified CSV file \"" + url + "\" does not exist, this plugin is read-only so no new file will be created");
        } else {
            return createDataStore(params);
        }
    }

    public FileDataStore createDataStore(Map params) throws IOException {
        if (!canProcess(params)) {
            throw new FileNotFoundException("Unable to process CSV file: " + params);
        }

        URL url = (URL) params.get(PARAM_URL.key);
        String srs = (String) params.get(PARAM_SRS.key);

        // Check if params contain keys (checkColumnCount, seperator, column_x, column_y), otherwise use default value
        char seperator = (params.containsKey(PARAM_SEPERATOR.key)
                ? (((String) params.get(PARAM_SEPERATOR.key)).length() == 1
                ? ((String) params.get(PARAM_SEPERATOR.key)).charAt(0)
                : ',')
                : ',');

        boolean checkColumnCount;
        if (params.containsKey(PARAM_CHECKCOLUMNCOUNT.key)) {
            if (params.get(PARAM_CHECKCOLUMNCOUNT.key) instanceof Boolean) {
                checkColumnCount = (Boolean) params.get(PARAM_CHECKCOLUMNCOUNT.key);
            } else if (params.get(PARAM_CHECKCOLUMNCOUNT.key) instanceof String) {
                checkColumnCount = ((String) params.get(PARAM_CHECKCOLUMNCOUNT.key)).toLowerCase().equals("true");
            } else {
                checkColumnCount = false;
            }
        } else {
            checkColumnCount = false;
        }

        int column_x, column_y;
        if (params.containsKey(PARAM_COLUMN_X.key)) {
            if (params.get(PARAM_COLUMN_X.key) instanceof Integer) {
                column_x = (Integer) params.get(PARAM_COLUMN_X.key);
            } else if (params.get(PARAM_COLUMN_X.key) instanceof String) {
                column_x = Integer.parseInt((String) params.get(PARAM_COLUMN_X.key));
            } else {
                column_x = -1;
            }
        } else {
            column_x = -1;
        }

        if (params.containsKey(PARAM_COLUMN_Y.key)) {
            if (params.get(PARAM_COLUMN_Y.key) instanceof Integer) {
                column_y = (Integer) params.get(PARAM_COLUMN_Y.key);
            } else if (params.get(PARAM_COLUMN_Y.key) instanceof String) {
                column_y = Integer.parseInt((String) params.get(PARAM_COLUMN_Y.key));
            } else {
                column_y = -1;
            }
        } else {
            column_y = -1;
        }

        return new CSVDataStore(url, srs, checkColumnCount, seperator, column_x, column_y);
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException("This plugin is read-only");
    }
}
