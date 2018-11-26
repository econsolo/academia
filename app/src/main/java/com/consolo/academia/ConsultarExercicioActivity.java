package com.consolo.academia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.consolo.academia.dialog.CadastrarExercicioDialog;
import com.consolo.academia.interfaces.OnDismissDialogFragment;
import com.consolo.academia.model.Exercicio;
import com.consolo.academia.model.Treino;
import com.consolo.academia.model.enuns.EnumTipoCarga;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ConsultarExercicioActivity extends AppCompatActivity implements OnDismissDialogFragment {
    public static final int MENU_CONCLUIR = 1;
    public static final int MENU_ALTERAR = 2;
    public static final int MENU_EXCLUIR = 3;

    private ConsultarExercicioAdapter mAdapter;
    private Treino treino;
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_exercicio);

        Bundle param = getIntent().getExtras();
        if (param != null) {
            treino = new Treino().setId(param.getString(Intent.EXTRA_TEXT));
        }

        FloatingActionButton fab = findViewById(R.id.fab_adicionar_exercicio);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirDialog(null);
            }
        });

        ListView mListView = findViewById(R.id.list_view_exercicio);
        mAdapter = new ConsultarExercicioActivity.ConsultarExercicioAdapter(this);
        mListView.setAdapter(mAdapter);

        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(Menu.NONE, MENU_CONCLUIR, Menu.NONE, "Concluir");
                menu.add(Menu.NONE, MENU_ALTERAR, Menu.NONE, "Alterar");
                menu.add(Menu.NONE, MENU_EXCLUIR, Menu.NONE, "Excluir");
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExercicioItem item = mAdapter.getItem(position);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRealm = Realm.getDefaultInstance();
        getExercicios();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = menuInfo.position;
        final ConsultarExercicioActivity.ExercicioItem exercicioItem = mAdapter.getItem(position);
        if (item.getItemId() == MENU_CONCLUIR) {
            marcarComoConcluido(exercicioItem.id);
        }

        if (item.getItemId() == MENU_ALTERAR) {
            abrirDialog(exercicioItem.id);
        }

        if (item.getItemId() == MENU_EXCLUIR) {
            excluirExercicio(exercicioItem.id);
        }

        return super.onContextItemSelected(item);
    }

    private void marcarComoConcluido(final String id) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                Exercicio e = Exercicio.buscarPorId(realm, id);
                e.setDataConclusao(new Date());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                getExercicios();
                Toast.makeText(ConsultarExercicioActivity.this, "Exercício marcado como concluído", Toast.LENGTH_LONG).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                Toast.makeText(ConsultarExercicioActivity.this, "Erro ao marcar o exercício como concluído", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void excluirExercicio(final String id) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                Exercicio exercicio = Exercicio.buscarPorId(realm, id);
                exercicio.deleteFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(ConsultarExercicioActivity.this, "Exercício excluído", Toast.LENGTH_LONG).show();
                getExercicios();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                Toast.makeText(ConsultarExercicioActivity.this, "Erro ao excluir o exercício", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getExercicios() {
        RealmResults<Exercicio> exercicios = Exercicio.buscarPorTreino(mRealm, treino.getId());
        List<ExercicioItem> itens = new ArrayList<>();
        if (!Util.isNullOrEmpty(exercicios)) {
            for (Exercicio e : exercicios) {
                ExercicioItem ei = new ExercicioItem();
                ei.id = e.getId();
                ei.nome = e.getNome();
                ei.serie = e.getSerie();
                ei.repeticao = e.getRepeticao();
                ei.carga = e.getCarga();
                ei.tipoCarga = e.getTipoCarga();
                ei.dataConclusao = e.getDataConclusao();
                itens.add(ei);
            }
        }
        mAdapter.updateList(itens);
    }

    private void abrirDialog(String idExercicio) {
        CadastrarExercicioDialog dialogFragment = CadastrarExercicioDialog.newInstance(treino.getId(), idExercicio);
        dialogFragment.show(getFragmentManager(), null);
        dialogFragment.setOnDismissListener(this);
    }

    @Override
    public void OnDismissListener(boolean success) {
        if (success)
            getExercicios();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRealm.close();
    }

    private class ConsultarExercicioAdapter extends BaseAdapter {
        private Context mContext;
        private List<ExercicioItem> exercicios;

        ConsultarExercicioAdapter(Context context) {
            this.mContext = context;
        }

        public int getCount() {
            return exercicios == null ? 0 : this.exercicios.size();
        }

        public ExercicioItem getItem(int position) {
            return exercicios.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = newView(parent);
            }

            ExercicioItem item = getItem(position);
            bindView(item, view);

            return view;
        }

        void updateList(List<ExercicioItem> exercicios) {
            this.exercicios = exercicios;
            notifyDataSetChanged();
        }

        @NonNull
        private View newView(ViewGroup parent) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_exercicio, parent, false);
            view.setTag(new ViewHolder(view));
            return view;
        }

        private void bindView(ConsultarExercicioActivity.ExercicioItem exercicio, View view) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            viewHolder.lblNome.setText(exercicio.getTitulo());
            viewHolder.lblSerieRepeticao.setText(exercicio.getSerieRepeticao());
            String carga = exercicio.getCarga();
            if (!Util.isNullOrEmpty(carga)) {
                viewHolder.lblCarga.setText(carga);
                viewHolder.lblCarga.setVisibility(View.VISIBLE);
            }
            viewHolder.lblDataConclusao.setText(exercicio.getData());

            if (exercicio.dataConclusao != null && DateUtils.isToday(exercicio.dataConclusao.getTime())) {
                viewHolder.iconeConcluido.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iconeConcluido.setVisibility(View.GONE);
            }
        }

        private class ViewHolder {
            TextView lblNome;
            TextView lblSerieRepeticao;
            TextView lblCarga;
            TextView lblDataConclusao;
            ImageView iconeConcluido;

            ViewHolder(View view) {
                lblNome = view.findViewById(R.id.lbl_nome_exercicio);
                lblSerieRepeticao = view.findViewById(R.id.lbl_serie_exercicio);
                lblCarga = view.findViewById(R.id.lbl_carga_exercicio);
                lblDataConclusao = view.findViewById(R.id.lbl_data_conclusao_exercicio);
                iconeConcluido = view.findViewById(R.id.img_exercicio_concluido);
            }
        }

    }

    private class ExercicioItem {
        String id;
        String nome;
        Integer serie;
        Integer repeticao;
        Integer tipoCarga;
        Integer carga;
        Date dataConclusao;

        String getTitulo() {
            return nome;
        }

        String getSerieRepeticao() {
            StringBuilder sb = new StringBuilder();
            sb.append(serie);
            if (serie > 1) {
                sb.append(" séries de ");
            } else {
                sb.append(" série de ");
            }
            sb.append(repeticao);
            return sb.toString();
        }

        String getCarga() {
            if (Util.isNullOrEmpty(carga)) return null;
            StringBuilder sb = new StringBuilder();
            if (EnumTipoCarga.PESO.value.equals(tipoCarga)) {
                sb.append(carga);
                sb.append("kg de carga");
            } else if (EnumTipoCarga.TEMPO.value.equals(tipoCarga)) {
                sb.append(Util.formatarSegundos(carga));
                sb.append(" de duração");
            }
            return sb.toString();
        }

        String getData() {
            StringBuilder sb = new StringBuilder("Último treino: ");
            sb.append(Util.isNullOrEmpty(dataConclusao) ? "--" : Util.dataParaString(dataConclusao, "dd/MM/yyyy HH:mm"));
            return sb.toString();
        }
    }
}
