package com.weblogic.todo.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FacesMessagesTest {

    @Mock
    private FacesContext facesContext;

    @Test
    void infoAndErrorAddMessages() {
        var messages = new FacesMessages() {
            @Override
            FacesContext currentContext() {
                return facesContext;
            }
        };

        messages.info("ok");
        messages.error("fail");

        var captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext, org.mockito.Mockito.times(2)).addMessage(isNull(), captor.capture());
        assertEquals(FacesMessage.SEVERITY_INFO, captor.getAllValues().get(0).getSeverity());
        assertEquals("ok", captor.getAllValues().get(0).getSummary());
        assertEquals(FacesMessage.SEVERITY_ERROR, captor.getAllValues().get(1).getSeverity());
        assertEquals("fail", captor.getAllValues().get(1).getDetail());
    }

    @Test
    void ignoresMissingFacesContext() {
        var messages = new FacesMessages() {
            @Override
            FacesContext currentContext() {
                return null;
            }
        };

        messages.info("ignored");
        verify(facesContext, never()).addMessage(isNull(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void currentContextReadsStaticFacesContextWhenAbsent() {
        assertNull(new FacesMessages().currentContext());
    }
}
