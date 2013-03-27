/**
 * 
 */
package eu.emi.emir.client.util;

/**
 * @author a.memon
 *
 */
public class Duration {
	private int hours = 0;
	private int minutes = 0;
	private int seconds = 0;
	private int days = 0;
	private int months = 0;
	private int years = 0;
	
	/**
	 * @return the hours
	 */
	public int getHours() {
		return hours;
	}
	/**
	 * @param hours the hours to set
	 */
	public void setHours(int hours) {
		this.hours = hours;
	}
	/**
	 * @return the minutes
	 */
	public int getMinutes() {
		return minutes;
	}
	/**
	 * @param minutes the minutes to set
	 */
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	/**
	 * @return the seconds
	 */
	public int getSeconds() {
		return seconds;
	}
	/**
	 * @param seconds the seconds to set
	 */
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
	/**
	 * @return the days
	 */
	public int getDays() {
		return days;
	}
	/**
	 * @param days the days to set
	 */
	public void setDays(int days) {
		this.days = days;
	}
	/**
	 * @return the months
	 */
	public int getMonths() {
		return months;
	}
	/**
	 * @param months the months to set
	 */
	public void setMonths(int months) {
		this.months = months;
	}
	/**
	 * @return the years
	 */
	public int getYears() {
		return years;
	}
	/**
	 * @param years the years to set
	 */
	public void setYears(int years) {
		this.years = years;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		
		if (years > 0) {
			bf.append(years+" years ");
			if (months > 0) {
				bf.append(months+" months ");
			}
			return bf.toString().trim();
			
		} else if (months > 0){
			bf.append(months+" months ");
			if (hours > 0) {
				bf.append(hours+" hours ");
			}
			return bf.toString().trim();
			
		} else if (hours > 0) {
			bf.append(hours+" hours ");
			if (minutes > 0) {
				bf.append(minutes+" minutes ");
			}
			return bf.toString().trim();
		} else if (minutes > 0) {
			bf.append(minutes+" minutes ");
			return bf.toString().trim();
		}
		
		return bf.toString().trim();
	}
	
	
}
