/*******************************************************************************
 * Copyright (C) UrFilez, S.P.C. - All Rights Reserved
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited Proprietary and confidential.
 * @author uaftab
 ******************************************************************************/
package com.d3velopers.sms_search.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.util.Assert;

public abstract class GenericHibernateDAO<T, ID extends Serializable> {

    private Logger LOG = LoggerFactory.getLogger(GenericHibernateDAO.class);
    private Class<T> persistentClass;
    private HibernateTemplate hibernateTemplate;
    private SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    public GenericHibernateDAO() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Session getSession() {
        return hibernateTemplate.getSessionFactory().getCurrentSession();
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    public T findById(ID id) {
        T entity = (T) hibernateTemplate.get(this.persistentClass, id);
        return entity;
    }

    public List<T> findAll() {
        return hibernateTemplate.loadAll(this.persistentClass);
    }

    public boolean exists(ID id) {
        T entity = (T) hibernateTemplate.get(this.persistentClass, id);
        return entity != null;
    }

    public List<T> getAllDistinct() {
        Collection result = new LinkedHashSet(findAll());
        return new ArrayList(result);
    }

    public T makePersistent(T entity) {
        hibernateTemplate.saveOrUpdate(entity);
        return entity;
    }

    public void makeTransient(T entity) {
        hibernateTemplate.delete(entity);
    }

    public void flush() {
        getHibernateTemplate().flush();
    }

    public void clear() {
        getHibernateTemplate().clear();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    protected List<T> findByCriteria(final Criterion... criterion) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                final Criteria criteria = session.createCriteria(getPersistentClass());
                for (Criterion c : criterion) {
                    criteria.add(c);
                }
                return criteria.list();
            }
        });
    }

    protected T findUniqueObjectByCriteria(final Criterion... criterion) {
        return (T) getHibernateTemplate().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                final Criteria criteria = session.createCriteria(getPersistentClass());
                for (Criterion c : criterion) {
                    criteria.add(c);
                }
                return criteria.uniqueResult();
            }
        });
    }

    protected T findUniqueObjectByCriteria(final Order order, final Criterion... criterion) {
        return (T) getHibernateTemplate().executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                final Criteria criteria = session.createCriteria(getPersistentClass());
                for (Criterion c : criterion) {
                    criteria.add(c);
                }
                return returnUniqueResult(criteria.addOrder(order).list());
            }
        });
    }

    protected List<T> findByCriteria(final Order order, final int maxRecords, final Criterion... criterion) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                final Criteria criteria = session.createCriteria(getPersistentClass());
                for (Criterion c : criterion) {
                    criteria.add(c);
                }
                if (maxRecords > -1) {
                    return criteria.addOrder(order).setMaxResults(maxRecords).list();
                } else {
                    return criteria.addOrder(order).list();
                }

            }
        });
    }

    protected List<T> findByCriteriaPage(final Order order, final int start, final int maxRecords,
            final Criterion... criterion) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                final Criteria criteria = session.createCriteria(getPersistentClass());
                for (Criterion c : criterion) {
                    criteria.add(c);
                }
                if (start > -1) {
                    criteria.setFirstResult(start);
                }
                if (maxRecords > 0) {
                    return criteria.addOrder(order).setMaxResults(maxRecords).list();
                } else {
                    return criteria.addOrder(order).list();
                }

            }
        });
    }

    public void update(T t) {
        hibernateTemplate.update(t);
    }

    public T save(T t) {
        return (T) hibernateTemplate.save(t);
    }

    public void delete(T t) {
        hibernateTemplate.delete(t);
    }

    public void deleteById(ID id) {
        T obj = findById(id);
        hibernateTemplate.delete(obj);
    }

    @Autowired
    @Required
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    protected List<T> findByNamedQueryAndNamedParam(String queryName, String[] paramNames, Object[] values) {
        return findByNamedQueryAndNamedParam(queryName, paramNames, values, getPersistentClass());
    }

    protected T findUniqueByNamedQuery(String queryName, String[] paramNames, Object[] values) {
        List<T> result = findByNamedQueryAndNamedParam(queryName, paramNames, values);
        return returnUniqueResult(result);
    }

    protected <V> V findUniqueByNamedQuery(String queryName, String[] paramNames, Object[] values,
            Class<V> returnType) {
        List<V> resultList = findByNamedQueryAndNamedParam(queryName, paramNames, values, returnType);
        return returnUniqueResult(resultList);
    }

    protected <V> List<V> findByNamedQueryAndNamedParam(String queryName, String[] paramNames,
            Object[] values, Class<V> returnType) {
        return getHibernateTemplate().findByNamedQueryAndNamedParam(queryName, paramNames, values);
    }

    private <V> V returnUniqueResult(List<V> result) {
        if (result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Query did not return a unique result: {0}", result.size()));
        }
    }

    protected int updateByNamedQuery(final String namedQueryName, final Object[] values) {
        Integer count = getHibernateTemplate().execute(new HibernateCallback<Integer>() {

            @Override
            public Integer doInHibernate(Session session) throws HibernateException, SQLException {
                Query queryObject = session.getNamedQuery(namedQueryName);
                if (getHibernateTemplate().isCacheQueries()) {
                    queryObject.setCacheable(true);
                    if (getHibernateTemplate().getQueryCacheRegion() != null) {
                        queryObject.setCacheRegion(getHibernateTemplate().getQueryCacheRegion());
                    }
                }
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        queryObject.setParameter(i, values[i]);
                    }
                }
                return new Integer(queryObject.executeUpdate());
            }
        });
        return count;
    }

    protected int updateByNamedQueryAndNamedParam(final String namedQueryName, final String[] paramNames,
            final Object[] values) {
        if (paramNames != null && values != null) {
            Assert.isTrue(paramNames.length == values.length);
        } else {
            throw new IllegalArgumentException(
                    "Pass an empty values or paramNames array -these cannot be null");
        }
        Integer count = getHibernateTemplate().execute(new HibernateCallback<Integer>() {

            @Override
            public Integer doInHibernate(Session session) throws HibernateException, SQLException {
                Query queryObject = session.getNamedQuery(namedQueryName);
                if (getHibernateTemplate().isCacheQueries()) {
                    queryObject.setCacheable(true);
                    if (getHibernateTemplate().getQueryCacheRegion() != null) {
                        queryObject.setCacheRegion(getHibernateTemplate().getQueryCacheRegion());
                    }
                }
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                if (values != null) {
                    for (int i = 0; i < paramNames.length; i++) {
                        queryObject.setParameter(paramNames[i], values[i]);
                    }
                }
                return new Integer(queryObject.executeUpdate());
            }
        });
        return count;
    }

    private SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }
}
