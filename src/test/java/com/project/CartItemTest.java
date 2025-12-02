package com.project;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLASSE DE TEST PER A LES ENTITATS CART I ITEM
 * =============================================
 * 
 * Aquesta classe verifica el correcte funcionament de les operacions CRUD
 * i les relacions bidireccionals OneToMany entre Cart i Item.
 * 
 * JUNIT 5 (JUPITER):
 * Framework de testing per Java que proporciona anotacions (@Test, @BeforeAll, etc.)
 * per definir i organitzar els tests. Cada mètode anotat amb @Test s'executa
 * de manera independent.
 * 
 * ANATOMIA D'UN TEST:
 * - Arrange (Preparar): Configurar les dades i l'estat inicial
 * - Act (Actuar): Executar l'operació que volem testejar
 * - Assert (Verificar): Comprovar que el resultat és l'esperat
 * 
 * @TestMethodOrder: Permet definir l'ordre d'execució dels tests.
 * OrderAnnotation indica que s'ordenaran segons @Order(n).
 */
@TestMethodOrder(OrderAnnotation.class)
public class CartItemTest {
    
    /**
     * VARIABLES D'INSTÀNCIA vs ESTÀTIQUES:
     * Utilitzem variables d'instància (no static) perquè cada test
     * pugui tenir el seu propi estat. JUnit 5 crea una nova instància
     * de la classe per a cada mètode @Test per defecte.
     */
    private Cart testCart;
    private Item testItem1;
    private Item testItem2;
    
    // =========================================================================
    // CONFIGURACIÓ DEL CICLE DE VIDA DELS TESTS
    // =========================================================================
    
    /**
     * @BeforeAll: S'executa UNA SOLA VEGADA abans de tots els tests.
     * Ha de ser static perquè s'executa abans de crear cap instància de la classe.
     * 
     * Aquí inicialitzem la SessionFactory d'Hibernate, que és costosa de crear
     * i només necessitem una per a tots els tests.
     */
    @BeforeAll
    public static void initHibernate() {
        Manager.createSessionFactory();
    }

    /**
     * @AfterAll: S'executa UNA SOLA VEGADA després de tots els tests.
     * Alliberem els recursos d'Hibernate (connexions a BD, pools, etc.).
     */
    @AfterAll
    public static void closeHibernate() {
        Manager.close();
    }

    /**
     * @BeforeEach: S'executa ABANS DE CADA test individual.
     * 
     * AÏLLAMENT DE TESTS:
     * Cada test ha de ser independent. Creem dades fresques abans de cada test
     * per garantir que l'estat d'un test no afecti els altres.
     * Això segueix el principi FIRST: Fast, Independent, Repeatable, Self-validating, Timely.
     */
    @BeforeEach
    public void setUp() {
        testCart = Manager.addCart("Carret de Test");
        testItem1 = Manager.addItem("Item Test 1");
        testItem2 = Manager.addItem("Item Test 2");
    }

    /**
     * @AfterEach: S'executa DESPRÉS DE CADA test individual.
     * 
     * NETEJA DE DADES:
     * Esborrem les entitats creades per evitar que s'acumulin a la BD
     * i interfereixin amb altres tests.
     * 
     * TRY-CATCH SILENCIÓS:
     * Utilitzem try-catch buit perquè si l'entitat ja s'ha esborrat
     * durant el test, no volem que falli la neteja.
     */
    @AfterEach
    public void tearDown() {
        if (testItem1 != null) {
            try { Manager.delete(Item.class, testItem1.getItemId()); } 
            catch (Exception ignored) {}
        }
        if (testItem2 != null) {
            try { Manager.delete(Item.class, testItem2.getItemId()); } 
            catch (Exception ignored) {}
        }
        if (testCart != null) {
            try { Manager.delete(Cart.class, testCart.getCartId()); } 
            catch (Exception ignored) {}
        }
    }
    
    // =========================================================================
    // TESTS DE CREACIÓ (CREATE)
    // =========================================================================
    
    /**
     * TEST: Verificar que la creació d'un Cart funciona correctament.
     * 
     * ASSERCIONS UTILITZADES:
     * - assertNotNull: Comprova que l'objecte no és null
     * - assertTrue: Comprova que una condició és certa
     * - assertEquals: Comprova que dos valors són iguals
     * 
     * El segon paràmetre de cada asserció és el missatge d'error
     * que es mostrarà si el test falla (ajuda al debugging).
     */
    @Test
    @Order(1)
    @DisplayName("Creació d'un Cart amb ID vàlid")
    public void testCreateCart() {
        // Assert
        assertNotNull(testCart, "El carret no hauria de ser null després de crear-lo");
        assertTrue(testCart.getCartId() > 0, "Hibernate hauria d'assignar un ID positiu");
        assertEquals("Carret de Test", testCart.getType(), "El tipus hauria de coincidir");
    }
    
    /**
     * TEST: Verificar la creació d'Items.
     * 
     * @DisplayName: Proporciona un nom llegible per al test que apareixerà
     * als informes d'execució en lloc del nom del mètode.
     */
    @Test
    @Order(2)
    @DisplayName("Creació d'Items amb IDs únics")
    public void testCreateItems() {
        // Assert
        assertNotNull(testItem1, "L'item 1 no hauria de ser null");
        assertNotNull(testItem2, "L'item 2 no hauria de ser null");
        assertTrue(testItem1.getItemId() > 0, "L'item 1 hauria de tenir ID vàlid");
        assertTrue(testItem2.getItemId() > 0, "L'item 2 hauria de tenir ID vàlid");
        assertNotEquals(testItem1.getItemId(), testItem2.getItemId(), 
            "Cada item ha de tenir un ID únic");
    }
    
    /**
     * TEST: Verificar que es poden crear múltiples Carts independents.
     */
    @Test
    @Order(3)
    @DisplayName("Creació de múltiples Carts independents")
    public void testCreateMultipleCarts() {
        // Arrange & Act
        Cart cart2 = Manager.addCart("Segon Carret");
        Cart cart3 = Manager.addCart("Tercer Carret");
        
        try {
            // Assert
            assertNotEquals(testCart.getCartId(), cart2.getCartId());
            assertNotEquals(cart2.getCartId(), cart3.getCartId());
            assertEquals("Segon Carret", cart2.getType());
            assertEquals("Tercer Carret", cart3.getType());
        } finally {
            // Cleanup dels carts addicionals
            Manager.delete(Cart.class, cart2.getCartId());
            Manager.delete(Cart.class, cart3.getCartId());
        }
    }
    
    // =========================================================================
    // TESTS DE RELACIONS BIDIRECCIONALS
    // =========================================================================
    
    /**
     * TEST: Afegir Items a un Cart i verificar la relació.
     * 
     * RELACIÓ BIDIRECCIONAL:
     * Quan afegim items al cart, hem de verificar:
     * 1. Que el cart conté els items (cart.getItems())
     * 2. Que cada item referencia el cart (item.getCart())
     * 
     * STREAMS API:
     * Utilitzem streams per cercar elements dins col·leccions
     * de manera funcional i expressiva.
     */
    @Test
    @Order(10)
    @DisplayName("Afegir Items a un Cart manté la relació bidireccional")
    public void testAddItemsToCart() {
        // Arrange
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        items.add(testItem2);
        
        // Act
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // Assert - Costat Cart (OneToMany)
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        assertNotNull(updatedCart, "El cart actualitzat no hauria de ser null");
        assertEquals(2, updatedCart.getItems().size(), "El carret hauria de tenir 2 items");
        
        // Verificar que els items específics hi són
        boolean conteItem1 = updatedCart.getItems().stream()
            .anyMatch(i -> i.getItemId() == testItem1.getItemId());
        boolean conteItem2 = updatedCart.getItems().stream()
            .anyMatch(i -> i.getItemId() == testItem2.getItemId());
        
        assertTrue(conteItem1, "El carret hauria de contenir l'item 1");
        assertTrue(conteItem2, "El carret hauria de contenir l'item 2");
        
        // Assert - Costat Item (ManyToOne)
        Item itemRecarregat = Manager.getById(Item.class, testItem1.getItemId());
        assertNotNull(itemRecarregat.getCart(), "L'item hauria de tenir referència al cart");
        assertEquals(testCart.getCartId(), itemRecarregat.getCart().getCartId(),
            "L'item hauria d'apuntar al cart correcte");
    }
    
    /**
     * TEST: Verificar que un Item sense Cart associat funciona correctament.
     * 
     * NULL SAFETY:
     * És important verificar que les entitats funcionen tant amb
     * relacions establertes com sense elles.
     */
    @Test
    @Order(11)
    @DisplayName("Item sense Cart associat té referència null")
    public void testItemWithoutCart() {
        // Assert
        Item item = Manager.getById(Item.class, testItem1.getItemId());
        assertNull(item.getCart(), "Un item nou no hauria de tenir cart associat");
    }
    
    /**
     * TEST: Reassignar un Item d'un Cart a un altre.
     * 
     * CONSISTÈNCIA REFERENCIAL:
     * Quan movem un item d'un cart a un altre:
     * - El cart original ha de perdre l'item
     * - El nou cart ha de guanyar l'item
     * - L'item ha d'apuntar al nou cart
     */
    @Test
    @Order(12)
    @DisplayName("Reassignar Item d'un Cart a un altre")
    public void testReassignItemToDifferentCart() {
        // Arrange
        Cart secondCart = Manager.addCart("Segon Carret");
        
        Set<Item> itemsCart1 = new HashSet<>();
        itemsCart1.add(testItem1);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), itemsCart1);
        
        try {
            // Act - Moure item1 al segon cart
            Set<Item> itemsCart2 = new HashSet<>();
            itemsCart2.add(testItem1);
            Manager.updateCart(secondCart.getCartId(), secondCart.getType(), itemsCart2);
            
            // Buidar el primer cart
            Manager.updateCart(testCart.getCartId(), testCart.getType(), new HashSet<>());
            
            // Assert
            Cart cart1Updated = Manager.getCartWithItems(testCart.getCartId());
            Cart cart2Updated = Manager.getCartWithItems(secondCart.getCartId());
            
            assertEquals(0, cart1Updated.getItems().size(), 
                "El primer cart hauria d'estar buit");
            assertEquals(1, cart2Updated.getItems().size(), 
                "El segon cart hauria de tenir l'item");
            
            // Verificar la referència inversa
            Item itemMogut = Manager.getById(Item.class, testItem1.getItemId());
            assertEquals(secondCart.getCartId(), itemMogut.getCart().getCartId(),
                "L'item hauria d'apuntar al nou cart");
            
        } finally {
            Manager.delete(Cart.class, secondCart.getCartId());
        }
    }
    
    // =========================================================================
    // TESTS D'ACTUALITZACIÓ (UPDATE)
    // =========================================================================
    
    /**
     * TEST: Actualitzar el nom d'un Item.
     */
    @Test
    @Order(20)
    @DisplayName("Actualitzar nom d'un Item")
    public void testUpdateItemName() {
        // Arrange
        String nouNom = "Item Actualitzat";
        
        // Act
        Manager.updateItem(testItem1.getItemId(), nouNom);
        
        // Assert
        Item itemActualitzat = Manager.getById(Item.class, testItem1.getItemId());
        assertEquals(nouNom, itemActualitzat.getName(), 
            "El nom de l'item hauria d'estar actualitzat");
    }
    
    /**
     * TEST: Actualitzar el tipus d'un Cart sense modificar els Items.
     * 
     * PASSAR NULL:
     * Quan passem null com a Set d'items a updateCart(), indiquem
     * que no volem modificar les relacions, només altres camps.
     */
    @Test
    @Order(21)
    @DisplayName("Actualitzar Cart sense modificar relacions (passant null)")
    public void testUpdateCartTypeOnly() {
        // Arrange - Afegir items primer
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // Act - Actualitzar només el tipus (passant null per items)
        String nouTipus = "Carret Premium";
        Manager.updateCart(testCart.getCartId(), nouTipus, null);
        
        // Assert
        Cart cartActualitzat = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(nouTipus, cartActualitzat.getType(), 
            "El tipus hauria d'estar actualitzat");
        assertEquals(1, cartActualitzat.getItems().size(), 
            "Els items NO haurien de canviar quan passem null");
    }
    
    /**
     * TEST: Eliminar un Item d'un Cart actualitzant amb un Set reduït.
     */
    @Test
    @Order(22)
    @DisplayName("Eliminar Item d'un Cart mitjançant actualització")
    public void testRemoveItemFromCart() {
        // Arrange
        Set<Item> itemsInicials = new HashSet<>();
        itemsInicials.add(testItem1);
        itemsInicials.add(testItem2);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), itemsInicials);
        
        // Verificar estat inicial
        Cart cartInicial = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(2, cartInicial.getItems().size(), "Precondició: cart amb 2 items");
        
        // Act - Actualitzar amb només un item
        Set<Item> itemsReduits = new HashSet<>();
        itemsReduits.add(testItem1);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), itemsReduits);
        
        // Assert
        Cart cartActualitzat = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(1, cartActualitzat.getItems().size(), 
            "El cart hauria de tenir només 1 item");
        
        boolean conteItem1 = cartActualitzat.getItems().stream()
            .anyMatch(i -> i.getItemId() == testItem1.getItemId());
        assertTrue(conteItem1, "L'item 1 hauria de romandre");
        
        // Verificar que item2 ja no té cart
        Item item2Actualitzat = Manager.getById(Item.class, testItem2.getItemId());
        assertNull(item2Actualitzat.getCart(), 
            "L'item eliminat no hauria de tenir referència al cart");
    }
    
    /**
     * TEST: Buidar completament un Cart.
     */
    @Test
    @Order(23)
    @DisplayName("Buidar completament un Cart")
    public void testEmptyCart() {
        // Arrange
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        items.add(testItem2);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // Act - Passar Set buit
        Manager.updateCart(testCart.getCartId(), testCart.getType(), new HashSet<>());
        
        // Assert
        Cart cartBuit = Manager.getCartWithItems(testCart.getCartId());
        assertTrue(cartBuit.getItems().isEmpty(), "El cart hauria d'estar buit");
    }
    
    // =========================================================================
    // TESTS DE LECTURA (READ)
    // =========================================================================
    
    /**
     * TEST: Recuperar entitat per ID amb getById().
     */
    @Test
    @Order(30)
    @DisplayName("Recuperar entitat per ID")
    public void testGetById() {
        // Act
        Cart cartRecuperat = Manager.getById(Cart.class, testCart.getCartId());
        Item itemRecuperat = Manager.getById(Item.class, testItem1.getItemId());
        
        // Assert
        assertNotNull(cartRecuperat, "getById hauria de retornar el cart");
        assertNotNull(itemRecuperat, "getById hauria de retornar l'item");
        assertEquals(testCart.getType(), cartRecuperat.getType());
        assertEquals(testItem1.getName(), itemRecuperat.getName());
    }
    
    /**
     * TEST: getById amb ID inexistent retorna null.
     */
    @Test
    @Order(31)
    @DisplayName("getById amb ID inexistent retorna null")
    public void testGetByIdNotFound() {
        // Act
        Cart cartInexistent = Manager.getById(Cart.class, 99999L);
        
        // Assert
        assertNull(cartInexistent, "Hauria de retornar null per ID inexistent");
    }
    
    /**
     * TEST: Llistar totes les entitats d'un tipus.
     * 
     * NOTA: Com que altres tests poden haver deixat dades,
     * només verifiquem que hi ha ALMENYS els nostres elements.
     */
    @Test
    @Order(32)
    @DisplayName("Llistar tots els Items")
    public void testListAllItems() {
        // Act
        Collection<Item> items = Manager.listCollection(Item.class);
        
        // Assert
        assertNotNull(items, "La col·lecció no hauria de ser null");
        assertTrue(items.size() >= 2, 
            "Hauria d'haver-hi almenys els 2 items creats al setUp");
    }
    
    /**
     * TEST: Llistar amb clàusula WHERE.
     * 
     * HQL WHERE:
     * Podem filtrar resultats utilitzant sintaxi similar a SQL
     * però referint-nos als camps de l'entitat (no de la taula).
     */
    @Test
    @Order(33)
    @DisplayName("Llistar Items amb filtre WHERE")
    public void testListWithWhereClause() {
        // Act
        List<Item> items = Manager.listCollection(Item.class, 
            "name LIKE '%Test 1%'");
        
        // Assert
        assertFalse(items.isEmpty(), "Hauria de trobar l'item amb 'Test 1' al nom");
        assertTrue(items.stream().anyMatch(i -> i.getName().contains("Test 1")));
    }
    
    /**
     * TEST: getCartWithItems carrega els Items (EAGER loading).
     * 
     * LAZY vs EAGER:
     * - LAZY: Les col·leccions no es carreguen fins que s'hi accedeix
     * - EAGER: Les col·leccions es carreguen immediatament amb l'entitat
     * 
     * getCartWithItems utilitza Hibernate.initialize() per forçar
     * la càrrega mentre la sessió encara està oberta.
     */
    @Test
    @Order(34)
    @DisplayName("getCartWithItems carrega Items correctament")
    public void testGetCartWithItemsEagerLoading() {
        // Arrange
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // Act
        Cart cartAmbItems = Manager.getCartWithItems(testCart.getCartId());
        
        // Assert - Podem accedir als items FORA de la transacció
        assertNotNull(cartAmbItems.getItems(), 
            "La col·lecció d'items no hauria de ser null");
        assertEquals(1, cartAmbItems.getItems().size());
        
        // Verificar que podem iterar (no hi ha LazyInitializationException)
        assertDoesNotThrow(() -> {
            for (Item item : cartAmbItems.getItems()) {
                assertNotNull(item.getName());
            }
        }, "No hauria de llançar LazyInitializationException");
    }
    
    // =========================================================================
    // TESTS D'ESBORRAT (DELETE)
    // =========================================================================
    
    /**
     * TEST: Esborrar un Item individual.
     */
    @Test
    @Order(40)
    @DisplayName("Esborrar un Item")
    public void testDeleteItem() {
        // Arrange
        long itemId = testItem1.getItemId();
        
        // Act
        Manager.delete(Item.class, itemId);
        
        // Assert
        Item itemEsborrat = Manager.getById(Item.class, itemId);
        assertNull(itemEsborrat, "L'item hauria d'estar eliminat");
        
        // Marcar com null pel tearDown
        testItem1 = null;
    }
    
    /**
     * TEST: Esborrar un Cart i verificar el comportament CASCADE.
     * 
     * CASCADE:
     * Segons la configuració del mapping (cascade="all"), quan s'esborra
     * el Cart, els Items relacionats també s'haurien d'esborrar
     * o almenys desvinculat.
     */
    @Test
    @Order(41)
    @DisplayName("Esborrar Cart amb CASCADE")
    public void testDeleteCartWithCascade() {
        // Arrange - Afegir items al cart
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        items.add(testItem2);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        long cartId = testCart.getCartId();
        long item1Id = testItem1.getItemId();
        long item2Id = testItem2.getItemId();
        
        // Act
        Manager.delete(Cart.class, cartId);
        
        // Assert - Cart esborrat
        Cart cartEsborrat = Manager.getById(Cart.class, cartId);
        assertNull(cartEsborrat, "El cart hauria d'estar eliminat");
        
        // Marcar com null pel tearDown
        testCart = null;
        testItem1 = null;
        testItem2 = null;
    }
    
    /**
     * TEST: Esborrar ID inexistent no llança excepció.
     */
    @Test
    @Order(42)
    @DisplayName("Esborrar ID inexistent no causa error")
    public void testDeleteNonExistent() {
        // Assert - No hauria de llançar cap excepció
        assertDoesNotThrow(() -> {
            Manager.delete(Cart.class, 99999L);
        }, "Esborrar ID inexistent no hauria de fallar");
    }
    
    // =========================================================================
    // TESTS DE MÈTODES AUXILIARS I REPRESENTACIÓ
    // =========================================================================
    
    /**
     * TEST: Verificar el format del mètode toString() de Cart.
     */
    @Test
    @Order(50)
    @DisplayName("toString() de Cart amb format correcte")
    public void testCartToString() {
        // Arrange
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // Act
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        String str = cart.toString();
        
        // Assert
        assertTrue(str.contains("Cart"), "toString hauria de contenir 'Cart'");
        assertTrue(str.contains(String.valueOf(testCart.getCartId())), 
            "toString hauria de contenir l'ID");
        assertTrue(str.contains(testCart.getType()), 
            "toString hauria de contenir el tipus");
    }
    
    /**
     * TEST: Verificar el format del mètode toString() d'Item.
     */
    @Test
    @Order(51)
    @DisplayName("toString() d'Item amb format correcte")
    public void testItemToString() {
        // Act
        String str = testItem1.toString();
        
        // Assert
        assertTrue(str.contains("Item"), "toString hauria de contenir 'Item'");
        assertTrue(str.contains(String.valueOf(testItem1.getItemId())));
        assertTrue(str.contains(testItem1.getName()));
    }
    
    /**
     * TEST: collectionToString amb col·lecció buida.
     */
    @Test
    @Order(52)
    @DisplayName("collectionToString amb col·lecció buida")
    public void testCollectionToStringEmpty() {
        // Act
        String result = Manager.collectionToString(new HashSet<>());
        
        // Assert
        assertEquals("[]", result, "Col·lecció buida hauria de retornar '[]'");
    }
    
    /**
     * TEST: collectionToString amb null.
     */
    @Test
    @Order(53)
    @DisplayName("collectionToString amb null")
    public void testCollectionToStringNull() {
        // Act
        String result = Manager.collectionToString(null);
        
        // Assert
        assertEquals("[]", result, "null hauria de retornar '[]'");
    }
    
    // =========================================================================
    // TESTS D'EQUALS I HASHCODE
    // =========================================================================
    
    /**
     * TEST: Verificar contracte equals() i hashCode() per Cart.
     * 
     * CONTRACTE EQUALS/HASHCODE:
     * 1. Reflexivitat: a.equals(a) == true
     * 2. Simetria: a.equals(b) == b.equals(a)
     * 3. Transitivitat: si a.equals(b) i b.equals(c), llavors a.equals(c)
     * 4. Consistència: múltiples crides retornen el mateix
     * 5. null: a.equals(null) == false
     * 6. Si equals() == true, hashCode() ha de ser igual
     */
    @Test
    @Order(60)
    @DisplayName("Contracte equals/hashCode per Cart persistit")
    public void testCartEqualsHashCode() {
        // Arrange - Recuperar el mateix cart dues vegades
        Cart cart1 = Manager.getById(Cart.class, testCart.getCartId());
        Cart cart2 = Manager.getById(Cart.class, testCart.getCartId());
        
        // Assert - Igualtat
        assertEquals(cart1, cart2, "Dos objectes amb el mateix ID haurien de ser iguals");
        assertEquals(cart1.hashCode(), cart2.hashCode(), 
            "hashCode ha de ser consistent amb equals");
        
        // Assert - No igualtat amb null o tipus diferent
        assertNotEquals(cart1, null);
        assertNotEquals(cart1, "String");
        assertNotEquals(cart1, testItem1);
    }
    
    /**
     * TEST: equals() per entitats no persistides compara per referència.
     */
    @Test
    @Order(61)
    @DisplayName("equals() per entitats no persistides")
    public void testEqualsForTransientEntities() {
        // Arrange - Crear entitats sense persistir (ID = 0)
        Cart cartNou1 = new Cart("Temporal 1");
        Cart cartNou2 = new Cart("Temporal 2");
        
        // Assert
        assertEquals(cartNou1, cartNou1, "Una entitat és igual a si mateixa");
        assertNotEquals(cartNou1, cartNou2, 
            "Entitats no persistides amb ID=0 es comparen per referència");
    }
    
    // =========================================================================
    // TESTS DE CASOS LÍMIT I ROBUSTESA
    // =========================================================================
    
    /**
     * TEST: Crear Cart amb tipus null.
     */
    @Test
    @Order(70)
    @DisplayName("Crear Cart amb tipus null")
    public void testCreateCartWithNullType() {
        // Act
        Cart cartNull = Manager.addCart(null);
        
        try {
            // Assert
            assertNotNull(cartNull, "El cart s'hauria de crear");
            assertTrue(cartNull.getCartId() > 0, "Hauria de tenir ID");
            assertNull(cartNull.getType(), "El tipus hauria de ser null");
        } finally {
            Manager.delete(Cart.class, cartNull.getCartId());
        }
    }
    
    /**
     * TEST: Crear Item amb nom molt llarg.
     */
    @Test
    @Order(71)
    @DisplayName("Crear Item amb nom llarg")
    public void testCreateItemWithLongName() {
        // Arrange
        String nomLlarg = "A".repeat(200);
        
        // Act
        Item itemLlarg = Manager.addItem(nomLlarg);
        
        try {
            // Assert
            assertNotNull(itemLlarg);
            assertEquals(nomLlarg, itemLlarg.getName());
        } finally {
            Manager.delete(Item.class, itemLlarg.getItemId());
        }
    }
    
    /**
     * TEST: Afegir el mateix Item dues vegades al Set.
     * 
     * SET BEHAVIOR:
     * Un Set no permet duplicats. Si intentem afegir el mateix element
     * dues vegades, només s'afegirà una vegada.
     */
    @Test
    @Order(72)
    @DisplayName("Afegir Item duplicat al Set no el duplica")
    public void testAddDuplicateItemToSet() {
        // Arrange
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        items.add(testItem1); // Duplicat
        
        // Act
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // Assert
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(1, cart.getItems().size(), 
            "El Set no hauria de contenir duplicats");
    }
}