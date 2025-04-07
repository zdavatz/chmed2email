package ch.ywesee;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import jakarta.json.*;

public class EPrescription {
    class PatientId {
        public int type;
        public String value;
    }
    class PField {
        public String nm;
        public String value;
    }
    class TakingTime {
        public int off;
        public int du;
        public int doFrom;
        public int doTo;
        public int a;
        public int ma;
    }
    class Posology {
        public Instant dtFrom;
        public Instant dtTo;
        public int cyDu;
        public int inRes;
        public ArrayList<Integer> d;
        public ArrayList<TakingTime> tt;
    }
    class Medicament {
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
        this.zsr = jsonObj.getString("Zsr", null);
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
        }

        ArrayList<PatientId> patientIds = new ArrayList<PatientId>();
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
        if (str == null || !(str instanceof String)) {
            return null;
        }
        Exception lastException = null;
        try {
            TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(str);
            return Instant.from(ta);
        } catch (DateTimeException e) {
            // no op
            lastException = e;
        }

        // The specification says it's ISO8601, but I got a non-standard date as the sample input
        try {
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

        Pattern regex = Pattern.compile("([\\+|\\-])([0-9]{1,2}):?([0-9]{1,2})$");
        Matcher m = regex.matcher(str);
        if (m.find()) {
            String pre = str.substring(0, m.start());
            String timeZoneOffsetMark = m.group(1);
            String timeZoneOffsetHour = m.group(2);
            String timeZoneOffsetMinutes = m.group(3);

            String newDateString = String.format("%s%s%02d%02d", pre, timeZoneOffsetMark, Integer.parseInt(timeZoneOffsetHour), Integer.parseInt(timeZoneOffsetMinutes));

            try {
                TemporalAccessor ta = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ssZ").parse(newDateString);
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
}
