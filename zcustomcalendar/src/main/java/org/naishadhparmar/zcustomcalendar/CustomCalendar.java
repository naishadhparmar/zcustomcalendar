package org.naishadhparmar.zcustomcalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Map;

/**
 * This class represents the calendar that will be visible on the screen.
 *
 * @author Naishadh Parmar
 * @version 1.0
 * @since 2017-07-14
 */

public class CustomCalendar extends LinearLayout {
	
	public static final int PREVIOUS = -1;
	public static final int NEXT = 1;
	
	private final String[] MONTHS = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	
	private Context context = null;
	private View view = null;
	
	private ImageButton butLeft = null;
	private ImageButton butRight = null;
	private TextView tvMonthYear = null;
	private TextView[] tvDaysOfWeek = null;
	private LinearLayout llWeeks = null;
	private View[] btnAll = null;
	
	private Calendar selectedDate = null;
	private OnDateSelectedListener listener = null;
	private OnNavigationButtonClickedListener leftButtonListener = null;
	private OnNavigationButtonClickedListener rightButtonListener = null;
	private float rowHeight = 0;
	private View selectedButton = null;
	private int startFrom = -1;
	private int monthYearFormat = -1;
	private int dayOfWeekLength = -1;
	private Drawable draLeftButton = null;
	private Drawable draRightButton = null;
	private Map<Integer, Object> mapDateToTag = null;
	private Map<Integer, Object> mapDateToDesc = null;
	private Map<Object, Property> mapDescToProp = null;
	
	/**
	 * Constructor that is called when inflating from XML
	 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
	 * @param attrs The attributes of the XML tag that is inflating the view.
	 */
	public CustomCalendar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomCalendar);
		startFrom = attributes.getInt(R.styleable.CustomCalendar_day_of_week_start_from, 0);
		monthYearFormat = attributes.getInt(R.styleable.CustomCalendar_month_year_format, 0);
		dayOfWeekLength = attributes.getInt(R.styleable.CustomCalendar_day_of_week_length, 1);
		draLeftButton = attributes.getDrawable(R.styleable.CustomCalendar_left_button_src);
		draRightButton = attributes.getDrawable(R.styleable.CustomCalendar_right_button_src);
		rowHeight = attributes.getDimension(R.styleable.CustomCalendar_row_height, 0.0f);
		initialize();
	}
	
	private void initialize() {
		view = inflate(context, R.layout.customcalendar, this);
		butLeft = (ImageButton) findViewById(R.id.but_left);
		butRight = (ImageButton) findViewById(R.id.but_right);
		if(draLeftButton != null) butLeft.setImageDrawable(draLeftButton);
		if(draRightButton != null) butRight.setImageDrawable(draRightButton);
		tvMonthYear = (TextView) findViewById(R.id.tv_month_year);
		tvDaysOfWeek = new TextView[7];
		tvDaysOfWeek[0] = (TextView) findViewById(R.id.tv_day_of_week_0);
		tvDaysOfWeek[1] = (TextView) findViewById(R.id.tv_day_of_week_1);
		tvDaysOfWeek[2] = (TextView) findViewById(R.id.tv_day_of_week_2);
		tvDaysOfWeek[3] = (TextView) findViewById(R.id.tv_day_of_week_3);
		tvDaysOfWeek[4] = (TextView) findViewById(R.id.tv_day_of_week_4);
		tvDaysOfWeek[5] = (TextView) findViewById(R.id.tv_day_of_week_5);
		tvDaysOfWeek[6] = (TextView) findViewById(R.id.tv_day_of_week_6);
		llWeeks = (LinearLayout) findViewById(R.id.ll_weeks);
		selectedDate = Calendar.getInstance();
		readyDaysOfWeek();
		setAll();
		butLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar previousMonth = Calendar.getInstance();
				previousMonth.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH)-1 != -1 ? selectedDate.get(Calendar.MONTH)-1: Calendar.DECEMBER);
				previousMonth.set(Calendar.YEAR, selectedDate.get(Calendar.MONTH)-1 != -1 ? selectedDate.get(Calendar.YEAR) : selectedDate.get(Calendar.YEAR)-1);
				previousMonth.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH) < previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH) ? selectedDate.get(Calendar.DAY_OF_MONTH) : previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
				selectedDate = previousMonth;
				if(rightButtonListener != null ) {
					Map<Integer, Object>[] arr = leftButtonListener.onNavigationButtonClicked(PREVIOUS, previousMonth);
					mapDateToDesc = arr[0];
					mapDateToTag = arr[1];
				}
				else {
					mapDateToDesc = null;
					mapDateToTag = null;
				}
				setAll();
			}
		});
		butRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar nextMonth = Calendar.getInstance();
				nextMonth.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH)+1 != 12 ? selectedDate.get(Calendar.MONTH)+1: Calendar.JANUARY);
				nextMonth.set(Calendar.YEAR, selectedDate.get(Calendar.MONTH)+1 != 12 ? selectedDate.get(Calendar.YEAR) : selectedDate.get(Calendar.YEAR)+1);
				nextMonth.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH) < nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH) ? selectedDate.get(Calendar.DAY_OF_MONTH) : nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
				selectedDate = nextMonth;
				if(leftButtonListener != null) {
					Map<Integer, Object>[] arr = leftButtonListener.onNavigationButtonClicked(NEXT, nextMonth);
					mapDateToDesc = arr[0];
					mapDateToTag = arr[1];
				}
				else {
					mapDateToDesc = null;
					mapDateToTag = null;
				}
				setAll();
			}
		});
	}
	
	private void setAll() {
		readyMonthAndYear();
		llWeeks.removeAllViews();
		btnAll  =   new View[selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH)];
		LinearLayout llWeek = new LinearLayout(context);
		llWeek.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rowHeight == 0?LayoutParams.WRAP_CONTENT:(int)rowHeight));
		llWeek.setOrientation(LinearLayout.HORIZONTAL);
		Calendar previousMonth = Calendar.getInstance();
		previousMonth.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH)-1 != -1 ? selectedDate.get(Calendar.MONTH)-1: Calendar.DECEMBER);
		previousMonth.set(Calendar.YEAR, selectedDate.get(Calendar.MONTH)-1 != -1 ? selectedDate.get(Calendar.YEAR) : selectedDate.get(Calendar.YEAR)-1);
		Calendar thisMonth = Calendar.getInstance();
		thisMonth.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
		thisMonth.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
		thisMonth.set(Calendar.DAY_OF_MONTH, 1);
		int j = thisMonth.get(Calendar.DAY_OF_WEEK)-startFrom-1;
		for(int i = 0 ; i < j ; i++) {
			View btn = null;
			if(mapDescToProp != null && mapDescToProp.get("disabled") != null) {
				Property prop = mapDescToProp.get("disabled");
				if (prop.layoutResource != -1) {
					btn = LayoutInflater.from(context).inflate(prop.layoutResource, null);
					if(prop.dateTextViewResource != -1) {
						((TextView)btn.findViewById(prop.dateTextViewResource)).setText("" + (previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH)-(j-i-1)));
					}
				}
				else {
					btn = new Button(context);
					((Button)btn).setText("" + (previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH)-(j-i-1)));
				}
			}
			else {
				btn = new Button(context);
				((Button)btn).setText("" + (previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH)-(j-i-1)));
			}
			llWeek.addView(btn);
			btn.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
			btn.setEnabled(false);
		}
		int index = 0;
		for(int i = 0 ; i < (7-j) ; i++) {
			btnAll[index] = readyButton(index+1);
			btnAll[index].setEnabled(true);
			llWeek.addView(btnAll[index]);
			index++;
		}
		llWeeks.addView(llWeek);
		while((thisMonth.getActualMaximum(Calendar.DAY_OF_MONTH)-7)>index) {
			llWeek = new LinearLayout(context);
			llWeek.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rowHeight == 0?LayoutParams.WRAP_CONTENT:(int)rowHeight));
			llWeek.setOrientation(LinearLayout.HORIZONTAL);
			for (int i = 0; i < 7; i++) {
				btnAll[index] = readyButton(index+1);
				llWeek.addView(btnAll[index]);
				btnAll[index].setEnabled(true);
				index++;
			}
			llWeeks.addView(llWeek);
		}
		llWeek = new LinearLayout(context);
		llWeek.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rowHeight == 0?LayoutParams.WRAP_CONTENT:(int)rowHeight));
		llWeek.setOrientation(LinearLayout.HORIZONTAL);
		int i = 0;
		for (; index < selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH); index++, i++) {
			btnAll[index] = readyButton(index+1);
			llWeek.addView(btnAll[index]);
			btnAll[index].setEnabled(true);
		}
		for(int k=1; k<=(7-i) ; k++) {
			View btn = null;
			if(mapDescToProp != null && mapDescToProp.get("disabled") != null) {
				Property prop = mapDescToProp.get("disabled");
				btn =  LayoutInflater.from(context).inflate(prop.layoutResource, null);
				btn.setEnabled(false);
				((TextView)btn.findViewById(prop.dateTextViewResource)).setText("" + k);
			}
			else {
				btn = new Button(context);
				((Button)btn).setText("" + k);
			}
			btn.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
			llWeek.addView(btn);
			btn.setEnabled(false);
		}
		llWeeks.addView(llWeek);
	}

	private void readyDaysOfWeek() {
		String[] arrOfDaysOfWeek = getResources().getStringArray(R.array.days_of_week);
		int j = 0;
		for(int i = startFrom; i < 7 ; i++, j++) {
			if(dayOfWeekLength > arrOfDaysOfWeek[i].length()) tvDaysOfWeek[j].setText(arrOfDaysOfWeek[i]);
			else tvDaysOfWeek[j].setText(arrOfDaysOfWeek[i].substring(0, dayOfWeekLength));
		}
		for(int i = 0 ; i < startFrom ; i++, j++) {
			if(dayOfWeekLength > arrOfDaysOfWeek[i].length()) tvDaysOfWeek[j].setText(arrOfDaysOfWeek[i]);
			else tvDaysOfWeek[j].setText(arrOfDaysOfWeek[i].substring(0, dayOfWeekLength));
		}
	}
	
	private void readyMonthAndYear() {
		switch (monthYearFormat) {
			case 0: tvMonthYear.setText(MONTHS[selectedDate.get(Calendar.MONTH)].substring(0,3) + " " + selectedDate.get(Calendar.YEAR));
				    break;
			case 1: tvMonthYear.setText(MONTHS[selectedDate.get(Calendar.MONTH)] + " " + selectedDate.get(Calendar.YEAR));
				    break;
		}
	}
	
	private View readyButton(final int date) {
		final View btn;
		if(mapDescToProp != null) {
			Property prop = null;
			if(mapDateToDesc != null) {
				boolean useDefault = false;
				if (mapDateToDesc.get(new Integer(date)) == null || mapDateToDesc.get(new Integer(date)).equals("default"))
					useDefault = true;
				prop = useDefault ? mapDescToProp.get("default") : mapDescToProp.get(mapDateToDesc.get(new Integer(date)));
			}
			else {
				prop = mapDescToProp.get("default");
			}
			btn = LayoutInflater.from(context).inflate(prop.layoutResource, null);
			if (!prop.enable) btn.setEnabled(false);
			((TextView) btn.findViewById(prop.dateTextViewResource)).setText("" + date);
		}
		else {
			btn = new Button(context);
			((Button)btn).setText("" + date);
		}
		Log.i("height", btn.getHeight() + "");
		btn.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
		if(mapDateToTag != null) btn.setTag(mapDateToTag.get(new Integer(date)));
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedDate.set(Calendar.DAY_OF_MONTH, date);
				if(listener != null) {
					listener.onDateSelected(btn, selectedDate, mapDateToDesc.get(new Integer(date)));
				}
				if(selectedButton != null) selectedButton.setSelected(false);
				btn.setSelected(true);
				selectedButton = btn;
			}
		});
		if(selectedDate.get(Calendar.DAY_OF_MONTH) == date) {
			btn.setSelected(true);
			selectedButton = btn;
		}
		return btn;
	}
	
	/**
	 * Set the date shown on the CustomCalendar.
	 * @param calendar The month and year combination in this calendar object will be used to show the current month and the day of month in this calendar object will be set selected.
	 */
	public void setDate(Calendar calendar) {
		this.selectedDate = calendar;
		setAll();
	}
	
	/**
	 * Set the date shown on the CustomCalendar and the map linking a date to its description.
	 * @param calendar The month and year combination in this calendar object will be used to show the current month and the day of month in this calendar object will be set selected.
	 * @param mapDateToDesc The map linking a date to its description. This description will be accessible from the {@code desc} parameter of the onDateSelected method of OnDateSelectedListener.
	 */
	public void setDate(Calendar calendar, Map<Integer, Object> mapDateToDesc) {
		this.selectedDate = calendar;
		this.mapDateToDesc = mapDateToDesc;
		this.mapDateToTag = mapDateToTag;
		setAll();
	}
	
	/**
	 * Set the date shown on the CustomCalendar, the map linking a date to its description and the map linking a date to the tag on its view.
	 * @param calendar The month and year combination in this calendar object will be used to show the current month and the day of month in this calendar object will be set selected.
	 * @param mapDateToDesc The map linking a date to its description. This description will be accessible from the {@code desc} parameter of the onDateSelected method of OnDateSelectedListener.
	 * @param mapDateToTag The map linking a date to the tag to be set on its date view. This tag will be accessible from the {@code view} parameter of the onDateSelected method of the OnDateSelectedListener.
	 */
	public void setDate(Calendar calendar, Map<Integer, Object> mapDateToDesc, Map<Integer, Object> mapDateToTag) {
		this.selectedDate = calendar;
		this.mapDateToDesc = mapDateToDesc;
		this.mapDateToTag = mapDateToTag;
		setAll();
	}
	
	/**
	 * Set the map linking a description to its respective Property object
	 * @param mapDescToProp The map linking description to its property
	 */
	public void setMapDescToProp(Map<Object, Property> mapDescToProp) {
		this.mapDescToProp = mapDescToProp;
		setAll();
	}
	
	/**
	 * Register a callback to be invoked when a date is clicked.
	 * @param listener The callback that will run
	 */
	public void setOnDateSelectedListener(OnDateSelectedListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Set the height of every row of the CustomCalendar
	 * @param rowHeight Height of the row
	 */
	public void setRowHeight(float rowHeight) {
		if(rowHeight>0) {
			this.rowHeight = rowHeight;
			setAll();
		}
	}
	
	/**
	 * Register a callback to be invoked when a month navigation button is clicked.
	 * @param whichButton Either {@code CustomCalendar.PREVIOUS} or {@code CustomCalendar.NEXT}
	 * @param listener The callback that will run
	 */
	public void setOnNavigationButtonClickedListener(int whichButton, OnNavigationButtonClickedListener listener) {
		if(whichButton == PREVIOUS) leftButtonListener = listener;
		else if(whichButton == NEXT) rightButtonListener = listener;
	}
	
	/**
	 * Set the enabled state of a month navigation button
	 * @param whichButton Either {@code CustomCalendar.PREVIOUS} or {@code CustomCalendar.NEXT}
	 * @param enable True if the button is enabled, false otherwise.
	 */
	public void setNavigationButtonEnabled(int whichButton, boolean enable) {
		if(whichButton == PREVIOUS) butLeft.setEnabled(enable);
		else if(whichButton == NEXT) butRight.setEnabled(enable);
	}
	
	/**
	 * Returns an array of all the date views.
	 * @return An array of all the date views. (Does not include the disabled previous month and next month views shown for continuity)
	 */
	public View[] getAllViews() {
		return btnAll;
	}
	
}
