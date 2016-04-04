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
 * FossStdise.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input.foss;

import adams.core.IEEE754;
import adams.core.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

/**
 * Load, and perform standardisation/unstandardisation from foss standardisation file
 * NOTE: Only does slope and intercept standardisation
 * NOTE 2: Established by looking at .std files with hex editor. Use with caution  
 * 
 * @author dale
 * @version $Revision: 2237 $
 */
public class FossStdise {
  
  /** number of wavelengths. */
  protected static int datapoint_offset=0x06; // number of nir wavelengths
  
  /** start of segment data. */
  protected static int segments_offset=0x280;
  
  /** start of intercepts. */
  protected static int delta_offset_2segments=0x2b4; // start of deltas
  protected static int delta_offset_1segment=0x2a0; // start of deltas
   
  /** raw bytes of file. */
  private byte[] m_bytes=null;
  
  /** number of wavelengths. */
  protected int m_num_points;
   
  /** 
   * Constructor.
   * 
   * @param bytes	byte array of .std file
   */
  public FossStdise(byte[] bytes){
    m_bytes=bytes;
  } 

  /**
   * Get start of segments data.
   * 
   * @return	segments start
   */
  public int getSegmentsOffset(){
    int offset=-1;
    int segs=(int)m_bytes[segments_offset];
    switch(segs){
      case 1:
	offset=delta_offset_1segment;
	break;
      case 2:
	offset=delta_offset_2segments;
	break;
      default:
	break;  	  
    }
    return(offset);
  }
  
  /**
   * Get start of intercept data.
   * 
   * @return	intercept start
   */
  public int getDeltaOffset(){
    int offset=-1;
    int segs=(int)m_bytes[segments_offset];
    switch(segs){
      case 1:
	offset=delta_offset_1segment;
	break;
      case 2:
	offset=delta_offset_2segments;
	break;
      default:
	break;  	  
    }
    return(offset+((getNumDatapoints()*4)+16));
  }
  
  
  /**
   * Testing: output raw bytes.
   * 
   * @param bytes
   * @return
   */
  public String rawDump(double[] bytes){
    if (bytes==null){
      return("NULL");
    }
    String ret="";
    for (int i=0;i<bytes.length;i++){
      if (i==bytes.length-1){
	ret+=bytes[i];
      }else {
	ret+=bytes[i]+",";
      }
    }
    return(ret);
  }
  
  /**
   * Testing: Dump array is iee754 encoded ints.
   * 
   * @param bytes
   * @return
   */
  public String iee754Dump(double[] bytes){
    if (bytes==null){
      return("NULL");
    }
    String ret="";
    for (int i=0;i<bytes.length;i++){
      long l=(long)bytes[i];
      double conv = Float.intBitsToFloat(IEEE754.longToIntBits(l));
      String ad=""+conv;
      if (Double.isNaN(conv)){
	ad="NaN";
      }
      if (i==bytes.length-1){	
	ret+=ad;
      }else {
	ret+=ad+",";
      }
    }
    return(ret);
  }
  
  /**
   * Get start of coefficents.
   * 
   * @return	coefficient offset
   */
  public int getCoefficientOffset(){
    return(getDeltaOffset()+(getNumDatapoints()*4));
  }
  
  /**
   * Get array of values starting from given offset.
   * Size is number of wavelengths
   * 
   * @param i	start from
   * @return	double array
   */
  public double[] getDataFromOffset(int i){
    int offset=i;
    m_num_points=this.getNumDatapoints();
    double[] ret=new double[m_num_points];
    
    //Log.log(Level.FINE,"datastart="+datastart+" & length="+file_image.length);
    try{
      for (int count=0;count<m_num_points;count++){
	long n=convertToLong(m_bytes,offset+(count*4));
	ret[count]=(double)n;	
      }
      
    }catch(Exception e){
      System.err.println("Error reading NIR values."+e.toString());
      return(null);
    }
    return(ret);
  }
  
  /**
   * standardise nir data according to coefficients and intercepts taken from .std file.
   *  
   * @param input	byte array to standardise
   * @param fs		FossStdise
   * @return		standardised data
   */
  static public double[] standardise(double[] input, FossStdise fs){
    if (input == null){
      return(null);
    }
    double[] ret=new double[input.length];
    double[] coeff=fs.getCoefficients();
    double[] incpts=fs.getIntercepts();
    
    if (coeff==null || incpts==null || coeff.length != input.length || incpts.length != input.length){
      return(null);
    }
    for (int i=0;i<input.length;i++){
      ret[i]=(input[i]*coeff[i])+incpts[i];
    }
    return(ret);
  }
  
  /**
   * Unstandardise nir data according to coefficients and intercepts taken from .std file.
   *  
   * @param input	byte array to unstandardise
   * @param fs		FossStdise
   * @return		unstandardised data
   */
  static public double[] unstandardise(double[] input, FossStdise fs){
    if (input == null){
      return(null);
    }
    double[] ret=new double[input.length];
    double[] coeff=fs.getCoefficients();
    double[] incpts=fs.getIntercepts();
    
    if (coeff==null || incpts==null || coeff.length != input.length || incpts.length != input.length){
     // Log.log(Level.WARNING,"Problem getting standardisation data. Check .std file matches .cal file");
      return(null);
    }
    for (int i=0;i<input.length;i++){
      ret[i]=(input[i]-incpts[i])/coeff[i];
    }
    return(ret);
  }
  
  /**
   * Get array of bytes from file.
   * 
   * @param f	file
   * @return	byte array
   */
  static public byte[] getFileAsBytes(File f){
    BufferedInputStream bis=null;
    FileInputStream fis = null;
    ByteArrayOutputStream bytesIn=null;    
    try {
      fis = new FileInputStream(f);
      bis=new BufferedInputStream(fis);
      bytesIn=new ByteArrayOutputStream();
      int ch;
      while ((ch=bis.read())!=-1){
	bytesIn.write(ch);
      }
    }catch(Exception e){
      return(null);
    }
    finally {
      FileUtils.closeQuietly(bis);
      FileUtils.closeQuietly(fis);
    }
    return(bytesIn.toByteArray());
  }

  /**
   * Get coefficients as array.
   * 
   * @return	coefficients
   */

  public double[] getCoefficients(){
    //m_num_points=this.getNumDatapoints();
    return(IEEE754.toDoubleArray(getDataFromOffset(this.getCoefficientOffset())));
    
  }
  
  /**
   * Get intercepts as array.
   * 
   * @return	intercepts
   */
  public double[] getIntercepts(){
    return(IEEE754.toDoubleArray(getDataFromOffset(this.getDeltaOffset())));
  }
  
  /**
   * Get segments as array. TODO: Not currently processed!!
   * 
   * @return	segments array
   */
  public double[] getSegs(){
    return(IEEE754.toDoubleArray(getDataFromOffset(this.getSegmentsOffset())));
  }
  
  /**
   * Get number of nir wavelengths.
   * 
   * @return	num wavelengths
   */
  public int getNumDatapoints(){
    return((int)longFrom2Bytes(m_bytes,FossHelper.datapoint_offset));
  }
    
  /**
   * Convert byte to unsigned byte.
   * 
   * @param by	byte
   * @return	unsigned byte
   */
  protected  int byte2UByte(byte by){
    int k = by;
    if (k < 0){
      k=k+256;
    }
    return(k);
  }

  /**
   * Convert 4 bytes to long. LSByte first
   * @param b		byte array
   * @param offset	starting pos
   * @return		long
   */
  protected long convertToLong(byte[] b,int offset){
    long ret=(long)byte2UByte(b[offset]);
    ret=ret+((long)(byte2UByte(b[offset+1]))) *  256;
    ret=ret+((long)(byte2UByte(b[offset+2]))) *  65536;
    ret=ret+((long)(byte2UByte(b[offset+3]))) *  16777216;
    return(ret);
  }

  /**
   * Get long from 2 bytes LSByte first.
   * 
   * @param b		byte array
   * @param offset	start in array
   * @return		long
   */
  protected long longFrom2Bytes(byte[] b,int offset){
    long ret=(long)byte2UByte(b[offset]);
    ret=ret+((long)(byte2UByte(b[offset+1]))) *  256;
    return(ret);
  }

  /**
   * TEST
   * args0 foss std file to read.
   * 
   * @param args	commandline arguments
   * @throws Exception	if something goes wrong
   */
  public static void main(String[] args) throws Exception {
    String name=args[0];
    File f=new File(name);      
    System.out.println("Loading:"+args[0]);
    try {               
      byte[] bytes=FossStdise.getFileAsBytes(f);
      FossStdise fh=new FossStdise(bytes);
      double[] segs=fh.getSegs();
      double[] offs=fh.getIntercepts();
      double[] coeffs=fh.getCoefficients();
      System.out.println(fh.rawDump(segs));
      double[] t=IEEE754.toIntBitsArray(segs);
      double[] back=IEEE754.toDoubleArray(t);
      System.out.println(fh.rawDump(back));
      System.out.println(fh.rawDump(coeffs));
      String charsetName = "UTF-8";
      Scanner scanner = new Scanner(System.in, charsetName);
      while (true){
	String s=scanner.nextLine();
	Integer i=Integer.parseInt(s);
	double[] d=fh.getDataFromOffset(i);
	for (int j=0;j<d.length;j++){
	  long l=(long)d[j];
	  double conv = Float.intBitsToFloat(IEEE754.longToIntBits(l));
	  if (Double.isNaN(conv)){	  
	    System.err.println("Encountered non-IEEE 754 floating-point value");
	    //Log.log(Level.WARNING,row[i]+","+(int)row[i]+","+(long)row[i]);
	    //return(null);	   
	    System.out.print("NAN,");
	  } else {
	    System.out.print(conv+",");
	  }
	}
	for (int j=0;j<d.length;j++){
	  //System.out.print(d[j]+",");
	}
	System.out.println(d.length);
      }
    }catch(Exception e){
      System.err.println("cannot process:"+name);
      try{
      }catch(Exception e2){

      }      
    }

  }
}
