/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.deltaspike.scxml.impl.faces;

import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogViewDeclarationLanguageFactory extends ViewDeclarationLanguageFactory {

    private final ViewDeclarationLanguageFactory wrapped;

    public DialogViewDeclarationLanguageFactory(ViewDeclarationLanguageFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ViewDeclarationLanguageFactory getWrapped() {
        return wrapped;
    }

    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId) {
        return new SCXMLViewDeclarationLanguage(getWrapped().getViewDeclarationLanguage(viewId));
    }

}
