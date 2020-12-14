package ba.unsa.etf.rma.klase;

import java.io.Serializable;

public class Score implements Serializable {
    private String nazivIgraca;
    private Double procenatTacnih;
    private String nazivKviza;

    public Score(String nazivIgraca, Double procenatTacnih, String nazivKviza) {
        this.nazivIgraca = nazivIgraca;
        this.procenatTacnih = procenatTacnih;
        this.nazivKviza = nazivKviza;
    }
    public Score(){}

    public String getNazivIgraca() {
        return nazivIgraca;
    }

    public void setNazivIgraca(String nazivIgraca) {
        this.nazivIgraca = nazivIgraca;
    }

    public Double getProcenatTacnih() {
        return procenatTacnih;
    }

    public void setProcenatTacnih(Double procenatTacnih) {
        this.procenatTacnih = procenatTacnih;
    }

    public String getNazivKviza() {
        return nazivKviza;
    }

    public void setNazivKviza(String nazivKviza) {
        this.nazivKviza = nazivKviza;
    }


}
