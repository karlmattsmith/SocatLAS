package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.ClientFactoryImpl;
import gov.noaa.pmel.tmap.las.client.event.WidgetSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.map.GeoUtil;
import gov.noaa.pmel.tmap.las.client.serializable.TimeAxisSerializable;
import gov.noaa.pmel.tmap.las.client.time.AllLeapChronology;
import gov.noaa.pmel.tmap.las.client.time.NoLeapChronology;
import gov.noaa.pmel.tmap.las.client.time.ThreeSixtyDayChronology;

import org.gwttime.time.Chronology;
import org.gwttime.time.DateTime;
import org.gwttime.time.DateTimeZone;
import org.gwttime.time.format.DateTimeFormat;
import org.gwttime.time.format.DateTimeFormatter;
import org.gwttime.time.format.ISODateTimeFormat;
import org.gwttime.time.chrono.GregorianChronology;
import org.gwttime.time.chrono.JulianChronology;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.maps.jsio.rebind.LongFragmentGenerator;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;


/**
 * A pure GWT implementation of the LAS Date Widget.
 * @author rhs
 *
 */
public class DateTimeWidget extends Composite {
	
	private DateTimeFormatter longForm;
	private DateTimeFormatter mediumForm;
	private DateTimeFormatter shortForm;
	private DateTimeFormatter shortFerretForm;
	private DateTimeFormatter mediumFerretForm;
	private DateTimeFormatter longFerretForm; 
	private DateTimeFormatter isoForm;
	
	DateTime lo;
	DateTime hi;
    
	
	Label d_label = new Label("Date/Time: ");
	Label d_label_lo_range = new Label("Start date/time: ");
	Label d_label_hi_range = new Label("End date/time: ");
	
	ListBox lo_year = new ListBox();
	ListBox lo_month = new ListBox();
	ListBox lo_day = new ListBox();
	HourListBox lo_hour = new HourListBox();

	ListBox hi_year = new ListBox();
	ListBox hi_month = new ListBox();
	ListBox hi_day = new ListBox();
	HourListBox hi_hour = new HourListBox();
    
	Grid dateTimeWidget = new Grid(2, 6);

	boolean hasYear = false;
	boolean hasMonth = false;
	boolean hasDay = false;
	boolean hasHour = false;

	boolean climatology;
	boolean range;
	
	boolean isMenu = false;
	
	String render;
	
	String lo_date;
	String hi_date;
	
	int delta;
	
	Chronology chrono;
	
	DateTimeFormatter monthFormat;
	
	EventBus eventBus;
	
	private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	
	//public DateTimeWidget(String lo_date, String hi_date, int deltaMinutes, int minuteOffset, String render, boolean range, boolean climatology) {
	/**
	 * Construct using a TimeAxisSerializable object. Range set to false means there is only one widget (or set of
	 * widgets in the case of time) visible and the user can only select one
	 * point from that axis. Range set to true means that there are two
	 * identical coordinated widgets (or set of widgets in the case of time)
	 * from which you can select a starting point and an ending point from that
	 * axis. The coordination between the widgets is such that you can not
	 * select an endpoint that is before the starting point select. The widgets
	 * update themselves to prevent this from happening.
	 * 
	 * @param tAxis
	 * @param range
	 */
	public DateTimeWidget(TimeAxisSerializable tAxis, boolean range) {
	    ClientFactory factory = GWT.create(ClientFactory.class);
	    eventBus = factory.getEventBus();
        init(tAxis, range);
        setListeners();
		initWidget(dateTimeWidget);
	}
	
	/**
	 * 
	 */
	public DateTimeWidget() {
	    ClientFactory factory = GWT.create(ClientFactory.class);
        eventBus = factory.getEventBus();
		setListeners();
		initWidget(dateTimeWidget);
	}
	public String getFerretDateMin() {
		if ( climatology ) {
			StringBuilder date = new StringBuilder();
			date.append(GeoUtil.format_two(lo.getDayOfMonth()));
			date.append("-"+monthFormat.print(lo.getMillis()));
			date.append("-0001"); 
			return date.toString();
		} else if ( isMenu ) {
			return lo_day.getValue(0);
		} else {
		
			if ( hasHour ) {
				return mediumFerretForm.print(lo.getMillis());
			} else {
				return shortFerretForm.print(lo.getMillis());
			}
		}
	}
	public String getFerretDateMax() {
		if ( climatology ) {
			StringBuilder date = new StringBuilder();
			date.append(GeoUtil.format_two(hi.getDayOfMonth()));
			date.append("-"+monthFormat.print(hi.getMillis()));
			date.append("-0001"); 
			return date.toString();
		} else if ( isMenu ) { 
			return hi_day.getValue(hi_day.getItemCount()-1);
		} else  {
			if ( hasHour ) {
				return mediumFerretForm.print(hi.getMillis());
			} else {
				return shortFerretForm.print(hi.getMillis());
			}
		}
	}
	/**
	 * Initialize using a TimeAxisSerializable object. Range set to false means there is only one widget (or set of
	 * widgets in the case of time) visible and the user can only select one
	 * point from that axis. Range set to true means that there are two
	 * identical coordinated widgets (or set of widgets in the case of time)
	 * from which you can select a starting point and an ending point from that
	 * axis. The coordination between the widgets is such that you can not
	 * select an endpoint that is before the starting point select. The widgets
	 * update themselves to prevent this from happening.
	 * 
	 * @param tAxis
	 * @param range
	 */
	public void init(TimeAxisSerializable tAxis, boolean range) {
		dateTimeWidget.clear();
		hasYear = false;
		hasMonth = false;
		hasDay = false;
		hasHour = false;
		isMenu = false;
		this.range = range;
		
		if (tAxis.getNames() != null && tAxis.getNames().length > 0 ) {
			initChrono(tAxis.getCalendar());
			isMenu = true;
			lo_day.clear();
			hi_day.clear();
			String[] names = tAxis.getNames();
			String[] values = tAxis.getValues();
			for(int i = 0; i < names.length; i++ ) {
				lo_day.addItem(names[i], values[i]);
				hi_day.addItem(names[i], values[i]);
			}
			hasYear = false;
			hasMonth = false;
			hasDay = true;
			hasHour = false;
			
			lo_date = values[0];
			hi_date = values[values.length - 1];
			loadWidget();
		} else {
			lo_date = tAxis.getLo();
			hi_date = tAxis.getHi();
			if ( tAxis.getRenderString().toLowerCase().contains("t") ) {
               init(lo_date, hi_date, (int)(tAxis.getMinuteInterval()), tAxis.getRenderString(), tAxis.getCalendar(), tAxis.isClimatology());
			} else {
				init(lo_date, hi_date, tAxis.getRenderString(), tAxis.getCalendar(), tAxis.isClimatology());
			}
		}
		String display_hi = tAxis.getAttributes().get("display_hi");
        String display_lo = tAxis.getAttributes().get("display_lo");
        if ( display_hi != null ) {
            setHi(display_hi);
        }
        if ( display_lo != null ) {
            setLo(display_lo);
        }
			
	}
	public void reinit() {

		if ( isMenu ) {
			lo_day.setSelectedIndex(0);
			hi_day.setSelectedIndex(hi_day.getItemCount() - 1);
		} else {
			setLo(longFerretForm.print(lo.getMillis()));
			setHi(longFerretForm.print(hi.getMillis()));
		}
	}
	public void setListeners() {
		lo_year.addChangeHandler(loYearHandler);
		lo_month.addChangeHandler(loMonthHandler);
		lo_day.addChangeHandler(loDayHandler);
		lo_hour.addChangeHandler(loHourHandler);
		
		hi_year.addChangeHandler(hiYearHandler);
		hi_month.addChangeHandler(hiMonthHandler);
		hi_day.addChangeHandler(hiDayHandler);
		hi_hour.addChangeHandler(hiHourHandler);
	}
	private void initChrono(String calendar) {
		chrono = GregorianChronology.getInstance(DateTimeZone.UTC);

		if ( calendar != null && !calendar.equals("") )	{
			if ( calendar.equalsIgnoreCase("proleptic_gregorian") ) {
				chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
			} else if ( calendar.equalsIgnoreCase("noleap") || calendar.equals("365_day") ) {
				chrono = NoLeapChronology.getInstanceUTC();
			} else if (calendar.equals("julian") ) {
				chrono = JulianChronology.getInstanceUTC();
			} else if ( calendar.equals("all_leap") || calendar.equals("366_day") ) {
				chrono = AllLeapChronology.getInstanceUTC();
			} else if ( calendar.equals("360_day") ) {
				chrono = ThreeSixtyDayChronology.getInstanceUTC();
			}
		} 
		monthFormat = DateTimeFormat.forPattern("MMM").withChronology(chrono).withZone(DateTimeZone.UTC);
		longForm = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withChronology(chrono).withZone(DateTimeZone.UTC);
		mediumForm = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withChronology(chrono).withZone(DateTimeZone.UTC);
		shortForm = DateTimeFormat.forPattern("yyyy-MM-dd").withChronology(chrono).withZone(DateTimeZone.UTC);
		shortFerretForm = DateTimeFormat.forPattern("dd-MMM-yyyy").withChronology(chrono).withZone(DateTimeZone.UTC);
		mediumFerretForm = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm").withChronology(chrono).withZone(DateTimeZone.UTC);
		longFerretForm = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss").withChronology(chrono).withZone(DateTimeZone.UTC);
        isoForm = ISODateTimeFormat.dateTime().withChronology(chrono).withZone(DateTimeZone.UTC);;
	}
	public void init(String lo_date, String hi_date, String render,  String calendar, boolean climo) {
		init(lo_date, hi_date, -1, render, calendar, climo);
	}
	public void init(String lo_date, String hi_date, int delta, String render, String calendar, boolean climo) {
		initChrono(calendar);
		this.render = render;
		this.climatology = climo;
        this.delta = delta;
        lo = parseDate(lo_date);
        hi = parseDate(hi_date);
		years(lo, hi, lo_year);
		years(lo, hi, hi_year);
		months(lo_month, lo.getYear());
		months(hi_month, hi.getYear());
		days(lo_day, lo.getYear(), lo.getMonthOfYear());
		days(hi_day, hi.getYear(), hi.getMonthOfYear());
	    hours(lo_hour, lo.getHourOfDay(), lo.getMinuteOfHour(), 24, 0);
		hours(hi_hour, 0, 0, hi.getHourOfDay(), lo.getMinuteOfHour());
		hi_year.setSelectedIndex(hi_year.getItemCount() - 1);
		hi_month.setSelectedIndex(hi_month.getItemCount() - 1);
		hi_day.setSelectedIndex(hi_day.getItemCount() - 1);
		loadWidget();
	}
	/**
	 * 
	 * @param hours
	 * @param start_hour
	 * @param start_minute
	 * @param end_hour
	 * @param end_minute
	 * @param delta
	 */
	private void hours(HourListBox hours, int start_hour, int start_minute, int end_hour, int end_minute) {
		hours.clear();
		if ( delta < 0 || (start_hour == 0 && end_hour == 0) ) {
            hours.addItem("00:00", "00:00");
		} else {
			int current = start_hour*60 + start_minute;
			int end = end_hour*60 + end_minute;
			while ( current < end ) {
				int hr = (int) Math.floor(current/60);
				int min = current - hr*60;
				hours.addItem(hr, min);
				current = current + delta;
			}
		}
	}
	
	private void loadWidget() {
		dateTimeWidget.clear();
		if ( isMenu ) {
			dateTimeWidget.setWidget(0, 0, d_label);
			dateTimeWidget.setWidget(0, 1, lo_day);
			dateTimeWidget.setWidget(1, 0, d_label_hi_range);
			dateTimeWidget.setWidget(1, 1, hi_day);
		} else {
			
			dateTimeWidget.setWidget(0, 0, d_label);
			dateTimeWidget.setWidget(1, 0, d_label_hi_range);
			
			
			if ( render.toLowerCase().contains("y") ) {
				dateTimeWidget.setWidget(0, 1, lo_year);
				dateTimeWidget.setWidget(1, 1, hi_year);
				hasYear = true;
			}
			
			if ( render.toLowerCase().contains("m") ) {
				dateTimeWidget.setWidget(0, 2, lo_month);
				dateTimeWidget.setWidget(1, 2, hi_month);
				hasMonth = true;
			}
			
			if ( render.toLowerCase().contains("d") ) {
				dateTimeWidget.setWidget(0, 3, lo_day);
				dateTimeWidget.setWidget(1, 3, hi_day);
				hasDay = true;
			}
			
			if ( render.toLowerCase().contains("t") ) {
				dateTimeWidget.setWidget(0, 4, lo_hour);
				dateTimeWidget.setWidget(1, 4, hi_hour);
				hasHour = true;
			}
			
		}
		CellFormatter cellFormatter = dateTimeWidget.getCellFormatter();
		if ( range ) {
			for ( int i = 0; i < 5; i++ ) {
				cellFormatter.setVisible(1, i, true);
			}
		} else {
			for ( int i = 0; i < 5; i++ ) {
				cellFormatter.setVisible(1, i, false);
			}
		}
	}

	/**
	 * Range set to false means there is only one widget (or set of widgets in
	 * the case of time) visible and the user can only select one point from
	 * that axis. isRange set to true means that there are two identical
	 * coordinated widgets (or set of widgets in the case of time) from which
	 * you can select a starting point and an ending point from that axis. The
	 * coordination between the widgets is such that you can not select an
	 * ending point that is before the selected starting point. The widgets update
	 * themselves to prevent this from happening.
	 * 
	 * @param isRange
	 */
	public void setRange(boolean isRange) {
		if ( isRange ) {
			// Want range, not currently range do something.
			if ( !range ) {
				range = isRange;
				CellFormatter cellFormatter = dateTimeWidget.getCellFormatter();
				for ( int i = 0; i < 5; i++ ) {
					cellFormatter.setVisible(1, i, true);
				}
				dateTimeWidget.setWidget(0, 0, d_label_lo_range);
				checkRangeEndYear();
			}
		} else {
			// Don't want range.  Currently range, do something.
			if ( range ) {
				range = isRange;
				CellFormatter cellFormatter = dateTimeWidget.getCellFormatter();
				for ( int i = 0; i < 5; i++ ) {
					cellFormatter.setVisible(1, i, false);
				}
				dateTimeWidget.setWidget(0, 0, d_label);
			}
		}
	}

	/**
	 * Range set to false means there is only one widget (or set of widgets in
	 * the case of time) visible and the user can only select one point from
	 * that axis. Range set to true means that there are two identical
	 * coordinated widgets (or set of widgets in the case of time) from which
	 * you can select a starting point and an ending point from that axis. The
	 * coordination between the widgets is such that you can not select an
	 * endpoint that is before the starting point select. The widgets update
	 * themselves to prevent this from happening.
	 * 
	 * @return
	 */
	public boolean isRange() {
		return range;
	}
	private void days(ListBox day, int year, int month) {
		day.clear();
		int lo_year = lo.getYear();
		int hi_year = hi.getYear();
		
		int lo_month = lo.getMonthOfYear();
		int hi_month = hi.getMonthOfYear();
		
		int start = 1;
		int end = maxDays(year, month);
		
		if ( lo_year == year && lo_month == month ) {
			start = lo.getDayOfMonth();
			end = maxDays(year, month);
			
		}
		// If it starts and ends in the same month replace with the day of the high month.
		if ( hi_year == year && hi_month == month ) {
			end = hi.getDayOfMonth();
		}
		for ( int i = start; i <= end; i++) {
			day.addItem(GeoUtil.format_two(i), GeoUtil.format_two(i));
		}
	}
	
	private int maxDays(int year, int month) {
		DateTime dt = new DateTime(year, month, 1, 0, 0, 0, chrono);
		return dt.dayOfMonth().getMaximumValue();
	}
	private void months(ListBox month, int year) {
		month.clear();
		int lo_year = lo.getYear();
		int hi_year = hi.getYear();
		
		int start = 1;
		int end = 12;
		
		if  ( lo_year == year ) {
			start = lo.getMonthOfYear();
		} 
		if ( hi_year == year ) {
			end = hi.getMonthOfYear();
		}
		
//		DateTime startDate = new DateTime(year, start, 1, 0, 0, DateTimeZone.UTC).withChronology(chrono);
//		DateTime endDate = new DateTime(year, end, 1, 0, 0, DateTimeZone.UTC).withChronology(chrono);
		
		DateTime startDate = lo.withYear(year).withMonthOfYear(start).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0);
		DateTime endDate = hi.withYear(year).withMonthOfYear(end).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0);
		
		
		while (startDate.isBefore(endDate.getMillis()) || startDate.equals(endDate)) {
			month.addItem(monthFormat.print(startDate.getMillis()));
			startDate = startDate.plusMonths(1);
		}
	}
	
	private void years(DateTime lo, DateTime hi, ListBox year) {
        year.clear();
		int start = lo.getYear();
		int end = hi.getYear();
		for ( int y = start; y <= end; y++ ) {
			year.addItem(GeoUtil.format_four(y), GeoUtil.format_four(y));
		}
	}
	public void setEnabled(boolean b) {
		lo_year.setEnabled(b);
		lo_month.setEnabled(b);
		lo_day.setEnabled(b);
		lo_hour.setEnabled(b);

		hi_year.setEnabled(b);
		hi_month.setEnabled(b);
		hi_day.setEnabled(b);
		hi_hour.setEnabled(b);
	}

	public void setVisible(boolean b) {
		dateTimeWidget.setVisible(b);
	}
	public String getISODateLo() {
	    String lo = getFerretDateLo();
	    DateTime dtlo;
	    try {
	        dtlo = shortFerretForm.parseDateTime(lo);
	    } catch (Exception e) {
	        try {
	            dtlo = mediumFerretForm.parseDateTime(lo);
	        } catch ( Exception e2 ) {
	            try {
	                dtlo = longFerretForm.parseDateTime(lo);
	            } catch (Exception e3) {
	                // punt
	                return "";
	            }
	        }
	    }
	    try {
	        return isoForm.print(dtlo.getMillis());
	    } catch (Exception e) {
	        return "";
	    }
	}
	public String getISODateHi() {
	    String hi = getFerretDateHi();
        DateTime dthi;
        try {
            dthi = shortFerretForm.parseDateTime(hi);
        } catch (Exception e) {
            try {
                dthi = mediumFerretForm.parseDateTime(hi);
            } catch ( Exception e2 ) {
                try {
                    dthi = longFerretForm.parseDateTime(hi);
                } catch (Exception e3) {
                    // punt
                    return "";
                }
            }
        }
        try {
            return isoForm.print(dthi.getMillis());
        } catch (Exception e) {
            return "";
        }
	}
	public String getFerretDateLo() {
		StringBuffer date = new StringBuffer();
		if ( isMenu ) {
			return lo_day.getValue(lo_day.getSelectedIndex());
		} else {
			if ( hasDay ) {
				date.append(lo_day.getValue(lo_day.getSelectedIndex()));
			} else {
				date.append(GeoUtil.format_two(lo.getDayOfMonth()));
			}
			if ( hasMonth ) {
				date.append("-"+lo_month.getValue(lo_month.getSelectedIndex()));
			} else {
				date.append("-"+monthFormat.print(lo.getMillis()));
			}

			if ( climatology ) {
				date.append("-0001");   
			} else {

				if ( hasYear ) {
					date.append("-"+lo_year.getValue(lo_year.getSelectedIndex()));
				}
			}
			if ( hasHour ) {
				date.append(" "+lo_hour.getValue(lo_hour.getSelectedIndex()));
			}
			return date.toString();
		}
	}

	public String getFerretDateHi() {
		if ( range ) {
			if ( isMenu ) {
				return hi_day.getValue(hi_day.getSelectedIndex());
			} else {
				StringBuffer date = new StringBuffer();
				if ( hasDay ) {
					date.append(hi_day.getValue(hi_day.getSelectedIndex()));
				} else {				
					date.append(GeoUtil.format_two(lo.getDayOfMonth()));
				}
				if ( hasMonth ) {
					date.append("-"+hi_month.getValue(hi_month.getSelectedIndex()));
				} else {
					date.append("-"+monthFormat.print(lo.getMillis()));
				}

				if ( climatology ) {
					date.append("-0001");   
				} else {

					if ( hasYear ) {
						date.append("-"+hi_year.getValue(hi_year.getSelectedIndex()));
					}
				}
				if ( hasHour ) {
					date.append(" "+hi_hour.getValue(hi_hour.getSelectedIndex()));
				}
				return date.toString();
			}
		} else {
			return getFerretDateLo();
		}
	}
	public void setLo(String tlo) {
		
		if ( isMenu ) {
			for(int d = 0; d < lo_day.getItemCount(); d++) {
				String value = lo_day.getValue(d);
				if ( value.equalsIgnoreCase(tlo) ) {
					lo_day.setSelectedIndex(d);
					loDayChange();
				}
			}
			int lo_i = lo_day.getSelectedIndex();
			int hi_i = hi_day.getSelectedIndex();
			if ( lo_i > hi_i ) {
				hi_day.setSelectedIndex(lo_i);
			}
		} else {
			DateTime lo = parseFerretDate(tlo);
			
			if ( hasYear ) {
				String year = String.valueOf(lo.getYear());
				for ( int y = 0; y < lo_year.getItemCount(); y++ ) {
					String value = lo_year.getValue(y);
					if ( value.equals(year) ) {
						lo_year.setSelectedIndex(y);
						loYearChange();
					}
				}
			}
			if ( hasMonth ) {
				String month = monthFormat.print(lo.getMillis());
				for ( int m = 0; m < lo_month.getItemCount(); m++ ) {
					String value = lo_month.getValue(m);
					if ( value.equals(month) ) {
						lo_month.setSelectedIndex(m);
						loMonthChange();
					}
				}

			}
			if ( hasDay ) {
				String day = GeoUtil.format_two(lo.getDayOfMonth());
				for(int d = 0; d < lo_day.getItemCount(); d++) {
					String value = lo_day.getValue(d);
					if ( value.equals(day) ) {
						lo_day.setSelectedIndex(d);
						loDayChange();
					}
				}
			} 
			if ( hasHour ) {
				int hour = lo.getHourOfDay();
				int min = lo.getMinuteOfDay();
				String hours_value = GeoUtil.format_two(hour)+":"+GeoUtil.format_two(min);
				for( int h = 0; h < lo_hour.getItemCount(); h++ ) {
					String value = lo_hour.getValue(h);
					if ( hours_value.equals(value) ) {
						lo_hour.setSelectedIndex(h);
					}
				}
			}
			
			 // The new value is set.  Check the range (even it it's not visible).
			if ( hasYear ) {
				checkRangeEndYear();
			} else if ( hasMonth ) {
				checkRangeEndMonth();
			} else if ( hasDay ) {
				checkRangeEndDay();
			} else {
				checkRangeEndHour();
			}
		}
	}
	public void setHi(String thi) {
		
		if ( isMenu ) {
			for(int d = 0; d < hi_day.getItemCount(); d++) {
				String value = hi_day.getValue(d);
				if ( value.equalsIgnoreCase(thi) ) {
					hi_day.setSelectedIndex(d);
				}
			}
			int lo_i = lo_day.getSelectedIndex();
			int hi_i = hi_day.getSelectedIndex();
			if ( lo_i > hi_i ) {
				lo_day.setSelectedIndex(hi_i);
			}
		} else {
			DateTime hi = parseFerretDate(thi);
			if ( hasYear ) {
				String year = String.valueOf(hi.getYear());
				for ( int y = 0; y < hi_year.getItemCount(); y++ ) {
					String value = hi_year.getValue(y);
					if ( value.equals(year) ) {
						hi_year.setSelectedIndex(y);
						hiYearChange();
					}
				}
			}
			if ( hasMonth ) {
				String month = monthFormat.print(hi.getMillis());
				for ( int m = 0; m < hi_month.getItemCount(); m++ ) {
					String value = hi_month.getValue(m);
					if ( value.equals(month) ) {
						hi_month.setSelectedIndex(m);
						hiMonthChange();
					}
				}

			}
			if ( hasDay ) {
				String day = GeoUtil.format_two(hi.getDayOfMonth());
				for(int d = 0; d < hi_day.getItemCount(); d++) {
					String value = hi_day.getValue(d);
					if ( value.equals(day) ) {
						hi_day.setSelectedIndex(d);
						hiDayChange();
					}
				}
			} 	
			if ( hasHour ) {
				int hour = hi.getHourOfDay();
				int min = hi.getMinuteOfHour();
				String hours_value = GeoUtil.format_two(hour)+":"+GeoUtil.format_two(min);
				for( int h = 0; h < hi_hour.getItemCount(); h++ ) {
					String value = hi_hour.getValue(h);
					if ( hours_value.equals(value) ) {
					    hi_hour.setSelectedIndex(h);
					}
				}
			}
            // The new value is set.  Check the range (even it it's not visible).
			if ( hasYear ) {
				checkRangeStartYear();
			} else if ( hasMonth ) {
				checkRangeStartMonth();
			} else if ( hasDay ) {
				checkRangeStartDay();
			} else {
				checkRangeStartHour();
			}
		}
	}

	private void loadAndSetMonthDayHour(ListBox month_list, ListBox day_list, HourListBox hour_list, int year, int month, int day, int hour, int min) {
		// Load the valid months for this year.
		months(month_list, year);

		int low_month_number = monthToInt(month_list.getValue(0));
		int hi_month_number = monthToInt(month_list.getValue(month_list.getItemCount() - 1));
		
		DateTime requested_instance = new DateTime(year, month, day, hour, min, chrono).withZone(DateTimeZone.UTC);
		String month_name = monthFormat.print(requested_instance.getMillis());

		if ( month < low_month_number ) {
			// If the current month is before the first month in the list
			// set to the first month.
			month_list.setSelectedIndex(0);
		} else if ( month > hi_month_number) {
			// If the current month is after the last month in the list
			// set to the last month.
			month_list.setSelectedIndex(month_list.getItemCount() - 1);
		} else {
			// Else set to that month
			for (int i = 0; i < month_list.getItemCount(); i++) {
				String v = month_list.getValue(i);
				if ( v.equals(month_name) ) {
					month_list.setSelectedIndex(i);
				}
			}
		}

		
		int selected_month = monthToInt(month_list.getValue(month_list.getSelectedIndex()));
		loadAndSetDayHour(day_list, hour_list, year, selected_month, day, hour, min);	
	}
	
	public void loadAndSetDayHour(ListBox day_list, HourListBox hour_list, int year, int month, int day, int hour, int min) {
		// Load the valid days for this month (which as set above) and year.
		days(day_list, year, month);

		if ( day < Integer.valueOf(day_list.getValue(0)).intValue() ) {
			day_list.setSelectedIndex(0);
		} else if ( day > Integer.valueOf(day_list.getValue(day_list.getItemCount() - 1)).intValue() ) {
			day_list.setSelectedIndex(day_list.getItemCount() - 1);
		} else {

			for (int i = 0; i < day_list.getItemCount(); i++) {
				String v = day_list.getValue(i);
				if ( v.equals(GeoUtil.format_two(day)) ) {
					day_list.setSelectedIndex(i);
				}
			}
		}
		
		loadAndSetHour(hour_list, year, month, Integer.valueOf(day_list.getValue(day_list.getSelectedIndex())).intValue(), hour, min);
		
	}
	public void loadAndSetHour(HourListBox hour_list, int year, int month, int day, int hour, int min) {
		int start_year = lo.getYear();
		int start_month = lo.getMonthOfYear();
		int start_day = lo.getDayOfMonth();
		int start_hour = lo.getHourOfDay();
		int start_min = lo.getMinuteOfHour();
		
		int end_year = hi.getYear();
		int end_month = hi.getMonthOfYear();
		int end_day = hi.getDayOfMonth();
		int end_hour = hi.getHourOfDay();
		int end_min = hi.getMinuteOfHour();
		if ( start_year == year && start_month == month && start_day == day ) {
			hours(hour_list, start_hour, start_min, 24, 0);
		} else if ( end_year == year && end_month == month && end_day == day ) {
			hours(hour_list, 0, 0, end_hour, end_min);
		} else {
			hours(hour_list, 0, 0, 24, 0);
		}
	}
	private void checkRangeEndYear() {
	    if ( isMenu ) {
	        // This is the start of the cascade to check the end date, but since it's a menu
	        // it's the only one you have to check and the values are stored in the "day" widget.
	        if ( hi_day.getSelectedIndex() < lo_day.getSelectedIndex() ) {
	            hi_day.setSelectedIndex(lo_day.getSelectedIndex());
	        }
	    } else {
	        String current_lo = getFerretDateLo();
	        String current_hi = getFerretDateHi();

	        DateTime clo = parseFerretDate(current_lo);
	        DateTime chi = parseFerretDate(current_hi);

	        // Set the hi year to the lo year and check the month...
	        if ( clo.isAfter(chi) ) {
	            int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex()));
	            hi_year.setSelectedIndex(lo_year.getSelectedIndex());

	            loadAndSetMonthDayHour(hi_month, hi_day, hi_hour, year, monthToInt(hi_month.getValue(hi_month.getSelectedIndex())), Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue(), hi_hour.getHour(), hi_hour.getMin());
	            checkRangeEndMonth();
	        }
	    }
	}
	private void checkRangeEndMonth() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		DateTime clo = parseFerretDate(current_lo);
		DateTime chi = parseFerretDate(current_hi);
		
		if ( clo.isAfter(chi) ) {
			int ny = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex()));
			int month = monthToInt(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex()));
			hi_month.setSelectedIndex(lo_month.getSelectedIndex());
			int hour = hi_hour.getHour();
			int min = hi_hour.getMin();
			loadAndSetDayHour(hi_day, hi_hour, ny, month, day, hour, min);
			checkRangeEndDay();
		}
	}
	private void checkRangeEndDay() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		DateTime clo = parseFerretDate(current_lo);
		DateTime chi = parseFerretDate(current_hi);
		
		if ( clo.isAfter(chi) ) {
			hi_day.setSelectedIndex(lo_day.getSelectedIndex());
			checkRangeEndHour();
		}
	}
	private void checkRangeEndHour() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		DateTime clo = parseFerretDate(current_lo);
		DateTime chi = parseFerretDate(current_hi);
		
		if ( clo.isAfter(chi) ) {
			hi_hour.setSelectedIndex(lo_hour.getSelectedIndex());
		}
	}
	private void checkRangeStartYear() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		DateTime clo = parseFerretDate(current_lo);
		DateTime chi = parseFerretDate(current_hi);
		
		// Set the hi year to the lo year and check the month...
		if ( clo.isAfter(chi) ) {
			lo_year.setSelectedIndex(hi_year.getSelectedIndex());
			checkRangeStartMonth();
		}
		
	}
	private void checkRangeStartMonth() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		DateTime clo = parseFerretDate(current_lo);
		DateTime chi = parseFerretDate(current_hi);

		if ( clo.isAfter(chi) ) {
			lo_month.setSelectedIndex(hi_month.getSelectedIndex());
			checkRangeStartDay();
		}
	}
	private void checkRangeStartDay() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		DateTime clo = parseFerretDate(current_lo);
		DateTime chi = parseFerretDate(current_hi);
		
		if ( clo.isAfter(chi) ) {
			lo_day.setSelectedIndex(hi_day.getSelectedIndex());
			checkRangeStartHour();
		}
	}
	public void checkRangeStartHour() {
		String current_lo = getFerretDateLo();
		String current_hi = getFerretDateHi();
		
		DateTime clo = parseFerretDate(current_lo);
		DateTime chi = parseFerretDate(current_hi);
		
		if ( clo.isAfter(chi) ) {
			lo_hour.setSelectedIndex(hi_hour.getSelectedIndex());
		}
	}
	public ChangeHandler loYearHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			loYearChange();
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false), DateTimeWidget.this);
		}
	};
	private void loYearChange() {
		int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
		int month = monthToInt(lo_month.getValue(lo_month.getSelectedIndex()));
		int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
		int hour = lo_hour.getHour();
		int min = lo_hour.getMin();
		loadAndSetMonthDayHour(lo_month, lo_day, lo_hour, year, month, day, hour, min);
		checkRangeEndYear();
	}
	public ChangeHandler loMonthHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent event) {
			loMonthChange();
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false), DateTimeWidget.this);
		}	
	};
	private void loMonthChange() {
		int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
		int month = monthToInt(lo_month.getValue(lo_month.getSelectedIndex()));
		int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
		int hour = lo_hour.getHour();
		int min = lo_hour.getMin();
		loadAndSetDayHour(lo_day, lo_hour, year, month, day, hour, min);
		checkRangeEndMonth();
	}
	// The loDayListener and hiDayListener are the only ones that should fire when the Widget contains a menu.
	public ChangeHandler loDayHandler = new ChangeHandler() {

		@Override
		public void onChange(ChangeEvent arg0) {
			loDayChange();
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false), DateTimeWidget.this);
		}
	};
	private void loDayChange() {
		if ( isMenu ) {
			int lo_i = lo_day.getSelectedIndex();
			int hi_i = hi_day.getSelectedIndex();
			if ( lo_i > hi_i ) {
				hi_day.setSelectedIndex(lo_i);
			}
		} else {
			int year = Integer.valueOf(lo_year.getValue(lo_year.getSelectedIndex())).intValue();
			int month = monthToInt(lo_month.getValue(lo_month.getSelectedIndex()));
			int day = Integer.valueOf(lo_day.getValue(lo_day.getSelectedIndex())).intValue();
			int hour = lo_hour.getHour();
			int min = lo_hour.getMin();
			loadAndSetHour(lo_hour, year, month, day, hour, min);
			checkRangeEndDay();
		} 
	}
	public ChangeHandler loHourHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			checkRangeEndHour();
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false), DateTimeWidget.this);
		}
	};
	public ChangeHandler hiYearHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			hiYearChange();
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false), DateTimeWidget.this);
		}
	};
	private void hiYearChange() {
		int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
		int month = monthToInt(hi_month.getValue(hi_month.getSelectedIndex()));
		int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
		int hour = hi_hour.getHour();
		int min = hi_hour.getMin();
		loadAndSetMonthDayHour(hi_month, hi_day, hi_hour, year, month, day, hour, min);
		checkRangeStartYear();
	}
	public ChangeHandler hiMonthHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			hiMonthChange();
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false), DateTimeWidget.this);
		}	
	};
	private void hiMonthChange() {
		int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
		int month = monthToInt(hi_month.getValue(hi_month.getSelectedIndex()));
		int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
		int hour = hi_hour.getHour();
		int	min = hi_hour.getMin();  
		loadAndSetDayHour(hi_day, hi_hour, year, month, day, hour, min);
		checkRangeStartMonth();
	}
	public ChangeHandler hiDayHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			hiDayChange();
			eventBus.fireEventFromSource(new WidgetSelectionChangeEvent(false), DateTimeWidget.this);
		}
	};
	private void hiDayChange() {
		if ( isMenu ) {
			int lo_i = lo_day.getSelectedIndex();
			int hi_i = hi_day.getSelectedIndex();
			if ( lo_i > hi_i ) {
				lo_day.setSelectedIndex(hi_i);
			}
		} else {
			int year = Integer.valueOf(hi_year.getValue(hi_year.getSelectedIndex())).intValue();
			int month = monthToInt(hi_month.getValue(hi_month.getSelectedIndex()));
			int day = Integer.valueOf(hi_day.getValue(hi_day.getSelectedIndex())).intValue();
			int hour = hi_hour.getHour();
			int	min = hi_hour.getMin();  
			loadAndSetHour(hi_hour, year, month, day, hour, min);
			checkRangeStartDay();
		}
	}
	public ChangeHandler hiHourHandler = new ChangeHandler() {
		@Override
		public void onChange(ChangeEvent arg0) {
			checkRangeStartHour();
		}
	};
	public boolean isContainedBy(String ferretDateLo, String ferretDateHi) {
		DateTime dateLo = parseFerretDate(ferretDateLo);
		DateTime dateHi = parseFerretDate(ferretDateHi);
		DateTime clo = parseFerretDate(getFerretDateLo());
		DateTime chi = parseFerretDate(getFerretDateHi());
		return (clo.isEqual(dateLo.getMillis()) || clo.isAfter(dateLo.getMillis())) && (chi.isEqual(dateHi.getMillis()) || chi.isBefore(dateHi.getMillis()));
	}
	/**
	 * Helper method to parse ferret dates.
	 * @param date_string of the form 15-Jan-1983, 20-Mar-1997 12:32 or 19-Mar-1962 12:11:03
	 * @return Date for the parse date
	 */
	private DateTime parseFerretDate(String date_string) {
		DateTime date;
		if ( date_string.length() == 6 ) {
			// A lovely climo date of the form 15-Jan
			date = shortFerretForm.parseDateTime(date_string+"-0001");
		} else if ( date_string.length() == 11 ) {
			date = shortFerretForm.parseDateTime(date_string);
		} else if ( date_string.length() == 17 ) {
			date = mediumFerretForm.parseDateTime(date_string);
		} else {
			date = longFerretForm.parseDateTime(date_string);
		}
		return date;
	}
	/**
	 * Helper method to parse date strings
	 * @param date_string of the form 1998-11-05, 1998-12-31 11:02 or 1923-11-14 04:13:21
	 * @return
	 */
	private DateTime parseDate(String date_string) {
		DateTime date;
		 try {
	        	date = longForm.parseDateTime(date_string);
	        } catch (IllegalArgumentException e) {
	        	try {
	        		 date = mediumForm.parseDateTime(date_string);
	        	} catch (IllegalArgumentException e1) {
	        		try{
	        			date = shortForm.parseDateTime(date_string);
	        		} catch (IllegalArgumentException e2) {
	        			date = null;
	        			Window.alert("Date parsing failed for "+date_string);
	        		}
	        	}
	        }
		return date;
	}
	// In the weird calendars, the short names don't work so well so we force the issue a little bit.
	private int monthToInt(String month_name) {
		DateTime dt = monthFormat.parseDateTime(month_name);
		return dt.getMonthOfYear();
	}
}
