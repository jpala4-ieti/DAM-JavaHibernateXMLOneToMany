package com.project;

import java.io.Serializable;

public class Item implements Serializable {

	private long itemId;
	private String name;
    private Cart cart;

	public Item() { }

	public Item(String name) {
		this.name = name;
	}

	public long getItemId() {
		return this.itemId;
	}

	public void setItemId(long id) {
		this.itemId = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCart (Cart cart) {
		this.cart = cart;
	}

	public Cart getCart () {
		return this.cart;
	}

	@Override
    public String toString () {
      	return this.getItemId() + ": " + this.getName();
    }
}