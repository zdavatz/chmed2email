package ch.ywesee;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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

        sw.writeAttribute("lastName", this.lastName);

        if (this.firstName != null) {
            sw.writeAttribute("firstName", this.firstName);
        };

        sw.writeAttribute("street", this.street);
        if (this.zipCode != null) {
            sw.writeAttribute("zipCode", this.zipCode);
        } else {
            sw.writeAttribute("zipCode", "");
        }
        sw.writeAttribute("city", this.city);

        if (this.kanton != null) {
            sw.writeAttribute("kanton", this.kanton);
        } else {
            sw.writeAttribute("kanton", "");
        }
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
