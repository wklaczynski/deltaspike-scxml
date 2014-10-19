/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.el;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import javax.el.FunctionMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.Builtin;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class BuiltinFunctionMapper extends FunctionMapper implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Log log = LogFactory.getLog(BuiltinFunctionMapper.class);

        public BuiltinFunctionMapper() {
            super();
        }

        @Override
        public Method resolveFunction(final String prefix, final String localName) {
            if (localName.equals("In")) {
                Class[] attrs = new Class[]{Set.class, String.class};
                try {
                    return Builtin.class.getMethod("isMember", attrs);
                } catch (SecurityException e) {
                    log.error("resolving isMember(Set, String)", e);
                } catch (NoSuchMethodException e) {
                    log.error("resolving isMember(Set, String)", e);
                }
            } else if (localName.equals("Data")) {
                // rvalue in expressions, coerce to String
                Class[] attrs =
                        new Class[]{Map.class, Object.class, String.class};
                try {
                    return Builtin.class.getMethod("data", attrs);
                } catch (SecurityException e) {
                    log.error("resolving data(Node, String)", e);
                } catch (NoSuchMethodException e) {
                    log.error("resolving data(Node, String)", e);
                }
            } else if (localName.equals("LData")) {
                // lvalue in expressions, retain as Node
                Class[] attrs =
                        new Class[]{Map.class, Object.class, String.class};
                try {
                    return Builtin.class.getMethod("dataNode", attrs);
                } catch (SecurityException e) {
                    log.error("resolving data(Node, String)", e);
                } catch (NoSuchMethodException e) {
                    log.error("resolving data(Node, String)", e);
                }
            }
            return null;
        }
    }