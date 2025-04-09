package ch.ywesee;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Objects;

public class ZurRoseAddress {
    public String title; // optional
    public int titleCode = -1; // optional
    public String lastName;
    public String firstName; // optional
    public String street;
    public String zipCode;
    public String city = "";
    public String kanton; // optional
    public String country; // optional
    public String phoneNrBusiness; // optional
    public String phoneNrHome; // optional
    public String faxNr; // optional
    public String email; // optional

    public void writeBodyToXMLElement(XMLStreamWriter sw) throws XMLStreamException {
        if (this.title != null) {
            sw.writeAttribute("title", this.title);
        };

        if (this.titleCode != -1) {
            sw.writeAttribute("titleCode", Integer.toString(this.titleCode));
        }

        sw.writeAttribute("lastName", Objects.requireNonNullElse(this.lastName, ""));

        if (this.firstName != null) {
            sw.writeAttribute("firstName", this.firstName);
        };

        sw.writeAttribute("street", Objects.requireNonNullElse(this.street, ""));
        sw.writeAttribute("zipCode", Objects.requireNonNullElse(this.zipCode, ""));
        sw.writeAttribute("city", Objects.requireNonNullElse(this.city, ""));

        sw.writeAttribute("kanton", Objects.requireNonNullElse(this.kanton, ""));
        if (this.country != null) {
            sw.writeAttribute("country", this.country);
        };
        if (this.phoneNrBusiness != null) {
            sw.writeAttribute("phoneNrBusiness", this.phoneNrBusiness);
        };
        if (this.phoneNrHome != null) {
            sw.writeAttribute("phoneNrHome", this.phoneNrHome);
        };
        if (this.faxNr != null) {
            sw.writeAttribute("faxNr", this.faxNr);
        };
        if (this.email != null) {
            sw.writeAttribute("email", this.email);
        };
    }
}
