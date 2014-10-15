/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.api;

import java.util.Map;
import java.util.Stack;
import org.apache.commons.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface DialogManager {
    
    SCXMLExecutor getExecutor();
    
    SCXMLExecutor getRootExecutor();
    
    void start(String src, Map params);
    
    void stop();
    
    void pushExecutor(SCXMLExecutor executor);

    void popExecutor();

    boolean isStarted();
    
    Stack<SCXMLExecutor> getStack();
    
    void flush();

    Stack<SCXMLExecutor> popStack();

    void pushStack(Stack<SCXMLExecutor> pushed);
}
