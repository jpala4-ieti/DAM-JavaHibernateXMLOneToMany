package com.project;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;

public class Manager {

    private static SessionFactory factory;

    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.addResource("Cart.hbm.xml");
            configuration.addResource("Item.hbm.xml");

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) { 
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex); 
        }
    }

    public static void close() {
        if (factory != null) {
            factory.close();
        }
    }
  
    public static Cart addCart(String type) {
        Session session = factory.openSession();
        Transaction tx = null;
        Cart result = null;
        try {
            tx = session.beginTransaction();
            result = new Cart(type);
            session.persist(result);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error adding Cart: " + e.getMessage());
            throw new RuntimeException(e); // CORRECCIÓ: Rellançar excepció
        } finally {
            session.close();
        }
        return result;
    }

    public static Item addItem(String name) {
        Session session = factory.openSession();
        Transaction tx = null;
        Item result = null;
        try {
            tx = session.beginTransaction();
            result = new Item(name);
            session.persist(result);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error adding Item: " + e.getMessage());
            throw new RuntimeException(e); // CORRECCIÓ: Rellançar excepció
        } finally {
            session.close();
        }
        return result;
    }

    public static void updateItem(long itemId, String name) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Item obj = session.get(Item.class, itemId); 
            if (obj != null) {
                obj.setName(name);
                session.merge(obj);
                tx.commit();
            } else {
                System.out.println("Item not found with ID: " + itemId);
            }
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error updating Item: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public static void updateCart(long cartId, String type, Set<Item> items) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Cart cart = session.get(Cart.class, cartId);
            if (cart == null) {
                throw new RuntimeException("Cart not found with id: " + cartId);
            }
            
            cart.setType(type);
            
            // Gestió curosa de la col·lecció per evitar orfes si calgués
            if (cart.getItems() != null) {
                // Copia per evitar ConcurrentModificationException
                for (Item oldItem : new HashSet<>(cart.getItems())) {
                    cart.removeItem(oldItem);
                }
            }
            
            if (items != null) {
                for (Item item : items) {
                    // És important recarregar l'item dins la sessió actual
                    Item managedItem = session.get(Item.class, item.getItemId());
                    if (managedItem != null) {
                        cart.addItem(managedItem);
                    }
                }
            }
            
            session.merge(cart);
            tx.commit();
            
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error updating Cart: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }
        
    public static Cart getCartWithItems(long cartId) {
        Cart cart;
        // Utilitzem try-with-resources si la versió de Hibernate ho permet, sino el bloc clàssic
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            cart = session.get(Cart.class, cartId);
            // Com que hem posat lazy="false" al XML, això ja no és estrictament necessari,
            // però no fa mal per assegurar la inicialització si canviem el XML.
            if (cart != null) {
                cart.getItems().size(); 
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return cart;
    }

    public static <T> T getById(Class<? extends T> clazz, long id) {
        Session session = factory.openSession();
        Transaction tx = null;
        T obj = null;
        try {
            tx = session.beginTransaction();
            obj = clazz.cast(session.get(clazz, id)); 
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace(); 
        } finally {
            session.close();
        }
        return obj;
    }

    public static <T> void delete(Class<? extends T> clazz, Serializable id) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            T obj = clazz.cast(session.get(clazz, id));
            if (obj != null) {
                session.remove(obj);
                tx.commit();
            } else {
                System.out.println("Object not found for deletion.");
            }
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error deleting: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public static <T> Collection<?> listCollection(Class<? extends T> clazz) {
        return listCollection(clazz, "");
    }

    public static <T> Collection<?> listCollection(Class<? extends T> clazz, String where) {
        Session session = factory.openSession();
        Transaction tx = null;
        Collection<?> result = null;
        try {
            tx = session.beginTransaction();
            // CORRECCIÓ: Construcció de query una mica més neta.
            // NOTA: Segueix sent vulnerable a injecció HQL si 'where' ve de l'usuari.
            // Per arreglar-ho del tot caldria passar paràmetres, però això trencaria Main.java.
            String hql = "FROM " + clazz.getName();
            if (where != null && !where.isEmpty()) {
                hql += " WHERE " + where;
            }
            
            result = session.createQuery(hql, clazz).list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Error listing collection: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
        return result;
    }

    // CORRECCIÓ: Ús de StringBuilder per evitar concatenació ineficient en bucles
    public static <T> String collectionToString(Class<? extends T> clazz, Collection<?> collection) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            T cObj = clazz.cast(obj);
            sb.append("\n").append(cObj.toString());
        }
        if (sb.length() > 0 && sb.charAt(0) == '\n') {
            return sb.substring(1);
        }
        return sb.toString();
    }

    public static void queryUpdate(String queryString) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            NativeQuery<?> query = session.createNativeQuery(queryString, Void.class);
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace(); 
        } finally {
            session.close();
        }
    }

    public static List<Object[]> queryTable(String queryString) {
        Session session = factory.openSession();
        Transaction tx = null;
        List<Object[]> result = null;
        try {
            tx = session.beginTransaction();
            NativeQuery<Object[]> query = session.createNativeQuery(queryString, Object[].class);
            result = query.getResultList();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    // CORRECCIÓ: Ús de StringBuilder
    public static String tableToString(List<Object[]> rows) {
        StringBuilder sb = new StringBuilder();
        for (Object[] row : rows) {
            for (Object cell : row) {
                sb.append(cell.toString()).append(", ");
            }
            // Eliminar la coma final de la fila
            if (sb.length() >= 2 && ", ".equals(sb.substring(sb.length() - 2))) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("\n");
        }
        // Eliminar l'últim salt de línia
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}