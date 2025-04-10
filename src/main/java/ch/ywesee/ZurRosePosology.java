package ch.ywesee;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class ZurRosePosology {
    public int qtyMorning = -1; // optional, -1 = null
    public int qtyMidday = -1; // optional, -1 = null
    public int qtyEvening = -1; // optional, -1 = null
    public int qtyNight = -1; // optional, -1 = null
    public String qtyMorningString; // optional
    public String qtyMiddayString; // optional
    public String qtyEveningString; // optional
    public String qtyNightString; // optional
    public String posologyText; // optional
    public int label = -1; // optional, boolean, -1 = null

    public void toXML(XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement("posology");

    if (this.qtyMorning != -1) {
        sw.writeAttribute("qtyMorning", Integer.toString(this.qtyMorning));
    }
    if (this.qtyMidday != -1) {
        sw.writeAttribute("qtyMidday", Integer.toString(this.qtyMidday));
    }
    if (this.qtyEvening != -1) {
        sw.writeAttribute("qtyEvening", Integer.toString(this.qtyEvening));
    }
    if (this.qtyNight != -1) {
        sw.writeAttribute("qtyNight", Integer.toString(this.qtyNight));
    }
    if (this.qtyMorningString != null) {
        sw.writeAttribute("qtyMorningString", this.qtyMorningString);
    }
    if (this.qtyMiddayString != null) {
        sw.writeAttribute("qtyMiddayString", this.qtyMiddayString);
    }
    if (this.qtyEveningString != null) {
        sw.writeAttribute("qtyEveningString", this.qtyEveningString);
    }
    if (this.qtyNightString != null) {
        sw.writeAttribute("qtyNightString", this.qtyNightString);
    }
    if (this.posologyText != null) {
        sw.writeAttribute("posologyText", this.posologyText);
    }
    if (this.label != -1) {
        sw.writeAttribute("label", this.label == 1 ? "true" : "false");
    }

        sw.writeEndElement();
    }
}
