/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.d3velopers.sms_search.dao;

import com.d3velopers.sms_search.dao.pojos.Query;
import java.io.Serializable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author boy
 */
@Repository("queryDao")
public class QueryDao extends GenericHibernateDAO<Query, Serializable> {
    
}
