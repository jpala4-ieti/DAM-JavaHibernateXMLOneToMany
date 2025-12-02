package com.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe que representa un carret de compra.
 * 
 * SERIALIZABLE: Implementa Serializable per permetre que els objectes Cart
 * es puguin convertir a bytes (per guardar-los a disc, enviar per xarxa, etc.)
 */
public class Cart implements Serializable {
    
    // Identificador únic del carret (clau primària a la base de dades)
    private long cartId;
    
    // Tipus de carret (exemple: "premium", "standard", etc.)
    private String type;  
    
    /**
     * HASHSET: Col·lecció que emmagatzema els Items d'aquest carret.
     * - HashSet no permet duplicats (utilitza equals/hashCode per comparar)
     * - No garanteix cap ordre dels elements
     * - Aquesta és la part "One" de la relació OneToMany amb Item
     */
    private Set<Item> items = new HashSet<>();

    // Constructor buit requerit per Hibernate/JPA
    public Cart() {}

    public Cart(String type) {
        this.type = type;
    }

    public long getCartId() { return cartId; }
    public void setCartId(long cartId) { this.cartId = cartId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    /**
     * Retorna la col·lecció directa perquè Hibernate pugui gestionar-la.
     * IMPORTANT: No retornem Collections.unmodifiableSet() perquè trencaria
     * el "Dirty Checking" de Hibernate (detecció automàtica de canvis).
     */
    public Set<Item> getItems() {
        return items;
    }
  
    public void setItems(Set<Item> items) {
        this.items = items;
        if (this.items != null) {
            for (Item item : this.items) {
                item.setCart(this);
            }
        }
    }
  
    /**
     * GESTIÓ BIDIRECCIONAL: Quan afegim un Item al carret, també hem d'actualitzar
     * la referència inversa (item.setCart(this)) per mantenir la coherència
     * entre les dues bandes de la relació.
     */
    public void addItem(Item item) {
        if (items.add(item)) {
            item.setCart(this);
        }
    }

    public void removeItem(Item item) {
        if (items.remove(item)) {
            item.setCart(null);
        }
    }
  
    /**
     * STREAMS I COLLECTORS: Utilitza l'API de Streams de Java 8+ per transformar
     * la col·lecció d'items en una representació en text.
     * - stream(): converteix la col·lecció en un flux de dades
     * - map(): transforma cada Item al seu nom (String)
     * - Collectors.joining(): uneix tots els noms amb ", " i afegeix "[" i "]"
     */
    @Override
    public String toString() {
        String llistaItems = "[]";
        
        if (items != null && !items.isEmpty()) {
            llistaItems = items.stream()
                .map(Item::getName)
                .collect(Collectors.joining(", ", "[", "]"));
        }

        return String.format("Cart [ID=%d, Type=%s, Items: %s]", cartId, type, llistaItems);
    }
    
    /**
     * EQUALS PER ENTITATS JPA: Compara per ID si l'entitat està persistida.
     * Si l'ID és 0 (no persistida), compara per referència d'objecte.
     * Això evita problemes quan l'ID s'assigna després de persistir.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        if (cartId == 0 || cart.cartId == 0) return this == cart;
        return cartId == cart.cartId;
    }
    
    /**
     * HASHCODE PER ENTITATS JPA: Si està persistida (ID > 0), utilitza l'ID.
     * Si no, utilitza el hashCode per defecte de Object.
     * IMPORTANT: equals i hashCode han de ser coherents entre si.
     */
    @Override
    public int hashCode() {
        return (cartId > 0) ? Objects.hash(cartId) : super.hashCode();
    }    
}