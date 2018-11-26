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

import com.consolo.academia.dialog.CadastrarTreinoDialog;
import com.consolo.academia.interfaces.OnDismissDialogFragment;
import com.consolo.academia.model.Exercicio;
import com.consolo.academia.model.Treino;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ConsultarTreinoActivity extends AppCompatActivity implements OnDismissDialogFragment {

    public static final int MENU_CONCLUIR = 1;
    public static final int MENU_ALTERAR = 2;
    public static final int MENU_EXCLUIR = 3;

    private ConsultarTreinoAdapter mAdapter;
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_treino);

        FloatingActionButton fab = findViewById(R.id.fab_adicionar_treino);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirDialog(null);
            }
        });

        ListView mListView = findViewById(R.id.list_view_treino);
        mAdapter = new ConsultarTreinoAdapter(this);
        mListView.setAdapter(mAdapter);

        mListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(Menu.NONE, MENU_CONCLUIR, Menu.NONE, R.string.concluir);
                menu.add(Menu.NONE, MENU_ALTERAR, Menu.NONE, R.string.alterar);
                menu.add(Menu.NONE, MENU_EXCLUIR, Menu.NONE, R.string.excluir);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TreinoItem item = mAdapter.getItem(position);
                Intent intent = new Intent(ConsultarTreinoActivity.this, ConsultarExercicioActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, item.id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRealm = Realm.getDefaultInstance();
        getTreinos();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = menuInfo.position;
        final TreinoItem treinoItem = mAdapter.getItem(position);
        if (item.getItemId() == MENU_CONCLUIR) {
            marcarComoConcluido(treinoItem);
        }

        if (item.getItemId() == MENU_ALTERAR) {
            abrirDialog(treinoItem.id);
        }

        if (item.getItemId() == MENU_EXCLUIR) {
            excluirTreino(treinoItem.id);
        }

        return super.onContextItemSelected(item);
    }

    private void marcarComoConcluido(final TreinoItem treinoItem) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<Exercicio> exercicios = Exercicio.buscarPorTreino(realm, treinoItem.id);

                for (Exercicio exercicio : exercicios) {
                    if (Util.isNullOrEmpty(exercicio.getDataConclusao())) {
                        exercicio.setDataConclusao(new Date());
                    }
                }

                Treino t = Treino.buscarPorId(realm, treinoItem.id);
                t.setDataConclusao(new Date());
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                getTreinos();
                Toast.makeText(ConsultarTreinoActivity.this, "Treino marcado como concluído", Toast.LENGTH_LONG).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                Toast.makeText(ConsultarTreinoActivity.this, "Erro ao marcar o treino como concluído", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void excluirTreino(final String id) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<Exercicio> exercicios = Exercicio.buscarPorTreino(realm, id);
                for (Exercicio exercicio : exercicios) {
                    exercicio.deleteFromRealm();
                }

                Treino treino = Treino.buscarPorId(realm, id);
                treino.deleteFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(ConsultarTreinoActivity.this, "Treino excluído", Toast.LENGTH_LONG).show();
                getTreinos();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                Toast.makeText(ConsultarTreinoActivity.this, "Erro ao excluir o treino", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getTreinos() {
        RealmResults<Treino> treinos = Treino.buscarTodos(mRealm);
        List<TreinoItem> itens = new ArrayList<>();
        if (!Util.isNullOrEmpty(treinos)) {
            for (Treino t : treinos) {
                TreinoItem ti = new TreinoItem();
                ti.id = t.getId();
                ti.nome = t.getNome();
                ti.dataConclusao = t.getDataConclusao();
                itens.add(ti);
            }
        }
        mAdapter.updateList(itens);
    }

    private void abrirDialog(String idTreino) {
        CadastrarTreinoDialog dialogFragment = CadastrarTreinoDialog.newInstance(idTreino);
        dialogFragment.show(getFragmentManager(), null);
        dialogFragment.setOnDismissListener(this);
    }

    @Override
    public void OnDismissListener(boolean success) {
        if (success)
            getTreinos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRealm.close();
    }

    private class ConsultarTreinoAdapter extends BaseAdapter {
        private Context mContext;
        private List<TreinoItem> treinos;

        ConsultarTreinoAdapter(Context context) {
            this.mContext = context;
        }

        public int getCount() {
            return treinos == null ? 0 : this.treinos.size();
        }

        public TreinoItem getItem(int position) {
            return treinos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = newView(parent);
            }

            TreinoItem item = getItem(position);
            bindView(item, view);

            return view;
        }

        void updateList(List<TreinoItem> treinos) {
            this.treinos = treinos;
            notifyDataSetChanged();
        }

        @NonNull
        private View newView(ViewGroup parent) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_treino, parent, false);
            view.setTag(new ViewHolder(view));
            return view;
        }

        private void bindView(TreinoItem treino, View view) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            viewHolder.lblNome.setText(treino.getTitulo());
            viewHolder.lblDataConclusao.setText(treino.getDescricao());

            if (treino.dataConclusao != null && DateUtils.isToday(treino.dataConclusao.getTime())) {
                viewHolder.iconeConcluido.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iconeConcluido.setVisibility(View.GONE);
            }
        }

        private class ViewHolder {
            TextView lblNome;
            TextView lblDataConclusao;
            ImageView iconeConcluido;

            ViewHolder(View view) {
                lblNome = view.findViewById(R.id.lbl_nome_treino);
                lblDataConclusao = view.findViewById(R.id.lbl_data_conclusao_treino);
                iconeConcluido = view.findViewById(R.id.img_treino_concluido);
            }
        }

    }

    private class TreinoItem {
        String id;
        String nome;
        Date dataConclusao;

        String getTitulo() {
            return nome;
        }

        String getDescricao() {
            StringBuilder sb = new StringBuilder("Último treino: ");
            sb.append(Util.isNullOrEmpty(dataConclusao) ? "--" : Util.dataParaString(dataConclusao, "dd/MM/yyyy HH:mm"));
            return sb.toString();
        }
    }


}
