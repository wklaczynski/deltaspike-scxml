/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface DialogStateManager {
    
    void saveState(Object[] state);
    
    Object[] restoreState();
    
}
