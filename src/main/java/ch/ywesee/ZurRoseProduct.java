package ch.ywesee;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;

public class ZurRoseProduct {
    public String pharmacode; // optional
    public String eanId; // optional
    public String description_; // optional
    public boolean repetition;
    public int nrOfRepetitions; // optional, 0 - 99
    public int quantity = 0; // 0 - 999
    public String validityRepetition; // optional
    public int notSubstitutableForBrandName; // optional
    public String remark; // optional
    public int dailymed; // optional boolean
    public int dailymed_mo; // optional boolean
    public int dailymed_tu; // optional boolean
    public int dailymed_we; // optional boolean
    public int dailymed_th; // optional boolean
    public int dailymed_fr; // optional boolean
    public int dailymed_sa; // optional boolean
    public int dailymed_su; // optional boolean

    public String insuranceEanId; // optional
    public String insuranceBsvNr; // optional
    public String insuranceInsuranceName; // optional
    public int insuranceBillingType; // required
    public String insuranceInsureeNr; // optional

    public ArrayList<ZurRosePosology> posology;

    public ZurRoseProduct() {
        this.nrOfRepetitions = -1;
        this.notSubstitutableForBrandName = -1;
        this.dailymed = -1;
        this.dailymed_mo = -1;
        this.dailymed_tu = -1;
        this.dailymed_we = -1;
        this.dailymed_th = -1;
        this.dailymed_fr = -1;
        this.dailymed_sa = -1;
        this.dailymed_su = -1;
    }

    public void toXML(XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement("product");

        if (this.pharmacode != null) {
            sw.writeAttribute("pharmacode", this.pharmacode);
        }
        if (this.eanId != null) {
            sw.writeAttribute("eanId", this.eanId);
        }
        if (this.description_ != null) {
            sw.writeAttribute("description", this.description_);
        }
        sw.writeAttribute("repetition", this.repetition ? "true" : "false");
        if (this.nrOfRepetitions >= 0) {
            sw.writeAttribute("nrOfRepetitions", Integer.toString(this.nrOfRepetitions));
        }
        sw.writeAttribute("quantity", Integer.toString(this.quantity <= 0 ? 1 : this.quantity));
        if (this.validityRepetition != null) {
            sw.writeAttribute("validityRepetition", this.validityRepetition);
        }
        if (this.notSubstitutableForBrandName >= 0) {
            sw.writeAttribute("notSubstitutableForBrandName", Integer.toString(this.notSubstitutableForBrandName));
        }
        if (this.remark != null) {
            sw.writeAttribute("remark", this.remark);
        }
        if (this.dailymed != -1) {
            sw.writeAttribute("dailymed", this.dailymed == 1 ? "true" : "false");
        }
        if (this.dailymed_mo != -1) {
            sw.writeAttribute("dailymed_mo", this.dailymed_mo == 1 ? "true" : "false");
        }
        if (this.dailymed_tu != -1) {
            sw.writeAttribute("dailymed_tu", this.dailymed_tu == 1 ? "true" : "false");
        }
        if (this.dailymed_we != -1) {
            sw.writeAttribute("dailymed_we", this.dailymed_we == 1 ? "true" : "false");
        }
        if (this.dailymed_th != -1) {
            sw.writeAttribute("dailymed_th", this.dailymed_th == 1 ? "true" : "false");
        }
        if (this.dailymed_fr != -1) {
            sw.writeAttribute("dailymed_fr", this.dailymed_fr == 1 ? "true" : "false");
        }
        if (this.dailymed_sa != -1) {
            sw.writeAttribute("dailymed_sa", this.dailymed_sa == 1 ? "true" : "false");
        }
        if (this.dailymed_su != -1) {
            sw.writeAttribute("dailymed_su", this.dailymed_su == 1 ? "true" : "false");
        }

        sw.writeStartElement("insurance");

        sw.writeAttribute("eanId", this.insuranceEanId == null ? "1" : this.insuranceEanId);

        if (this.insuranceBsvNr != null) {
            sw.writeAttribute("bsvNr", this.insuranceBsvNr);
        }
        if (this.insuranceInsuranceName != null) {
            sw.writeAttribute("insuranceName", this.insuranceInsuranceName);
        }

        sw.writeAttribute("billingType", Integer.toString(this.insuranceBillingType));

        if (this.insuranceInsureeNr != null) {
            sw.writeAttribute("insureeNr", this.insuranceInsureeNr);
        }
        sw.writeEndElement();

        for (ZurRosePosology p : this.posology) {
            p.toXML(sw);
        }

        sw.writeEndElement();
    }
}
