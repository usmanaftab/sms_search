/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.d3velopers.sms_search.business.service.impl;

import com.d3velopers.sms_search.business.manager.OnlineQueryManager;
import com.d3velopers.sms_search.business.manager.VerticalManager;
import com.d3velopers.sms_search.business.service.QueryHandler;
import com.d3velopers.sms_search.util.model.BasicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author boy
 */
@Service("queryHandler")
public class QueryHandlerImpl implements QueryHandler {
    
    @Autowired
    private VerticalManager verticalManager;
    @Autowired
    private OnlineQueryManager onlineQueryManager;

    public String executeQuery(String phoneNumber, String query) throws BasicException {
        String result = null;
       if(verticalManager.hasVertical(query)){
           result = verticalManager.executeQuery();
       } else {
           result = onlineQueryManager.executeQuery();
       }
       
       //TODO: save result in db.
       return result;
    }
}
