/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Compound.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.quantitation;

import java.io.Serializable;

/**
 * Base class for Compound data stored in Quantitation Report.
 * 
 * @author dale
 * @version $Revision: 3800 $
 */
public abstract class QuantitationCompound
  implements Serializable {
  
  /** for serialization. */
  private static final long serialVersionUID = -4549446314031085148L;

  /** the compound type. */
  public enum Type {
    /** unknown unit. */
    UNKNOWN("unknown"),
    /** microgram/l (= ug/l). */
    UG_PER_L("ug/l"),
    /** nanogram? */
    NG("ng"),
    /** milligram/l (= mg/l). */
    MG_PER_L("mg/l"),
    /** cal. */
    CAL("cal"),
    /** not available. */
    NA("d");
    
    /** the display string of the type. */
    private String m_Display;
    
    /**
     * Initializes the type.
     * 
     * @param display	the string used for displaying
     */
    private Type(String display) {
      m_Display = display;
    }
    
    /**
     * Returns the display string.
     * 
     * @return		the display string
     */
    public String toString() {
      return m_Display;
    }
    
    /**
     * Returns the corresponding type. First tries to parse using the 
     * valueOf method of the Enum class, then going over all the enums 
     * and checking the display string.
     * 
     * @param s		the string to parse
     * @return		the corresponding type or UNKNOWN if not found
     */
    public static Type parse(String s) {
      Type	result;
      
      // try parsing 
      try {
	result = Type.valueOf(s);
      }
      catch (Exception e) {
	result = UNKNOWN;
      }
      
      // try display string
      if (result == UNKNOWN) {
	for (Type t: Type.values()) {
	  if (t.toString().equals(s.toLowerCase())) {
	    result = t;
	    break;
	  }
	}
      }
      
      return result;
    }
    
    /**
     * Returns the corresponding type.
     * 
     * @param id	the ID of the enum (i.e., ordinal())
     * @return		the corresponding type or UNKNOWN if not found
     */
    public static Type valueOf(int id) {
      Type	result;
      
      result = UNKNOWN;
      
      for (Type t: Type.values()) {
	if (t.ordinal() == id) {
	  result = t;
	  break;
	}
      }
      
      return result;
    }
  }
  
  /** number in group e.g 1). */
  protected int m_number;
  
  /** compound name e.g Toluene-d8. */
  protected String m_name;
  
  /** TODO retention time. (of peak?) */
  protected double m_retentionTime;
  
  /** TODO not sure.. -1 = TIC ??? */
  protected int m_QIon;
  
  /** TODO integrated area? */
  protected int m_response;
  
  /** final result. */
  protected double m_concentration;
  
  /** the units value. */
  protected Type m_units; 
  
  /** was manual integration performed? */
  protected boolean m_manual_integration;
  
  /** qualifier out of range? */
  protected boolean m_qual_oor;
  
  /**
   * Return string representation of this object.
   * 
   * @return string 
   */
  public String toString(){
    String res;
    
    res  = "name:" + m_name + ",";
    res += "retent:" + m_retentionTime + ",";
    res += "qion:" + m_QIon + ",";
    res += "resp:" + m_response + ",";
    res += "conc:" + m_concentration + ",";
    res += "units:" + m_units + ",";
    res += "mi:" + m_manual_integration + ",";
    res += "oor:" + m_qual_oor;
    
    return(res);
  }
  
  /**
   * Construct a compound entry with minimum required values.
   * 
   * @param name	the name of the compound
   * @param conc	the concentration
   * @param units	the units
   */
  public QuantitationCompound(String name, double conc, Type units){
    m_name          = name;
    m_concentration = conc;
    m_units         = units;
  }
  
  /**
   * Sets retention time.
   * 
   * @param time	the retention time
   */
  public void setRetentionTime(double time){
    m_retentionTime=time;
  }
  
  /**
   * Gets retention time.
   * 
   * @return	retention time	
   */
  public double getRetentionTime(){
    return(m_retentionTime);
  }
  
  /**
   * Set QIon value.
   * 
   * @param qion	the QIon value
   */
  public void setQIon(int qion){
    m_QIon=qion;
  }
  
  /**
   * Get QIon value.
   * 
   * @return QIon
   */
  public double getQIon(){
    return(m_QIon);
  }
  
  /**
   * Set response.
   * 
   * @param response	the response
   */
  public void setResponse(int response){
    m_response=response;
  }
  
  /**
   * Get response value.
   * 
   * @return response
   */
  public double getResponse(){
    return(m_response);
  }
  
  /**
   * Get unit reference number.
   * 
   * @return	unit reference number
   */
  public Type getUnitValue(){
    return(m_units);
  }
  
  /**
   * Get concentration.
   * 
   * @return	concentration
   */
  public double getConcentration(){
    return(m_concentration);
  }
  
  /**
   * Get name of compound.
   * 
   * @return compound name
   */
  public String getName(){
    return(m_name);
  }
  
  /**
   * Sets if manual integration was used for this compound.
   * 
   * @param m 	manual integration used?
   */
  public void setManual(boolean m){
    m_manual_integration=m;
  }
  
  /**
   * Return if manual integration was used for this compound.
   * 
   * @return manual integration used?
   */
  public boolean getManual(){
    return(m_manual_integration);
  }
  
  /**
   * Set qualifier out of range.
   * 
   * @param q	qualifier out of range?
   */
  public void setQOOR(boolean q){
    m_qual_oor=q;
  }
  
  /**
   * Qualifier out of range?
   * 
   * @return Qualifier out of range?
   */
  public boolean getQOOR(){
    return(m_qual_oor);
  }
  
  /**
   * Get unit from string. Return -1 if not valid.
   * 
   * @param sunit	the string to analyze
   * @return		unit type value
   */
  public static Type getUnit(String sunit) {
    return Type.parse(sunit);
  }
}
