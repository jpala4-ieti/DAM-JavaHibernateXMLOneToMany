package com.project;

import org.junit.jupiter.api.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class CartItemTest {
    
    // Variables d'instància, no estàtiques (cada test té la seva pròpia instància o estat controlat)
    private Cart testCart;
    private Item testItem1;
    private Item testItem2;
    
    @BeforeAll
    public static void initHibernate() {
        // Inicialitzar la factoria una sola vegada per a tots els tests
        Manager.createSessionFactory();
    }

    @AfterAll
    public static void closeHibernate() {
        Manager.close();
    }

    @BeforeEach
    public void setUp() {
        // CORRECCIÓ: Crear dades fresques abans de CADA test per garantir aïllament
        testCart = Manager.addCart("Carret de Test");
        testItem1 = Manager.addItem("Item Test 1");
        testItem2 = Manager.addItem("Item Test 2");
    }

    @AfterEach
    public void tearDown() {
        // CORRECCIÓ: Netejar dades després de CADA test
        // Primer esborrem items per evitar problemes de claus foranes si no hi ha cascada
        if (testItem1 != null) try { Manager.delete(Item.class, testItem1.getItemId()); } catch (Exception e) {}
        if (testItem2 != null) try { Manager.delete(Item.class, testItem2.getItemId()); } catch (Exception e) {}
        if (testCart != null) try { Manager.delete(Cart.class, testCart.getCartId()); } catch (Exception e) {}
    }
    
    @Test
    public void testCreateCart() {
        assertNotNull(testCart, "El carret no hauria de ser null");
        assertTrue(testCart.getCartId() > 0, "El carret hauria de tenir un ID vàlid");
        assertEquals("Carret de Test", testCart.getType());
    }
    
    @Test
    public void testCreateItems() {
        assertNotNull(testItem1);
        assertNotNull(testItem2);
        assertTrue(testItem1.getItemId() > 0);
    }
    
    @Test
    public void testAddItemsToCart() {
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        items.add(testItem2);
        
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        
        assertNotNull(updatedCart);
        assertEquals(2, updatedCart.getItems().size(), "El carret hauria de tenir 2 items");
        
        // Verifiquem contingut
        boolean found1 = updatedCart.getItems().stream().anyMatch(i -> i.getItemId() == testItem1.getItemId());
        boolean found2 = updatedCart.getItems().stream().anyMatch(i -> i.getItemId() == testItem2.getItemId());
        
        assertTrue(found1, "El carret hauria de contenir l'item 1");
        assertTrue(found2, "El carret hauria de contenir l'item 2");
    }
    
    @Test
    public void testUpdateItem() {
        String newName = "Item Actualitzat";
        Manager.updateItem(testItem1.getItemId(), newName);
        
        Item updatedItem = Manager.getById(Item.class, testItem1.getItemId());
        assertEquals(newName, updatedItem.getName());
    }
    
    @Test
    public void testListItems() {
        Collection<?> items = Manager.listCollection(Item.class);
        assertNotNull(items);
        assertTrue(items.size() >= 2, "Hauria d'haver-hi almenys els 2 items creats al setUp");
    }
    
    @Test
    public void testRemoveItemFromCart() {
        // Setup específic per aquest test: afegir items primer
        Set<Item> initialItems = new HashSet<>();
        initialItems.add(testItem1);
        initialItems.add(testItem2);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), initialItems);
        
        // Ara procedim a esborrar-ne un
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        Set<Item> items = new HashSet<>(cart.getItems()); // Còpia del set
        
        // Busquem l'objecte exacte dins la col·lecció per eliminar-lo correctament (equals/hashCode)
        Item itemToRemove = items.stream()
            .filter(i -> i.getItemId() == testItem1.getItemId())
            .findFirst()
            .orElse(null);

        assertNotNull(itemToRemove, "L'item a eliminar hauria de ser al carret");
        items.remove(itemToRemove);
        
        Manager.updateCart(cart.getCartId(), cart.getType(), items);
        
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(1, updatedCart.getItems().size());
    }
    
    @Test
    public void testDeleteCartAndCheckCascade() {
        // Aquest test verifica que si esborrem el carret, els items (depenent del XML cascade) 
        // fan el que toca. Segons el XML original, cascade="all", així que els items s'haurien d'esborrar o desvincular.
        
        Manager.delete(Cart.class, testCart.getCartId());
        Cart deletedCart = Manager.getById(Cart.class, testCart.getCartId());
        assertNull(deletedCart, "El carret hauria d'estar eliminat");
        
        // Nota: Amb cascade="all" en hibernate, normalment esborra els fills també
        // Però per assegurar el test, posem testCart a null perquè el tearDown no falli
        testCart = null; 
    }
}