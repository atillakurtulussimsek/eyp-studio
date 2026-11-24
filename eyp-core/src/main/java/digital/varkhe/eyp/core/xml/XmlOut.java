package digital.varkhe.eyp.core.xml;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Thin StAX wrapper: UTF-8, no DTD, skips null optional values. */
final class XmlOut {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final XMLStreamWriter w;

    XmlOut() {
        try {
            w = XMLOutputFactory.newFactory().createXMLStreamWriter(buffer, StandardCharsets.UTF_8.name());
            w.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    XmlOut start(String prefix, String ns, String name) {
        try {
            w.writeStartElement(prefix, name, ns);
            return this;
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    XmlOut ns(String prefix, String ns) {
        try {
            if (prefix.isEmpty()) w.writeDefaultNamespace(ns);
            else w.writeNamespace(prefix, ns);
            return this;
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    XmlOut attr(String name, String value) {
        if (value == null) return this;
        try {
            w.writeAttribute(name, value);
            return this;
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    XmlOut attr(String prefix, String ns, String name, String value) {
        try {
            w.writeAttribute(prefix, ns, name, value);
            return this;
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    XmlOut text(String value) {
        try {
            w.writeCharacters(value);
            return this;
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    XmlOut end() {
        try {
            w.writeEndElement();
            return this;
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Writes {@code <prefix:name>value</prefix:name>} when value is not null. */
    XmlOut leaf(String prefix, String ns, String name, Object value) {
        if (value == null) return this;
        return start(prefix, ns, name).text(value.toString()).end();
    }

    byte[] finish() {
        try {
            w.writeEndDocument();
            w.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e);
        }
        return buffer.toByteArray();
    }
}
