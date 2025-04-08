package ch.ywesee;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class ZurRosePrescriptorAddress extends ZurRoseAddress {
    public int langCode; // 1 = de, 2 = fr, 3 = it
    public String clientNrClustertec;
    public String zsrId;
    public String eanId; // optional

    public void toXML(XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement("prescriptorAddress");
        super.writeBodyToXMLElement(sw);
        sw.writeAttribute("langCode", Integer.toString(this.langCode));
        sw.writeAttribute("clientNrClustertec", this.clientNrClustertec);
        sw.writeAttribute("zsrId", this.zsrId);
        if (this.eanId != null) {
            sw.writeAttribute("eanId", this.eanId);
        }
        sw.writeEndElement();
    }
}
