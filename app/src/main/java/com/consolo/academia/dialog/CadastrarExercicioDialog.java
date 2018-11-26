package com.consolo.academia.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.consolo.academia.R;
import com.consolo.academia.Util;
import com.consolo.academia.interfaces.OnDismissDialogFragment;
import com.consolo.academia.model.Dominio;
import com.consolo.academia.model.Exercicio;
import com.consolo.academia.model.Treino;
import com.consolo.academia.model.enuns.EnumTipoCarga;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.TimePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;

import static com.consolo.academia.Util.isNullOrEmpty;

public class CadastrarExercicioDialog extends DialogFragment {

    private static final String PARAM_TREINO_ID = "TREINO_ID";
    private static final String PARAM_ID = "EXERCICIO_ID";

    private String id;
    private String idTreino;
    private Realm mRealm;
    private Exercicio exercicio;
    private OnDismissDialogFragment listener;

    private EditText txtNome;
    private TextInputLayout layoutNome;
    private EditText txtSerie;
    private TextInputLayout layoutSerie;
    private EditText txtRepeticao;
    private TextInputLayout layoutRepeticao;
    private Spinner spnTipoCarga;
    private TextInputLayout layoutTipoCarga;
    private EditText txtCargaPeso;
    private TextInputLayout layoutCargaPeso;
    private EditText txtCargaTempo;
    private TextInputLayout layoutCargaTempo;


    public static CadastrarExercicioDialog newInstance(String idTreino, String idExercicio) {
        CadastrarExercicioDialog fragment = new CadastrarExercicioDialog();
        Bundle args = new Bundle();
        args.putString(PARAM_ID, idExercicio);
        args.putString(PARAM_TREINO_ID, idTreino);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString(PARAM_ID);
            idTreino = getArguments().getString(PARAM_TREINO_ID);
        }
        mRealm = Realm.getDefaultInstance();

        if (!isNullOrEmpty(id)) {
            exercicio = Exercicio.buscarPorId(mRealm, id);
        }

    }

    public void setOnDismissListener(OnDismissDialogFragment listener) {
        this.listener = listener;
    }

    @Override
    public void onStop() {
        super.onStop();
        mRealm.close();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle((isNullOrEmpty(id) ? "Cadastrar" : "Alterar") + " Exercicio");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewDialog = inflater.inflate(R.layout.dialog_cadastrar_exercicio, null);

        inicializarCampos(viewDialog);

        if (!isNullOrEmpty(exercicio)) {
            preencherCampos();
        }

        montarTimePicker();

        builder.setView(viewDialog);
        builder.setPositiveButton("Salvar", null);
        builder.setNegativeButton("Cancelar", null);

        final AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        persistir(dialog);
                    }
                });
            }
        });


        return dialog;
    }

    private void montarTimePicker() {
        txtCargaTempo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int horas = 0, minutos = 0, segundos = 30;
                if (txtCargaTempo.getTag() != null) {
                    int[] horario = Util.recuperarHorasMinutosSegundos(Integer.parseInt(txtCargaTempo.getTag().toString()));
                    horas = horario[0]; minutos = horario[1]; segundos = horario[2];
                }

                MyTimePickerDialog mTimePicker = new MyTimePickerDialog(getActivity(), new MyTimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                        int segundos = (hourOfDay * 3600) + (minute * 60) + seconds;
                        txtCargaTempo.setText(Util.formatarSegundos(segundos));
                        txtCargaTempo.setTag(segundos);
                    }
                }, horas, minutos, segundos, true);
                mTimePicker.show();
            }
        });
    }

    private void inicializarCampos(View viewDialog) {
        layoutNome = viewDialog.findViewById(R.id.layout_nome_exercicio);
        txtNome = viewDialog.findViewById(R.id.txt_nome_exercicio);

        layoutSerie = viewDialog.findViewById(R.id.layout_serie_exercicio);
        txtSerie = viewDialog.findViewById(R.id.txt_serie_exercicio);

        layoutRepeticao = viewDialog.findViewById(R.id.layout_repeticao_exercicio);
        txtRepeticao = viewDialog.findViewById(R.id.txt_repeticao_exercicio);

        layoutTipoCarga = viewDialog.findViewById(R.id.layout_tipo_carga_exercicio);
        spnTipoCarga = viewDialog.findViewById(R.id.spn_tipo_carga_exercicio);

        layoutCargaPeso = viewDialog.findViewById(R.id.layout_carga_peso_exercicio);
        txtCargaPeso = viewDialog.findViewById(R.id.txt_carga_peso_exercicio);

        layoutCargaTempo = viewDialog.findViewById(R.id.layout_carga_tempo_exercicio);
        txtCargaTempo = viewDialog.findViewById(R.id.txt_carga_tempo_exercicio);

        final ArrayAdapter<Dominio> tipoCargaAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.layout_dominio_spinner, montarSpinner());
        spnTipoCarga.setAdapter(tipoCargaAdapter);

        spnTipoCarga.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Dominio tipoCarga = tipoCargaAdapter.getItem(position);
                if (Objects.equals(EnumTipoCarga.PESO.value, tipoCarga.id)) {
                    layoutCargaPeso.setVisibility(View.VISIBLE);
                    layoutCargaTempo.setVisibility(View.GONE);
                } else if (Objects.equals(EnumTipoCarga.TEMPO.value, tipoCarga.id)) {
                    layoutCargaPeso.setVisibility(View.GONE);
                    layoutCargaTempo.setVisibility(View.VISIBLE);
                } else {
                    layoutCargaPeso.setVisibility(View.GONE);
                    layoutCargaTempo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private List<Dominio> montarSpinner() {
        List<Dominio> dominios = new ArrayList<>();
        dominios.add(new Dominio(0, "Tipo de Medida"));
        dominios.add(new Dominio(EnumTipoCarga.PESO.value, EnumTipoCarga.PESO.name));
        dominios.add(new Dominio(EnumTipoCarga.TEMPO.value, EnumTipoCarga.TEMPO.name));
        return dominios;
    }

    private void preencherCampos() {
        txtNome.setText(exercicio.getNome());
        txtSerie.setText(exercicio.getSerie().toString());
        txtRepeticao.setText(exercicio.getRepeticao().toString());
        selecionarItemPeloValor(spnTipoCarga, exercicio.getTipoCarga());
        if (!Util.isNullOrEmpty(exercicio.getCarga())) {
            if (Objects.equals(exercicio.getTipoCarga(), EnumTipoCarga.PESO.value)) {
                txtCargaPeso.setText(exercicio.getCarga().toString());
            } else if (Objects.equals(exercicio.getTipoCarga(), EnumTipoCarga.TEMPO.value)) {
                txtCargaTempo.setTag(exercicio.getCarga().toString());
                txtCargaTempo.setText(Util.formatarSegundos(exercicio.getCarga()));
            }
        }
    }

    private void persistir(final AlertDialog dialog) {
        if (isCamposValidos()) {
            mRealm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    Exercicio e = Exercicio.buscarPorId(realm, id);

                    if (isNullOrEmpty(e))
                        e = new Exercicio();

                    e.setNome(txtNome.getText().toString());
                    e.setSerie(Integer.parseInt(txtSerie.getText().toString()));
                    e.setRepeticao(Integer.parseInt(txtRepeticao.getText().toString()));
                    Dominio tipoCarga = (Dominio) spnTipoCarga.getSelectedItem();
                    e.setTipoCarga(tipoCarga.id);
                    e.setTreino(Treino.buscarPorId(realm, idTreino));

                    if (Objects.equals(tipoCarga.id, EnumTipoCarga.PESO.value)) {
                        e.setCarga(Integer.parseInt(txtCargaPeso.getText().toString()));
                    } else if (Objects.equals(tipoCarga.id, EnumTipoCarga.TEMPO.value)) {
                        e.setCarga((Integer) txtCargaTempo.getTag());
                    }

                    if (isNullOrEmpty(id))
                        e.cria(realm);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), "Exercício salvo com sucesso!", Toast.LENGTH_SHORT).show();

                    if (!isNullOrEmpty(listener))
                        listener.OnDismissListener(true);

                    dialog.dismiss();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    if (!isNullOrEmpty(listener))
                        listener.OnDismissListener(false);
                    dialog.dismiss();
                }
            });
        }
    }

    private boolean isCamposValidos() {
        boolean validos = true;

        layoutNome.setErrorEnabled(false);
        txtNome.setError(null);

        layoutSerie.setErrorEnabled(false);
        txtSerie.setError(null);

        layoutRepeticao.setErrorEnabled(false);
        txtRepeticao.setError(null);

        if (txtNome.getText() == null || Objects.equals(txtNome.getText().toString(), "")) {
            validos = false;
            layoutNome.setErrorEnabled(true);
            txtNome.setError(getString(R.string.preenchimento_obrigatorio));
        }

        if (txtSerie.getText() == null || Objects.equals(txtSerie.getText().toString(), "")) {
            validos = false;
            layoutSerie.setErrorEnabled(true);
            txtSerie.setError(getString(R.string.preenchimento_obrigatorio));
        }

        if (txtRepeticao.getText() == null || Objects.equals(txtRepeticao.getText().toString(), "")) {
            validos = false;
            layoutRepeticao.setErrorEnabled(true);
            txtRepeticao.setError(getString(R.string.preenchimento_obrigatorio));
        }

        return validos;
    }

    public void selecionarItemPeloValor(Spinner spnr, Integer value) {
        if (Util.isNullOrEmpty(value)) return;
        ArrayAdapter adapter = (ArrayAdapter) spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItemId(position) == value) {
                spnr.setSelection(position);
                return;
            }
        }
    }

}
