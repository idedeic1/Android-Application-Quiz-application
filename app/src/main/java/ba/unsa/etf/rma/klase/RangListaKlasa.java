package ba.unsa.etf.rma.klase;

import java.util.HashMap;
import java.util.Map;

public class RangListaKlasa {
    private String nazivKviza;
    private Map<Integer, Map<String, Double>> lista = new HashMap<>();

    public RangListaKlasa(String nazivKviza, Map<Integer, Map<String, Double>> lista) {
        this.nazivKviza = nazivKviza;
        this.lista = lista;
    }

    public RangListaKlasa(){
        this.nazivKviza = "";
        this.lista = null;
    }

    public Map<String, Double> dajPodatke(){
        Map<String, Double> mapa = new HashMap<>();
        for(Map.Entry<Integer, Map<String, Double>> entry1 : this.lista.entrySet()) {
            for(Map.Entry<String, Double> j : entry1.getValue().entrySet()){
                mapa.put(j.getKey(),j.getValue());
            }
        }
        return mapa;
    }

    public String getNazivKviza() {
        return nazivKviza;
    }

    public void setNazivKviza(String nazivKviza) {
        this.nazivKviza = nazivKviza;
    }

    public Map<Integer, Map<String, Double>> getLista() {
        return lista;
    }

    public void setLista(Map<Integer, Map<String, Double>> lista) {
        this.lista = lista;
    }
}
