package com.consolo.academia.model;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class Treino extends RealmObject {

    @PrimaryKey
    private String id;
    private String nome;
    private Date dataConclusao;

    private RealmList<Exercicio> exercicios;

    public static RealmResults<Treino> buscarTodos(Realm realm) {
        return realm.where(Treino.class).findAll();
    }

    public static Treino buscarPorId(Realm realm, String id) {
        return realm.where(Treino.class)
                .equalTo("id", id)
                .findFirst();
    }

    public Treino cria(Realm realm) {
        return realm.createObject(Treino.class, UUID.randomUUID().toString())
                .setNome(nome);
    }

    public String getId() {
        return id;
    }

    public Treino setId(String id) {
        this.id = id;
        return this;
    }

    public String getNome() {
        return nome;
    }

    public Treino setNome(String nome) {
        this.nome = nome;
        return this;
    }

    public Date getDataConclusao() {
        return dataConclusao;
    }

    public Treino setDataConclusao(Date dataConclusao) {
        this.dataConclusao = dataConclusao;
        return this;
    }

    public RealmList<Exercicio> getExercicios() {
        return exercicios;
    }

    public Treino setExercicios(RealmList<Exercicio> exercicios) {
        this.exercicios = exercicios;
        return this;
    }

}
