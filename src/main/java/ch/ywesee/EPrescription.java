package ch.ywesee;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import jakarta.json.*;

public class EPrescription {
    static public class PatientId {
        public int type;
        public String value;
    }
    static public class PField {
        public String nm;
        public String value;
    }
    static public class TakingTime {
        public int off;
        public int du;
        public int doFrom;
        public int doTo;
        public int a;
        public int ma;
    }
    static public class Posology {
        public Instant dtFrom;
        public Instant dtTo;
        public int cyDu;
        public int inRes;
        public ArrayList<Integer> d;
        public ArrayList<TakingTime> tt;
    }
    static public class Medicament {
        public String appInstr;
        public String medicamentId;
        int idType;
        public String unit;
        int rep;
        int nbPack;
        int subs;
        ArrayList<Posology> pos;
    }
    public String auth;
    public Instant date;
    public String prescriptionId;
    public int medType;
    public String zsr;
    public ArrayList<PField> PFields;
    public String rmk;
    public String valBy; // The GLN of the healthcare professional who has validated the medication plan.
    public Instant valDt; // Date of validation

    public String patientFirstName;
    public String patientLastName;
    public Instant patientBirthdate;
    public int patientGender;
    public String patientStreet;
    public String patientCity;
    public String patientZip;
    public String patientLang; // Patient’s language (ISO 639-19 language code) (e.g. de)
    public String patientPhone;
    public String patientEmail;
    public String patientReceiverGLN;
    public ArrayList<PatientId> patientIds;
    public ArrayList<PField> patientPFields;

    public ArrayList<Medicament> medicaments;

    EPrescription(String chmed16aString) throws Exception {
        if (chmed16aString.startsWith("https://eprescription.hin.ch")) {
            int index = chmed16aString.indexOf("#");
            if (index != -1) {
                chmed16aString = chmed16aString.substring(index + 1);
                index = chmed16aString.indexOf("&");
                if (index != -1) {
                    chmed16aString = chmed16aString.substring(0, index);
                }
            }
        }

        Reader reader = null;
        String prefix = "CHMED16A1";
        if (chmed16aString.startsWith(prefix)) {
            chmed16aString = chmed16aString.substring(prefix.length());
            byte[] compressed = Base64.getDecoder().decode(chmed16aString);
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            BufferedReader bf = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
            reader = bf;
        } else {
            prefix = "CHMED16A0";
            if (chmed16aString.startsWith(prefix)) {
                chmed16aString = chmed16aString.substring(prefix.length());
                reader = new StringReader(chmed16aString);
            }
        }
        if (reader == null) {
            throw new Exception("Unexpected prefix");
        }
        JsonReader jsonReader = Json.createReader(reader);
        JsonObject jsonObj = jsonReader.readObject();

        this.auth = jsonObj.getString("Auth", null);
        this.date = this.parseDateString(jsonObj.getString("Dt", null));
        this.prescriptionId = jsonObj.getString("Id", null);
        this.medType = jsonObj.getInt("MedType");
        this.zsr = jsonObj.getString("Zsr", "");
        this.rmk = jsonObj.getString("Rmk", null);

        ArrayList<PField> pfields = new ArrayList<PField>();
        JsonArray jsonPfields = jsonObj.getJsonArray("PFields");
        if (jsonPfields != null) {
            for (int i = 0; i < jsonPfields.size(); i++) {
                JsonObject pfield = jsonPfields.getJsonObject(i);
                PField pf = new PField();
                pf.nm = pfield.getString("Nm", null);
                pf.value = pfield.getString("Val", null);
                pfields.add(pf);
            }
        }
        this.PFields = pfields;

        ArrayList<PatientId> patientIds = new ArrayList<PatientId>();
        JsonObject patient = jsonObj.getJsonObject("Patient");
        if (patient != null) {
            this.patientBirthdate = this.parseDateString(patient.getString("BDt", null));
            this.patientCity = patient.getString("City", null);
            this.patientFirstName = patient.getString("FName", null);
            this.patientLastName = patient.getString("LName", null);
            this.patientGender = patient.getInt("Gender", 1);
            this.patientPhone = patient.getString("Phone", null);
            this.patientStreet = patient.getString("Street", null);
            this.patientZip = patient.getString("Zip", null);
            this.patientEmail = patient.getString("Email", null);
            this.patientReceiverGLN = patient.getString("Rcv", null);
            this.patientLang = patient.getString("Lng", null);

            JsonArray jsonPatientIds = patient.getJsonArray("Ids");
            if (jsonPatientIds != null) {
                for (int i = 0; i < jsonPatientIds.size(); i++) {
                    JsonObject patientIdDict = jsonPatientIds.getJsonObject(i);
                    PatientId pid = new PatientId();
                    pid.value = patientIdDict.getString("Val", null);
                    pid.type = patientIdDict.getInt("Type");
                    patientIds.add(pid);
                }
            }
        }
        this.patientIds = patientIds;

        ArrayList<PField> patientPFields = new ArrayList<PField>();
        JsonArray jsonPatientPFields = patient.getJsonArray("PFields");
        if (jsonPatientPFields != null) {
            for (int i = 0; i < jsonPatientPFields.size(); i++) {
                JsonObject patientPField = jsonPatientPFields.getJsonObject(i);
                PField pf = new PField();
                pf.nm = patientPField.getString("Nm", null);
                pf.value = patientPField.getString("Val");
                patientPFields.add(pf);
            }
        }
        this.patientPFields = patientPFields;

        ArrayList<Medicament> medicaments = new ArrayList<Medicament>();
        JsonArray jsonMedicaments = jsonObj.getJsonArray("Medicaments");
        if (jsonMedicaments != null) {
            for (int i = 0; i < jsonMedicaments.size(); i++) {
                JsonObject medicament = jsonMedicaments.getJsonObject(i);
                Medicament m = new Medicament();
                m.appInstr = medicament.getString("AppInstr", null);
                m.medicamentId = medicament.getString("Id", null);
                m.idType = medicament.getInt("IdType", -1);
                m.unit = medicament.getString("Unit", null);
                m.rep = medicament.getInt("rep", 0);
                m.nbPack = medicament.getInt("NbPack", 1);
                m.subs = medicament.getInt("Subs", 0);

                ArrayList<Posology> pos = new ArrayList<Posology>();
                JsonArray medicamentPos = medicament.getJsonArray("Pos");
                if (medicamentPos != null) {
                    for (int j = 0; j < medicamentPos.size(); j++) {
                        JsonObject posDict = medicamentPos.getJsonObject(j);
                        Posology p = new Posology();

                        p.dtFrom = this.parseDateString(posDict.getString("DtFrom", null));
                        p.dtTo = this.parseDateString(posDict.getString("DtTo", null));
                        p.cyDu = posDict.getInt("CyDu", -1);
                        p.inRes = posDict.getInt("InRes", -1);

                        JsonArray jsonPosDictD = posDict.getJsonArray("D");
                        ArrayList<Integer> pd = new ArrayList<>();
                        if (jsonPosDictD != null) {
                            for (int k = 0; k < jsonPosDictD.size(); k++) {
                                pd.add(jsonPosDictD.getInt(k));
                            }
                        }
                        p.d = pd;

                        ArrayList<TakingTime> tts = new ArrayList<TakingTime>();
                        JsonArray posDictTt = posDict.getJsonArray("TT");
                        if (posDictTt != null) {
                            for (int k = 0; k < posDictTt.size(); k++) {
                                JsonObject ttDict = posDictTt.getJsonObject(k);
                                TakingTime tt = new TakingTime();
                                tt.off = ttDict.getInt("Off", -1);
                                tt.du = ttDict.getInt("Du", -1);
                                tt.doFrom = ttDict.getInt("DoFrom", -1);
                                tt.doTo = ttDict.getInt("DoTo", -1);
                                tt.a = ttDict.getInt("A", -1);
                                tt.ma = ttDict.getInt("MA", -1);
                                tts.add(tt);
                            }
                        }
                        p.tt = tts;
                        pos.add(p);
                    }
                }
                m.pos = pos;

                medicaments.add(m);
            }
        }
        this.medicaments = medicaments;
    }

    private Instant parseDateString(String str) {
        if (str == null) {
            return null;
        }
        // The specification says it's ISO8601, but I got a non-standard date as the sample input
        // here we try a few different date formats just in case
        Exception lastException = null;
        try {
            TemporalAccessor ta = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(str);
            return Instant.from(ta);
        } catch (DateTimeException e) {
            // no op
            lastException = e;
        }

        try {
            // Date only string, 1990-01-01
            Pattern pattern = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}$");
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                DateTimeFormatter formatter =
                        new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd")
                                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                                .parseDefaulting(ChronoField.INSTANT_SECONDS, 0)
                                .toFormatter();
                TemporalAccessor ta = formatter.parse(str);
                return Instant.from(ta);
            }
        } catch (DateTimeException e) {
            // no op
            lastException = e;
        }

        try {
            TemporalAccessor ta = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ssZ").parse(str);
            return Instant.from(ta);
        } catch (DateTimeException e) {
            // no op
            lastException = e;
        }

        // Handle invalid format, like: 2025-02-26T15:20:01+2:00
        Pattern regex = Pattern.compile("([\\+|\\-])([0-9]{1,2}):?([0-9]{1,2})$");
        Matcher m = regex.matcher(str);
        if (m.find()) {
            String pre = str.substring(0, m.start());
            String timeZoneOffsetMark = m.group(1);
            String timeZoneOffsetHour = m.group(2);
            String timeZoneOffsetMinutes = m.group(3);

            String newDateString1 = String.format("%s%s%02d%02d", pre, timeZoneOffsetMark, Integer.parseInt(timeZoneOffsetHour), Integer.parseInt(timeZoneOffsetMinutes));
            String newDateString2 = String.format("%s%s%02d:%02d", pre, timeZoneOffsetMark, Integer.parseInt(timeZoneOffsetHour), Integer.parseInt(timeZoneOffsetMinutes));

            try {
                TemporalAccessor ta = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ssZ").parse(newDateString1);
                return Instant.from(ta);
            } catch (DateTimeException e) {
                // no op
                lastException = e;
            }
            try {
                TemporalAccessor ta = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(newDateString2);
                return Instant.from(ta);
            } catch (DateTimeException e) {
                // no op
                lastException = e;
            }
        }

        if (!str.isEmpty() && lastException != null) {
            System.err.println("Cannot parse date string: " + str + ". Last error: " + lastException);
        }

        return null;
    }

    ZurRosePrescription toZurRosePrescription() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.from(ZoneOffset.UTC));
        ZurRosePrescription prescription = new ZurRosePrescription();

        prescription.issueDate = this.date;
        int random = Math.abs(new Random().nextInt());
        prescription.prescriptionNr = String.format("%09d", random).substring(0, 9);
        prescription.remark = this.rmk;
        prescription.validity = this.valDt; // ???

        prescription.user = "";
        prescription.password = "";
        prescription.deliveryType = ZurRosePrescription.DeliveryType.Patient;
        prescription.ignoreInteractions = false;
        prescription.interactionsWithOldPres = false;

        ZurRosePrescriptorAddress prescriptor = new ZurRosePrescriptorAddress();
        prescription.prescriptorAddress = prescriptor;
        prescriptor.zsrId = this.zsr;
        prescriptor.lastName = this.auth; // ???

        prescriptor.langCode = 1;
        prescriptor.clientNrClustertec = "888870";
        prescriptor.street = "";
        prescriptor.zipCode = "";
        prescriptor.city = "";
        prescriptor.eanId = "";

        String insuranceEan = null;
        String coverCardId = null;
        for (PatientId pid : this.patientIds) {
            if (pid.type == 1) {
                if (pid.value.length() == 13) {
                    insuranceEan = pid.value;
                } else if (pid.value.length() == 20 || pid.value.contains(".")) {
                    coverCardId = pid.value;
                }
            }
        }

        ZurRosePatientAddress patient = new ZurRosePatientAddress();
        prescription.patientAddress = patient;
        patient.lastName = this.patientLastName;
        patient.firstName = this.patientFirstName;
        patient.street = this.patientStreet;
        patient.city = this.patientCity;
        patient.kanton = "ZH"; // TODO: [self swissKantonFromZip:self.patientZip];
        patient.zipCode = this.patientZip;
        patient.birthday = this.patientBirthdate;
        patient.sex = this.patientGender; // same, 1 = m, 2 = f
        patient.phoneNrHome = this.patientPhone;
        patient.email = this.patientEmail;
        patient.langCode = 1; // de
        // TODO: get langauge
//        [self.patientLang.lowercaseString hasPrefix:@"de"] ? 1
//        : [self.patientLang.lowercaseString hasPrefix:@"fr"] ? 2
//        : [self.patientLang.lowercaseString hasPrefix:@"it"] ? 3
//        : 1;
        patient.patientNr = "";
        patient.coverCardId = coverCardId != null ? coverCardId : "";

        ArrayList<ZurRoseProduct> products = new ArrayList<>();
        for (Medicament medi : this.medicaments) {
            ZurRoseProduct product = new ZurRoseProduct();
            products.add(product);

            switch (medi.idType) {
                case 2:
                    // GTIN
                    product.eanId = medi.medicamentId;
                    break;
                case 3:
                    // Pharmacode
                    product.pharmacode = medi.medicamentId;
                    break;
            }
            product.quantity = medi.nbPack;
            product.remark = "";
            product.insuranceBillingType = 1;
            product.insuranceEanId = insuranceEan;

            boolean repetition = false;
            Instant validityRepetition = null;
            ArrayList<ZurRosePosology> poses = new ArrayList<>();
            ZurRosePosology pos = new ZurRosePosology();
            poses.add(pos);
            for (Posology mediPos : medi.pos) {
                if (!mediPos.d.isEmpty()) {
                    pos.qtyMorning = mediPos.d.get(0);
                    pos.qtyMidday = mediPos.d.get(1);
                    pos.qtyEvening = mediPos.d.get(2);
                    pos.qtyNight = mediPos.d.get(3);
                    pos.posologyText = medi.appInstr;
                }
                if (mediPos.dtTo != null) {
                    repetition = true;
                    validityRepetition = mediPos.dtTo;
                }
            }
            product.validityRepetition = validityRepetition == null ? "" : formatter.format(validityRepetition);
            product.repetition = repetition;
            product.posology = poses;
        }
        prescription.products = products;

        return prescription;
    }
}
