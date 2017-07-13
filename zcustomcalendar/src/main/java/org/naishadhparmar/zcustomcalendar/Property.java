package org.naishadhparmar.zcustomcalendar;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * This class represents the property object associated with a description.
 * @author Naishadh Parmar
 * @version 1.0
 * @since 2017-07-14
 */
public class Property {
	
	/**
	 * Resource id for the layout to be inflated.
	 */
	public int layoutResource = -1;
	
	/**
	 * Resource id for the text view within the date view which will be used to display day of month.
	 */
	public int dateTextViewResource = -1;
	
	/**
	 * true if the date view should be enabled, false otherwise.
	 */
	public boolean enable = true;
	
}
