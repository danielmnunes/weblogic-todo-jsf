package com.weblogic.todo.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@ApplicationScoped
public class FacesMessages {

    public void info(String summary) {
        add(FacesMessage.SEVERITY_INFO, summary);
    }

    public void error(String summary) {
        add(FacesMessage.SEVERITY_ERROR, summary);
    }

    private void add(FacesMessage.Severity severity, String summary) {
        var context = currentContext();
        if (context == null) {
            return;
        }
        context.addMessage(null, new FacesMessage(severity, summary, summary));
    }

    FacesContext currentContext() {
        try {
            return FacesContext.getCurrentInstance();
        } catch (RuntimeException | LinkageError ex) {
            return null;
        }
    }
}
