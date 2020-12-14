package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.kategorije;
import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.kvizovi;

public class DodajKvizAkt extends AppCompatActivity {

    private ListView lvDodanaPitanja;
    private ListView lvMogucaPitanja;
    private Spinner spKategorije;
    private EditText etNaziv;
    private Button btnDodajKviz;
    private Button btnImportKviz;
    private ArrayAdapter<Kategorija> kategorijeAdap;
    private Kviz neki = new Kviz();
    private Pitanje zaDodatnoPitanje = new Pitanje("Dodaj pitanje", null, null, null);
    private static final int EDIT_REQUEST_CODE = 14;
    public static ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();
    public static ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();
    public  ArrayList<Pitanje> pitanjaUBAZI = new ArrayList<>();
    public  ArrayAdapter<Pitanje> mogucaAdap;
    public  ArrayAdapter<Pitanje> dodanaAdap;
    public AlertDialog alertDialog;
    private boolean boolImport = false;
    private boolean internetKonekcija = true;


    public class KreirajDokumentKviz extends AsyncTask<Kviz, Void, Void> {
        GoogleCredential credentials;

        @Override
        protected Void doInBackground(Kviz... params) {
            try {
                Kviz novi = params[0];

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();

                String TOKEN = credentials.getAccessToken();

                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Kvizovi?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                String dokumentKviz = new String();
                try {
                    JSONObject dokument = new JSONObject();
                    JSONArray values =  new JSONArray();

                    for(Pitanje p: novi.getPitanja()){
                        JSONObject element = new JSONObject();
                        element.put("stringValue", p.getNaziv());
                        values.put(element);
                    }
                    JSONObject fields = new JSONObject();

                    dokument.put("fields", fields);
                    fields.put("naziv", new JSONObject().put("stringValue", novi.getNaziv()));
                    fields.put("idKategorije", new JSONObject().put("stringValue", novi.getKategorija().getNaziv()));
                    fields.put("pitanja", new JSONObject().put("arrayValue", new JSONObject().put("values", values)));
                    dokumentKviz = "{\"fields\":" + fields.toString() + "}" ;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try(OutputStream os = conn.getOutputStream()){
                    byte[] input = dokumentKviz.getBytes("UTF-8");
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

    public class PromjenaKviza extends AsyncTask<Kviz, Void, Void>{
        GoogleCredential credentials;

        @Override
        protected Void doInBackground(Kviz... params) {
            try {
                Kviz novi = params[0];

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();

                String TOKEN = credentials.getAccessToken();

                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Kvizovi?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                String dokumentKviz = new String();
                try {
                    JSONObject dokument = new JSONObject();
                    JSONArray values =  new JSONArray();

                    for(Pitanje p: novi.getPitanja()){
                        JSONObject element = new JSONObject();
                        element.put("stringValue", p.getNaziv());
                        values.put(element);
                    }
                    JSONObject fields = new JSONObject();

                    dokument.put("fields", fields);
                    fields.put("naziv", new JSONObject().put("stringValue", novi.getNaziv()));
                    fields.put("idKategorije", new JSONObject().put("stringValue", novi.getKategorija().getNaziv()));
                    fields.put("pitanja", new JSONObject().put("arrayValue", new JSONObject().put("values", values)));
                    dokumentKviz = "{\"fields\":" + fields.toString() + "}" ;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try(OutputStream os = conn.getOutputStream()){
                    byte[] input = dokumentKviz.getBytes("UTF-8");
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

    public class KreirajDokumentKategorija extends AsyncTask<Kategorija, Void, Void> {
        GoogleCredential credentials;

        @Override
        protected Void doInBackground(Kategorija... params) {
            try {
                Kategorija nova = params[0];

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();

                String TOKEN = credentials.getAccessToken();

                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Kategorije?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                String dokument = "{ \"fields\": {\"idIkonice\": {\"integerValue\": \"" + Integer.parseInt(nova.getId()) + "\"},\"naziv\": {\"stringValue\": \""+ nova.getNaziv() + "\"}}}";

                try(OutputStream os = conn.getOutputStream()){
                    byte[] input = dokument.getBytes("utf-8");
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

    public class DajKolekcijuPitanja extends AsyncTask<Void, Void, ArrayList<Pitanje>>{
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        if(!radiLiNet()) internetKonekcija = false;

        try {
            pitanjaUBAZI = new DajKolekcijuPitanja().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lvDodanaPitanja = (ListView)findViewById(R.id.lvDodanaPitanja);
        lvMogucaPitanja = (ListView)findViewById(R.id.lvMogucaPitanja);
        spKategorije = (Spinner) findViewById(R.id.spKategorije);
        etNaziv = (EditText)findViewById(R.id.etNaziv);
        btnDodajKviz = (Button)findViewById(R.id.btnDodajKviz);
        btnImportKviz = (Button)findViewById(R.id.btnImportKviz);

        final Kategorija dodajKategoriju = new Kategorija("Dodaj kategoriju", "-1");

        if(!kategorije.contains(dodajKategoriju))
            kategorije.add(dodajKategoriju);

        kategorijeAdap = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        kategorijeAdap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKategorije.setAdapter(kategorijeAdap);
        kategorijeAdap.notifyDataSetChanged();



        final Kategorija nova = new Kategorija();

        spKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(radiLiNet()) {
                    Intent intent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                    nova.setId(kategorije.get(i).getId());
                    nova.setNaziv(kategorije.get(i).getNaziv());
                    if (kategorije.get(i).getNaziv().equals("Dodaj kategoriju")) {
                        DodajKvizAkt.this.startActivity(intent);
                    }
                }
                else{
                    nemaNetaKategorije();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        //Importovanje kviza
        btnImportKviz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(intent, EDIT_REQUEST_CODE);
            }
        });


        btnDodajKviz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean proslo = false;
                Kviz novi = new Kviz();
                novi.setNaziv(etNaziv.getText().toString());
                novi.setKategorija(nova);
                novi.setPitanja(dodanaPitanja);
                if (etNaziv.getText().toString().equals("") || etNaziv.getText().toString().equals("Naziv kviza - INPUT")) {
                    etNaziv.setBackgroundColor(getResources().getColor(R.color.red));
                } else if (dodanaPitanja.size() == 1) {
                    lvDodanaPitanja.setBackgroundColor(getResources().getColor(R.color.red));
                } else{
                    etNaziv.setBackgroundColor(getResources().getColor(R.color.white));
                    lvDodanaPitanja.setBackgroundColor(getResources().getColor(R.color.white));
                    proslo =true;
                }

                for(Kviz k : kvizovi){
                    if(k.getNaziv().equals(etNaziv.getText().toString())){
                        etNaziv.setBackgroundColor(getResources().getColor(R.color.red));
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DodajKvizAkt.this);
                        alertDialogBuilder.setMessage("Uneseni kviz već postoji!");
                        AlertDialog upozorenje = alertDialogBuilder.create();
                        upozorenje.show();
                        proslo=false;
                    }
                }

                if(novi.getKategorija().getNaziv().equals("Dodaj kategoriju")){
                    proslo = false;
                    spKategorije.setBackgroundColor(getResources().getColor(R.color.red));
                }
                else{
                    spKategorije.setBackgroundColor(getResources().getColor(R.color.white));
                }
                if(proslo || getIntent().hasExtra("Edit")){

                    kvizovi.add(novi);
                    novi.obrisiPitanje("Dodaj pitanje");

                    if(getIntent().hasExtra("Novi"))
                        new KreirajDokumentKviz().execute(novi);

                    if(getIntent().hasExtra("Edit"))
                        new PromjenaKviza().execute(novi);

                    kategorije.remove(dodajKategoriju);
                    Intent vratiKvizNaPocetnu =new Intent(DodajKvizAkt.this,KvizoviAkt.class);
                    if(!kategorije.contains(nova)){
                        kategorije.add(nova);
                        new KreirajDokumentKategorija().execute(nova);
                    }
                    DodajKvizAkt.this.startActivity(vratiKvizNaPocetnu);
                }
            }
        });

        Intent vrijednosti = getIntent();
        if(dodanaPitanja.size() == 0) {
            dodanaPitanja.add(zaDodatnoPitanje);
        }
        dodanaAdap = new ArrayAdapter<Pitanje>(this, android.R.layout.simple_list_item_1, dodanaPitanja);
        lvDodanaPitanja.setAdapter(dodanaAdap);
        dodanaAdap.notifyDataSetChanged();

        if (vrijednosti.hasExtra("Novi")) {


            dodanaPitanja.clear();
            dodanaPitanja.add(zaDodatnoPitanje);

            dodanaAdap = new ArrayAdapter<Pitanje>(this, android.R.layout.simple_list_item_1, dodanaPitanja);
            lvDodanaPitanja.setAdapter(dodanaAdap);
            dodanaAdap.notifyDataSetChanged();

            mogucaPitanja = pitanjaUBAZI;
            mogucaAdap = new ArrayAdapter<Pitanje>(this, android.R.layout.simple_list_item_1, mogucaPitanja);
            lvMogucaPitanja.setAdapter(mogucaAdap);
            mogucaAdap.notifyDataSetChanged();

        }
        if (vrijednosti.hasExtra("Edit")) {

            neki = (Kviz)vrijednosti.getSerializableExtra("Kviz");
            Kategorija nekog = (Kategorija)vrijednosti.getSerializableExtra("Kategorija");
            spKategorije.setSelection((kategorije.indexOf(nekog)));
            etNaziv.setText(neki.getNaziv());
            dodanaPitanja = dajDodana(neki);
            dodanaAdap = new ArrayAdapter<Pitanje>(this, android.R.layout.simple_list_item_1, dodanaPitanja);
            lvDodanaPitanja.setAdapter(dodanaAdap);
            dodanaAdap.notifyDataSetChanged();

            mogucaPitanja = pitanjaUBAZI;
            dajMogucaIZBAZEEdit(neki);
            mogucaAdap = new ArrayAdapter<Pitanje>(this, android.R.layout.simple_list_item_1, mogucaPitanja);
            lvMogucaPitanja.setAdapter(mogucaAdap);
            mogucaAdap.notifyDataSetChanged();
        }



        lvMogucaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lvDodanaPitanja.setBackgroundColor(getResources().getColor(R.color.white));
                dodanaPitanja.add(mogucaPitanja.get(i));
                mogucaPitanja.remove(i);
                dodanaAdap.notifyDataSetChanged();
                mogucaAdap.notifyDataSetChanged();

            }
        });

        lvDodanaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(dodanaPitanja.get(i).getNaziv().equals("Dodaj pitanje")) {
                    if(radiLiNet()) {
                        Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                        DodajKvizAkt.this.startActivity(intent);
                    }
                    else{
                        nemaNetaPitanje();
                    }
                }
                else{
                    mogucaPitanja.add(dodanaPitanja.get(i));
                    dodanaPitanja.remove(i);
                    dodanaAdap.notifyDataSetChanged();
                    mogucaAdap.notifyDataSetChanged();

                }
            }
        });

    }

    public void dajMogucaIZBAZEEdit(Kviz kviz){

        ArrayList<Pitanje> temp = kviz.getPitanja();
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        Iterator<Pitanje> iterator = pitanjaUBAZI.iterator();

        while( iterator.hasNext()){
            for (Pitanje y: temp){
                Object element = iterator.next();
                if(((Pitanje) element).getNaziv().equals(y.getNaziv())){
                    iterator.remove();
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();

                try {
                    Kviz novi = new Kviz();
                    novi =  readValues(uri);
                    setKviz(novi);
                    boolImport = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private Kviz readValues(Uri uri) throws IOException {

        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        Kviz ne = new Kviz();
        ne.setNaziv("");
        ne.setKategorija(new Kategorija("Sve","0"));
        ne.setPitanja(new ArrayList<Pitanje>());
        int brojodgUKvizu = 0;
        String naziv_kviza = new String();
        String naziv_kategorije = new String();
        int kolikoImaPitanja = 0;
        int pitanjaUKvizu = 0;
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        Kviz kviz = new Kviz();
        Kategorija kategorija = new Kategorija();
        boolean proslo = true;

        String line;
        boolean prvi_red = true;

        try {
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (prvi_red) {
                    kviz.setNaziv(values[0]);
                    //ima li kviz
                    if(kvizovi.contains(values[0])){
                        alertDialog.setMessage("Kviz kojeg Importujere vec Postoji");
                        alertDialog.show();
                        proslo = false;
                    }
                    pitanjaUKvizu = Integer.parseInt(values[2]);
                    kviz.setKategorija(kategorije.get(kategorije.indexOf(values[1])));
                } else {
                    int brojOdg = 0;
                    Pitanje pitanje = new Pitanje();
                    pitanje.setNaziv(values[0]);
                    pitanje.setTekstPitanja("");
                    Integer tacan = Integer.parseInt(values[2]);
                    if(tacan < values.length) {
                        pitanje.setTacan(values[tacan]);
                    }
                    brojodgUKvizu = Integer.parseInt(values[1]);

                    for (int i = 3; i < values.length; i++) {
                        if(values[i].contains(",")){
                            alertDialog.setMessage("Neki od odogovora u kvizu sadrze zarez");
                            alertDialog.show();
                            proslo = false;
                        }
                        pitanje.dodajOdgovor(values[i]);
                        brojOdg++;
                    }
                    if(brojOdg != brojodgUKvizu){
                        alertDialog.setMessage("Kviz kojeg Importujere ima neispravan broj odgovora");
                        alertDialog.show();
                        proslo = false;
                    }
                    if(tacan<0 || tacan>=(brojOdg+3)){
                        alertDialog.setMessage("Kviz kojeg importujete ima neispravan index tacnog odgovora");
                        alertDialog.show();
                        proslo = false;
                    }

                    pitanja.add(pitanje);
                    kolikoImaPitanja++;
                }
                prvi_red = false;
            }
            if(kolikoImaPitanja != pitanjaUKvizu){
                alertDialog.setMessage("Kviz kojeg importujete ima neispravan broj pitanja");
                alertDialog.show();
                proslo = false;
            }
            if(proslo){
                kviz.setPitanja(pitanja);
            }
            else{
                kviz = ne;
            }

        } catch (IOException e){
            Log.w("DodajKvizAkt","Nije moguce procitati liniju");
        }
        inputStream.close();
        return kviz;

    }
    public void setKviz(Kviz k){
        spKategorije.setSelection(Integer.parseInt(k.getKategorija().getId()));
        etNaziv.setText(k.getNaziv());

        // dodana pitanja
        dodanaPitanja = dajDodana(k);
        dodanaAdap = new ArrayAdapter<Pitanje>(this, android.R.layout.simple_list_item_1, dodanaPitanja);
        lvDodanaPitanja.setAdapter(dodanaAdap);
        dodanaAdap.notifyDataSetChanged();

        // moguca pitanja
        mogucaPitanja = DajMogucaPitanja(dajSvaPitanja(kvizovi), k);
        mogucaAdap = new ArrayAdapter<Pitanje>(this, android.R.layout.simple_list_item_1, mogucaPitanja);
        lvMogucaPitanja.setAdapter(mogucaAdap);
        mogucaAdap.notifyDataSetChanged();
    }
    public ArrayList<Pitanje> dajSvaPitanja(ArrayList<Kviz> kvizovi) {
        ArrayList<Pitanje> svaPitanja = new ArrayList<>();
        for (int i = 0; i < kvizovi.size(); i++) {
            for (int j = 0; j < kvizovi.get(i).getPitanja().size(); j++) {
                svaPitanja.add(kvizovi.get(i).getPitanja().get(j));
            }

        }
        return svaPitanja;
    }
    public ArrayList<Pitanje> DajMogucaPitanja(ArrayList<Pitanje> pitanja,Kviz k){
        ArrayList<Pitanje> pitanjaSva = pitanja;
        ArrayList<Pitanje> pitanjaIzKviza = k.getPitanja();

        for(int i = 0; i<pitanjaIzKviza.size();i++){
            for(int j = 0;j<pitanjaSva.size();j++ ){
                if(pitanjaIzKviza.get((i)).getNaziv().equals(pitanjaSva.get(j).getNaziv())) {
                    pitanjaSva.remove(j);
                }
            }

        }
        return pitanjaSva;
    }
    public ArrayList<Pitanje> dajDodana( Kviz k) {
        ArrayList<Pitanje> svaPitanja = new ArrayList<>();
        Pitanje pitanje =new Pitanje(zaDodatnoPitanje);
        svaPitanja.add(zaDodatnoPitanje);
        for (int i = 0; i < k.getPitanja().size(); i++) {
            svaPitanja.add(k.getPitanja().get(i));
        }
        return svaPitanja;
    }
    @Override
    public void onBackPressed() {
        finish();
        return;
    }
    public void nemaNetaPitanje(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DodajKvizAkt.this);
        alertDialogBuilder.setTitle("Offline rezim");
        alertDialogBuilder.setMessage("Nije moguce dodati pitanje u Offline rezimu!");
        alertDialogBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog upozorenje = alertDialogBuilder.create();
        upozorenje.show();
    }
    public void nemaNetaKategorije(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DodajKvizAkt.this);
        alertDialogBuilder.setTitle("Offline rezim");
        alertDialogBuilder.setMessage("Nije moguce dodati kategoriju u Offline rezimu!");
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

}
