package com.consolo.academia.model;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class Exercicio extends RealmObject {

    @PrimaryKey
    private String id;

    private String nome;
    private Integer serie;
    private Integer repeticao;
    private Integer tipoCarga;
    private Integer carga;
    private Date dataConclusao;
    private Treino treino;

    public static RealmResults<Exercicio> buscarPorTreino(Realm realm, String id) {
        return realm.where(Exercicio.class)
                .equalTo("treino.id", id)
                .findAll();
    }

    public static Exercicio buscarPorId(Realm realm, String id) {
        return realm.where(Exercicio.class)
                .equalTo("id", id)
                .findFirst();
    }

    public Exercicio cria(Realm realm) {
        return realm.createObject(Exercicio.class, UUID.randomUUID().toString())
                .setNome(nome)
                .setSerie(serie)
                .setRepeticao(repeticao)
                .setTipoCarga(tipoCarga)
                .setCarga(carga)
                .setDataConclusao(null)
                .setTreino(treino);
    }

    public String getId() {
        return id;
    }

    public Exercicio setId(String id) {
        this.id = id;
        return this;
    }

    public String getNome() {
        return nome;
    }

    public Exercicio setNome(String nome) {
        this.nome = nome;
        return this;
    }

    public Integer getSerie() {
        return serie;
    }

    public Exercicio setSerie(Integer serie) {
        this.serie = serie;
        return this;
    }

    public Integer getRepeticao() {
        return repeticao;
    }

    public Exercicio setRepeticao(Integer repeticao) {
        this.repeticao = repeticao;
        return this;
    }

    public Integer getTipoCarga() {
        return tipoCarga;
    }

    public Exercicio setTipoCarga(Integer tipoCarga) {
        this.tipoCarga = tipoCarga;
        return this;
    }

    public Integer getCarga() {
        return carga;
    }

    public Exercicio setCarga(Integer carga) {
        this.carga = carga;
        return this;
    }

    public Treino getTreino() {
        return treino;
    }

    public Exercicio setTreino(Treino treino) {
        this.treino = treino;
        return this;
    }

    public Date getDataConclusao() {
        return dataConclusao;
    }

    public Exercicio setDataConclusao(Date dataConclusao) {
        this.dataConclusao = dataConclusao;
        return this;
    }
}
