package com.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Cart implements Serializable {
    
    private long cartId;
    private String type;  
    private Set<Item> items = new HashSet<>();

    public Cart() {}

    public Cart(String type) {
        this.type = type;
    }

    public long getCartId() { return cartId; }
    public void setCartId(long cartId) { this.cartId = cartId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // CORRECCIÓ: Retornem la col·lecció directa perquè Hibernate pugui gestionar-la.
    // Retornar un wrapper (unmodifiableSet) trenca el "Dirty Checking" de Hibernate.
    public Set<Item> getItems() {
        return items;
    }
  
    // Hibernate utilitza aquest setter o reflexió. 
    // Mantenim la lògica per assegurar la coherència si s'usa manualment.
    public void setItems(Set<Item> items) {
        this.items = items;
        if (this.items != null) {
            // Assegurar que els items apunten a aquest carret
            for (Item item : this.items) {
                item.setCart(this);
            }
        }
    }
  
    // Helper: Gestió bidireccional segura
    public void addItem(Item item) {
        if (items.add(item)) {
            item.setCart(this);
        }
    }

    // Helper: Gestió bidireccional segura
    public void removeItem(Item item) {
        if (items.remove(item)) {
            item.setCart(null);
        }
    }
  
    @Override
    public String toString() {
        // Per defecte posem els claudàtors buits si no hi ha items
        String llistaItems = "[]";
        
        if (items != null && !items.isEmpty()) {
            llistaItems = items.stream()
                .map(Item::getName)
                // AQUI ESTÀ EL CANVI: separador ", ", prefix "[" i sufix "]"
                .collect(Collectors.joining(", ", "[", "]"));
        }

        return String.format("Cart [ID=%d, Type=%s, Items: %s]", cartId, type, llistaItems);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        // Igualtat per ID si està persistit, sinó per referència
        if (cartId == 0 || cart.cartId == 0) return this == cart;
        return cartId == cart.cartId;
    }
    
    @Override
    public int hashCode() {
        // HashCode constant o basat en ID per evitar problemes en Sets si l'ID canvia (encara que aquí assumim ID fix un cop persistit)
        return (cartId > 0) ? Objects.hash(cartId) : super.hashCode();
    }    
}