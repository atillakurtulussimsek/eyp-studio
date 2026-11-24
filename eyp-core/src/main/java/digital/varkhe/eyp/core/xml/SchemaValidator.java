package digital.varkhe.eyp.core.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Validates component XML against the bundled (patched) EYP 2.x schemas. */
public final class SchemaValidator {

    public enum Kind {
        USTVERI("EY-Ustveri-2.xsd"),
        NIHAI_USTVERI("EY-NihaiUstveri-2.xsd"),
        PAKET_OZETI("EY-PaketOzeti-2.xsd"),
        NIHAI_OZET("EY-NihaiOzet-2.xsd");

        final String file;

        Kind(String file) {
            this.file = file;
        }
    }

    private static final Map<Kind, Schema> CACHE = new ConcurrentHashMap<>();

    /** Returns schema violations; empty list means valid. */
    public static List<String> validate(Kind kind, byte[] xml) {
        List<String> errors = new ArrayList<>();
        try {
            Validator v = schema(kind).newValidator();
            v.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            v.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            v.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                }

                @Override
                public void error(SAXParseException e) {
                    errors.add(format(e));
                }

                @Override
                public void fatalError(SAXParseException e) {
                    errors.add(format(e));
                }
            });
            v.validate(new StreamSource(new ByteArrayInputStream(xml)));
        } catch (SAXException e) {
            if (errors.isEmpty()) errors.add(e.getMessage());
        } catch (IOException e) {
            errors.add(e.getMessage());
        }
        return errors;
    }

    private static String format(SAXParseException e) {
        return "satır " + e.getLineNumber() + ": " + e.getMessage();
    }

    private static Schema schema(Kind kind) throws SAXException {
        Schema s = CACHE.get(kind);
        if (s != null) return s;
        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // Bundled schemas import each other from the classpath (file: or jar:); nothing is fetched over the network.
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file,jar");
        f.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        URL url = SchemaValidator.class.getResource("/digital/varkhe/eyp/core/xsd/" + kind.file);
        if (url == null) throw new IllegalStateException("Missing bundled schema " + kind.file);
        s = f.newSchema(url);
        CACHE.put(kind, s);
        return s;
    }

    private SchemaValidator() {
    }
}
