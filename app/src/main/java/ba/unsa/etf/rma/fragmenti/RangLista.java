package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.klase.RangListaKlasa;
import ba.unsa.etf.rma.klase.Score;

public class RangLista extends Fragment {
    private static ListView lvRangLista;
    private static ArrayList<Score> listaIgraca = IgrajKvizAkt.rangLista;

    public RangLista() {}
    public static RangLista novaRangLista(Score novi){
        listaIgraca.add(novi);
        RangLista argumenti = new RangLista();
        Bundle args = new Bundle();
        args.putSerializable("lista", listaIgraca);
        argumenti.setArguments(args);

        return argumenti;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listaIgraca=(ArrayList)getArguments().getSerializable("lista");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rang_lista, container, false);

        lvRangLista = (ListView)v.findViewById(R.id.lvRangLista);
        ArrayAdapter<Score> rangListaAdap = new ArrayAdapter<Score>(getContext(), R.layout.elementrangliste, listaIgraca);
        lvRangLista.setAdapter(rangListaAdap);
        return v;
    }

}
