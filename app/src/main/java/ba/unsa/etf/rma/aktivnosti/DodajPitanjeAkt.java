package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.DodajKvizAkt.dodanaPitanja;
import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.pitanjaUFire;

public class DodajPitanjeAkt extends AppCompatActivity {

    private ListView lvOdgovori;
    private EditText etNaziv;
    private EditText etOdgovor;
    private Button btnDodajOdgovor;
    private Button btnDodajTacan;
    private Button btnDodajPitanje;
    private ArrayAdapter<String> odgovoriAdap;
    private ArrayList<String> odgovori = new ArrayList<>();
    private String tacanOdgovor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        lvOdgovori = (ListView)findViewById(R.id.lvOdgovori);
        etNaziv = (EditText)findViewById(R.id.etNaziv);
        etOdgovor = (EditText)findViewById(R.id.etOdgovor);
        btnDodajOdgovor = (Button)findViewById(R.id.btnDodajOdgovor);
        btnDodajTacan = (Button)findViewById(R.id.btnDodajTacan);
        btnDodajPitanje = (Button)findViewById(R.id.btnDodajPitanje);

        odgovoriAdap = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,odgovori);
        lvOdgovori.setAdapter(odgovoriAdap);

        btnDodajOdgovor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                odgovoriAdap.add(etOdgovor.getText().toString());
                odgovoriAdap.notifyDataSetChanged();
            }
        });

        btnDodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                odgovori.add(0,etOdgovor.getText().toString());
                odgovoriAdap.notifyDataSetChanged();
                tacanOdgovor=odgovori.get(0);

                lvOdgovori.getChildAt(0).setBackgroundResource(R.color.green);
                odgovoriAdap.notifyDataSetChanged();
            }
        });

        lvOdgovori.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                odgovori.remove(i);
                odgovoriAdap.notifyDataSetChanged();
            }
        });

        btnDodajPitanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nazivPitanja = etNaziv.getText().toString();
                Pitanje novo = new Pitanje(nazivPitanja, "", odgovori, tacanOdgovor);
                boolean sadrzan = false;
                for (Pitanje x : pitanjaUFire) {
                    if (x.getNaziv().equals(novo.getNaziv())) sadrzan = true;
                }

                if (sadrzan) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DodajPitanjeAkt.this);
                    alertDialogBuilder.setMessage("Uneseno pitanje već postoji!");
                    AlertDialog upozorenje = alertDialogBuilder.create();
                    upozorenje.show();
                }
                else{
                    Intent intent = new Intent(DodajPitanjeAkt.this,DodajKvizAkt.class);
                    new KreirajDokumentPitanje().execute(novo);
                    dodanaPitanja.add(novo);
                    DodajPitanjeAkt.this.startActivity(intent);
                }

            }
        });

    }

    public class KreirajDokumentPitanje extends AsyncTask<Pitanje, Void, Void> {
        GoogleCredential credentials;

        @Override
        protected Void doInBackground(Pitanje... params) {
            try {
                Pitanje novoPitanje = params[0];

                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();

                String TOKEN = credentials.getAccessToken();

                String url = "https://firestore.googleapis.com/v1/projects/rma-spirala3-17514/databases/(default)/documents/Pitanja?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                String dokument = "{\"fields\": {\"indexTacnog\": {\"integerValue\": \"" + novoPitanje.getOdgovori().indexOf(novoPitanje.getTacan()) +"\"},\"naziv\": {\"stringValue\": \"" + novoPitanje.getNaziv() + "\"},\"odgovori\": {\"arrayValue\": {\"values\": [ ";
                for(int i = 0; i < novoPitanje.getOdgovori().size(); i++){
                    if(i != novoPitanje.getOdgovori().size()-1)
                        dokument += "{\"stringValue\": \""+novoPitanje.getOdgovori().get(i)+ "\"},";
                    else dokument += "{\"stringValue\": \""+novoPitanje.getOdgovori().get(i)+ "\"}";
                }
                dokument += "]}}}}";

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

    @Override
    public void onBackPressed() {
        finish();
        return;
    }
}
