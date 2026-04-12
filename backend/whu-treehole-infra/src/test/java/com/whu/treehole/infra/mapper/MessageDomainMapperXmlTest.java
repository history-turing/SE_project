package com.whu.treehole.infra.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;

class MessageDomainMapperXmlTest {

    @Test
    void shouldRemainWellFormedXml() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/mapper/MessageDomainMapper.xml")) {
            assertNotNull(inputStream, "MessageDomainMapper.xml should exist on test classpath");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.newDocumentBuilder().parse(inputStream);
        }
    }
}
