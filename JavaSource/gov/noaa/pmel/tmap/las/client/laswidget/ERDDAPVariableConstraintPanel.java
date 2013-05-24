package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddVariableConstraintEvent;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ERDDAPVariableConstraintPanel extends Composite {

    HorizontalPanel row1 = new HorizontalPanel();
    HorizontalPanel row2 = new HorizontalPanel();
    HorizontalPanel row3 = new HorizontalPanel();
    TextBox lhs = new TextBox();
    TextBox rhs = new TextBox();

    PushButton apply = new PushButton("Apply");
    HTML lessThan = new HTML("&lt;");
    HTML lessThanEqual = new HTML("&le;");

    VariableSerializable variable;



    FlowPanel mainPanel = new FlowPanel();
    FlexTable layout = new FlexTable();
    VariableListBox variablesListBox = new VariableListBox(false);
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();

    public ERDDAPVariableConstraintPanel() {
        variablesListBox.setVisibleItemCount(1);
        variablesListBox.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                int index = variablesListBox.getSelectedIndex();
                variable = variablesListBox.getVariable(index);
                eventBus.fireEventFromSource(new AddVariableConstraintEvent(variable.getDSID(), variable.getID(), "", "gt", variable.getName(), "", "le", false), ERDDAPVariableConstraintPanel.this);
            }

        });

        lhs.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                fireEvent();
            }
            
        });
        rhs.addChangeHandler( new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                fireEvent();
            }
            
        });

        layout.setWidget(0, 0, apply);
       
        
        row1.add(lhs);
        row1.add(lessThan);

        row2.add(variablesListBox);


        row3.add(lessThanEqual);
        row3.add(rhs);

        layout.setWidget(0, 1, row1);
        layout.setWidget(1, 0, row2);
        layout.setWidget(2, 0, row3);
        
        layout.getFlexCellFormatter().setRowSpan(0, 0, 3);
        
        mainPanel.add(layout);

        initWidget(mainPanel);
    }
    public void setVariable(VariableSerializable variable) {
        this.variable = variable;
    }
    public void setVariables(List<VariableSerializable> variables) {
        Vector vs = new Vector();
        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
            VariableSerializable variableSerializable = (VariableSerializable) varIt.next();
            if ( variableSerializable.getAttributes().get("units") == null || !variableSerializable.getAttributes().get("units").equals("text") ) {
                variablesListBox.addItem(variableSerializable);          
            }
        }
    }
    public void setVariables(VariableSerializable[] variables) {
        variablesListBox.clear();
        Vector vs = new Vector();
        for (int i = 0; i < variables.length; i++) {
            if ( variables[i].getAttributes().get("units") == null || !variables[i].getAttributes().get("units").equals("text") ) {
                variablesListBox.addItem(variables[i]);
            }
        }
    }
    public void clearTextField(ConstraintTextAnchor anchor) {
        if ( variable.getID().equals(anchor.getVarid()) && variable.getDSID().equals(anchor.getDsid())) {
            if ( anchor.getOp().equals("gt") ) {
                lhs.setText("");
            } else if ( anchor.getOp().equals("le")) {
                rhs.setText("");
            }
        }
    }
    public void setLhs(String text) {
        lhs.setText(text);
    }
    public void setRhs(String text) {
        rhs.setText(text);
    }
    private void fireEvent() {
        String lhsText = lhs.getText();
        String op1 = "gt";
        String op2 = "le";
        String rhsText = rhs.getText();              
        eventBus.fireEventFromSource(new AddVariableConstraintEvent(variable.getDSID(), variable.getID(), lhsText, op1, variable.getName(), rhsText, op2, true), ERDDAPVariableConstraintPanel.this);
    }
}
