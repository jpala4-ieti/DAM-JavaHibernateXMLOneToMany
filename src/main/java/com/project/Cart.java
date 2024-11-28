package com.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Cart implements Serializable {
    
      private long cartId;
   
      private String type;  

      private Set<Item> items = new HashSet<>();

      public Cart() {}

      public Cart(String type) {
          this.type = type;
      }
  
      public long getCartId() {
          return cartId;
      }
  
      public void setCartId(long cartId) {
          this.cartId = cartId;
      }
  
      public String getType() {
          return type;
      }
  
      public void setType(String type) {
          this.type = type;
      }
  
      public Set<Item> getItems() {
          return items;
      }
  
      public void setItems(Set<Item> items) {
          if (items != null) {
              items.forEach(this::addItem);
          }
      }
  
      public void addItem(Item item) {
          items.add(item);
          item.setCart(this);
      }
  
      public void removeItem(Item item) {
          items.remove(item);
          item.setCart(null);
      }
  
      @Override
      public String toString() {
          StringBuilder str = new StringBuilder();
          for (Item item : items) {
              if (str.length() > 0) {
                  str.append(" | ");
              }
              str.append(item.getName());
          }
          return this.getCartId() + ": " + this.getType() + ", Items: [" + str + "]";
      }   
  }
  