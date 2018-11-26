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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.consolo.academia.R;
import com.consolo.academia.interfaces.OnDismissDialogFragment;
import com.consolo.academia.model.Treino;

import java.util.Objects;

import io.realm.Realm;

import static com.consolo.academia.Util.isNullOrEmpty;

public class CadastrarTreinoDialog extends DialogFragment {

    private static final String PARAM_ID = "TREINO_ID";

    private String id;
    private Realm mRealm;
    private Treino treino;
    private OnDismissDialogFragment listener;
    private EditText txtNome;
    private TextInputLayout layoutNome;

    public static CadastrarTreinoDialog newInstance(String idTreino) {
        CadastrarTreinoDialog fragment = new CadastrarTreinoDialog();
        Bundle args = new Bundle();
        args.putString(PARAM_ID, idTreino);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString(PARAM_ID);
        }
        mRealm = Realm.getDefaultInstance();

        if (!isNullOrEmpty(id)) {
            treino = Treino.buscarPorId(mRealm, id);
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
        builder.setTitle((isNullOrEmpty(id) ? "Cadastrar" : "Alterar") + " Treino");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewDialog = inflater.inflate(R.layout.dialog_cadastrar_treino, null);

        layoutNome = viewDialog.findViewById(R.id.layout_nome_treino);
        txtNome = viewDialog.findViewById(R.id.txt_nome_treino);
        if (!isNullOrEmpty(treino))
            txtNome.setText(treino.getNome());

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

    private void persistir(final AlertDialog dialog) {
        if (isCamposValidos()) {
            mRealm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    Treino t = Treino.buscarPorId(realm, id);

                    if (isNullOrEmpty(t))
                        t = new Treino();

                    t.setNome(txtNome.getText().toString());

                    if (isNullOrEmpty(id))
                        t.cria(realm);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), "Treino salvo com sucesso!", Toast.LENGTH_SHORT).show();

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
        if (txtNome.getText() == null || Objects.equals(txtNome.getText().toString(), "")) {
            validos = false;
            layoutNome.setErrorEnabled(true);
            txtNome.setError(getString(R.string.preenchimento_obrigatorio));
        }

        return validos;
    }
}
