package com.project;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Hibernate;
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;

public class Manager {

    private static SessionFactory factory;

    public static void createSessionFactory() {
        try {
            // configure() carrega hibernate.cfg.xml automàticament
            factory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) { 
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex); 
        }
    }

    public static void close() {
        if (factory != null) factory.close();
    }
  
    // --- GESTIÓ DE TRANSACCIONS (Dry Pattern) ---

    // Executa codi void dins una transacció
    private static void executeInTransaction(Consumer<Session> action) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            action.accept(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            // Important: rellançar l'excepció perquè els tests fallin si hi ha error
            throw new RuntimeException("Error en transacció Hibernate", e);
        }
    }

    // Executa codi que retorna valor dins una transacció
    private static <T> T executeInTransactionWithResult(Function<Session, T> action) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            T result = action.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error en transacció Hibernate", e);
        }
    }

    // --- OPERACIONS CRUD ---

    public static Cart addCart(String type) {
        return executeInTransactionWithResult(session -> {
            Cart cart = new Cart(type);
            session.persist(cart);
            return cart;
        });
    }

    public static Item addItem(String name) {
        return executeInTransactionWithResult(session -> {
            Item item = new Item(name);
            session.persist(item);
            return item;
        });
    }

    public static void updateItem(long itemId, String name) {
        executeInTransaction(session -> {
            Item item = session.get(Item.class, itemId);
            if (item != null) {
                item.setName(name);
                session.merge(item);
            }
        });
    }

    public static void updateCart(long cartId, String type, Set<Item> newItems) {
        executeInTransaction(session -> {
            Cart cart = session.get(Cart.class, cartId);
            if (cart == null) return;
            
            // Actualitzem el nom
            cart.setType(type);
            
            // Si és null, ignorem aquesta part i es mantenen els que hi havia.
            if (newItems != null) {

                // 1. Netejar items existents
                if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                    // Fem còpia per evitar ConcurrentModificationException
                    List<Item> itemsToRemove = List.copyOf(cart.getItems());
                    itemsToRemove.forEach(cart::removeItem);
                }

                // 2. Afegir nous items (si la llista nova no és buida)
                for (Item item : newItems) {
                    Item managedItem = session.get(Item.class, item.getItemId());
                    if (managedItem != null) {
                        cart.addItem(managedItem);
                    }
                }
            }

            session.merge(cart);
        });
    }
        
    public static Cart getCartWithItems(long cartId) {
        return executeInTransactionWithResult(session -> {
            Cart cart = session.get(Cart.class, cartId);
            if (cart != null) {
                // Forcem la inicialització de la col·lecció abans de tancar la sessió
                Hibernate.initialize(cart.getItems());
            }
            return cart;
        });
    }

    public static <T> T getById(Class<T> clazz, long id) {
        return executeInTransactionWithResult(session -> session.get(clazz, id));
    }

    public static <T> void delete(Class<T> clazz, Serializable id) {
        executeInTransaction(session -> {
            T obj = session.get(clazz, id);
            if (obj != null) {
                session.remove(obj);
            }
        });
    }

    public static <T> List<T> listCollection(Class<T> clazz, String whereClause) {
        return executeInTransactionWithResult(session -> {
            String hql = "FROM " + clazz.getName();
            if (whereClause != null && !whereClause.trim().isEmpty()) {
                hql += " WHERE " + whereClause;
            }
            return session.createQuery(hql, clazz).list();
        });
    }

    // Sobrecàrrega per comoditat
    public static <T> List<T> listCollection(Class<T> clazz) {
        return listCollection(clazz, "");
    }

    public static <T> String collectionToString(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (T obj : collection) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }

    // --- Mètodes Natius ---

    public static void queryUpdate(String queryString) {
        executeInTransaction(session -> {
            NativeQuery<?> query = session.createNativeQuery(queryString, Void.class);
            query.executeUpdate();
        });
    }

    public static List<Object[]> queryTable(String queryString) {
        return executeInTransactionWithResult(session -> {
            NativeQuery<Object[]> query = session.createNativeQuery(queryString, Object[].class);
            return query.getResultList();
        });
    }
}