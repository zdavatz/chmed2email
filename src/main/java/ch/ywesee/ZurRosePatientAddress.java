package ch.ywesee;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZurRosePatientAddress extends ZurRoseAddress {
    public ZonedDateTime birthday;
    public int langCode; // 1 = de, 2 = fr, 3 = it
    public String coverCardId; // optional
    public int sex; // 1 = m, 2 = f
    public String patientNr;
    public String phoneNrMobile; // optional
    public String room; // optional
    public String section; // optional

    public void toXML(XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement("patientAddress");
        super.writeBodyToXMLElement(sw);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (this.birthday != null) {
            sw.writeAttribute("birthday", formatter.format(this.birthday));
        } else {
            sw.writeAttribute("birthday", "");
        }

        sw.writeAttribute("langCode", Integer.toString(this.langCode));

        if (this.coverCardId != null) {
            sw.writeAttribute("coverCardId", this.coverCardId);
        }

        sw.writeAttribute("sex", Integer.toString(this.sex));

        sw.writeAttribute("patientNr", this.patientNr);

        if (this.phoneNrMobile != null) {
            sw.writeAttribute("phoneNrMobile", this.phoneNrMobile);
        }
        if (this.room != null) {
            sw.writeAttribute("room", this.room);
        }
        if (this.section != null) {
            sw.writeAttribute("section", this.section);
        }

        sw.writeEndElement();
    }
}
