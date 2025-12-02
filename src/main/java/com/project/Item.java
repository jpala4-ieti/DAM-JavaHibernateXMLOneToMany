package com.project;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe que representa un article/producte dins d'un carret.
 * 
 * SERIALIZABLE: Permet que l'objecte es pugui convertir a una seqüència de bytes
 * per a persistència, transmissió per xarxa o emmagatzematge en sessió.
 */
public class Item implements Serializable {

    // Identificador únic de l'article (clau primària a la base de dades)
    private long itemId;
    
    // Nom de l'article
    private String name;
    
    /**
     * RELACIÓ MANYTOONE: Referència al carret que conté aquest article.
     * Aquesta és la part "Many" de la relació (molts Items pertanyen a un Cart).
     * És el costat INVERS de la relació bidireccional amb Cart.
     * A la base de dades, això es tradueix en una columna FK (Foreign Key) 
     * a la taula Item que apunta a la taula Cart.
     */
    private Cart cart;

    /**
     * CONSTRUCTOR BUIT: Obligatori per Hibernate/JPA.
     * Hibernate necessita crear instàncies buides mitjançant reflexió
     * abans d'omplir els camps amb les dades de la base de dades.
     */
    public Item() {}

    public Item(String name) {
        this.name = name;
    }

    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    /**
     * STRING.FORMAT: Crea una cadena formatada substituint %d per enters
     * i %s per strings. Més llegible que concatenar amb +.
     */
    @Override
    public String toString() {
        return String.format("Item [ID=%d, Name=%s]", itemId, name);
    }

    /**
     * EQUALS PER ENTITATS JPA:
     * - Si és el mateix objecte en memòria → true
     * - Si algun dels dos no està persistit (ID=0) → compara per referència
     * - Si tots dos estan persistits → compara per ID
     * 
     * Aquesta estratègia evita problemes quan l'ID s'assigna automàticament
     * després de persistir l'entitat a la base de dades.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        if (itemId == 0 || item.itemId == 0) return this == item;
        return itemId == item.itemId;
    }
    
    /**
     * HASHCODE COHERENT AMB EQUALS:
     * - Si l'entitat té ID (està persistida) → hashCode basat en ID
     * - Si no té ID → hashCode per defecte d'Object (basat en memòria)
     * 
     * REGLA FONAMENTAL: Si dos objectes són equals(), han de tenir
     * el mateix hashCode(). Per això ambdós mètodes segueixen la mateixa lògica.
     */
    @Override
    public int hashCode() {
        return (itemId > 0) ? Objects.hash(itemId) : super.hashCode();
    }    
}