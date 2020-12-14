package ba.unsa.etf.rma.aktivnosti;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.klase.Dogadaj;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.RangListaKlasa;
import ba.unsa.etf.rma.klase.Score;


public class IgrajKvizAkt extends AppCompatActivity {

    public static Kviz kviz;
    public static ArrayList<Score> rangLista = new ArrayList<>();
    public static Score noviScore = new Score();
    public Date trenutnoVrijeme = new Date();
    public int vrijemeTrajanjaAlarma;

    public class KreirajDokumentRanglista extends AsyncTask<RangListaKlasa, Void, Void> {
        GoogleCredential credentials;

        @Override
        protected Void doInBackground(RangListaKlasa... params) {
            try {
                RangListaKlasa novaRanglista = params[0];

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();

                String TOKEN = credentials.getAccessToken();

                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Rangliste?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");



                String dokumentRangLista = "{\"fields\": {\"lista\": {\"mapValue\": {\"fields\": {";

                for(Map.Entry<Integer, Map<String, Double>> entry1 : novaRanglista.getLista().entrySet()){
                    dokumentRangLista += "\""+ entry1.getKey() + "\": { \"mapValue\": { \"fields\": {";
                    for(Map.Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
                        dokumentRangLista += "\""+entry2.getKey()+"\": { \"doubleValue\": \""+ entry2.getValue() + "\"}}}},";
                    }

                }
                dokumentRangLista+= "}}},\"nazivKviza\": {\"stringValue\": \"" +novaRanglista.getNazivKviza()+ "\"}}}";

                try(OutputStream os = conn.getOutputStream()){
                    byte[] input = dokumentRangLista.getBytes("UTF-8");
                    os.write(input, 0, input.length);

                }

                int code = conn.getResponseCode();
                InputStream odgovor = conn.getInputStream();
                try(BufferedReader br = new BufferedReader(new InputStreamReader(odgovor, "utf-8"))){
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while((responseLine = br.readLine()) != null){
                        response.append(responseLine.trim());
                    }
                    Log.d("ODGOVOR", response.toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    public class DajRangListeFire extends AsyncTask<Void, Void, ArrayList<RangListaKlasa>>{
        GoogleCredential credentials;
        ArrayList<RangListaKlasa> FireRangListe = new ArrayList<>();

        @Override
        protected ArrayList<RangListaKlasa> doInBackground(Void... strings) {
            try {

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Rangliste?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String rezultat = convertStreamToString(in);
                Map<Integer, Map<String, Double>> lista = new HashMap<>();
                Map<String, Double> listica = new HashMap<>();

                try {
                    JSONArray dokumenti = new JSONObject(rezultat).getJSONArray("documents");
                    for (int i = 0; i < dokumenti.length(); i++) {
                        JSONObject fields = dokumenti.getJSONObject(i).getJSONObject("fields");
                        String naziv = fields.getJSONObject("nazivKviza").getString("stringValue");
                        FireRangListe.add(new RangListaKlasa(naziv, null));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return FireRangListe;
        }
        private  String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);
        Intent intent = getIntent();

        kviz = (Kviz) intent.getSerializableExtra("kviz");
        noviScore.setNazivKviza(kviz.getNaziv());

        trenutnoVrijeme = dajTrenutnoVrijeme();
        vrijemeTrajanjaAlarma = dajVrijemeTrajanja();
        if(vrijemeTrajanjaAlarma != -1)
            postaviAlarm(trenutnoVrijeme.getHours(), trenutnoVrijeme.getMinutes()+vrijemeTrajanjaAlarma);

        InformacijeFrag informacijeFrag = new InformacijeFrag();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.informacijePlace,informacijeFrag).commit();

        PitanjeFrag pitanjeFrag = new PitanjeFrag();
        manager.beginTransaction().replace(R.id.pitanjePlace,pitanjeFrag).commit();

    }

    public void unesiIme(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unesite svoje ime: ");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noviScore.setNazivIgraca(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void postaviAlarm(int hours,int minutes) {

        Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
        i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        i.putExtra(AlarmClock.EXTRA_MESSAGE, "Gotov kviz!");
        i.putExtra(AlarmClock.EXTRA_HOUR, hours);
        i.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        startActivity(i);

    }

    public static Date dajTrenutnoVrijeme() {
        Date date = new Date();
        String strDateFormat = "hh:mm:ss a";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        return zaokruziNaMinutu(date);
    }

    public int dajVrijemeTrajanja(){
        if(kviz.getPitanja() == null) return -1;
        int n = kviz.getPitanja().size();
        double x = (double)n / 2;
        return (int) Math.ceil(x);
    }

    public static Date  zaokruziNaMinutu(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);

        if (c.get(Calendar.SECOND) >= 10)
            c.add(Calendar.MINUTE, 1);

        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    public void dajKalendare() {

        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.OWNER_ACCOUNT
        };

        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        final int PROJECTION_DISPLAY_NAME_INDEX = 2;
        final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

        ContentResolver contentResolver = getContentResolver();
        Cursor cur = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, null, null, null);

        ArrayList<String> calendarInfos = new ArrayList<>();
        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            String calendarInfo = String.format("Calendar ID: %s\nDisplay Name: %s\nAccount Name: %s\nOwner Name: %s", calID, displayName, accountName, ownerName);
            System.out.println("Kalendarinfo : " + calendarInfo);
            calendarInfos.add(calendarInfo);
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        return;
    }

}
