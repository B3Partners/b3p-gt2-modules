package nl.b3p.geotools.data.dxf.entities;

import java.io.EOFException;
import nl.b3p.geotools.data.dxf.parser.DXFLineNumberReader;
import java.io.IOException;


import nl.b3p.geotools.data.dxf.parser.DXFUnivers;
import nl.b3p.geotools.data.dxf.header.DXFLayer;
import nl.b3p.geotools.data.dxf.parser.DXFCodeValuePair;
import nl.b3p.geotools.data.dxf.parser.DXFGroupCode;
import nl.b3p.geotools.data.dxf.parser.DXFParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DXFVertex extends DXFPoint {

    private static final Log log = LogFactory.getLog(DXFVertex.class);
    private static final long serialVersionUID = 1L;
    protected double _bulge = 0;

    public DXFVertex(double x, double y, double b, int c, DXFLayer l, DXFPolyline refPolyline, int visibility) {
        super(x, y, c, l, visibility, 1);
        _bulge = b;
    }

    public DXFVertex() {
        super(0, 0, -1, null, 0, 1);
    }

    public DXFVertex(DXFVertex v) {
        this._bulge = v._bulge;
        this._color = v._color;
        this._point = v._point;
        this._refLayer = v._refLayer;
    }

    public DXFVertex(DXFVertex orig, boolean bis) {
        super(orig._point.x, orig._point.y, orig._color, orig._refLayer, 0, 1);
        _bulge = orig._bulge;
    }

    public static DXFVertex read(DXFLineNumberReader br, DXFUnivers univers, DXFPolyline p) throws IOException {
        DXFLayer l = null;
        int visibility = 0, c = -1;
        double x = 0, y = 0, b = 0;

        int sln = br.getLineNumber();
        log.debug(">>Enter at line: " + sln);

        DXFCodeValuePair cvp = null;
        DXFGroupCode gc = null;

        boolean doLoop = true;
        while (doLoop) {
            cvp = new DXFCodeValuePair();
            try {
                gc = cvp.read(br);
            } catch (DXFParseException ex) {
                throw new IOException("DXF parse error", ex);
            } catch (EOFException e) {
                doLoop = false;
                break;
            }

            switch (gc) {
                case TYPE:
                    String type = cvp.getStringValue();
                    // geldt voor alle waarden van type
                    br.reset();
                    doLoop = false;
                    break;
                case LAYER_NAME: //"8"
                    l = univers.findLayer(cvp.getStringValue());
                    break;
                case DOUBLE_3: //"42"
                    b = cvp.getDoubleValue();
                    break;
                case X_1: //"10"
                    x = cvp.getDoubleValue();
                    break;
                case Y_1: //"20"
                    y = cvp.getDoubleValue();
                    break;
                case COLOR: //"62"
                    c = cvp.getShortValue();
                    break;
                case VISIBILITY: //"60"
                    visibility = cvp.getShortValue();
                    break;
                default:
                    break;
            }

        }

        DXFVertex e = new DXFVertex(x, y, b, c, l, p, visibility);
        e.setType(DXFEntity.TYPE_UNSUPPORTED);
        e.setStartingLineNumber(sln);

        log.debug(e.toString());
        return e;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(" [");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append(": ");
        s.append(", ");
        s.append("]");
        return s.toString();
    }
}
