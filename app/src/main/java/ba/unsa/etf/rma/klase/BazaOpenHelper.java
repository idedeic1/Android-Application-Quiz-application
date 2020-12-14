package ba.unsa.etf.rma.klase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class BazaOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "spiralaDB";

    public BazaOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public BazaOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static final String TABLE_KATEGORIJA = "Kategorije";
    public static final String TABLE_KVIZ = "Kvizovi";
    public static final String TABLE_PITANJE = "Pitanja";
    public static final String TABLE_RANGLISTA = "RangListe";

    //kolone za Kategorije
    public static final String KATEGORIJE_ID = "_id";
    public static final String KATEGORIJE_NAZIV = "nazivKategorije";

    //kolone za Kvizove
    public static final String KVIZ_ID = "_id";
    public static final String KVIZ_NAZIV = "nazivKviza";
    public static final String KVIZ_KATEGORIJA = "idKategorije";
    public static final String KVIZ_PITANJA = "pitanja";
    //kolone za Pitanja
    public static final String PITANJE_ID = "_id";
    public static final String PITANJE_NAZIV = "naziv";
    public static final String PITANJE_TEKST = "tekstPitanja";
    public static final String PITANJE_TACAN = "tacanOdgovor";
    public static final String PITANJE_ODGOVORI = "odgovori";


    //kolone za RangListe
    public static final String RL_ID = "_id";
    public static final String RL_NAZIVKVIZA = "nazivKviza";
    public static final String RL_PROCENAT = "procenatTacnih";
    public static final String RL_NAZIVIGRACA = "nazivIgraca";

    private static final String CREATE_TABLE_KATEGORIJE = "create table " +
            TABLE_KATEGORIJA + " (" + KATEGORIJE_ID +
            " integer primary key, " +
            KATEGORIJE_NAZIV + " text unique);";

    private static final String CREATE_TABLE_KVIZOVI = "create table " + TABLE_KVIZ + " (" + KVIZ_ID +
            " integer primary key autoincrement, " +
            KVIZ_NAZIV + " text, " + KVIZ_PITANJA + " text, " + KVIZ_KATEGORIJA + " text);";

    private static final String CREATE_TABLE_PITANJA = "create table " + TABLE_PITANJE + " (" + PITANJE_ID
            + " integer primary key autoincrement, " +
            PITANJE_NAZIV + " text, " + PITANJE_TEKST + " text," + PITANJE_ODGOVORI + " text," + PITANJE_TACAN + " text);";

    private static final String CREATE_TABLE_RL = "create table " + TABLE_RANGLISTA + " (" + RL_ID +
            " integer primary key autoincrement, " + RL_NAZIVKVIZA + " text, " + RL_NAZIVIGRACA + " text, " +
            RL_PROCENAT + " real);";

    private String[] koloneKviz = new String[]{KVIZ_ID,KVIZ_NAZIV, KVIZ_PITANJA,KVIZ_KATEGORIJA};
    private String[] kolonePitanje = new String[]{PITANJE_ID, PITANJE_NAZIV, PITANJE_TEKST, PITANJE_ODGOVORI, PITANJE_TACAN};
    private String[] koloneRangLista = new String[]{RL_ID, RL_NAZIVIGRACA, RL_NAZIVKVIZA, RL_PROCENAT};
    private String[] koloneKategorija = new String[]{KATEGORIJE_ID,KATEGORIJE_NAZIV};

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_KATEGORIJE);
        db.execSQL(CREATE_TABLE_KVIZOVI);
        db.execSQL(CREATE_TABLE_PITANJA);
        db.execSQL(CREATE_TABLE_RL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KATEGORIJA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KVIZ);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PITANJE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RANGLISTA);
        onCreate(db);
    }

    public void delete(SQLiteDatabase db){
        db.delete(TABLE_KVIZ, null, null);
        db.delete(TABLE_PITANJE, null, null);
        db.delete(TABLE_RANGLISTA, null, null);
        db.delete(TABLE_KATEGORIJA, null, null);
    }

    public long dodajKategoriju(Kategorija kategorija){
        ContentValues novi = new ContentValues();
        novi.put(KATEGORIJE_ID, Integer.parseInt(kategorija.getId()));
        novi.put(KATEGORIJE_NAZIV, kategorija.getNaziv());
        SQLiteDatabase db = getWritableDatabase();
        return db.insertWithOnConflict(TABLE_KATEGORIJA,null,novi,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public long dodajRangListu(String nazivKviza, String nazivIgraca, double procenatTacnih){
        ContentValues novi = new ContentValues();
        novi.put(RL_NAZIVKVIZA, nazivKviza);
        novi.put(RL_NAZIVIGRACA, nazivIgraca);
        novi.put(RL_PROCENAT, procenatTacnih);
        SQLiteDatabase db = getWritableDatabase();
        return db.insertWithOnConflict(TABLE_RANGLISTA, null, novi, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public long dodajKviz(Kviz kviz){
        provjeriKategoriju(kviz);
        provjeriPitanja(kviz);

        ContentValues novi = new ContentValues(0);
        novi.put(KVIZ_NAZIV, kviz.getNaziv());
        String temp="";
        for(Pitanje p: kviz.getPitanja()){
            temp += p.getNaziv() + ",";
        }
        novi.put(KVIZ_PITANJA, temp);
        novi.put(KVIZ_KATEGORIJA, kviz.getKategorija().getNaziv());
        SQLiteDatabase db = getWritableDatabase();
        return db.insertWithOnConflict(TABLE_KVIZ, null, novi, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public long dodajPitanje(Pitanje pitanje){

        ContentValues novi = new ContentValues();
        novi.put(PITANJE_NAZIV, pitanje.getNaziv());
        novi.put(PITANJE_TEKST, pitanje.getTekstPitanja());
        String temp = "";
        for(String x: pitanje.getOdgovori()){
            temp += x + ",";
        }
        novi.put(PITANJE_ODGOVORI, temp);
        novi.put(PITANJE_TACAN, pitanje.getTacan());
        SQLiteDatabase db = getWritableDatabase();
        return db.insertWithOnConflict(TABLE_PITANJE, null, novi, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private String getString(Cursor c, String kolona) {
        return c.getString(c.getColumnIndexOrThrow(kolona));
    }

    private int getInt(Cursor c, String kolona) {
        return c.getInt(c.getColumnIndexOrThrow(kolona));
    }

    private float getFloat(Cursor c, String kolona){
        return c.getFloat(c.getColumnIndexOrThrow(kolona));
    }

    private Cursor getCursor(String tabela, String[] kolone, String where) {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(tabela, kolone, where, null, null, null, null);
    }

    public ArrayList<Kviz> getKvizovi() {
        ArrayList<Kviz> kvizovi = new ArrayList<>();
        Cursor c = getCursor(TABLE_KVIZ, koloneKviz, null);
        if (c.moveToFirst()) {
            do {
                kvizovi.add(new Kviz(getString(c,koloneKviz[1]), dajPitanja(getString(c,koloneKviz[2])), dajKategoriju(getString(c,koloneKviz[3]))));
            }
            while (c.moveToNext());
        }

        return kvizovi;
    }
    public ArrayList<Kategorija> getKategorije() {
        ArrayList<Kategorija> k = new ArrayList<>();
        Cursor c = getCursor(TABLE_KATEGORIJA, koloneKategorija, null);
        if (c.moveToFirst()) {
            do {
                k.add(new Kategorija( getString(c, koloneKategorija[1]),String.valueOf(getInt(c, koloneKategorija[0]))));
            }
            while (c.moveToNext());
        }

        return k;
    }
    public ArrayList<Score> getRangListe(){
        ArrayList<Score> rezultati = new ArrayList<>();
        Cursor c = getCursor(TABLE_RANGLISTA, koloneRangLista, null);

        if(c.moveToFirst()){
            do{
                rezultati.add(new Score(getString(c, koloneRangLista[1]), (double)getFloat(c, koloneRangLista[3]), getString(c,koloneRangLista[2]) ));
            }while(c.moveToNext());
        }
        return rezultati;
    }
    public ArrayList<Pitanje> getPitanja(){
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        Cursor c = getCursor(TABLE_PITANJE, kolonePitanje, null);
        if(c.moveToFirst()){
            do{
                pitanja.add(new Pitanje(getString(c,kolonePitanje[1]), getString(c,kolonePitanje[2]), dajOdgovore(getString(c,kolonePitanje[3])) , getString(c,kolonePitanje[4])));
            }while(c.moveToNext());
        }
        return pitanja;
    }

    private ArrayList<String> dajOdgovore(String temp){
        ArrayList<String> odgovori = new ArrayList<>();

        String[] p1 = temp.split(",");
        odgovori = new ArrayList<String>(Arrays.asList(p1));

        return odgovori;
    }
    private ArrayList<Pitanje> dajPitanja(String temp){
        ArrayList<Pitanje> pitanjaUBazi = getPitanja();
        ArrayList<Pitanje> pitanja = new ArrayList<>();
        ArrayList<String> pitanjaString = new ArrayList<>();
        String[] p1 = temp.split(",");
        pitanjaString = new ArrayList<String>(Arrays.asList(p1));
        if(pitanjaUBazi == null ) return pitanja;
        for(Pitanje p : pitanjaUBazi){
            for(String x : pitanjaString){
                if(p.getNaziv().equals(x) && !pitanja.contains(p)) pitanja.add(p);
            }
        }

        return pitanja;
    }
    private Kategorija dajKategoriju(String temp){
        ArrayList<Kategorija> kategorijeUBazi = getKategorije();
        Kategorija kat = new Kategorija();

        if(kategorijeUBazi == null)return new Kategorija("Greska", "-1");

        for(Kategorija k: kategorijeUBazi){
            if(k.getNaziv().equals(temp))  kat = k;
        }

        return kat;
    }
    private void provjeriKategoriju(Kviz kviz){
        ArrayList<Kategorija> kategorijeUBazi = getKategorije();
        if(kategorijeUBazi == null)return;
        for(Kategorija kat: kategorijeUBazi){
            if(!kat.getNaziv().equals(kviz.getNaziv())){
                dodajKategoriju(kviz.getKategorija());
            }
        }
    }
    private void provjeriPitanja(Kviz kviz){
        ArrayList<Pitanje> pitanjaUBazi = getPitanja();
        ArrayList<Pitanje> pitanjaUKvizu = kviz.getPitanja();
        if(pitanjaUBazi == null || pitanjaUKvizu == null) return;
        for(Pitanje i : pitanjaUBazi){
            for(Pitanje j: pitanjaUKvizu){
                if(!i.getNaziv().equals(j.getNaziv())){
                    dodajPitanje(j);
                }
            }
        }
    }



}
