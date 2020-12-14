package ba.unsa.etf.rma.aktivnosti;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.BazaOpenHelper;
import ba.unsa.etf.rma.klase.Dogadaj;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt.dajTrenutnoVrijeme;

public class KvizoviAkt extends AppCompatActivity {
    private ListView lvKvizovi;
    private Spinner spPostojeceKategorije;

    public static ArrayList<Kategorija> kategorije = new ArrayList<>();
    public static ArrayList<Kviz> kvizovi = new ArrayList<>();
    public static ArrayList<Pitanje> pitanjaUFire = new ArrayList<>();
    public BazaOpenHelper baza;
    public ArrayList<Dogadaj> eventi = new ArrayList<>();
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 14;

    public boolean internetKonekcija = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baza = new BazaOpenHelper(this);
        //baza.delete(baza.getWritableDatabase());

        if(!radiLiNet()){
            internetKonekcija = false;
            kategorije = baza.getKategorije();
            kvizovi = baza.getKvizovi();
            pitanjaUFire = baza.getPitanja();
        }
        else{

            kategorije.clear();
            kvizovi.clear();
            pitanjaUFire.clear();

            try {
                kategorije = new DajKolekcijuKategorijaFire().execute().get();
                pitanjaUFire = new DajKolekcijuPitanjaFire().execute().get();
                kvizovi = new DajKvizoveIzBaze().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            azurirajBazuKategorije(kategorije);
            azurirajBazuKvizovi(kvizovi);
            azurirajBazuPitanja(pitanjaUFire);
        }



        if(kategorije.contains(new Kategorija("Dodaj kategoriju", "-1")))
            kategorije.remove(new Kategorija("Dodaj kategoriju", "-1"));

        if(!kategorije.contains(new Kategorija("Svi", "0")))
            kategorije.add(0,(new Kategorija("Svi", "0")));

        //SPINNER
        spPostojeceKategorije = (Spinner) findViewById(R.id.spPostojeceKategorije);
        final ArrayAdapter<Kategorija> kategorijeAdap = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        kategorijeAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPostojeceKategorije.setAdapter(kategorijeAdap);

        spPostojeceKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                dajTrazenuKategoriju(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //LISTVIEW
        lvKvizovi = (ListView) findViewById(R.id.lvKvizovi);
        ArrayAdapter<Kviz> kvizoviAdap = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, kvizovi);
        lvKvizovi.setAdapter(kvizoviAdap);

        if(!kvizovi.contains(new Kviz("Dodaj Kviz", new ArrayList<Pitanje>(), new Kategorija())))
            kvizovi.add(0, (new Kviz("Dodaj Kviz", new ArrayList<Pitanje>(), new Kategorija("Svi", "0"))));

        lvKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(kvizovi.get(i).getNaziv().equals("Dodaj Kviz")){
                    if(radiLiNet()) {
                        Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                        intent.putExtra("Novi", true);
                        KvizoviAkt.this.startActivity(intent);
                    }
                    else{
                        nemaNeta();
                    }
                }
                else{
                    provjeriPermisije();
                    if(daLiSePreklapa(eventi, dajVrijemeTrajanja(kvizovi.get(i)))){

                    }
                    else{
                        Intent intent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
                        Kviz novi = kvizovi.get(i);
                        intent.putExtra("kviz", novi);
                        KvizoviAkt.this.startActivity(intent);
                    }

                }
            }
        });

        lvKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(kvizovi.get(i).getNaziv().equals("Dodaj Kviz")) {
                    if(radiLiNet()) {
                        Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                        intent.putExtra("Novi", true);
                        KvizoviAkt.this.startActivity(intent);
                    }
                    else{
                        nemaNeta();
                    }
                }
                else {
                    if(radiLiNet()) {
                        Intent zaEdit = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                        zaEdit.putExtra("Edit", true);
                        zaEdit.putExtra("Kviz", kvizovi.get(i));
                        zaEdit.putExtra("Kategorija", kvizovi.get(i).getKategorija());
                        KvizoviAkt.this.startActivity(zaEdit);
                    }
                    else{
                        nemaNeta();
                    }
                }
                return false;
            }
        });

    }

    public class DajKvizoveIzBaze extends AsyncTask<Void, Void, ArrayList<Kviz>> {
        GoogleCredential credentials;
        ArrayList<Kviz> FireKvizovi = new ArrayList<>();

        @Override
        protected ArrayList<Kviz> doInBackground(Void... strings) {
            try {

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Kvizovi?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String rezultat = convertStreamToString(in);

                try {
                    JSONArray dokumenti = new JSONObject(rezultat).getJSONArray("documents");
                    for (int i = 0; i < dokumenti.length(); i++) {
                        JSONObject fields = dokumenti.getJSONObject(i).getJSONObject("fields");
                        String naziv = fields.getJSONObject("naziv").getString("stringValue");
                        String idKategorije = fields.getJSONObject("idKategorije").getString("stringValue");

                        ArrayList<String> pitanja = new ArrayList<>();
                        JSONArray nizOdgovori = fields.getJSONObject("pitanja").getJSONObject("arrayValue").getJSONArray("values");

                        for(int j = 0; j<nizOdgovori.length(); j++)
                            pitanja.add(nizOdgovori.getJSONObject(j).getString("stringValue"));

                        FireKvizovi.add(new Kviz(naziv, dajPitanja(pitanja), dajKategoriju(idKategorije)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return FireKvizovi;
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
    public class DajKolekcijuKategorijaFire extends AsyncTask<Void, Void, ArrayList<Kategorija>>{
        GoogleCredential credentials;
        ArrayList<Kategorija> FireKategorije = new ArrayList<>();
        @Override
        protected ArrayList<Kategorija> doInBackground(Void... arg0){
            try {

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();

                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Kategorije?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();

                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String rezultat = convertStreamToString(in);

                try {
                    JSONObject jo = new JSONObject(rezultat);
                    JSONArray items = jo.getJSONArray("documents");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject elementJSONNiza = items.getJSONObject(i).getJSONObject("fields");
                        JSONObject idIkonice = elementJSONNiza.getJSONObject("idIkonice");
                        JSONObject nazivKategorije = elementJSONNiza.getJSONObject("naziv");
                        FireKategorije.add(new Kategorija(nazivKategorije.getString("stringValue"),String.valueOf(idIkonice.getString("integerValue"))));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return FireKategorije;
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
    public class DajKolekcijuPitanjaFire extends AsyncTask<Void, Void, ArrayList<Pitanje>>{
        @Override
        public ArrayList<Pitanje> doInBackground(Void ... parameters){
            GoogleCredential credentials;
            ArrayList<Pitanje> temp = new ArrayList<>();
            try{

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Pitanja?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String rezultat = convertStreamToString(in);

                try {
                    JSONArray dokumenti = new JSONObject(rezultat).getJSONArray("documents");

                    for (int i = 0; i < dokumenti.length(); i++) {
                        JSONObject fields = dokumenti.getJSONObject(i).getJSONObject("fields");

                        int indexTacnog = fields.getJSONObject("indexTacnog").getInt("integerValue");
                        String nazivPitanja = fields.getJSONObject("naziv").getString("stringValue");

                        ArrayList<String> odgovori = new ArrayList<>();
                        JSONArray nizOdgovori = fields.getJSONObject("odgovori").getJSONObject("arrayValue").getJSONArray("values");

                        for(int j = 0; j<nizOdgovori.length(); j++)
                            odgovori.add(nizOdgovori.getJSONObject(j).getString("stringValue"));

                        temp.add(new Pitanje(nazivPitanja, "", odgovori, odgovori.get(indexTacnog)));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch(IOException e){
                e.printStackTrace();
            }

            return temp;
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


    public void nemaNeta(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(KvizoviAkt.this);
        alertDialogBuilder.setTitle("Offline rezim");
        alertDialogBuilder.setMessage("Nije moguce dodati/editovati kviz u Offline rezimu!");
        alertDialogBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog upozorenje = alertDialogBuilder.create();
        upozorenje.show();
    }
    public boolean radiLiNet()  {
        ConnectivityManager cm = (ConnectivityManager)getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public void preklapanjeDogadaja(int minute){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(KvizoviAkt.this);
        alertDialogBuilder.setTitle("Zabranjeno igranje kviza!");
        alertDialogBuilder.setMessage("Imate dogadaj u kalendaru za " + minute + " minuta!\"");
        alertDialogBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog upozorenje = alertDialogBuilder.create();
        upozorenje.show();
    }
    public boolean daLiSePreklapa(ArrayList<Dogadaj> events, int X){
        Date trenutnoVrijeme = dajTrenutnoVrijeme();
        Date Y = new Date();
        if(events == null) return false;
        for(Dogadaj d: events){
            Y = d.getVrijeme();
            if(provjeriPreklapanje(X,Y,trenutnoVrijeme, d.getVrijemeKraja())){
                preklapanjeDogadaja( Y.getMinutes()-trenutnoVrijeme.getMinutes());
                return true;
            }
        }
        return false;
    }
    public int dajVrijemeTrajanja(Kviz kviz){
        if(kviz.getPitanja() == null) return -1;
        int n = kviz.getPitanja().size();
        double x = (double)n / 2;
        return (int) Math.ceil(x);
    }
    public boolean provjeriPreklapanje(int X, Date Y, Date trenutnoVrijeme, Date vrijemeKraja){
        boolean preklop = false;

        Calendar vrijemeEventa = new GregorianCalendar();
        vrijemeEventa.setTime(Y);

        Calendar kraj = new GregorianCalendar();
        kraj.setTime(vrijemeKraja);

        Calendar present = new GregorianCalendar();
        present.setTime(trenutnoVrijeme);
        present.add(Calendar.MINUTE , X);

        if(vrijemeEventa.get(Calendar.YEAR) == present.get(Calendar.YEAR)
        && vrijemeEventa.get(Calendar.MONTH) == present.get(Calendar.MONTH)
        && vrijemeEventa.get(Calendar.DAY_OF_MONTH) == present.get(Calendar.DAY_OF_MONTH)){
            if(present.getTime().getTime() > vrijemeEventa.getTime().getTime())
                preklop = true;
        }

        present.add(Calendar.MINUTE, -X);
        if(vrijemeEventa.get(Calendar.YEAR) == kraj.get(Calendar.YEAR)
                && vrijemeEventa.get(Calendar.MONTH) == kraj.get(Calendar.MONTH)
                && vrijemeEventa.get(Calendar.DAY_OF_MONTH) == kraj.get(Calendar.DAY_OF_MONTH)){
            if(present.getTime().getTime() > kraj.getTime().getTime())
                preklop = false;
        }

        return preklop;
    }
    public void provjeriPermisije(){
        if (ContextCompat.checkSelfPermission(KvizoviAkt.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Nema permisije za citanje kalendara!");
            ActivityCompat.requestPermissions(KvizoviAkt.this, new String[]{Manifest.permission.READ_CALENDAR}, MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        } else {
            eventi = dajEvente();
        }
    }
    public void azurirajBazuKategorije(ArrayList<Kategorija> kategorijeFire){
        ArrayList<Kategorija> kategorijeBAZA = baza.getKategorije();
        for(Kategorija k: kategorijeFire)
            if(!kategorijeBAZA.contains(k))
                baza.dodajKategoriju(k);
    }
    public void azurirajBazuKvizovi(ArrayList<Kviz> kvizoviFire){
        ArrayList<Kviz> kvizoviBaza = baza.getKvizovi();
        for(Kviz k: kvizoviFire)
            if(!kvizoviBaza.contains(k))
                baza.dodajKviz(k);
    }
    public void azurirajBazuPitanja(ArrayList<Pitanje> pitanjaFire){
        ArrayList<Pitanje> pitanjaBAZA = baza.getPitanja();
        for(Pitanje p: pitanjaFire)
            if(!pitanjaBAZA.contains(p))
                baza.dodajPitanje(p);
    }
    public ArrayList<Dogadaj> dajEvente() {
        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.TITLE,          // 2
                CalendarContract.Instances.END
        };

        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_BEGIN_INDEX = 1;
        final int PROJECTION_TITLE_INDEX = 2;
        final int PROJECTION_END_INDEX = 3;

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(dajTrenutnoVrijeme().getYear(), dajTrenutnoVrijeme().getMonth(), dajTrenutnoVrijeme().getDate(), dajTrenutnoVrijeme().getHours(), dajTrenutnoVrijeme().getMinutes());
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2020,1,1,0,0);
        long endMillis = endTime.getTimeInMillis();


        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {"207"};

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        Cursor cur =  getContentResolver().query(builder.build(), INSTANCE_PROJECTION, null, null, null);

        ArrayList<Dogadaj> events = new ArrayList<>();
        while (cur.moveToNext()) {

            long eventID = cur.getLong(PROJECTION_ID_INDEX);
            long beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            String title = cur.getString(PROJECTION_TITLE_INDEX);
            long endVal = cur.getLong(PROJECTION_END_INDEX);

            Calendar pocetak = Calendar.getInstance();
            pocetak.setTimeInMillis(beginVal);

            Calendar kraj = Calendar.getInstance();
            kraj.setTimeInMillis(endVal);

            events.add(new Dogadaj(title,pocetak.getTime(),kraj.getTime()));
        }
        return events;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    eventi = dajEvente();
                } else {
                    //Korisnik nije dodijelio potrebne permisije
                }
                return;
            }

        }
    }

    @Override
    public void onBackPressed()  {
        finish();
        return;
    }
    public Kategorija dajKategoriju(String naziv){
        Kategorija vrati = new Kategorija();
        for(Kategorija x: kategorije){
            if(x.getNaziv().equals(naziv)) vrati = x;
        }
        return vrati;
    }
    public ArrayList<Pitanje> dajPitanja(ArrayList<String> naziviPitanja){
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        for(Pitanje p: pitanjaUFire){
            for(String naziv: naziviPitanja){
                if(p.getNaziv().equals(naziv)){
                    pitanja.add(p);
                }
            }
        }
        return pitanja;
    }
    public void dajTrazenuKategoriju(int pozicija){

        ArrayList<Kviz> pomocni = new ArrayList<>();
        ArrayAdapter adapterKvizovi;

        if(pozicija == 0 && kategorije.get(pozicija).getNaziv().equals("Svi")){
            adapterKvizovi = new ArrayAdapter <Kviz>(this,android.R.layout.simple_list_item_1, kvizovi);
            lvKvizovi.setAdapter(adapterKvizovi);
        }
        else{
            for (Kviz k : kvizovi){
                if(k.getNaziv().equals("Dodaj Kviz") || k.getKategorija().getNaziv().equals(kategorije.get(pozicija).getNaziv())){
                    pomocni.add(k);
                }
            }
            adapterKvizovi = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,pomocni);
            lvKvizovi.setAdapter(adapterKvizovi);
        }
        adapterKvizovi.notifyDataSetChanged();
    }
}

