package com.akzia.inets.refrigerator.dao;

import com.akzia.inets.refrigerator.model.RefrigeratorShelf;
import com.akzia.inets.refrigerator.model.RefrigeratorState;
import com.akzia.inets.refrigerator.model.Shop;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public class RefrigeratorDao{
	@Autowired
	private SessionFactory sessionFactory;

	private Session getSession(){
		return sessionFactory.getCurrentSession();
	}

	public void saveOrUpdate(Serializable object){
		getSession().saveOrUpdate(object);
	}

	public void delete(Serializable object){
		getSession().delete(object);
	}

	public <X> X getById(Class<X> clazz, long id){
		//noinspection unchecked
		return (X)getSession().get(clazz, id);
	}

	public <X> List<X> getAll(Class<X> clazz){
		//noinspection unchecked
		return getSession().createCriteria(clazz).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllBy(Class<X> clazz, String fieldName, Object fieldValue){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).eq(fieldValue)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllBy(Class<X> clazz, String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName1).eq(fieldValue1)).add(Property.forName(fieldName2).eq(fieldValue2)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllByIn(Class<X> clazz, String fieldName, Collection valueCollection){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).in(valueCollection)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllByIn(Class<X> clazz, String fieldName, Object[] valueCollection){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).in(valueCollection)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> List<X> getAllByAndJoinBy(Class<X> clazz, String fieldName, Object fieldValue, String joinField){
		//noinspection unchecked
		return getSession().createCriteria(clazz).add(Property.forName(fieldName).eq(fieldValue)).setFetchMode(joinField, FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
	}

	public <X> X getFirstBy(Class<X> clazz, String fieldName, Object fieldValue){
		//noinspection unchecked
		return (X)getSession().createCriteria(clazz).add(Property.forName(fieldName).eq(fieldValue)).setMaxResults(1).uniqueResult();
	}

	public <X> X getFirstBy(Class<X> clazz, String fieldName1, Object fieldValue1, String fieldName2, Object fieldValue2){
		//noinspection unchecked
		return (X)getSession().createCriteria(clazz).add(Property.forName(fieldName1).eq(fieldValue1)).add(Property.forName(fieldName2).eq(fieldValue2)).setMaxResults(1).uniqueResult();
	}

	public RefrigeratorState getRefrigeratorStateCurrent(long refrigeratorId, boolean isPlanogram){
		return (RefrigeratorState)getSession().createCriteria(RefrigeratorState.class)
				.add(Restrictions.eq("refrigeratorId", refrigeratorId))
				.add(isPlanogram ? Restrictions.isNull("planogram") : Restrictions.isNotNull("planogram"))
				.addOrder(Order.desc("creationTime")).setMaxResults(1).uniqueResult();
	}

	public List<RefrigeratorState> getRefrigeratorStateByDate(long refrigeratorId, boolean isPlanogram, Date startDate, Date endDate){
		//noinspection unchecked
		return getSession().createCriteria(RefrigeratorState.class)
				.add(Restrictions.eq("refrigeratorId", refrigeratorId))
				.add(isPlanogram ? Restrictions.isNull("planogram") : Restrictions.isNotNull("planogram"))
				.add(Restrictions.between("creationTime", startDate, endDate))
				.list();
	}

	public RefrigeratorState getRefrigeratorStateFirstBefore(long refrigeratorId, boolean isPlanogram, Date endDate){
		//noinspection unchecked
		return (RefrigeratorState)getSession().createCriteria(RefrigeratorState.class)
				.add(Restrictions.eq("refrigeratorId", refrigeratorId))
				.add(isPlanogram ? Restrictions.isNull("planogram") : Restrictions.isNotNull("planogram"))
				.add(Restrictions.lt("creationTime", endDate))
				.addOrder(Order.desc("creationTime")).setMaxResults(1).uniqueResult();
	}

	public Shop getShopByRefrigeratorId(long refrigeratorId){
		return (Shop)getSession().createCriteria(Shop.class, "shop").createAlias("shop.refrigerators", "refrigerator").add(Property.forName("refrigerator.id").eq(refrigeratorId)).uniqueResult();
	}


	//TODO REMOVE
	public List<RefrigeratorShelf> listTest(){
		@SuppressWarnings("unchecked")
		List<RefrigeratorShelf> lines = (List<RefrigeratorShelf>)getSession().createCriteria(RefrigeratorShelf.class).setFetchMode("lines", FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return lines;
	}

	@SuppressWarnings("unchecked")
	public List<RefrigeratorShelf> listTest2(long id){
//		return (List<RefrigeratorShelf>)getSession().createCriteria(RefrigeratorShelf.class).add(Property.forName("lines").eq(id).setFetchMode("lines", FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return (List<RefrigeratorShelf>)getSession().createCriteria(RefrigeratorShelf.class, "shelf").createAlias("shelf.lines", "line").add(Property.forName("line.id").eq(id)).setFetchMode("lines", FetchMode.JOIN).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		//getSession().load(RefrigeratorShelf.class,Id)
	}
}
