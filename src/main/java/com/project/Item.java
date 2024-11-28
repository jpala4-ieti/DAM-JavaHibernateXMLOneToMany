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
        this.cart = cart;
    }

    @Override
    public String toString() {
        return this.getItemId() + ": " + this.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Item item = (Item) o;
        return itemId == item.itemId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(itemId);
    }    
}