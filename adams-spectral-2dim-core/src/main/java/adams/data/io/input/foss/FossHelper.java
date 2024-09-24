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
 * FossHelper.java
 * Copyright (C) 2009-2024 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input.foss;

import adams.core.IEEE754;
import adams.core.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for handling Foss cal files. Info used here is from studying
 * .cal files with hex editor. Has been tested on many .cal files, but
 * no guarantees.
 *
 * @author dale
 */
public class FossHelper {

  // offsets for header (from 0)
  /** number of Rows in this file. */
  protected static int count_offset=0x02;

  /** number of Rows in this file. */
  protected static int deleted_offset=0x04;

  /** number of nir wavelengths. */
  protected static int datapoint_offset=0x06;

  // reference names
  protected static int reference_count=0x08;
  protected static int reference_offset=0x180;
  protected static int reference_name_width=0x10;
  protected static int reference_name_num=32;
  
  protected static int NSeg_offset=128+32;
  protected static int Npps_offset=NSeg_offset+2;
  protected static int WaveType_offset=Npps_offset+40;
  protected static int Wave_offset=WaveType_offset+2;

  /** data header end. */
  protected static int head_end=0x380;

  /** raw bytes of file .cal. */
  private byte[] m_bytes=null;

  /** number of (non deleted) spectra. */
  protected int m_count;

  /** number of spectral data points. */
  protected int m_num_points;

  /** reference names. */
  protected List<String> m_ref_names;

  /** number of reference values per spectra. */
  protected int m_ref_count;

  /** size of spectrum header (static). */
  protected int m_spectra_head_size=0x100;

  /** size of spectra data. */
  protected int m_spectra_size;

  /** size of spectrum tail (static). */
  protected int m_spectra_tail_size=8*16;

  /** number of deleted rows in this file. */
  protected int m_deleted=0;

  /**
   * Stores non-spectral data for a row.
   *
   * @author dale
   */
  public class FossFields {
    /** ID field. */
    public String id;

    /** product code field. */
    public int product_code;

    /** other ID fields. */
    public String id1,id2,id3;

    /** is this a deleted row? */
    public boolean deleted=false;

    /** row number in file. */
    public int count;

    /** number of deleted rows in previous spectra. */
    private int num_deleted; // number of prevous rows deleted

    /**
     * Constructor.
     * Fills in data fields from parent byte array.
     *
     * @param i		count. Row position in file
     */
    public FossFields(int i){
      count=i;
      byte[] bytes=FossHelper.this.m_bytes;
      int offset=(FossHelper.this.getBlockSize()*i)+FossHelper.head_end;
      id=getZeroTerminatedString(offset);
      product_code=(int)bytes[offset+18];
      id1=getZeroTerminatedString(offset+29);
      id2=getZeroTerminatedString(offset+79);
      id3=getZeroTerminatedString(offset+129);
      if (bytes[offset+15]!=0){
	deleted=true;
      }
    }

    /**
     * Sets the number of previously deleted.
     *
     * @param d		num deleted
     */
    public void setNumDeleted(int d){
      num_deleted=d;
    }

    /**
     * Get actual (non-deleted)m row number.
     *
     * @return	row num
     */
    public int getRowNum(){
      return((count+1)-num_deleted);
    }

    /**
     * String representation of this.
     *
     * @return string
     */
    @Override
    public String toString(){
      StringBuilder sb=new StringBuilder();
      String del="";
      if (deleted){
	del=" DELETED";
      }
      sb.append("id=");
      sb.append(id);
      sb.append(", code=");
      sb.append(product_code);
      sb.append(",id1=");
      sb.append(id1);
      sb.append(",id2=");
      sb.append(id2);
      sb.append(",id3=");
      sb.append(id3);
      sb.append(del);
      return(sb.toString());
    }

  }

  /**
   * Constructor.
   *
   * @param bytes	byte array of cal file
   */
  public FossHelper(byte[] bytes){
    m_bytes=bytes;
  }

  /**
   * Get the non-spectral data for a given row.
   *
   * @param i	row number
   * @return
   */
  public FossFields getFields(int i){
    return(new FossFields(i));
  }

  /**
   * Get spectrum of row.
   *
   * @param i	row num
   * @return	spectrum as array
   */
  public double[] getSpectraForRow(int i){
    int offset=getSpectraOffsetForRow(i);
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
   * Get reference values for a row.
   * Sorted as per getRefNames()
   * @param i		row num
   * @return		array of reference values
   */
  public float[] getRefForRow(int i){
    int offset=getRefOffsetForRow(i);
    float[] ret=new float[m_ref_count];
    try{
      for (int count=0;count<m_ref_count;count++){
	int n=(int)convertToLong(m_bytes,offset+(count*4));
	Float f=Float.intBitsToFloat(n);
	if (f.isNaN()){
	  System.err.println("NAN for iee754:"+i);
	}
	ret[count]=Float.intBitsToFloat(n);
      }
    }catch(Exception e){
      System.err.println("Error reading REF values."+e.toString());
      return(null);
    }
    return(ret);
  }

  /**
   * Get block size of a row in bytes.
   *
   * @return	size of all data for a row
   */
  public int getBlockSize(){
    return(m_spectra_head_size+m_spectra_size+m_spectra_tail_size);
  }

  /**
   * Get size of the spectral data block. (taking account of padding)
   * @return	size of the spectral data block
   */
  protected int getSpectraBlockSize(){
    //spectra block
    int sp=m_num_points*4;
    int div=sp/128;
    if (div*128 != sp){
      div++;
      sp=div*128;
    }
    return(sp);
  }

  /**
   * Get start of spectrum for given row.
   *
   * @param i	row number
   * @return	spectrum start
   */
  public int getSpectraOffsetForRow(int i){
    return(head_end+(getBlockSize()*i)+m_spectra_head_size);
  }

  /**
   * Get start of reference for given row.
   *
   * @param i	row number
   * @return	ref values start
   */
  public int getRefOffsetForRow(int i){
    return(getSpectraOffsetForRow(i)+this.m_spectra_size);
  }

  /**
   * Process the data from the cal file header.
   *
   * @return true
   */
  public boolean processHeader(){
    m_count=getCount();
    //System.out.println("count="+m_count);
    m_num_points=getNumDatapoints();
    //System.out.println("datapoints="+m_num_points);
    m_ref_count=getRefCount();
    //System.out.println("ref count="+m_ref_count);
    m_ref_names=getReferenceNames();
    m_spectra_size=getSpectraBlockSize();
    //System.out.println("block size="+getBlockSize());
    //System.out.println("spectra block size="+getSpectraBlockSize());

    //System.out.println("1st start:"+(m_spectra_head_size+head_end));
    m_deleted=countDeleted();
    //System.out.println("deleted="+m_deleted);
    
  /*  int tp=getSegmentsType();
    System.out.println("type="+tp);
    
    int num=getNumSegments();
    System.out.println("numsegments="+num);
    
    double[] starts=getSegmentStart();
    for (int i=0;i<starts.length;i++){
    	System.err.println(starts[i]+" ");
    }
    System.err.println();
    
    double[] ends=getSegmentEnd();
    for (int i=0;i<ends.length;i++){
    	System.err.println(ends[i]+" ");
    }
    System.err.println();
    
    double[] inc=getSegmentInc();
    for (int i=0;i<inc.length;i++){
    	System.err.println(inc[i]+" ");
    }
    System.err.println();
    
    int[] p=getSegmentLengths();
    for (int i=0;i<p.length;i++){
    	System.err.println(p[i]+" ");
    }
    System.err.println();    
    
    double[] wn=getWavenumbers();
    for (int i=0;i<wn.length;i++){
    	System.err.println(wn[i]+" ");
    }
    System.err.println();*/
    
    return(true);
  }

  /**
   * Total number of rows, including deleted rows.
   *
   * @return	total num rows
   */
  public int getTotal(){
    return(m_count+m_deleted);
  }

  /**
   * How many deleted rows?
   * @return deleted rows?
   */
  public int countDeleted(){
    return((int)longFrom2Bytes(m_bytes,FossHelper.deleted_offset));
  }

  /**
   * number of reference values.
   *
   * @return ref count
   */
  public int getRefCount(){
    int count=(int)m_bytes[reference_count];
    return(count);
  }

  
  /**
   * Get segment type (00=TILFIL, 01=EQLSPC, 02=FILFIL, 03=SINSPA
   * @return type
   */
  public int getSegmentsType(){
	  return((int)longFrom2Bytes(m_bytes,FossHelper.WaveType_offset));
  }
  
  /** 
   * get number of segments 
   * @return num segments
   * 
   */
  public int getNumSegments(){
	  return((int)longFrom2Bytes(m_bytes,FossHelper.NSeg_offset));
  }
  
  public int[] getSegmentLengths(){
	  int[] segments=new int[20];
	  int start=FossHelper.Npps_offset;
	  for (int i=0;i<20;i++){
		  segments[i]=(int)longFrom2Bytes(m_bytes,start+(i*2));
	  }
	  return(segments);
  }
  
  public double[] getSegmentStart(){
	double[] starts=new double[7];
	int start=FossHelper.Wave_offset;
	 for (int i=0;i<7;i++){
		  starts[i]=(double)convertToLong(m_bytes,start+(i*4));
	  }
	 
	double[] nir=IEEE754.toDoubleArray(starts);
	return(nir);
  }
  
  public double[] getSegmentInc(){
	  double[] incs=new double[7];
	  
	  int start=FossHelper.Wave_offset+28;
		 for (int i=0;i<7;i++){
			  incs[i]=(double)convertToLong(m_bytes,start+(i*4));
		  }
		 
		double[] nir=IEEE754.toDoubleArray(incs);
		return(nir);
	  
  }
  
  
  public double[] getSegmentEnd(){
	  double[] ends=new double[7];
	  int start=FossHelper.Wave_offset+56;
		 for (int i=0;i<7;i++){
			  ends[i]=(double)convertToLong(m_bytes,start+(i*4));
		  }
		 
		double[] nir=IEEE754.toDoubleArray(ends);
		return(nir);
  }
  
  public double[] getWavenumbers(){
	  int[] segl=getSegmentLengths();
	  double[] starts=getSegmentStart();
	  double[] incs=getSegmentInc();
	  
	  if (getSegmentsType() != 1){ //can only handle EQLSPA
		  return(null);
	  }
	  
	  int tot=0;
	  for (int i=0;i<segl.length;i++){
		  tot+=segl[i];
	  }
	  double[] wavenumbers=new double[tot];
	  int pos=0;
	  for (int seg=0;seg<segl.length;seg++){
		  for(int j=0;j<segl[seg];j++){
			  wavenumbers[pos]=starts[seg]+(j*incs[seg]);
			  pos++;
		  }
	  }
	  return(wavenumbers);
  }
  
  /**
   * Get names of references.
   *
   * @return	reference names
   */
  public List<String> getReferenceNames(){
    List<String> vs=new ArrayList<>();
    int from=reference_offset;
    for (int i=0;i<reference_name_num;i++){
      String s=getZeroTerminatedString(from);
      if (s!=null && !s.isEmpty())
	vs.add(s);
      from+=reference_name_width;
    }
    return(vs);
  }

  /**
   * Build string until reaching zero termination.
   *
   * @param offset	pos in file to start
   * @return		string
   */
  protected String getZeroTerminatedString(int offset){
    StringBuilder sb=new StringBuilder();
    while (m_bytes[offset] != 0){
      sb.append((char)m_bytes[offset++]);
    }
    return(sb.toString());
  }

  /**
   * Number of non-deleted rows.
   *
   * @return num rows
   */
  public int getCount(){
    return((int)longFrom2Bytes(m_bytes,FossHelper.count_offset));
  }

  /**
   * Number of spectral data points.
   *
   * @return	num data points
   */
  public int getNumDatapoints(){
    return((int)longFrom2Bytes(m_bytes,FossHelper.datapoint_offset));
  }

  /**
   * TESTING.
   *
   * @param bytes
   * @param start
   * @param windowsize
   */
  private void logWindow(byte[] bytes, int start,int windowsize){
    String out="";
    for (int i=start;i<start+windowsize;i++){
      out+=bytes[i];
      if (i!= start+windowsize-1){
	out+=",";
      }
    }
    System.err.println(out);
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
   * Convert 4 bytes to long. LSByte first.
   *
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
   * Test.
   *
   * arg0 cal filename
   *
   * @param args	commandline arguments
   * @throws Exception	if somethin goes wrong
   */
  public static void main(String[] args) throws Exception {
    String name=args[0];
    File f=new File(name);
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
      FossHelper fh=new FossHelper(bytesIn.toByteArray());
      fh.processHeader();

      for (int i=0;i<fh.getTotal();i++){
	FossFields ff=fh.getFields(i);
	System.out.println(ff.toString());
	float[] d=fh.getRefForRow(i);
	for (int j=0;j<d.length;j++){
	  System.out.print(d[j]+" ");
	}
	System.out.println();
      }

    }catch(Exception e) {
      System.err.println("cannot process:" + name);
    }
    finally {
      FileUtils.closeQuietly(bis);
      FileUtils.closeQuietly(fis);
    }
  }
}
