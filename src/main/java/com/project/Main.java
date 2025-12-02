package com.project;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // Configuració de carpetes
        setupEnvironment();

        // Inicialitzem Hibernate
        Manager.createSessionFactory();

        try {
            System.out.println("--- 1. CREACIÓ ---");
            Cart refCart1 = Manager.addCart("Cart 1");
            Cart refCart2 = Manager.addCart("Cart 2"); 
            Cart refCart3 = Manager.addCart("Cart 3");

            Item refItem1 = Manager.addItem("Item 1");
            Item refItem2 = Manager.addItem("Item 2");
            Item refItem3 = Manager.addItem("Item 3");
            Item refItem4 = Manager.addItem("Item 4");
            Item refItem5 = Manager.addItem("Item 5");
            Item refItem6 = Manager.addItem("Item 6");

            printState("Després de la creació inicial");

            // ASSIGNACIÓ
            System.out.println("--- 2. ASSIGNACIÓ D'ITEMS ---");
            Set<Item> itemsCart1 = new HashSet<>();
            itemsCart1.add(refItem1);
            itemsCart1.add(refItem2);
            itemsCart1.add(refItem3);
            Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCart1);

            Set<Item> itemsCart2 = new HashSet<>();
            itemsCart2.add(refItem4);
            itemsCart2.add(refItem5);
            Manager.updateCart(refCart2.getCartId(), refCart2.getType(), itemsCart2);

            // Re-assignació (moure items al cart 1)
            Set<Item> itemsCart1New = new HashSet<>();
            itemsCart1New.add(refItem1); // Ja hi era
            itemsCart1New.add(refItem2); // Ja hi era
            // Item 3 desapareix del Set, per tant s'hauria de desvincular
            Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCart1New);

            printState("Després d'actualitzar relacions");

            // UPDATES
            System.out.println("--- 3. ACTUALITZACIÓ DE CAMPS ---");
            Manager.updateCart(refCart1.getCartId(), "Cart 1 ACTUALITZAT", null); // null per no tocar items
            Manager.updateItem(refItem1.getItemId(), "Item 1 ACTUALITZAT");

            printState("Després d'actualitzar noms");

            // DELETE
            System.out.println("--- 4. ESBORRAT ---");
            Manager.delete(Cart.class, refCart3.getCartId());
            Manager.delete(Item.class, refItem6.getItemId());

            printState("Després d'esborrar");

            // RECUPERACIÓ ESPECÍFICA
            System.out.println("--- 5. RECUPERACIÓ EAGER ---");
            Cart cart = Manager.getCartWithItems(refCart1.getCartId());
            if (cart != null) {
                System.out.println("Items del carret '" + cart.getType() + "':");
                for (Item item : cart.getItems()) {
                    System.out.println("- " + item.getName());
                }
            }

        } finally {
            // Tanquem la connexió amb Hibernate
            Manager.close();
        }
    }

    private static void printState(String title) {
        System.out.println("\n[" + title + "]");
        System.out.println("CARTS:");
        System.out.println(Manager.collectionToString(Manager.listCollection(Cart.class)));
        System.out.println("ITEMS:");
        System.out.println(Manager.collectionToString(Manager.listCollection(Item.class)));
        System.out.println("------------------------------\n");
    }

    private static void setupEnvironment() {
        String basePath = System.getProperty("user.dir") + "/data/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}