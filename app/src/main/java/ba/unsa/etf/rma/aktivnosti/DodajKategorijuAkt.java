package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;
import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

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

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;

import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.kategorije;

public class DodajKategorijuAkt extends AppCompatActivity  implements IconDialog.Callback {

    private EditText etNaziv;
    private EditText etIkona;
    private Button btnDodajKategoriju;
    private Button btnDodajIkonu;
    private Icon[] selectedIcons;
    private boolean sveOk1 = true;
    private boolean sveOk2 = false;
    private ArrayList<Kategorija> kategorijeIZBAZE = new ArrayList<>();

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
    public class DajKolekcijuKategorija extends AsyncTask<Void, Void, Void>{
        GoogleCredential credentials;

        @Override
        protected Void doInBackground(Void... arg0){
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
                        kategorijeIZBAZE.add(new Kategorija(nazivKategorije.getString("stringValue"),String.valueOf(idIkonice.getString("integerValue"))));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
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
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        new DajKolekcijuKategorija().execute();

        etNaziv = (EditText)findViewById(R.id.etNaziv);
        etIkona = (EditText)findViewById(R.id.etIkona);
        btnDodajKategoriju = (Button)findViewById(R.id.btnDodajKategoriju);
        btnDodajIkonu = (Button) findViewById(R.id.btnDodajIkonu);

        final IconDialog iconDialog = new IconDialog();
        iconDialog.setSelectedIcons(1,2,3);

        btnDodajIkonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.setSelectedIcons(selectedIcons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
                sveOk2 = true;
            }
        });

        etIkona.setFocusable(false);



        btnDodajKategoriju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Kategorija kat = new Kategorija();

                if (!sveOk2) {
                    etIkona.setBackgroundColor(getResources().getColor(R.color.red));
                }
                else{
                    etIkona.setBackgroundColor(getResources().getColor(R.color.white));
                }

                if (etNaziv.getText().toString().equals("") || etNaziv.getText().toString().equals("Naziv kategorije")) {
                    etNaziv.setBackgroundColor(getResources().getColor(R.color.red));
                    sveOk1 = false;
                }
                else{
                    sveOk1 = true;
                    etNaziv.setBackgroundColor(getResources().getColor(R.color.white));
                }

                if (sveOk1 && sveOk2) {
                    kat.setNaziv(etNaziv.getText().toString());
                    kat.setId(Integer.toString(selectedIcons[0].getId()));
                    etIkona.setText(Integer.toString(selectedIcons[0].getId()));
                    boolean sadrzan = false;
                    for (Kategorija x : kategorijeIZBAZE) {
                        if (x.getNaziv().equals(kat.getNaziv())) sadrzan = true;
                    }

                    if (sadrzan) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DodajKategorijuAkt.this);
                        alertDialogBuilder.setMessage("Unesena kategorija već postoji!");
                        AlertDialog upozorenje = alertDialogBuilder.create();
                        upozorenje.show();
                    } else {
                        new KreirajDokumentKategorija().execute(kat);
                        Intent intent = new Intent(DodajKategorijuAkt.this, DodajKvizAkt.class);
                        kategorije.add(kat);
                        intent.putExtra("NovaKategorija", true);
                        DodajKategorijuAkt.this.startActivity(intent);
                    }
                }
            }
        });

    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
    }
    @Override
    public void onBackPressed() {
        finish();
        return;
    }
}

