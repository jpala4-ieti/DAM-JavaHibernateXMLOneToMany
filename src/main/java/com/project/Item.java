package com.project;

import java.io.Serializable;

public class Item implements Serializable {

	private long itemId;
	private String name;
    private Cart cart;

    public Item() {}

    public Item(String name) {
        this.name = name;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        // Evitem recursió infinita
        if (this.cart != null && this.cart.equals(cart)) {
            return;
        }
        
        // Eliminem l'item del cart anterior si existeix
        if (this.cart != null) {
            this.cart.getItems().remove(this);
        }
        
        // Assignem el nou cart
        this.cart = cart;
        
        // Afegim l'item al nou cart si existeix
        if (cart != null) {
            cart.getItems().add(this);
        }
    }

    @Override
    public String toString() {
        return this.getItemId() + ": " + this.getName();
    }
}