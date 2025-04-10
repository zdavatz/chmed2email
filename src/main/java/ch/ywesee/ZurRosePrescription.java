package ch.ywesee;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ZurRosePrescription {
    enum DeliveryType {
        Patient, // 1
        Doctor, // 2
        Address, // 3
    }

    public ZonedDateTime issueDate;
    public ZonedDateTime validity;
    public String user = "";
    public String password = "";
    public String prescriptionNr = ""; // optional
    public DeliveryType deliveryType = DeliveryType.Patient;
    boolean ignoreInteractions;
    boolean interactionsWithOldPres;
    public String remark; // optional

    public ZurRosePrescriptorAddress prescriptorAddress;
    public ZurRosePatientAddress patientAddress;
//deliveryAddress // optional
//billingAddress // optional
//dailymed // optional

    ArrayList<ZurRoseProduct> products;

    public String toXML() throws IOException, XMLStreamException {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newDefaultFactory();
        StringWriter out = new StringWriter();
        XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(out);

        sw.writeStartDocument();
        sw.writeStartElement("prescription");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        sw.writeAttribute("issueDate", formatter.format(this.issueDate));
        sw.writeAttribute("validity",
                this.validity != null ? formatter.format(this.validity) :
                this.issueDate != null ? formatter.format(this.issueDate) : "");
        sw.writeAttribute("user", this.user);
        sw.writeAttribute("password", this.password);
        if (this.prescriptionNr != null) {
            sw.writeAttribute("prescriptionNr", this.prescriptionNr);
        }

        int deliveryTypeInt = 0;
        switch (this.deliveryType) {
            case Patient:
                deliveryTypeInt = 1;
                break;
            case Doctor:
                deliveryTypeInt = 2;
                break;
            case Address:
                deliveryTypeInt = 3;
                break;
        }

        sw.writeAttribute("deliveryType", Integer.toString(deliveryTypeInt));

        sw.writeAttribute("ignoreInteractions", this.ignoreInteractions ? "true" : "false");
        sw.writeAttribute("interactionsWithOldPres", this.interactionsWithOldPres ? "true" : "false");

        if (this.remark != null) {
            sw.writeAttribute("remark", this.remark);
        }

        if (this.prescriptorAddress != null) {
            this.prescriptorAddress.toXML(sw);
        }
        if (this.patientAddress != null) {
            this.patientAddress.toXML(sw);
        }

        if (this.products != null) {
            for (ZurRoseProduct product : this.products) {
                product.toXML(sw);
            }
        }

        sw.writeEndElement();
        sw.writeEndDocument();

        String xml = out.toString();
        return xml;
    }
}
