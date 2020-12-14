package ba.unsa.etf.rma.klase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Pitanje implements Serializable {
    private String naziv;
    private String tekstPitanja;
    private ArrayList<String> odgovori;
    private String tacan;

    public Pitanje(String naziv, String tekstPitanja, ArrayList<String> odgovori, String tacan) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        this.odgovori = odgovori;
        this.tacan = tacan;
    }

    public Pitanje(Pitanje p){
        odgovori = new ArrayList<>();
        this.naziv = p.naziv;
        this.tekstPitanja = p.tekstPitanja;
        this.odgovori = p.odgovori;
        this.tacan = p.tacan;
    }

    @Override
    public boolean equals(Object o){
        Pitanje p = (Pitanje) o;

        if(this.getNaziv().equals(p.getNaziv())) return true;

        return false;
    }

    @Override
    public String toString(){
        return this.getNaziv();
    }

    public Pitanje (){}

    public void dodajOdgovor(String temp){
        odgovori.add(temp);
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTekstPitanja() {
        return tekstPitanja;
    }

    public void setTekstPitanja(String tekstPitanja) {
        this.tekstPitanja = tekstPitanja;
    }

    public ArrayList<String> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(ArrayList<String> odgovori) {
        this.odgovori = odgovori;
    }

    public String getTacan() {
        return tacan;
    }

    public void setTacan(String tacan) {
        this.tacan = tacan;
    }

    public ArrayList<String> dajRandomOdgovore(){
        ArrayList<String> novaPitanja = new ArrayList<String>();
        if(odgovori!=null){
            novaPitanja = odgovori;
        }
        Collections.shuffle(novaPitanja);
        return novaPitanja;
    }
}
