/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.d3velopers.sms_search.business.service;

import com.d3velopers.sms_search.util.model.BasicException;

/**
 *
 * @author boy
 */
public interface QueryHandler {
    
    public String executeQuery(String phoneNumber, String query) throws BasicException;
    
}
