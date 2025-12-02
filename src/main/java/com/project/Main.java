package com.project;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe principal que demostra el funcionament de les operacions CRUD
 * (Create, Read, Update, Delete) amb Hibernate i relacions OneToMany.
 */
public class Main {
    public static void main(String[] args) {
        // Crea la carpeta 'data/' si no existeix (per la base de dades)
        setupEnvironment();

        /**
         * SESSIONFACTORY: És l'objecte principal d'Hibernate que gestiona
         * les connexions a la base de dades. Només s'ha de crear UN COP
         * durant tota l'execució de l'aplicació (patró Singleton).
         */
        Manager.createSessionFactory();

        try {
            // === 1. CREATE: Creació d'entitats ===
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

            // === 2. UPDATE: Assignació de relacions ===
            System.out.println("--- 2. ASSIGNACIÓ D'ITEMS ---");
            
            /**
             * HASHSET PER PASSAR RELACIONS: Creem un Set amb els Items
             * que volem assignar al Cart. El mètode updateCart s'encarregarà
             * de sincronitzar la relació bidireccional a la base de dades.
             */
            Set<Item> itemsCart1 = new HashSet<>();
            itemsCart1.add(refItem1);
            itemsCart1.add(refItem2);
            itemsCart1.add(refItem3);
            Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCart1);

            Set<Item> itemsCart2 = new HashSet<>();
            itemsCart2.add(refItem4);
            itemsCart2.add(refItem5);
            Manager.updateCart(refCart2.getCartId(), refCart2.getType(), itemsCart2);

            /**
             * RE-ASSIGNACIÓ: Quan actualitzem amb un nou Set que NO conté
             * Item3, aquest es desvincula automàticament del Cart1.
             * Hibernate detecta quins Items s'han afegit/eliminat del Set.
             */
            Set<Item> itemsCart1New = new HashSet<>();
            itemsCart1New.add(refItem1);
            itemsCart1New.add(refItem2);
            Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCart1New);

            printState("Després d'actualitzar relacions");

            // === 3. UPDATE: Modificació de camps ===
            System.out.println("--- 3. ACTUALITZACIÓ DE CAMPS ---");
            
            /**
             * PASSAR NULL: Quan passem null com a Set d'Items, indiquem
             * al mètode updateCart que NO volem modificar les relacions,
             * només actualitzar altres camps (com el nom/type).
             */
            Manager.updateCart(refCart1.getCartId(), "Cart 1 ACTUALITZAT", null);
            Manager.updateItem(refItem1.getItemId(), "Item 1 ACTUALITZAT");

            printState("Després d'actualitzar noms");

            // === 4. DELETE: Esborrat d'entitats ===
            System.out.println("--- 4. ESBORRAT ---");
            
            /**
             * ESBORRAT GENÈRIC: Utilitzem Class<T> per indicar quina
             * entitat volem esborrar. Això permet reutilitzar el mateix
             * mètode delete() per a qualsevol tipus d'entitat.
             */
            Manager.delete(Cart.class, refCart3.getCartId());
            Manager.delete(Item.class, refItem6.getItemId());

            printState("Després d'esborrar");

            // === 5. READ: Recuperació amb relacions carregades ===
            System.out.println("--- 5. RECUPERACIÓ EAGER ---");
            
            /**
             * EAGER LOADING: getCartWithItems() carrega el Cart i TOTS
             * els seus Items en una sola consulta. Això és útil quan
             * sabem que necessitarem accedir als Items immediatament.
             */
            Cart cart = Manager.getCartWithItems(refCart1.getCartId());
            if (cart != null) {
                System.out.println("Items del carret '" + cart.getType() + "':");
                for (Item item : cart.getItems()) {
                    System.out.println("- " + item.getName());
                }
            }

        } finally {
            /**
             * FINALLY: Aquest bloc s'executa SEMPRE, tant si hi ha errors
             * com si no. És crucial tancar la SessionFactory per alliberar
             * recursos i connexions a la base de dades.
             */
            Manager.close();
        }
    }

    /**
     * Mètode auxiliar per mostrar l'estat actual de la base de dades.
     * Llista tots els Carts i tots els Items.
     */
    private static void printState(String title) {
        System.out.println("\n[" + title + "]");
        System.out.println("CARTS:");
        System.out.println(Manager.collectionToString(Manager.listCollection(Cart.class)));
        System.out.println("ITEMS:");
        System.out.println(Manager.collectionToString(Manager.listCollection(Item.class)));
        System.out.println("------------------------------\n");
    }

    /**
     * Crea el directori 'data/' al directori de treball actual.
     * user.dir: Propietat del sistema que retorna el directori
     * des d'on s'ha executat l'aplicació Java.
     */
    private static void setupEnvironment() {
        String basePath = System.getProperty("user.dir") + "/data/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}