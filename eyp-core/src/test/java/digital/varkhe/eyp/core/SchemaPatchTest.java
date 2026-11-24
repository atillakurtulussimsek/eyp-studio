package digital.varkhe.eyp.core;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Documents why the bundled XSDs drop the ^...$ anchors: in XML Schema regexes these are literal characters,
 * so the official ST_GUID/ST_TCKN/ST_KKK patterns reject every valid value.
 */
class SchemaPatchTest {

    private static void validate(String pattern, String value) throws Exception {
        String xsd = "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='v'><xs:simpleType>"
                + "<xs:restriction base='xs:string'><xs:pattern value='" + pattern + "'/></xs:restriction>"
                + "</xs:simpleType></xs:element></xs:schema>";
        var schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(new StringReader(xsd)));
        schema.newValidator().validate(new StreamSource(new StringReader("<v>" + value + "</v>")));
    }

    @Test
    void anchoredOfficialPatternRejectsValidGuid() {
        String guid = "5F0C8A2E-3B7D-4C1E-9A6F-0D2B4E8C1A73";
        String official = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
        assertThrows(SAXException.class, () -> validate(official, guid));
        assertDoesNotThrow(() -> validate(official.substring(1, official.length() - 1), guid));
    }
}
