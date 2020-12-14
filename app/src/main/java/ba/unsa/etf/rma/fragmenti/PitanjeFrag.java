package ba.unsa.etf.rma.fragmenti;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjeFrag extends Fragment {
    public static ListView odgovoriPitanja;
    public TextView tekstPitanja;

    public ArrayList<Pitanje> pitanja = new ArrayList<>();
    public ArrayList<String> odgovori = new ArrayList<>();
    public ArrayAdapter adapter;

    public int brojTacnih = 0;
    public int brojOdgovorenih = 0;
    public int brojPreostalih = 0;
    public int brojOdgovora = IgrajKvizAkt.kviz.getPitanja().size();
    public Double procenatTacnih = 0.0;
    public String tacan;

    public Pitanje novoPitanje = new Pitanje();
    public PitanjeFrag() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View iv = inflater.inflate(R.layout.fragment_pitanje, container, false);

        odgovoriPitanja = (ListView) iv.findViewById(R.id.odgovoriPitanja);
        tekstPitanja = (TextView) iv.findViewById(R.id.tekstPitanja);

        pitanja = IgrajKvizAkt.kviz.dajRandomPitanja();

        novoPitanje = pitanja.get(brojOdgovorenih);
        tekstPitanja.setText(novoPitanje.getNaziv());
        brojPreostalih = pitanja.size();

        InformacijeFrag.infBrojPreostalihPitanja.setText(String.valueOf(brojPreostalih));
        InformacijeFrag.infBrojTacnihPitanja.setText(String.valueOf(brojTacnih));
        InformacijeFrag.infProcenatTacni.setText(procenatTacnih.toString());

        odgovori = novoPitanje.dajRandomOdgovore();
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,odgovori);
        odgovoriPitanja.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        tacan = novoPitanje.getTacan();




        odgovoriPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                tekstPitanja.setText(novoPitanje.getNaziv());
                boolean zavrsenKviz = false;

                if(brojOdgovorenih == pitanja.size() || zavrsenKviz) {
                   zavrsiKviz();
                }

                else {
                    if (odgovori.get(position).equals(tacan)) {

                        brojPreostalih--;
                        brojTacnih++;
                        brojOdgovorenih++;
                        procenatTacnih = ((double) brojTacnih / brojOdgovora) * 100;

                        odgovoriPitanja.getChildAt(odgovori.indexOf(tacan)).setBackgroundColor(getResources().getColor(R.color.zelena));

                        InformacijeFrag.infBrojPreostalihPitanja.setText(String.valueOf(brojPreostalih));
                        InformacijeFrag.infBrojTacnihPitanja.setText(String.valueOf(brojTacnih));
                        InformacijeFrag.infProcenatTacni.setText(String.valueOf(procenatTacnih));

                        if(pitanja.size() != brojOdgovorenih){
                            novoPitanje = pitanja.get(brojOdgovorenih);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    odgovori = novoPitanje.dajRandomOdgovore();
                                    adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, odgovori);
                                    odgovoriPitanja.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    tacan = novoPitanje.getTacan();
                                }
                            }, 2000);
                        }

                        if(pitanja.size() == brojOdgovorenih) zavrsenKviz = true;

                    }
                    else {

                        brojPreostalih--;
                        procenatTacnih = ((double) brojTacnih / brojOdgovora) * 100;


                        odgovoriPitanja.getChildAt(odgovori.indexOf(tacan)).setBackgroundColor(getResources().getColor(R.color.zelena));
                        odgovoriPitanja.getChildAt(position).setBackgroundResource(R.color.crvena);


                        InformacijeFrag.infBrojPreostalihPitanja.setText(String.valueOf(brojPreostalih));
                        InformacijeFrag.infProcenatTacni.setText(String.valueOf(procenatTacnih));

                        brojOdgovorenih++;
                        if(pitanja.size() != brojOdgovorenih){
                            novoPitanje = pitanja.get(brojOdgovorenih);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    odgovori = novoPitanje.dajRandomOdgovore();
                                    adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, odgovori);
                                    odgovoriPitanja.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    tacan = novoPitanje.getTacan();
                                }
                            }, 2000);
                        }

                        if(pitanja.size() == brojOdgovorenih) zavrsenKviz = true;

                    }
                }
                if(zavrsenKviz)
                    zavrsiKviz();

            }
        });
        return iv;
    }

    public void zavrsiKviz(){
        IgrajKvizAkt.noviScore.setProcenatTacnih(procenatTacnih);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
        public void run() {
                tekstPitanja.setText("Kviz je završen!");
                ArrayList<String> prazan = new ArrayList<>();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, prazan);
                odgovoriPitanja.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                }
        }, 2000);
        ((IgrajKvizAkt)getActivity()).unesiIme();

    }


}
