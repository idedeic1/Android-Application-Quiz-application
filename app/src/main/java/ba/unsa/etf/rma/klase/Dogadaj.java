package ba.unsa.etf.rma.klase;

import java.util.Date;

public class Dogadaj {
    private String naziv;
    private Date vrijeme;
    private Date vrijemeKraja;

    public Dogadaj(String naziv, Date vrijeme, Date vrijemeKraja) {
        this.naziv = naziv;
        this.vrijeme = vrijeme;
        this.vrijemeKraja = vrijemeKraja;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Date getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(Date vrijeme) {
        this.vrijeme = vrijeme;
    }

    public Date getVrijemeKraja() {
        return vrijemeKraja;
    }

    public void setVrijemeKraja(Date vrijemeKraja) {
        this.vrijemeKraja = vrijemeKraja;
    }
}
