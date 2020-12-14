package ba.unsa.etf.rma.fragmenti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;


public class InformacijeFrag extends Fragment {

    public static TextView infNazivKviza;
    public static TextView infBrojTacnihPitanja;
    public static TextView  infBrojPreostalihPitanja;
    public static TextView infProcenatTacni;
    public static Button btnKraj;

    public InformacijeFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View iv = inflater.inflate(R.layout.fragment_informacije, container, false);

        infNazivKviza = (TextView) iv.findViewById(R.id.infNazivKviza);
        infBrojTacnihPitanja = (TextView) iv.findViewById(R.id.infBrojTacnihPitanja);
        infBrojPreostalihPitanja = (TextView) iv.findViewById(R.id.infBrojPreostalihPitanja);
        infProcenatTacni = (TextView) iv.findViewById(R.id.infProcenatTacni);
        btnKraj = (Button) iv.findViewById(R.id.btnKraj);

        infNazivKviza.setText(IgrajKvizAkt.kviz.getNaziv());

        btnKraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), KvizoviAkt.class);
                startActivity(intent);
            }
        });

        return iv;
    }


}
