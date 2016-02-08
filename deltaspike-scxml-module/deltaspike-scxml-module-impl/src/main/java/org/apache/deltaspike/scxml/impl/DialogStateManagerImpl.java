/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl;

import java.io.Serializable;
import javax.enterprise.context.ConversationScoped;
import org.apache.deltaspike.scxml.api.DialogStateManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
@ConversationScoped
public class DialogStateManagerImpl implements DialogStateManager, Serializable {
    
    private Object[] state;
    
    @Override
    public void saveState(Object[] state){
        this.state = state;
    }

    @Override
    public Object[] restoreState(){
        return state;
    }
    
}
