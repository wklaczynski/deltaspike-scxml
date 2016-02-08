/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.el;

import com.sun.faces.facelets.util.ReflectionUtil;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.el.FunctionMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DefaultFunctionMapper extends FunctionMapper implements Externalizable {

    private Map functions = null;

    @Override
    public Method resolveFunction(String prefix, String localName) {
        if (this.functions != null) {
            Function f = (Function) this.functions.get(prefix + ":" + localName);
            return f.getMethod();
        }
        return null;
    }

    public void addFunction(String prefix, String localName, Method m) {
        if (this.functions == null) {
            this.functions = new HashMap();
        }
        Function f = new Function(prefix, localName, m);
        synchronized (this) {
            this.functions.put(prefix + ":" + localName, f);
        }
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.functions);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.functions = (Map) in.readObject();
    }
    
    private static class Function implements Externalizable {

        private static final long serialVersionUID = 1L;

        protected transient Method m;

        protected String owner;

        protected String name;

        protected String[] types;

        protected String prefix;

        protected String localName;

        public Function(String prefix, String localName, Method m) {
            if (localName == null) {
                throw new NullPointerException("LocalName cannot be null");
            }
            if (m == null) {
                throw new NullPointerException("Method cannot be null");
            }
            this.prefix = prefix;
            this.localName = localName;
            this.m = m;
        }

        public Function() {
            // for serialization
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF((this.prefix != null) ? this.prefix : "");
            out.writeUTF(this.localName);
            out.writeUTF(this.m.getDeclaringClass().getName());
            out.writeUTF(this.m.getName());
            out.writeObject(ReflectionUtil.toTypeNameArray(this.m.getParameterTypes()));
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

            this.prefix = in.readUTF();
            if ("".equals(this.prefix)) {
                this.prefix = null;
            }
            this.localName = in.readUTF();
            this.owner = in.readUTF();
            this.name = in.readUTF();
            this.types = (String[]) in.readObject();
        }

        public Method getMethod() {
            if (this.m == null) {
                try {
                    Class t = ReflectionUtil.forName(this.owner);
                    Class[] p = ReflectionUtil.toTypeArray(this.types);
                    this.m = t.getMethod(this.name, p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return this.m;
        }

        public boolean matches(String prefix, String localName) {
            if (this.prefix != null) {
                if (prefix == null) {
                    return false;
                }
                if (!this.prefix.equals(prefix)) {
                    return false;
                }
            }
            return this.localName.equals(localName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Function) {
                return this.hashCode() == obj.hashCode();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (this.prefix + this.localName).hashCode();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(32);
            sb.append("Function[");
            if (this.prefix != null) {
                sb.append(this.prefix).append(':');
            }
            sb.append(this.name).append("] ");
            sb.append(this.m);
            return sb.toString();
        }
    }
    
    
}
