/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.el;

import com.sun.faces.facelets.tag.TagLibrary;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import javax.el.FunctionMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class TagsFunctionMapper extends FunctionMapper implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Log log = LogFactory.getLog(TagsFunctionMapper.class);
    private final Map namespaces;
    private final TagLibrary tagLibrary;

    public TagsFunctionMapper(Map namespaces, TagLibrary tagLibrary) {
        super();
        this.namespaces = namespaces;
        this.tagLibrary = tagLibrary;
    }

    @Override
    public Method resolveFunction(final String prefix, final String localName) {
        
        if(namespaces.containsKey(prefix)){
            String ns = (String) namespaces.get(prefix);
            if(tagLibrary.containsFunction(ns, localName)){
                Method result = tagLibrary.createFunction(ns, localName);
                return result;
            }
        }
        
        return null;
    }
}
