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
 * FossOutputHelper.java
 * Copyright (C) 2009-2010 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output.foss;

import adams.core.IEEE754;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Classes and Methods for creating a byte array of a FOSS .nir or .cal file.
 *
 * @author dale
 *
 */
public class FossOutputHelper {

  /**
   * General Header.
   * @author dale
   *
   */
  public static class Generalheader{
    /** 2 BYTES: Use 1st BYTE. 01=.NIR, 02=.CAL. */
    public int m_type;
    /** 2 BYTES: number of (non deleted) spectra. */
    public int m_count;
    /** 2 BYTES: number of (deleted) spectra. */
    public int m_deleted;
    /** 2 BYTES:number of spectral data points. */
    public int m_num_points;
    /** 2 BYTES: number of constituents. */
    public int m_num_consts;
    /** 2 BYTES: unsigned int. Creation date. */
    public Date m_creation_date;
    /** 4 BYTES: long int. Time. */
    public Date m_time;
    /** 2 BYTES: CHK file? Use 00. */
    public int m_most_recent;
    /** char[71] file id. */
    public String m_file_id;
    /** char[9] master serial no.*/
    public String m_master;
    /** char[30] packing info. */
    public String m_packing="";

    /**
     * Get byte array of General Header.
     * @return	byte array.
     */
    public byte[] getBytes(){
      byte[] ret= new byte[128];// header 128
      putIntLittleEndian(ret,0,m_type);
      putIntLittleEndian(ret,2,m_count);
      putIntLittleEndian(ret,4,m_deleted);
      putIntLittleEndian(ret,6,m_num_points);
      putIntLittleEndian(ret,8,m_num_consts);
      putDate(ret,10, m_creation_date);
      putTime(ret,12, m_time);
      putIntLittleEndian(ret,16,m_most_recent);
      putZeroTerminatedString(ret,18,m_file_id,71);
      putZeroTerminatedString(ret,89,m_master,9);
      putZeroTerminatedString(ret,89+9,m_packing,30);
      return(ret);
    }
  }

  /**
   * Instrument Header class.
   * @author dale
   *
   */
  public static class InstrumentHeader{
    /**
     * Type of FOSS instrument.
     * @author dale
     *
     */
    public enum InstrumentType{
      SER_4250(0),
      SER_51A(1),
      SIC_4250(2),
      SIC_6250(3),
      SIC_6250V(4),
      PARALLEL_6250(5),
      PARALLEL_6250V(6),
      BL_500(7),
      BL_400(8),
      SIC_6500(9),
      SIC_5500(10),
      SIC_5000(11),
      SIC_4500(12),
      INFRATEC(13);
      private final int m_code;

      /**
       * Instrument Type.
       * @param code	map code to instrument type
       */
      InstrumentType(int code){
	m_code=code;
      }

      /**
       * Get file code for instrument type.
       * @return code
       */
      public int getCode(){
	return(m_code);
      }
    }
    /** 2 BYTES: Instrument type. */
    public InstrumentType m_instrument_type;
    /** char[21] model number. */
    public String m_model;
    /** char[9] serial number. */
    public String m_serial;
    /** 2 BYTES integer, number of segments up to 20 (?). */
    public int m_num_seg;
    /** 40 BYTES int[20], points per sement.. ? */
    public int[] m_points_per_segment;
    /** 2 BYTES int spacing mode: 00=TILFIL, 01=EQUALSPC, 02=FILFIL, 03=SIN. */
    public int m_spacing_mode;
    /** start float[7], inc float[7], end float[7] but looking at */
    public float[] m_wave; // 21 floats
    /** 2 BYTES int, number of EOC's (??) use 4400?. */
    public int m_neoc;
    /** 94 bytes of padding. */
    public byte[] m_padding;
    /** 32 * 16 chars, null terminated constituent names. */
    public String[] m_constituents;

    /**
     * Get byte array of Instrument Header.
     * @return	byte array.
     */
    public byte[] getBytes(){
      byte[] ret= new byte[256+512];// header 256 + constituents 512
      putIntLittleEndian(ret,0,m_instrument_type.getCode());
      putZeroTerminatedString(ret,2,m_model,21);
      putZeroTerminatedString(ret,23,m_serial,9);
      putIntLittleEndian(ret,32,m_num_seg);
      for (int i=0;i<20;i++){
	putIntLittleEndian(ret,34+(2*i),m_points_per_segment[i]);
      }
      putIntLittleEndian(ret,74,m_spacing_mode);
      for (int i=0;i<21;i++){
	putFloatLittleEndian(ret,76+(4*i),m_wave[i]);
      }
      putIntLittleEndian(ret,160,m_neoc);
      putZeroTerminatedString(ret,162,"",94); //padding
      for (int i=0;i<32;i++){
	putZeroTerminatedString(ret,162+94+(i*16),m_constituents[i],16);
      }
      return(ret);
    }
  }

  /**
   * Sample Header.
   * @author dale
   *
   */
  public static class SampleHeader{
    /** String(13): zero terminated. Sample name. */
    public String m_sample_no;
    /** 2 BYTE? Sequence number in file. */
    public int m_sequence;
    /** 1 BYTE?? Deleted? */
    public boolean m_deleted;
    /** 2 BYTES: unsigned int. date. */
    public Date m_date;
    /** 2 BYTES: unsigned int. code. */
    public int m_product_code;
    /** String(9): zero terminated. Client. */
    public String m_client;
    /** String(151): zero terminated. Sample id. (divided into 3?)*/
    /** SampleID #1. */
    public String m_sample_id1;
    /** SampleID #2. */
    public String m_sample_id2;
    /** SampleID #3. */
    public String m_sample_id3;
    /** String(32): zero terminated. operator*/
    public String m_operator;
    /** 2 BYTE?? Standardised? */
    public int m_standardised;
    /** sample time.*/
    public Date m_time;
    /** 38 bytes of padding. */
    public byte[] m_padding;

    /**
     * Get byte array for sample header.
     * @return	byte array.
     */
    public byte[] getBytes(){
      byte[] ret= new byte[256];
      putZeroTerminatedString(ret,0,m_sample_no,13);
      putIntLittleEndian(ret,13,m_sequence);
      putBooleanLittleEndian(ret,15,m_deleted);
      putDate(ret,16,m_date);
      putIntLittleEndian(ret,18,m_product_code);
      putZeroTerminatedString(ret,20,m_client,9);
      putZeroTerminatedString(ret,29,m_sample_id1,51); // overlap as 1&2 not 0 terminated
      putZeroTerminatedString(ret,79,m_sample_id2,51);
      putZeroTerminatedString(ret,129,m_sample_id3,51);
      putZeroTerminatedString(ret,180,m_operator,32);
      putIntLittleEndian(ret,180+32,m_standardised);
      putTime(ret,180+32+2,m_time);
      putZeroTerminatedString(ret,182+36,"",38);
      return(ret);
    }
  }

  /**
   * Sample Info.
   * @author dale
   *
   */
  public static class SampleInfo{
    /** String(13): zero terminated. Sample name. */
    public String m_sample_id;
    /** 2 BYTE? Sequence number in file. */
    public int m_sequence;
    /** 1 BYTE? Deleted? */
    public boolean m_deleted;

    /**
     * Get byte array for sample info.
     * @return	byte array.
     */
    public byte[] getBytes(){
      byte[] ret= new byte[16];
      putZeroTerminatedString(ret,0,m_sample_id,13);
      putIntLittleEndian(ret,13,m_sequence);
      putBooleanLittleEndian(ret,15,m_deleted);
      return(ret);
    }
  }


  /**
   * Data Block.
   * @author dale
   *
   */
  public static class DataBlock{
    //public Vector<Float> m_data;
    // size of: numpoints * 4

    /** The Spectrum. */
    public Spectrum m_Spectrum;

    /**
     * Get byte array for sample block.
     * @return	byte array.
     */
    public byte[] getBytes(){

      int size=(m_Spectrum.size()*4);
      int a = (int)(Math.ceil((double)size/128.0)*128.0);
      int diff = a-size;

      byte[] data=new byte[diff+m_Spectrum.size()*4];
      int i=0;
      for (SpectrumPoint sp:m_Spectrum){
	byte[] bytes=IEEE754.floatToIntBitsLittleEndian(sp.getAmplitude());
	data[i*4] = bytes[3];
	data[i*4+1] = bytes[2];
	data[i*4+2] = bytes[1];
	data[i*4+3] = bytes[0];
	i++;
      }
      return(data);
    }
  }

  /**
   * Constituent (or reference) values.
   * @author dale
   *
   */
  public static class ConstituentValues{
    /** the constituent values. */
    public Float[] m_Constituents=null;

    /**
     * Get byte array for the constituent values.
     * @return	byte array.
     */
    public byte[] getBytes(){
      byte[] data = new byte[128];
      if (m_Constituents!=null){
	if (m_Constituents.length > 32){
	  System.err.println("More than 32 constituents specified. Using 32");
	}
	for (int i=0;i<Math.min(32,m_Constituents.length );i++){
	  putFloatLittleEndian(data,i*4,m_Constituents[i]);
	}
      }
      return(data);
    }
  }

  /**
   * Put bytes of integer into array, Little Endian.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param num		integer
   */
  public static void putIntLittleEndian(byte[] data, int pos, int num){
    data[pos]=(byte)(num & 0xff);
    num >>=8;
    data[pos+1]=(byte)(num & 0xff);
  }

  /**
   * Put byte of boolean into array. True=1, False=0.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param b		boolean
   */
  public static void putBooleanLittleEndian(byte[] data, int pos, boolean b){
    if (b){
      data[pos]=1;
    } else {
      data[pos]=0;
    }
  }
  /**
   * Put String into array, terminate string with 0.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param srt		string
   * @param max_size	maximum size of string
   */
  public static void putZeroTerminatedString(byte[] data, int pos, String srt, int max_size){
    for (int i=0;i<max_size;i++){
      if (i < srt.length()){
	data[pos+i]=(byte)srt.charAt(i);
      } else {
	data[pos+i]=0;
      }
    }
    data[pos+max_size-1]=0;
  }

  /**
   * Put bytes of float into array, Little Endian, IEEE754.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param f		float
   */
  public static void putFloatLittleEndian(byte[] data, int pos, float f){
    byte[] ib=IEEE754.floatToIntBitsLittleEndian(f);
    data[pos]=ib[3];
    data[pos+1]=ib[2];
    data[pos+2]=ib[1];
    data[pos+3]=ib[0];
  }

  /**
   * Put bytes of date into array, Little Endian.
   * Format is:
   *
   * Least Significant Byte (MMMDDDDD)
   * Most Significant Byte  (YYYYYYYM)
   *
   * Year is: years since 1980.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param date	date
   */
  public static void putDate(byte[] data, int pos, Date date){
    int year=date.getYear()+1900;
    int month=date.getMonth()+1;
    int day=date.getDate();

    int lsb=day;
    lsb+=month<<5;
    data[pos] = (byte)(lsb & 0xff);

    int msb=month>>3;
    msb += ((year-1980) << 1);
    data[pos+1] = (byte)(msb & 0xff);
  }

  /**
   * Put bytes of date into array, Little Endian.
   * Format is:
   *
   * Least Significant Byte (MMMDDDDD)
   * Most Significant Byte  (YYYYYYYM)
   *
   * Year is: years since 1980.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param date	date
   */
  public static void putDate(byte[] data, int pos, Calendar date){
    int year=date.get(Calendar.YEAR);
    int month=date.get(Calendar.MONTH)+1;
    int day=date.get(Calendar.DATE);

    int lsb=day;
    lsb+=month<<5;
    data[pos] = (byte)(lsb & 0xff);

    int msb=month>>3;
    msb += ((year-1980) << 1);
    data[pos+1] = (byte)(msb & 0xff);
  }

  /**
   * Put bytes of time (in seconds since epoch) into array, Little Endian.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param time	Calendar time
   */
  public static void putTime(byte[] data, int pos, Calendar time){
    long mills=time.getTimeInMillis()/1000;
    data[pos]=(byte)(mills & 0xff); mills >>= 8;
    data[pos+1]=(byte)(mills & 0xff); mills >>= 8;
    data[pos+2]=(byte)(mills & 0xff); mills >>= 8;
    data[pos+3]=(byte)(mills & 0xff); mills >>= 8;
  }

  /**
   * Put bytes of time (in seconds since epoch) into array, Little Endian.
   *
   * @param data	byte array
   * @param pos		start position in array
   * @param time	Calendar time
   */
  public static void putTime(byte[] data, int pos, Date time){
    long mills=time.getTime()/1000;
    data[pos]=(byte)(mills & 0xff); mills >>= 8;
    data[pos+1]=(byte)(mills & 0xff); mills >>= 8;
    data[pos+2]=(byte)(mills & 0xff); mills >>= 8;
    data[pos+3]=(byte)(mills & 0xff); mills >>= 8;
  }

  /**
   * Output byte array in hex form.
   * @param bytes	byte array
   * @return		String of bytes in hex.
   */
  public static String showBytes(byte[] bytes){
    String ret="";
    for (int i=0;i<bytes.length;i++){
      ret += Integer.toHexString(byte2UByte(bytes[i]))+ " ";
      if ((i+1) % 16 == 0){
	ret+="\n";
      }
    }
    return(ret);
  }

  public static void main(String argc[]){
    byte[] b=new byte[4];
    putFloatLittleEndian(b,0,400.00f);
    System.err.println(showBytes(b));

    Spectrum sp=new Spectrum();
    sp.add(new SpectrumPoint(1,2));
    sp.add(new SpectrumPoint(2,3));
    sp.add(new SpectrumPoint(3,4));
    Vector<Spectrum> vs = new Vector<Spectrum>();
    vs.add(sp);
    //byte[] bs=toBytes(vs);
    //System.err.println(showBytes(bs));
    //putIntLittleEndian(b,0,1050);
    //System.err.println(showBytes(b));
  }
  /**
   * Convert byte to unsigned byte.
   *
   * @param by	byte
   * @return	unsigned byte
   */
  protected  static int byte2UByte(byte by){
    int k = by;
    if (k < 0){
      k=k+256;
    }
    return(k);
  }


}
