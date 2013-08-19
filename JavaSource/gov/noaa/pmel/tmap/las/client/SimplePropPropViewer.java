package gov.noaa.pmel.tmap.las.client;

import gov.noaa.pmel.tmap.las.client.event.CancelEvent;
import gov.noaa.pmel.tmap.las.client.event.StringValueChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.VariableConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.VariableSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.WidgetSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.laswidget.AlertButton;
import gov.noaa.pmel.tmap.las.client.laswidget.AxisWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.CancelButton;
import gov.noaa.pmel.tmap.las.client.laswidget.Constants;
import gov.noaa.pmel.tmap.las.client.laswidget.ConstraintLabel;
import gov.noaa.pmel.tmap.las.client.laswidget.ConstraintTextDisplay;
import gov.noaa.pmel.tmap.las.client.laswidget.ConstraintWidgetGroup;
import gov.noaa.pmel.tmap.las.client.laswidget.CruiseIconWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.DateTimeWidget;
import gov.noaa.pmel.tmap.las.client.laswidget.LASAnnotationsPanel;
import gov.noaa.pmel.tmap.las.client.laswidget.LASRequest;
import gov.noaa.pmel.tmap.las.client.laswidget.TextConstraintAnchor;
import gov.noaa.pmel.tmap.las.client.laswidget.UserListBox;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableConstraintAnchor;
import gov.noaa.pmel.tmap.las.client.laswidget.VariableConstraintWidget;
import gov.noaa.pmel.tmap.las.client.map.MapSelectionChangeListener;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConfigSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.DatasetSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraintGroup;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.ui.IESafeImage;
import gov.noaa.pmel.tmap.las.client.util.URLUtil;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

/**
 * A property property viewer that keeps the initial constraints constant and
 * allows new constraints to be added.
 * 
 * @author rhs
 * 
 */
public class SimplePropPropViewer implements EntryPoint {

    private static String YSELECTOR = "y-variable";
    private static String XSELECTOR = "x-variable";
    private static String COLORSELECTOR = "color-selector";
    Logger logger = Logger.getLogger(Correlation.class.getName());
    private static final AppConstants CONSTANTS = GWT.create(AppConstants.class);
    NumberFormat dFormat = NumberFormat.getFormat("########.##");
    CruiseIconWidget cruiseIcons = new CruiseIconWidget();
    FlexTable timePanel = new FlexTable();
    AxisWidget zAxisWidget = new AxisWidget();
    VerticalPanel spaceTimeConstraints = new VerticalPanel();
    HorizontalPanel spaceTimeTopRow = new HorizontalPanel();
    Map<String, VariableSerializable> xFilteredDatasetVariables = new HashMap<String, VariableSerializable>();
    Map<String, VariableSerializable> xAllDatasetVariables = new HashMap<String, VariableSerializable>();
    ToggleButton annotationsButton;
    LASAnnotationsPanel lasAnnotationsPanel = new LASAnnotationsPanel();
    HorizontalPanel buttonPanel = new HorizontalPanel();
    HorizontalPanel topRow = new HorizontalPanel();
    HorizontalPanel output = new HorizontalPanel();
    PopupPanel spin;
    HTML spinImage;
    HorizontalPanel coloredBy = new HorizontalPanel();
    ConstraintTextDisplay fixedConstraintPanel = new ConstraintTextDisplay();
    ScrollPanel fixedScroller = new ScrollPanel();
    Label selection = new Label("Current selection:");
    Label horizontalLabel = new Label("Horizontal: ");
    Label verticalLabel = new Label("Vertical: ");
    FlexTable controlPanel = new FlexTable();
    VerticalPanel topPanel = new VerticalPanel();
    FlexTable outputPanel = new FlexTable();
    UserListBox xVariables = new UserListBox(XSELECTOR, true);
    UserListBox yVariables = new UserListBox(YSELECTOR, true);
    UserListBox colorVariables = new UserListBox(COLORSELECTOR, true);
    AlertButton update = new AlertButton("Update Plot", Constants.UPDATE_NEEDED);
    PushButton print = new PushButton("Print");
    CheckBox colorCheckBox = new CheckBox();
    Label warnText;
    PopupPanel warning = new PopupPanel(true);
    PushButton ok;
    PushButton cancel;
    boolean youveBeenWarned = false;
    // The current intermediate file
    String netcdf = null;
    String dsid;
    String varid;
    String currentURL;
    String operationID;
    // Drawing start position
    int startx = -1;
    int starty = -1;
    int endx;
    int endy;
    boolean draw = false;
    IESafeImage plotImage;
    Context2d frontCanvasContext;
    Canvas frontCanvas;
    CssColor randomColor;
    // Have a cancel button ready to go for this panel.
    CancelButton cancelButton;
    final static String operationType = "v7";
    protected int x_image_size;
    protected int y_image_size;
    protected int x_plot_size;
    protected int y_plot_size;
    protected int x_offset_from_left;
    protected int y_offset_from_bottom;
    protected int x_offset_from_right;
    protected int y_offset_from_top;
    protected double x_axis_lower_left;
    protected double y_axis_lower_left;
    protected double x_axis_upper_right;
    protected double y_axis_upper_right;
    protected double world_startx;
    protected double world_starty;
    protected double world_endx;
    protected double world_endy;
    protected double x_per_pixel;
    protected double y_per_pixel;
    protected String printURL;
    protected String xlo;
    protected String xhi;
    protected String ylo;
    protected String yhi;
    protected String tlo;
    protected String thi;
    protected String zlo;
    protected String zhi;

    // There are 3 states we want to track.

    // The values for the first plot. This is used to determine if we need to
    // warn the user about fetching new data.
    LASRequest initialState;

    // The state immediately previous to a widget change that might cause the
    // prompt about new data.
    // If the user cancels the change, revert to this state.
    LASRequest undoState;

    // The current request.
    LASRequest lasRequest;

    EventBus eventBus;

    // Some information to control the size of the image as the browser window
    // changes size.
    int topAndBottomPadding = 190;

    int pwidth;

    double image_h = 722.;
    double image_w = 838.;
    int image_w_min = 400;

    int fixedZoom;
    boolean autoZoom = true;;

    double globalMin = 99999999.;
    double globalMax = -99999999.;

    double imageScaleRatio = 1.0; // Keep track of the factor by which the image
    
    ConstraintWidgetGroup constraintWidgetGroup = new ConstraintWidgetGroup();
    
    String initialHistory;
    boolean hasInitialHistory = false;
    @Override
    public void onModuleLoad() {
        logger.setLevel(Level.ALL);

        colorVariables.setColorBy(true);
        ClientFactory cf = GWT.create(ClientFactory.class);
        eventBus = cf.getEventBus();

        eventBus.addHandler(WidgetSelectionChangeEvent.TYPE, new WidgetSelectionChangeEvent.Handler() {
            @Override
            public void onAxisSelectionChange(WidgetSelectionChangeEvent event) {
                Object source = event.getSource();
                if (source instanceof DateTimeWidget) {
                    String initiallo = initialState.getRangeLo("t", 0);
                    String initialhi = initialState.getRangeHi("t", 0);
                    
                }
            }
        });

        // Listen for StringValueChangeEvents from LASAnnotationsPanel(s)
        eventBus.addHandler(StringValueChangeEvent.TYPE, new StringValueChangeEvent.Handler() {
            @Override
            public void onValueChange(StringValueChangeEvent event) {
                Object source = event.getSource();
                if ((source != null) && (source instanceof LASAnnotationsPanel)) {
                    String open = event.getValue();
                    boolean isOpen = Boolean.parseBoolean(open);
                    if (!isOpen) {
                        LASAnnotationsPanel sourceLASAnnotationsPanel = (LASAnnotationsPanel) source;
                        if ((sourceLASAnnotationsPanel != null) && (sourceLASAnnotationsPanel.equals(lasAnnotationsPanel)))
                            synchAnnotationsMode();
                    }
                }
            }
        });

        int rndRedColor = 190;
        int rndGreenColor = 40;
        int rndBlueColor = 40;
        double rndAlpha = 0.25;
        randomColor = CssColor.make("rgba(" + rndRedColor + ", " + rndGreenColor + "," + rndBlueColor + ", " + rndAlpha + ")");

        cancelButton = new CancelButton("Correlation");
        eventBus.addHandler(CancelEvent.TYPE, cancelRequestHandler);
       
        String spinImageURL = URLUtil.getImageURL() + "/mozilla_blu.gif";

        annotationsButton = new ToggleButton("Annotations", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                showAnnotations(annotationsButton.isDown());
            }

        });
        annotationsButton.addStyleDependentName("SMALLER");
        showAnnotations(true);
        annotationsButton.setTitle("Click to hide the annotations of the plot.");
        spinImage = new HTML("<img src=\"" + spinImageURL + "\" alt=\"Spinner\"/>");
        spin = new PopupPanel();
        spin.add(spinImage);
        update.addStyleDependentName("SMALLER");
        update.setWidth("80px");
        update.ensureDebugId("update");

        // Never show the add button...
        xVariables.setMinItemsForAddButtonToBeVisible(10000);
        yVariables.setMinItemsForAddButtonToBeVisible(10000);
        colorVariables.setMinItemsForAddButtonToBeVisible(10000);

        print.addStyleDependentName("SMALLER");
        print.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                printerFriendly();
            }

        });
        print.setEnabled(false);
        buttonPanel.add(update);
        buttonPanel.add(print);
        topRow.add(new HTML("<b>&nbsp;&nbsp;Data Selection: </b>"));
        controlPanel.setWidget(0, 0, topRow);
        controlPanel.getFlexCellFormatter().setColSpan(0, 0, 5);
        controlPanel.setWidget(1, 0, yVariables);
        controlPanel.setWidget(1, 1, new Label(" as a function of "));
        controlPanel.setWidget(1, 2, xVariables);

        colorCheckBox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if (colorCheckBox.getValue()) {
                    colorVariables.setEnabled(true);
                } else {
                    colorVariables.setEnabled(false);
                }
                setVariables();
            }

        });
        
        coloredBy.add(new Label("Colored By "));
        coloredBy.add(colorCheckBox);
        controlPanel.setWidget(2, 1, coloredBy);
        controlPanel.setWidget(2, 2, colorVariables);
        topPanel.add(controlPanel);
        colorVariables.setEnabled(false);
        update.addClickHandler(updateClick);
        String catid = Util.getParameterString("catid");
        String xml = Util.getParameterString("xml");
        if (xml != null && !xml.equals("")) {
            xml = decode(xml);
            lasRequest = new LASRequest(xml);
            dsid = lasRequest.getDataset(0);
            varid = lasRequest.getVariable(0);
            xlo = lasRequest.getRangeLo("x", 0);
            xhi = lasRequest.getRangeHi("x", 0);
            ylo = lasRequest.getRangeLo("y", 0);
            yhi = lasRequest.getRangeHi("y", 0);
            tlo = lasRequest.getRangeLo("t", 0);
            thi = lasRequest.getRangeHi("t", 0);

            // No need to wait, these can be set now...
            if (xlo != null && xhi != null && ylo != null && yhi != null) {
                
                setFixedXY(xlo, xhi, ylo, yhi);
                
                
            }
            setFixedT(tlo, thi);
            
            Util.getRPCService().getConfig(null, catid, dsid, varid, datasetCallback);
        } else {
            Window.alert("This app must be launched from the main interface.");
        }
        initialHistory = getAnchor();
        if ( initialHistory != null && !initialHistory.equals("") ) {
            hasInitialHistory = true;
            initialHistory = decode(initialHistory);
        } else {
            hasInitialHistory = false;
            initialHistory = "";
        }
        eventBus.addHandler(VariableSelectionChangeEvent.TYPE, new VariableSelectionChangeEvent.Handler() {

            @Override
            public void onVariableChange(VariableSelectionChangeEvent event) {
                Object source = event.getSource();
                if (source instanceof UserListBox) {
                    UserListBox u = (UserListBox) source;
                    String id = u.getName();
                    if (id != null && (id.equals(YSELECTOR) || id.equals(XSELECTOR) || id.equals(COLORSELECTOR)) ) {
                        setVariables();    
                    }
                }
            }
        });
        eventBus.addHandler(VariableConstraintEvent.TYPE, new VariableConstraintEvent.Handler() {

            @Override
            public void onChange(VariableConstraintEvent event) {
                String variable = event.getVariable();
                String op1 = event.getOp1();
                String op2 = event.getOp2();
                String lhs = event.getLhs();
                String rhs = event.getRhs();
                String varid = event.getVarid();
                String dsid = event.getDsid();
                boolean apply = event.isApply();
                VariableConstraintAnchor anchor1 = new VariableConstraintAnchor(Constants.VARIABLE_CONSTRAINT, dsid, varid, variable, lhs, variable, lhs, op1);
                VariableConstraintAnchor anchor2 = new VariableConstraintAnchor(Constants.VARIABLE_CONSTRAINT, dsid, varid, variable, rhs, variable, rhs, op2);
                VariableConstraintAnchor a = (VariableConstraintAnchor) constraintWidgetGroup.findMatchingAnchor(anchor1);
                VariableConstraintAnchor b = (VariableConstraintAnchor) constraintWidgetGroup.findMatchingAnchor(anchor2);
                VariableConstraintAnchor afixed = (VariableConstraintAnchor) fixedConstraintPanel.findMatchingAnchor(anchor1);
                VariableConstraintAnchor bfixed = (VariableConstraintAnchor) fixedConstraintPanel.findMatchingAnchor(anchor2);
                if ( afixed != null && !anchor1.getValue().equals("") && !a.getValue().equals("")) {
                    double v1 = Double.valueOf(anchor1.getValue());
                    double a1 = Double.valueOf(afixed.getValue());
                    if ( v1 < a1 ) {
                        a.setValue(afixed.getValue());
                        constraintWidgetGroup.setLhs(afixed.getValue());
                    }
                }
                if ( bfixed != null && !anchor2.getValue().equals("") && !b.getValue().equals("") ) {
                    double v2 = Double.valueOf(anchor2.getValue());
                    double b2 = Double.valueOf(bfixed.getValue());
                    if ( v2 > b2 ) {
                        b.setValue(bfixed.getValue());
                        constraintWidgetGroup.setRhs(bfixed.getValue());
                    }
                }

            }
        });
        outputPanel.setWidget(0, 0, lasAnnotationsPanel);
        output.add(annotationsButton);
        output.add(outputPanel);
        spaceTimeTopRow.add(new HTML("<b>Fixed Constraints from the main interface:</b>&nbsp;"));
        spaceTimeConstraints.add(spaceTimeTopRow);
        // TODO style and label
        fixedConstraintPanel.setSize(Constants.CONTROLS_WIDTH-25+"px", "125px");
        fixedScroller.addStyleName(Constants.ALLBORDER);
        fixedScroller.add(fixedConstraintPanel);
        fixedScroller.setSize(Constants.CONTROLS_WIDTH-10+"px", "100px");
        spaceTimeConstraints.add(fixedScroller);
        spaceTimeConstraints.add(timePanel);
        spaceTimeConstraints.add(zAxisWidget);
        RootPanel.get("icons").add(cruiseIcons);
        RootPanel.get("button_panel").add(buttonPanel);
        RootPanel.get("data_selection").add(topPanel);
        RootPanel.get("data_constraints").add(constraintWidgetGroup);
        RootPanel.get("correlation").add(output);
        RootPanel.get("space_time_constraints").add(spaceTimeConstraints);
        lasAnnotationsPanel.setPopupLeft(outputPanel.getAbsoluteLeft());
        lasAnnotationsPanel.setPopupTop(outputPanel.getAbsoluteTop());

        lasAnnotationsPanel.setError("Click \"Update plot\" to refresh the plot.&nbsp;");
        ok = new PushButton("Ok");
        ok.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                warning.hide();
                youveBeenWarned = true;
            }

        });
        ok.setWidth("80px");
        cancel = new PushButton("Cancel");
        cancel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                popHistory(undoState.toString());
                warning.hide();
            }

        });
        cancel.setWidth("80px");
        warnText = new Label("This operation will require new data to be extracted from the database.");
        Label go = new Label("  It may take a while.  Do you want to continue?");
        FlexTable layout = new FlexTable();
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);
        layout.getFlexCellFormatter().setColSpan(1, 0, 2);
        layout.setWidget(0, 0, warnText);
        layout.setWidget(1, 0, go);
        layout.setWidget(2, 0, ok);
        layout.setWidget(2, 1, cancel);
        warning.add(layout);
        warning.setPopupPosition(update.getAbsoluteLeft(), update.getAbsoluteTop());
        History.addValueChangeHandler(historyHandler);

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                resize(event.getWidth(), event.getHeight());
            }
        });
    }

   

   

    /**
     * The LASAnnotationsPanel closed, so make sure the
     * {@link annotationsButton} down state is set appropriately.
     */
    protected void synchAnnotationsMode() {
        showAnnotations(lasAnnotationsPanel.isVisible());
    }
    
    ClickHandler updateClick = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            updatePlot(true);
        }

    };

    private void printerFriendly() {
        StringBuilder urlfrag = new StringBuilder(URLUtil.getBaseURL() + "getAnnotations.do?template=image_w_annotations.vm&");
        urlfrag.append(printURL);
        Window.open(urlfrag.toString(), "_blank", null);
    }

    private void updatePlot(boolean addHistory) {
        // TODO Before submitting...

        boolean contained = true;
        setConstraints();
        
        //TODO this should wait until the result comes back and is good and should be an event, right?
        update.removeStyleDependentName("APPLY-NEEDED");

        lasAnnotationsPanel.setError("Fetching plot annotations...");

        spin.setPopupPosition(outputPanel.getAbsoluteLeft(), outputPanel.getAbsoluteTop());
        spin.show();

       
        

        String zlo = null;
        String zhi = null;
        if (zAxisWidget.isVisible()) {
            zlo = zAxisWidget.getLo();
            zhi = zAxisWidget.getHi();
        }
        String vix = xVariables.getUserObject(xVariables.getSelectedIndex()).getID();
        GridSerializable grid = xVariables.getUserObject(xVariables.getSelectedIndex()).getGrid();
        if (grid.hasT()) {

            if (tlo != null && thi != null) {
                lasRequest.setRange("t", tlo, thi, 0);
            }
            
        }
        if (xlo != null && xhi != null) {
            lasRequest.setRange("x", xlo, xhi, 0);
        } else if (xhi != null) {
            xlo = xhi;
            lasRequest.setRange("x", xhi, xhi, 0);
        }

        if (ylo != null && yhi != null) {
            lasRequest.setRange("y", ylo, yhi, 0);
        } else if (yhi != null) {
            ylo = yhi;
            lasRequest.setRange("y", yhi, yhi, 0);
        }

        // Check if the current selection bounding box is contained in by
        // original bounding box.

        
        if (grid.hasZ()) {

            if (zlo != null && zhi != null) {
                lasRequest.setRange("z", zlo, zhi, 0);
            } else if (zhi != null) {
                lasRequest.setRange("z", zhi, zhi, 0);
            }
            contained = contained && zAxisWidget.isContainedBy(initialState.getRangeLo("z", 0), initialState.getRangeHi("z", 0));
        }

        lasRequest.setProperty("product_server", "ui_timeout", "20");
        lasRequest.setProperty("las", "output_type", "xml");
        String grid_type = xVariables.getUserObject(0).getAttributes().get("grid_type");
        if (netcdf != null && contained && grid_type.equals("trajectory")) {
            operationID = "Trajectgory_correlation"; // No data base access;
            // plot only from
            // existing
            // netCDF file.
            String v0 = lasRequest.getVariable(0);
            String v1 = lasRequest.getVariable(1);
            String v2 = lasRequest.getVariable(2);
            if (v0 != null) {
                lasRequest.setProperty("data_0", "url", netcdf);
            }
            if (v1 != null) {
                lasRequest.setProperty("data_1", "url", netcdf);
            }
            if (v2 != null) {
                lasRequest.setProperty("data_2", "url", netcdf);
            }
            if (!cruiseIcons.getIDs().equals(""))
                lasRequest.setProperty("ferret", "cruise_list", cruiseIcons.getIDs());
        } else if ((!contained || netcdf == null) && grid_type.equals("trajectory")) {
            // This should only occur when the app loads for the first time...
            
            operationID = "Trajectory_correlation_plot";
            // We need to make the initial netCDF file with *all* the variables in each row.

            // Grab the existing variable
            String v0 = lasRequest.getVariable(0);
            lasRequest.removeVariables();
            // Find the first non-sub-set variable that is not already included

            String v1 = null;
            String vds = null;
            for (Iterator allIt = xAllDatasetVariables.keySet().iterator(); allIt.hasNext();) {
                String key = (String) allIt.next();
                VariableSerializable v = xAllDatasetVariables.get(key);
                if ( !v.getID().equals(v0) && v.getAttributes().get("subset_variable") == null ) {
                    v1 = v.getID();
                    vds = v.getDSID();
                }
            }
            if ( vds != null ) {
                lasRequest.addVariable(vds, v0, 0);
                if ( v1 != null ) {
                    lasRequest.addVariable(vds, v1, 0);
                    for (Iterator allIt = xAllDatasetVariables.keySet().iterator(); allIt.hasNext();) {
                        String key = (String) allIt.next();
                        VariableSerializable v = xAllDatasetVariables.get(key);
                        String vid = v.getID();
                        String did = v.getDSID();
                        if ( !vid.equals(v1) && !vid.equals(v0) ) {
                            lasRequest.addVariable(v.getDSID(), v.getID(), 0);
                        }
                    }
                    xVariables.setVariable(xAllDatasetVariables.get(v0));
                    yVariables.setVariable(xAllDatasetVariables.get(v1));
                }
            }
        } else {
            operationID = "prop_prop_plot";
        }
        lasRequest.setOperation(operationID, operationType);
        lasRequest.setProperty("ferret", "annotations", "file");

        frontCanvas = Canvas.createIfSupported();
        if (frontCanvas != null) {
            frontCanvasContext = frontCanvas.getContext2d();
        } else {
            Window.alert("You are accessing this site with an older, no longer supported browser. "
                    + "Some or all features of this site will not work correctly using your browser. " + "Recommended browsers include these or higher versions of these: "
                    + "IE 9.0   FF 17.0    Chorme 23.0    Safari 5.1");
        }

        String url = Util.getProductServer() + "?xml=" + URL.encode(lasRequest.toString());

        currentURL = url;

        if (addHistory) {
            pushHistory(lasRequest.toString());
        }

        RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
        try {
            sendRequest.sendRequest(null, lasRequestCallback);
        } catch (RequestException e) {
            HTML error = new HTML(e.toString());
            outputPanel.setWidget(1, 0, error);
        }

    }

    RequestCallback lasRequestCallback = new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {

            spin.hide();
            Window.alert("Product request failed.");

        }

        @Override
        public void onResponseReceived(Request request, Response response) {
            // If the submitted operation used the compound operation to
            // pull a new data set, we need a new initial state
            String plot_id = lasRequest.getOperation();
            if (plot_id.equals("Trajectory_correlation_plot")) {
                initialState = new LASRequest(lasRequest.toString());
            }
            
            
            
            // Need to warn again...
            youveBeenWarned = false;
            
            print.setEnabled(true);
            String doc = response.getText();
            String imageurl = "";
            String annourl = "";
            // Look at the doc. If it's not obviously XML, treat it as HTML.
            if (!doc.substring(0, 100).contains("<?xml")) {
                if ( initialHistory == null || initialHistory.equals("") ) { 
                    spin.hide();
                }
                VerticalPanel p = new VerticalPanel();
                ScrollPanel sp = new ScrollPanel();
                HTML result = new HTML(doc);
                p.add(result);
                PushButton again = new PushButton("Try Again");
                again.setWidth("75px");
                again.addStyleDependentName("SMALLER");
                again.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent click) {
                        updatePlot(false);
                    }
                });
                p.add(again);
                sp.add(p);
                sp.setSize((int) image_w + "px", (int) image_h + "px");

                outputPanel.setWidget(1, 0, sp);
            } else {
                doc = doc.replaceAll("\n", "").trim();
                Document responseXML = XMLParser.parse(doc);
                NodeList results = responseXML.getElementsByTagName("result");

                for (int n = 0; n < results.getLength(); n++) {
                    if (results.item(n) instanceof Element) {
                        Element result = (Element) results.item(n);
                        if (result.getAttribute("type").equals("image")) {
                            imageurl = result.getAttribute("url");
                        } else if (result.getAttribute("type").equals("batch")) {
                            String elapsed_time = result.getAttribute("elapsed_time");
                            cancelButton.setTime(Integer.valueOf(elapsed_time));
                            cancelButton.setSize(image_w * imageScaleRatio + "px", image_h * imageScaleRatio + "px");
                            outputPanel.setWidget(1, 0, cancelButton);
                            lasRequest.setProperty("product_server", "ui_timeout", "3");
                            String url = Util.getProductServer() + "?xml=" + URL.encode(lasRequest.toString());
                            if (currentURL.contains("cancel"))
                                url = url + "&cancel=true";
                            RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, url);
                            try {
                                sendRequest.setHeader("Pragma", "no-cache");
                                sendRequest.setHeader("cache-directive", "no-cache");
                                // These are needed or IE will cache and make
                                // infinite requests that always return 304
                                sendRequest.setHeader("If-Modified-Since", new Date().toString());
                                sendRequest.setHeader("max-age", "0");
                                logger.info("Pragma:" + sendRequest.getHeader("Pragma"));
                                logger.info("cache-directive:" + sendRequest.getHeader("cache-directive"));
                                logger.info("max-age:" + sendRequest.getHeader("max-age"));
                                logger.info("If-Modified-Since:" + sendRequest.getHeader("If-Modified-Since"));
                                logger.info("calling sendRequest with url:" + url);
                                sendRequest.sendRequest(null, lasRequestCallback);
                            } catch (RequestException e) {
                                HTML error = new HTML(e.toString());
                                error.setSize(image_w * imageScaleRatio + "px", image_h * imageScaleRatio + "px");
                                outputPanel.setWidget(1, 0, error);
                            }
                        } else if (result.getAttribute("type").equals("error")) {
                            if (result.getAttribute("ID").equals("las_message")) {
                                Node text = result.getFirstChild();
                                if (text instanceof Text) {
                                    Text t = (Text) text;
                                    HTML error = new HTML(t.getData().toString().trim());
                                    outputPanel.setWidget(1, 0, error);
                                }
                            }
                        } else if (result.getAttribute("type").equals("annotations")) {
                            annourl = result.getAttribute("url");
                            lasAnnotationsPanel.setAnnotationsHTMLURL(Util.getAnnotationService(annourl));
                        } else if (result.getAttribute("type").equals("icon_webrowset")) {
                            String iconurl = result.getAttribute("url");
                            cruiseIcons.init(iconurl);
                        } else if (result.getAttribute("type").equals("netCDF")) {
                            netcdf = result.getAttribute("file");

                        } else if (result.getAttribute("type").equals("map_scale")) {
                            NodeList map_scale = result.getElementsByTagName("map_scale");
                            for (int m = 0; m < map_scale.getLength(); m++) {
                                if (map_scale.item(m) instanceof Element) {
                                    Element map = (Element) map_scale.item(m);
                                    NodeList children = map.getChildNodes();
                                    for (int l = 0; l < children.getLength(); l++) {
                                        if (children.item(l) instanceof Element) {
                                            Element child = (Element) children.item(l);
                                            if (child.getNodeName().equals("x_image_size")) {
                                                x_image_size = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("y_image_size")) {
                                                y_image_size = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("x_plot_size")) {
                                                x_plot_size = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("y_plot_size")) {
                                                y_plot_size = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("x_offset_from_left")) {
                                                x_offset_from_left = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("y_offset_from_bottom")) {
                                                y_offset_from_bottom = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("x_offset_from_right")) {
                                                x_offset_from_right = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("y_offset_from_top")) {
                                                y_offset_from_top = getNumber(child.getFirstChild());
                                            } else if (child.getNodeName().equals("x_axis_lower_left")) {
                                                x_axis_lower_left = getDouble(child.getFirstChild());
                                            } else if (child.getNodeName().equals("y_axis_lower_left")) {
                                                y_axis_lower_left = getDouble(child.getFirstChild());
                                            } else if (child.getNodeName().equals("x_axis_upper_right")) {
                                                x_axis_upper_right = getDouble(child.getFirstChild());
                                            } else if (child.getNodeName().equals("y_axis_upper_right")) {
                                                y_axis_upper_right = getDouble(child.getFirstChild());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!imageurl.equals("")) {
                    plotImage = new IESafeImage(imageurl);
                    x_per_pixel = (x_axis_upper_right - x_axis_lower_left) / Double.valueOf(x_plot_size);
                    y_per_pixel = (y_axis_upper_right - y_axis_lower_left) / Double.valueOf(y_plot_size);
                    // If you are not going to pop the history, hide the spinner.
                    if ( initialHistory == null || initialHistory.equals("") ) { 
                      spin.hide();
                    }
                    if (frontCanvas != null) {
                        outputPanel.setWidget(2, 0, plotImage);
                        plotImage.setVisible(false);
                        
                        plotImage.addLoadHandler(new LoadHandler() {

                            @Override
                            public void onLoad(LoadEvent event) {
                                logger.info("image onLoad called");
                                int width = plotImage.getWidth();
                                int height = plotImage.getHeight();
                                // Set global maximums
                                image_w = width;
                                logger.info("image_w:" + image_w);
                                image_h = height;
                                logger.info("image_h:" + image_h);
                                String w = width - 18 + "px";
                                lasAnnotationsPanel.setPopupWidth(w);
                                lasAnnotationsPanel.setPopupLeft(outputPanel.getAbsoluteLeft());
                                lasAnnotationsPanel.setPopupTop(outputPanel.getAbsoluteTop());
                                frontCanvas.setCoordinateSpaceHeight(height);
                                frontCanvas.setCoordinateSpaceWidth(width);
                                frontCanvasContext.drawImage(ImageElement.as(plotImage.getElement()), 0, 0);
                                frontCanvas.addMouseDownHandler(new MouseDownHandler() {

                                    @Override
                                    public void onMouseDown(MouseDownEvent event) {
                                        logger.setLevel(Level.ALL);

                                        startx = event.getX();
                                        starty = event.getY();
                                        logger.info("(startx, starty):(" + startx + ", " + starty + ")");
                                        if (startx > x_offset_from_left && starty > y_offset_from_top && startx < x_offset_from_left + x_plot_size
                                                && starty < y_offset_from_top + y_plot_size) {

                                            draw = true;
                                            // frontCanvasContext.drawImage(ImageElement.as(image.getElement()),
                                            // 0, 0);
                                            drawToScreenScaled(imageScaleRatio);
                                            double scaled_x_per_pixel = x_per_pixel / imageScaleRatio;
                                            double scaled_y_per_pixel = y_per_pixel / imageScaleRatio;
                                            world_startx = x_axis_lower_left + (startx - x_offset_from_left * imageScaleRatio) * scaled_x_per_pixel;
                                            world_starty = y_axis_lower_left + ((y_image_size * imageScaleRatio - starty) - y_offset_from_bottom * imageScaleRatio)
                                                    * scaled_y_per_pixel;

                                            world_endx = world_startx;
                                            world_endy = world_starty;
                                            logger.info("(world_startx, world_starty):(" + world_startx + ", " + world_starty + ")");
                                            logger.info("(world_endx, world_endy):(" + world_endx + ", " + world_endy + ")");

                                            setTextValues();
                                       

                                        }
                                        logger.setLevel(Level.ALL);
                                    }
                                });
                                frontCanvas.addMouseMoveHandler(new MouseMoveHandler() {

                                    @Override
                                    public void onMouseMove(MouseMoveEvent event) {
                                        logger.setLevel(Level.ALL);
                                        int currentx = event.getX();
                                        int currenty = event.getY();
                                        // If you drag it out, we'll
                                        // stop
                                        // drawing.
                                        if (currentx < x_offset_from_left || currenty < y_offset_from_top || currentx > x_offset_from_left + x_plot_size
                                                || currenty > y_offset_from_top + y_plot_size) {

                                            draw = false;
                                            endx = currentx;
                                            endy = currenty;
                                            logger.info("(endx, endy):(" + endx + ", " + endy + ")");
                                        }
                                        if (draw) {
                                            double scaled_x_per_pixel = x_per_pixel / imageScaleRatio;
                                            double scaled_y_per_pixel = y_per_pixel / imageScaleRatio;
                                            world_endx = x_axis_lower_left + (currentx - x_offset_from_left * imageScaleRatio) * scaled_x_per_pixel;
                                            world_endy = y_axis_lower_left + ((y_image_size * imageScaleRatio - currenty) - y_offset_from_bottom * imageScaleRatio)
                                                    * scaled_y_per_pixel;
                                            logger.info("(world_endx, world_endy):(" + world_endx + ", " + world_endy + ")");
                                            setTextValues();
                                            logger.info("randomColor.value():" + randomColor.value());
                                            frontCanvasContext.setFillStyle(randomColor);
                                            // frontCanvasContext.drawImage(ImageElement.as(image.getElement()),
                                            // 0, 0);
                                            drawToScreenScaled(imageScaleRatio);
                                            frontCanvasContext.strokeRect(startx, starty, currentx - startx, currenty - starty);
                                        }
                                        logger.setLevel(Level.ALL);
                                    }
                                });
                                outputPanel.setWidget(1, 0, frontCanvas);
                                resize(Window.getClientWidth(), Window.getClientHeight());
                            }

                        });
                        frontCanvas.addMouseUpHandler(new MouseUpHandler() {

                            @Override
                            public void onMouseUp(MouseUpEvent event) {
                                logger.setLevel(Level.ALL);
                                // If we're still drawing when the mouse goes
                                // up, record the position.
                                if (draw) {
                                    endx = event.getX();
                                    endy = event.getY();
                                    logger.info("(endx, endy):(" + endx + ", " + endy + ")");
                                }
                                draw = false;
                                setConstraints();
                                logger.setLevel(Level.ALL);
                            }
                        });
                    } else {
                        // Browser cannot handle a canvas tag, so just put up
                        // the image.
                        outputPanel.setWidget(1, 0, plotImage);
                        plotImage.addLoadHandler(new LoadHandler() {

                            @Override
                            public void onLoad(LoadEvent event) {
                                resize(Window.getClientWidth(), Window.getClientHeight());
                                String w = plotImage.getWidth() - 18 + "px";
                                lasAnnotationsPanel.setPopupLeft(outputPanel.getAbsoluteLeft());
                                lasAnnotationsPanel.setPopupTop(outputPanel.getAbsoluteTop());
                                lasAnnotationsPanel.setPopupWidth(w);
                            }
                        });
                        resize(Window.getClientWidth(), Window.getClientHeight());

                    }
                    
                    // set the constraint text values and the world coordinates if we got an image back... 

                    world_startx = x_axis_lower_left;
                    world_endx = x_axis_upper_right;
                    world_starty = y_axis_lower_left;
                    world_endy = y_axis_upper_right;
                   
                    printURL = Util.getAnnotationsFrag(annourl, imageurl);
                    setTextValues();  
                }

            }
            
            if ( initialHistory != null && !initialHistory.equals("") ) {                      
                popHistory(initialHistory);
            }
        }
    };

    private void setTextValues() {
        // The should get reset to the proper values by the events below...
        constraintWidgetGroup.clearTextFields();
        String xid = xVariables.getUserObject(xVariables.getSelectedIndex()).getID();
        String xname = xVariables.getUserObject(xVariables.getSelectedIndex()).getName();
        String yid = yVariables.getUserObject(yVariables.getSelectedIndex()).getID();
        String yname = yVariables.getUserObject(yVariables.getSelectedIndex()).getName();
        String dsid = xVariables.getUserObject(xVariables.getSelectedIndex()).getDSID();
        double minx;
        double maxx;
        if ( world_endx > world_startx ) {
            minx = world_startx;
            maxx = world_endx;
        } else {
            minx = world_endx;
            maxx = world_startx;
        }
        VariableSerializable v = constraintWidgetGroup.getVariable();
        
        
        VariableConstraintAnchor ctax1 = new VariableConstraintAnchor(Constants.VARIABLE_CONSTRAINT, dsid, xid, xname, dFormat.format(minx), xname, dFormat.format(minx), "gt");      
        VariableConstraintAnchor ctax2 = new VariableConstraintAnchor(Constants.VARIABLE_CONSTRAINT, dsid, xid, xname, dFormat.format(maxx), xname, dFormat.format(maxx), "le");
        boundByFixed(ctax1);
        boundByFixed(ctax2);
        
        if ( constraintWidgetGroup.contains(ctax1)) {
            constraintWidgetGroup.remove(ctax1);
        }
        constraintWidgetGroup.addConstraint(ctax1);
        
        if ( constraintWidgetGroup.contains(ctax2) ) {
            constraintWidgetGroup.remove(ctax2);
        }
        constraintWidgetGroup.addConstraint(ctax2);
        
        if ( v.getName().equals(xname) ) {
            constraintWidgetGroup.setLhs(ctax1.getValue());
            constraintWidgetGroup.setRhs(ctax2.getValue());
        }
        double miny;
        double maxy;
        if ( world_endy > world_starty ) {
            miny = world_starty;
            maxy = world_endy;
        } else {
            miny = world_endy;
            maxy = world_starty;
        }
        
        VariableConstraintAnchor ctay1 = new VariableConstraintAnchor(Constants.VARIABLE_CONSTRAINT, dsid, yid, yname, dFormat.format(miny), yname, dFormat.format(miny), "gt");
        VariableConstraintAnchor ctay2 = new VariableConstraintAnchor(Constants.VARIABLE_CONSTRAINT, dsid, yid, yname, dFormat.format(maxy), yname, dFormat.format(maxy), "le");
        boundByFixed(ctay1);
        boundByFixed(ctay2);    
        
        if ( constraintWidgetGroup.contains(ctay1) ) {
            constraintWidgetGroup.remove(ctay1);
        }
        constraintWidgetGroup.addConstraint(ctay1);
        
        if ( constraintWidgetGroup.contains(ctay2) ) {
            constraintWidgetGroup.remove(ctay2);
        }
        constraintWidgetGroup.addConstraint(ctay2);
        
        if ( v.getName().equals(yname) ) {
            constraintWidgetGroup.setLhs(ctay1.getValue());
            constraintWidgetGroup.setRhs(ctay2.getValue());
        }
    }
    private void boundByFixed(VariableConstraintAnchor a) {
        VariableConstraintAnchor match = (VariableConstraintAnchor) fixedConstraintPanel.findMatchingAnchor(a);
        if ( match != null ) {
            double value = Double.valueOf(a.getValue());
            double matchV = Double.valueOf(match.getValue());
            String op = match.getOp();
            if ( op.equals("gt") || op.equals("ge")) {
                if ( value < matchV ) {
                    a.setValue(match.getValue());
                }
            } else if ( op.equals("lt") || op.equals("le") ) {
                if ( value > matchV) {
                    a.setValue(match.getValue());
                }
            }
        }
    }

    AsyncCallback<ConfigSerializable> datasetCallback = new AsyncCallback<ConfigSerializable>() {

        @Override
        public void onFailure(Throwable caught) {

            Window.alert("Could not get the variables list from the server.");

        }

        @Override
        public void onSuccess(ConfigSerializable config) {
            List<ERDDAPConstraintGroup> gs = config.getConstraintGroups();
            
            CategorySerializable cat = config.getCategorySerializable();
            
            VariableSerializable[] variables = null;
            if (cat != null && cat.isVariableChildren()) {
                DatasetSerializable ds = cat.getDatasetSerializable();
                variables = ds.getVariablesSerializable();
                constraintWidgetGroup.init(gs, variables);
            } else {
                Window.alert("Could not get the variables list from the server.");

            }

            if (variables == null) {
                Window.alert("Could not get the variables list from the server.");
            } else {

                int index = -1;
                int time_index = -1;
                for (int i = 0; i < variables.length; i++) {
                    VariableSerializable vs = variables[i];
                    if ( !vs.getAttributes().get("grid_type").equals("vector") ) {
                        xAllDatasetVariables.put(vs.getID(), vs);
                    }
                }
                xVariables.setVariables(Arrays.asList(variables));
                yVariables.setVariables(Arrays.asList(variables));
                colorVariables.setVariables(Arrays.asList(variables));
                
                // These are the variables filtered for vectors and sub-set variables
                List<VariableSerializable> filtered = xVariables.getVariables();
                for (Iterator iterator = filtered.iterator(); iterator.hasNext();) {
                    VariableSerializable variableSerializable = (VariableSerializable) iterator.next();
                    xFilteredDatasetVariables.put(variableSerializable.getID(), variableSerializable);
                }
                xVariables.setAddButtonVisible(false);
                yVariables.setAddButtonVisible(false);
                colorVariables.setAddButtonVisible(false);
                
                if (index > 0) {
                    yVariables.setSelectedIndex(index);
                }
                if (time_index > 0) {
                    xVariables.setSelectedIndex(time_index);
                }
                String grid_type = xVariables.getUserObject(0).getAttributes().get("grid_type");
                if (grid_type.equals("regular")) {
                    operationID = "prop_prop_plot";
                } else if (grid_type.equals("trajectory")) {
                    operationID = "Trajectory_correlation_plot";
                }

                GridSerializable grid = xVariables.getUserObject(xVariables.getSelectedIndex()).getGrid();
                

                // TODO this is not right for data with a z axis.  It needs to be analogous with the setFixedXY and setFixedT
                if (grid.hasZ()) {
                    zAxisWidget.init(grid.getZAxis());
                    zAxisWidget.setVisible(true);
                    zAxisWidget.setRange(true);
                } else {
                    zAxisWidget.setVisible(false);
                }

                if (zlo != null && zhi != null) {
                    zAxisWidget.setLo(zlo);
                    zAxisWidget.setHi(zhi);
                }

                setVariables();
                List<Map<String, String>> vcs = lasRequest.getVariableConstraints();
                
                setFixedConstraintsFromRequest(vcs);
                
                // The request is now set up like a property-property plot
                // request,
                // so save it.
                initialState = new LASRequest(lasRequest.toString());
                updatePlot(true);
            }
        }

    };

    private void setConstraintsFromRequest(List<Map<String, String>> vcs) {
        String op1 = "gt";
        String op2 = "le";
        for (Iterator vcIt = vcs.iterator(); vcIt.hasNext();) {
            Map<String, String> con = (Map<String, String>) vcIt.next();
            String varid = con.get("varID");
            String op = con.get("op");
            String value = con.get("value");
            String type = con.get("type");
            if ( type.equals(Constants.VARIABLE_CONSTRAINT) ) {
                VariableSerializable v = xFilteredDatasetVariables.get(varid);        
                VariableConstraintAnchor cta2 = new VariableConstraintAnchor(Constants.VARIABLE_CONSTRAINT, dsid, varid, v.getName(), value, v.getName(), value, op);       
                constraintWidgetGroup.addConstraint(cta2);
            } else if ( type.equals(Constants.TEXT_CONSTRAINT) ) {
                String lhs = con.get("lhs");
                String rhs = con.get("rhs");
                if ( rhs.contains("_ns_") ) {
                    String[] r = rhs.split("_ns_");
                    for (int i = 0; i < r.length; i++) {
                        TextConstraintAnchor cta = new TextConstraintAnchor(Constants.TEXT_CONSTRAINT, dsid, lhs, lhs, r[i], lhs, r[i], "is");
                        constraintWidgetGroup.addConstraint(cta);
                    }

                } else{
                    TextConstraintAnchor cta = new TextConstraintAnchor(Constants.TEXT_CONSTRAINT, dsid, lhs, lhs, rhs, lhs, rhs, "eq");
                    constraintWidgetGroup.addConstraint(cta);
                }
            }
        }
        setConstraints();
    }
    private void setFixedConstraintsFromRequest(List<Map<String, String>> vcs) {
        for (Iterator vcIt = vcs.iterator(); vcIt.hasNext();) {
            Map<String, String> con = (Map<String, String>) vcIt.next();
            String varid = con.get("varID");
            String op = con.get("op");
            String value = con.get("value");
            String id = con.get("id");
            String type = con.get("type");
            if ( type.equals(Constants.VARIABLE_CONSTRAINT) ) {
                VariableSerializable v = xFilteredDatasetVariables.get(varid);
                ConstraintLabel cta = new ConstraintLabel(Constants.VARIABLE_CONSTRAINT, dsid, varid, v.getName(), value, v.getName(), value, op);
                fixedConstraintPanel.add(cta);
            } else if ( type.equals(Constants.TEXT_CONSTRAINT) ) {
                String lhs = con.get("lhs");
                String rhs = con.get("rhs");
                if ( rhs.contains("_ns_") ) {
                    String[] r = rhs.split("_ns_");
                    for (int i = 0; i < r.length; i++) {
                        ConstraintLabel cta = new ConstraintLabel(Constants.TEXT_CONSTRAINT, dsid, lhs, lhs, r[i], lhs, r[i], "is");
                        fixedConstraintPanel.add(cta);
                    }

                } else{
                    ConstraintLabel cta = new ConstraintLabel(Constants.TEXT_CONSTRAINT, dsid, lhs, lhs, rhs, lhs, rhs, "eq");
                    fixedConstraintPanel.add(cta);
                }
            }
        }
        setConstraints();
    }
   
    private void setFixedT(String tlo, String thi) {
        
        ConstraintLabel cta_tlo = new ConstraintLabel(Constants.T_CONSTRAINT, dsid, "time", "time", tlo, "time", tlo, "ge");
        fixedConstraintPanel.add(cta_tlo);   
        ConstraintLabel cta_thi = new ConstraintLabel(Constants.T_CONSTRAINT, dsid, "time", "time", thi, "time", thi, "le");
        fixedConstraintPanel.add(cta_thi);  
        
    }
    private void setFixedXY(String xlo, String xhi, String ylo, String yhi) {
        ConstraintLabel cta_xlo = new ConstraintLabel(Constants.X_CONSTRAINT, dsid, "longitude", "longitude", xlo, "longitude", xlo, "ge");
        fixedConstraintPanel.add(cta_xlo);   
        ConstraintLabel cta_xhi = new ConstraintLabel(Constants.X_CONSTRAINT, dsid, "longitude", "longitude", xhi, "longitude", xhi, "le");
        fixedConstraintPanel.add(cta_xhi);   
        ConstraintLabel cta_ylo = new ConstraintLabel(Constants.Y_CONSTRAINT, dsid, "latitude", "latitude", ylo, "latitude", ylo, "ge");
        fixedConstraintPanel.add(cta_ylo);   
        ConstraintLabel cta_yhi = new ConstraintLabel(Constants.Y_CONSTRAINT, dsid, "latitude", "latitude", yhi, "latitude", yhi, "le");
        fixedConstraintPanel.add(cta_yhi);  
    }
    private int getNumber(Node firstChild) {
        if (firstChild instanceof Text) {
            Text content = (Text) firstChild;
            String value = content.getData().toString().trim();
            return Double.valueOf(value).intValue();
        } else {
            return -999;
        }
    }

    private double getDouble(Node firstChild) {
        if (firstChild instanceof Text) {
            Text content = (Text) firstChild;
            String value = content.getData().toString().trim();
            return Double.valueOf(value).doubleValue();
        } else {
            return -999.;
        }
    }

    ValueChangeHandler<String> historyHandler = new ValueChangeHandler<String>() {

        @Override
        public void onValueChange(ValueChangeEvent<String> event) {

            String xml = event.getValue();
            if (!xml.equals("")) {
                popHistory(xml);
            } else {
                print.setEnabled(false);
                outputPanel.removeCell(1, 0);
                xVariables.setSelectedIndex(0);
                yVariables.setSelectedIndex(0);
            }
        }

    };

    private void setVariables() {
        update.addStyleDependentName("APPLY-NEEDED");
        String vix = xVariables.getUserObject(xVariables.getSelectedIndex()).getID();
        String viy = yVariables.getUserObject(yVariables.getSelectedIndex()).getID();
        lasRequest.removeVariables();
        lasRequest.addVariable(dsid, vix, 0);
        lasRequest.addVariable(dsid, viy, 0);
        lasRequest.setProperty("data", "count", "2");
        if (colorCheckBox.getValue()) {
            lasRequest.setProperty("data", "count", "3");
            String varColor = colorVariables.getUserObject(colorVariables.getSelectedIndex()).getID();
            lasRequest.addVariable(dsid, varColor, 0);
        }
        for (Iterator varIt = xFilteredDatasetVariables.keySet().iterator(); varIt.hasNext();) {
            String id = (String) varIt.next();
            VariableSerializable var = xFilteredDatasetVariables.get(id);
            if (!id.equals(vix) && !id.equals(viy)) {
                if (colorCheckBox.getValue()) {
                    String varColor = colorVariables.getUserObject(colorVariables.getSelectedIndex()).getID();
                    if (!id.equals(varColor)) {
                        lasRequest.addVariable(dsid, id, 0);
                    }
                } else {
                    lasRequest.addVariable(dsid, id, 0);
                }
            }
        }
    }

    

    private void setConstraints() {
        
        update.addStyleDependentName("APPLY-NEEDED");
        undoState = new LASRequest(lasRequest.toString());
        lasRequest.removeConstraints();
        
        List<ConstraintSerializable> fixedcons = fixedConstraintPanel.getConstraints();
        List<ConstraintSerializable> cons = constraintWidgetGroup.getConstraints();
        // X, Y, Z and T are handled separately
        for (Iterator conIt = fixedcons.iterator(); conIt.hasNext();) {
            ConstraintSerializable constraintSerializable = (ConstraintSerializable) conIt.next();
            if ( constraintSerializable.getType().equals(Constants.VARIABLE_CONSTRAINT) || constraintSerializable.getType().equals(Constants.TEXT_CONSTRAINT)) {
                lasRequest.addConstraint(constraintSerializable);
            }
        }
        lasRequest.addConstraints(cons);
       
    }

   

    private void popHistory(String xml) {

        // If the pop was requested from the undoState in particular and it is
        // not set, return.
        if (xml == null || xml.equals(""))
            return;

        lasRequest = new LASRequest(xml);

        String vx = lasRequest.getVariable(0);
        String vy = lasRequest.getVariable(1);
        String vc = lasRequest.getVariable(2);
        
        String count = lasRequest.getProperty("data", "count");
        int c = 2;
        try {
            c = Integer.valueOf(count);
        } catch (Exception e) {
            // Pretend it's 2.
        }
        if (vc != null && !vc.equals("") && c >= 3) {
            colorVariables.setSelectedVariableById(vc);
            colorCheckBox.setValue(true);
            colorVariables.setEnabled(true);
        } else {
            colorCheckBox.setValue(false);
            colorVariables.setEnabled(false);
        }

        List<Map<String, String>> vcons = lasRequest.getVariableConstraints();
        setConstraintsFromRequest(vcons);

        setVariables();

        initialHistory = "";
        hasInitialHistory = false;
        updatePlot(false);

    }

    private void pushHistory(String xml) {
        History.newItem(xml, false);
    }

    private void warn(String message) {
        String grid_type = xVariables.getUserObject(0).getAttributes().get("grid_type");
        // Don't warn if the grid type is regular...
        if (!grid_type.contains("reg")) {
            warnText.setText(message);
            warning.show();
        }
    }

    /**
     * uses {@link plotImage}
     * 
     * @param newPlotImageWidth
     */
    void setPlotWidth(int newPlotImageWidth) {
        autoZoom = true;
        int pwidth = Math.max(newPlotImageWidth, image_w_min);
        if (autoZoom) {
            imageScaleRatio = 1.;
            if (pwidth < image_w) {
                // If the panel is less than the image, shrink the image.
                double h = ((Double.valueOf(image_h) / Double.valueOf(image_w)) * Double.valueOf(pwidth));
                imageScaleRatio = h / Double.valueOf(image_h);
            }
            if (plotImage != null) {
                // set the plotImage to the grid before scaling so plotImage's
                // imageElement will be valid
                outputPanel.setWidget(2, 0, plotImage);
                plotImage.setVisible(false);
                drawToScreenScaled(imageScaleRatio);
            }
        } else {
            setImageSize(fixedZoom);
        }
        if (spin.isVisible()) {
            spinSetPopupPositionCenter(plotImage);
        }
        outputPanel.setWidth(getPlotWidth());
        // Piggy back setting the annotations width onto this method.
        lasAnnotationsPanel.setPopupWidth(getPlotWidth());
    }

    /**
     * Position spin at the center of the plotWidget
     * 
     * @param plotWidget
     */
    void spinSetPopupPositionCenter(Widget plotWidget) {
        int absoluteLeft = plotWidget.getAbsoluteLeft();
        int offsetWidth = plotWidget.getOffsetWidth();
        int absoluteTop = plotWidget.getAbsoluteTop();
        int offsetHeight = plotWidget.getOffsetHeight();
        int left = absoluteLeft + (offsetWidth / 2);
        int top = absoluteTop + (offsetHeight / 2);
        spin.setPopupPosition(left, top);
    }

    private void drawToScreenScaled(double scaleRatio) {
        ImageData scaledImage = scaleImage(scaleRatio);
        drawToScreen(scaledImage);
    }

    private ImageData scaleImage(double scaleToRatio) {
        logger.info("entering scaleImage with scaleToRatio:" + scaleToRatio);
        Canvas canvasTmp = Canvas.createIfSupported();
        Context2d context = canvasTmp.getContext2d();

        int imageHeight = plotImage.getHeight();
        if (imageHeight <= 0) {
            logger.warning("imageHeight:" + imageHeight);
        }
        double ch = (imageHeight * scaleToRatio);
        int imageWidth = plotImage.getWidth();
        if (imageWidth <= 0) {
            logger.warning("imageWidth:" + imageWidth);
        }
        double cw = (imageWidth * scaleToRatio);

        canvasTmp.setCoordinateSpaceHeight((int) ch);
        canvasTmp.setCoordinateSpaceWidth((int) cw);

        // TODO: make a temp imageElement?
        ImageElement imageElement = ImageElement.as(plotImage.getElement());

        // s = source
        // d = destination
        double sx = 0;
        double sy = 0;
        int imageElementWidth = imageElement.getWidth();
        if (imageElementWidth <= 0) {
            logger.warning("imageElementWidth:" + imageElementWidth);
            imageElementWidth = imageWidth;
            logger.info("imageElementWidth:" + imageElementWidth);
        }
        double sw = imageElementWidth;
        int imageElementHeight = imageElement.getHeight();
        if (imageElementHeight <= 0) {
            logger.warning("imageElementHeight:" + imageElementHeight);
            imageElementHeight = imageHeight;
            logger.info("imageElementHeight:" + imageElementHeight);
        }
        double sh = imageElementHeight;

        double dx = 0;
        double dy = 0;
        double dw = imageElementWidth;
        double dh = imageElementHeight;

        // tell it to scale image
        context.scale(scaleToRatio, scaleToRatio);

        // draw image to canvas
        context.drawImage(imageElement, sx, sy, sw, sh, dx, dy, dw, dh);

        // get image data
        double w = dw * scaleToRatio;
        double h = dh * scaleToRatio;
        ImageData imageData = null;
        try {
            imageData = context.getImageData(0, 0, w, h);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            if (plotImage == null) {
                logger.warning("image:null");
            } else {
                logger.warning("image:" + plotImage.toString());
            }
            logger.warning("scaleToRatio:" + scaleToRatio);
            logger.warning("ch:" + ch);
            logger.warning("cw:" + cw);
            logger.warning("dw:" + dw);
            logger.warning("w:" + w);
            logger.warning("dh:" + dh);
            logger.warning("h:" + h);
            if (imageData == null) {
                logger.warning("imageData:null");
            } else {
                logger.warning("imageData:" + imageData.toString());
            }
            e.printStackTrace();
        }

        frontCanvas.setCoordinateSpaceHeight((int) h + 10);
        frontCanvas.setCoordinateSpaceWidth((int) w + 10);
        if (imageData != null)
            logger.info("scaleImage exiting returning imageData:" + imageData);
        else
            logger.severe("scaleImage exiting returning imageData:null");
        return imageData;
    }

    private void drawToScreen(ImageData imageData) {
        if (frontCanvasContext != null)
            frontCanvasContext.putImageData(imageData, 0, 0);
    }

    public void setImageSize(int percent) {
        fixedZoom = percent;
        double factor = percent / 100.;
        int w = (int) (Double.valueOf(image_w).doubleValue() * factor);
        int h = (int) (Double.valueOf(image_h).doubleValue() * factor);
        outputPanel.setWidth(w + "px");
        outputPanel.setHeight(h + "px");
        autoZoom = false;
    }

    String getPlotWidth() {
        logger.fine("entering getPlotWidth()");
        int antipadding = 0;// 100;
        String w = CONSTANTS.DEFAULT_ANNOTATION_PANEL_WIDTH();
        if (plotImage != null) {
            if (imageScaleRatio == 0.0)
                imageScaleRatio = 1.0;
            int wt = (int) ((plotImage.getWidth() - antipadding) * imageScaleRatio);
            w = wt + "px";
        }
        logger.fine("exiting getPlotWidth(), retuning w:" + w);
        return w;
    }

    /**
     * @param plotImage
     * @param newPlotImageHeight
     */
    void setPlotHeight(IESafeImage plotImage, int newPlotImageHeight) {
        autoZoom = true;
        pwidth = (int) ((image_w / image_h) * Double.valueOf(newPlotImageHeight));
        setPlotWidth(pwidth);
    }

    /**
     * @param windowWidth
     * @param windowHeight
     */
    void resize(int windowWidth, int windowHeight) {
        logger.info("resize called with windowWidth=" + windowWidth + " and windowHeight=" + windowHeight);
        try {
            IESafeImage plotImage = (IESafeImage) outputPanel.getWidget(2, 0);
            if (plotImage != null) {
                // Check width first
                int leftPaddingWidth = RootPanel.get("leftPadding").getOffsetWidth();
                int leftControlsWidth = RootPanel.get("leftControls").getOffsetWidth();
                int newPlotImageWidth = windowWidth - leftPaddingWidth - leftControlsWidth;
                logger.info("newPlotImageWidth=" + newPlotImageWidth);
                int plotImageWidth = plotImage.getWidth();
                logger.info("plotImageWidth=" + plotImageWidth);
                if (newPlotImageWidth != plotImageWidth) {
                    setPlotWidth(newPlotImageWidth);
                    // Check that height is still small enough
                    int newPlotImageHeight = windowHeight - topAndBottomPadding - lasAnnotationsPanel.getOffsetHeight();
                    logger.info("newPlotImageHeight=" + newPlotImageHeight);
                    int plotImageHeight = plotImage.getHeight();
                    logger.info("plotImageHeight=" + plotImageHeight);
                    if (newPlotImageHeight < plotImageHeight) {
                        setPlotHeight(plotImage, newPlotImageHeight);
                    }
                } else {
                    // It's the correct width, so now check the height
                    int newPlotImageHeight = windowHeight - topAndBottomPadding - lasAnnotationsPanel.getOffsetHeight();
                    logger.info("newPlotImageHeight=" + newPlotImageHeight);
                    int plotImageHeight = plotImage.getHeight();
                    logger.info("plotImageHeight=" + plotImageHeight);
                    if (newPlotImageHeight != plotImageHeight) {
                        setPlotHeight(plotImage, newPlotImageHeight);
                        // Check that width is still small enough
                        leftPaddingWidth = RootPanel.get("leftPadding").getOffsetWidth();
                        leftControlsWidth = RootPanel.get("leftControls").getOffsetWidth();
                        newPlotImageWidth = windowWidth - leftPaddingWidth - leftControlsWidth;
                        logger.info("newPlotImageWidth=" + newPlotImageWidth);
                        plotImageWidth = plotImage.getWidth();
                        logger.info("plotImageWidth=" + plotImageWidth);
                        if (newPlotImageWidth < plotImageWidth) {
                            setPlotWidth(newPlotImageWidth);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * @param down
     */
    void showAnnotations(boolean down) {
        annotationsButton.setDown(down);
        lasAnnotationsPanel.setVisible(down);
        if (down) {
            annotationsButton.setTitle("Click to hide the annotations of the plot.");
        } else {
            annotationsButton.setTitle("Click to show the annotations of the plot.");
        }
    }

    ChangeHandler constraintChange = new ChangeHandler() {

        @Override
        public void onChange(ChangeEvent event) {
            TextBox w = (TextBox) event.getSource();
            Widget wp = w.getParent();
            Widget gp = wp.getParent();
            if (gp instanceof VariableConstraintWidget) {
                VariableConstraintWidget vcw = (VariableConstraintWidget) gp;
                vcw.setApply(true);
            }
            setConstraints();
        }

    };

    ClickHandler applyHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
            update.addStyleDependentName("APPLY-NEEDED");
            setConstraints();
        }

    };
    CancelEvent.Handler cancelRequestHandler = new CancelEvent.Handler() {

        @Override
        public void onCancel(CancelEvent event) {
            if (event.getID().equals("Correlation")) {
                currentURL = currentURL + "&cancel=true";
                RequestBuilder sendRequest = new RequestBuilder(RequestBuilder.GET, currentURL);
                try {

                    lasAnnotationsPanel.setError("Fetching plot annotations...");
                    sendRequest.sendRequest(null, lasRequestCallback);
                } catch (RequestException e) {
                    Window.alert("Unable to cancel request.");
                }
            }
        }
    };
    private String plotVariable(String id) {
        VariableSerializable varX = xVariables.getUserObject(xVariables.getSelectedIndex());
        VariableSerializable varY = yVariables.getUserObject(yVariables.getSelectedIndex());
        
        if (varX.getID().equals(id))
            return "x";
        if (varY.getID().equals(id))
            return "y";
        return "";
    }
    protected String getAnchor() {
        String url = Window.Location.getHref();
        if (url.contains("#")) {
            return url.substring(url.indexOf("#") + 1, url.length());
        } else {
            return "";
        }

    }
    private String decode(String xml) {
        xml = URL.decode(xml);

        // Get rid of the entity values for > and <
        xml = xml.replace("&gt;", ">");
        xml = xml.replace("&lt;", "<");
        // Replace the op value with gt ge eq lt le as needed.
        xml = xml.replace("op=\">=\"", "op=\"ge\"");
        xml = xml.replace("op=\">\"", "op=\"gt\"");
        xml = xml.replace("op=\"=\"", "op=\"eq\"");
        xml = xml.replace("op=\"<=\"", "op=\"le\"");
        xml = xml.replace("op=\"<\"", "op=\"lt\"");
        return xml;
    }
}