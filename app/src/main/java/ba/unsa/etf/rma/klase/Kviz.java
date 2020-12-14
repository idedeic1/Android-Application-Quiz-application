package ba.unsa.etf.rma.klase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Kviz implements Serializable {
    private String naziv;
    private ArrayList<Pitanje> pitanja;
    private Kategorija kategorija;

    public Kviz(){}

    public Kviz(String naziv, ArrayList<Pitanje> pitanja, Kategorija kategorija) {
        this.naziv = naziv;
        this.pitanja = pitanja;
        this.kategorija = kategorija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public ArrayList<Pitanje> getPitanja() {
        return pitanja;
    }

    public void setPitanja(ArrayList<Pitanje> pitanja) {
        this.pitanja = pitanja;
    }

    public Kategorija getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija = kategorija;
    }

    public void dodajPitanje(Pitanje p){
        this.pitanja.add(p);
    }

    public void obrisiPitanje(String naziv){
        for(Pitanje p : pitanja){
            if(p.getNaziv().equals(naziv)){
                pitanja.remove(p);
                break;
            }
        }
    }

    public ArrayList<Pitanje> dajRandomPitanja(){
        ArrayList<Pitanje> novaPitanja = new ArrayList<Pitanje>();
        if(pitanja!=null)
            novaPitanja = pitanja;
        Collections.shuffle(novaPitanja);
        return novaPitanja;
    }

    @Override
    public String toString(){
        return this.getNaziv();
    }
}
