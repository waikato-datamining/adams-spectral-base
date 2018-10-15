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
 * OpusBlockHelper.java
 * Copyright (C) 2016-2018 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input.opus;

import adams.core.MessageCollection;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.logging.LoggingObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Analyzer for Opus file blocks in the header.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class OpusBlockHelper {

  /**
   * Container class for Opus block definitions in the header.
   *
   * @author FracPete (fracpete at waikato dot ac dot nz)
   * @version $Revision$
   */
  public static class BlockDefinition
    implements Serializable {

    /** the type of the block. */
    public int type;

    /** the length in 4-byte blocks. */
    public int lengthBlocks;

    /** the length in bytes. */
    public int lengthBytes;

    /** the offset. */
    public int offset;

    /**
     * String representation of the container.
     *
     * @return		the string representation
     */
    @Override
    public String toString() {
      return "type=" + type + ", lenBlocks=" + lengthBlocks + ", lenBytes=" + lengthBytes + ", offset=" + offset;
    }
  }

  /**
   * Convenience class for handling blocks.
   *
   * @author FracPete (fracpete at waikato dot ac dot nz)
   * @version $Revision$
   */
  public static class Block
    extends LoggingObject {

    /** the content of the file. */
    protected byte[] m_Buffer;

    /** the definition the block is based on. */
    protected int m_Definition;

    /** the start of the block (incl). */
    protected int m_Start;

    /** the end of the block (incl). */
    protected int m_End;

    /** the type of the block. */
    protected int m_Type;

    /** the name of the block. */
    protected String m_Name;

    /**
     * Initializes the block.
     *
     * @param buffer	the file content
     * @param def	the definition the block is based on
     * @param start	the start of the block (incl)
     * @param end	the end of the block (incl)
     */
    public Block(byte[] buffer, int def, int start, int end, int type) {
      m_Buffer     = buffer;
      m_Definition = def;
      m_Start      = start;
      m_End        = (end < buffer.length ? end : buffer.length - 1);
      m_Type       = type;
      if (Character.isLetter(m_Buffer[m_Start]) && Character.isLetter(m_Buffer[m_Start+1]) && Character.isLetter(m_Buffer[m_Start+2]))
	m_Name = new String(new byte[]{m_Buffer[m_Start], m_Buffer[m_Start+1], m_Buffer[m_Start+2]});
      else
	m_Name = null;
    }

    /**
     * Returns the definition that the block is based on.
     *
     * @return		the definition
     */
    public int getDefinition() {
      return m_Definition;
    }

    /**
     * Returns the start of the block.
     *
     * @return		the start
     */
    public int getStart() {
      return m_Start;
    }

    /**
     * Returns the end of the block.
     *
     * @return		the end
     */
    public int getEnd() {
      return m_End;
    }

    /**
     * Returns the type of the block.
     *
     * @return		the type
     */
    public int getType() {
      return m_Type;
    }

    /**
     * Returns the name of the block.
     *
     * @return		the name, null if no valid name
     */
    public String getName() {
      return m_Name;
    }

    /**
     * Returns the size of the block.
     *
     * @return		the size
     */
    public int size() {
      return m_End - m_Start + 1;
    }

    /**
     * Returns whether the ID is present in the block.
     *
     * @param id	the ID to check
     * @return		true if present
     */
    public boolean hasID(byte[] id) {
      return (findID(id) > -1);
    }

    /**
     * Returns the offset of the ID.
     *
     * @param id	the ID to locate
     * @return		the position, -1 if not found
     */
    public int findID(byte[] id) {
      return OpusBlockHelper.findID(m_Buffer, id, m_Start);
    }

    /**
     * Returns the byte at the specified offset.
     *
     * @param offset	the offset to use
     * @return		the byte
     */
    public Byte getByte(int offset) {
      return (byte) OpusBlockHelper.byteToUnsignedByte(m_Buffer[m_Start + offset]);
    }

    /**
     * Returns the long with the given ID.
     *
     * @param id	the ID of the float
     * @param offset	the offset to use from the start of the ID
     * @return		the long, null if not found
     */
    public Byte getByte(byte[] id, int offset) {
      int	pos;

      pos = findID(id);
      if (pos > m_End)
	pos = -1;

      if (pos == -1)
	return null;

      return (byte) OpusBlockHelper.byteToUnsignedByte(m_Buffer[pos + offset]);
    }

    /**
     * Returns the long at the specified offset.
     *
     * @param offset	the offset to use
     * @return		the long
     */
    public Long getLong(int offset) {
      return OpusBlockHelper.getLong(m_Buffer, m_Start + offset);
    }

    /**
     * Returns the long with the given ID.
     *
     * @param id	the ID of the float
     * @param offset	the offset to use from the start of the ID
     * @return		the long, null if not found
     */
    public Long getLong(byte[] id, int offset) {
      int	pos;

      pos = findID(id);
      if (pos > m_End)
	pos = -1;

      if (pos == -1)
	return null;

      return OpusBlockHelper.getLong(m_Buffer, pos + offset);
    }

    /**
     * Returns the double at the specified offset.
     *
     * @param offset 	starting pos
     * @return 		the double value, null if not found
     */
    public Double getDouble(int offset) {
      return OpusBlockHelper.getDouble(m_Buffer, m_Start + offset);
    }

    /**
     * Returns the double with the given ID.
     *
     * @param id      	the ID of the double
     * @param offset	the offset to use from the start of the ID
     * @return 		the double value, null if not found
     */
    public Double getDouble(byte[] id, int offset) {
      int	pos;

      pos = findID(id);
      if (pos > m_End)
	pos = -1;

      if (pos == -1)
	return null;

      return OpusBlockHelper.getDouble(m_Buffer, pos + offset);
    }

    /**
     * Returns the text at the specified offset.
     *
     * @param offset 	starting pos
     * @return 		the text value
     */
    public String getText(int offset) {
      return OpusBlockHelper.getText(m_Buffer, m_Start + offset);
    }

    /**
     * Returns the text with the given ID.
     *
     * @param id      	the ID of the text
     * @param offset	the offset to use from the start of the ID
     * @return 		the text value, null if not found
     */
    public String getText(byte[] id, int offset) {
      int	pos;

      pos = findID(id);
      if (pos > m_End)
	pos = -1;

      if (pos == -1)
	return null;

      return OpusBlockHelper.getText(m_Buffer, pos + offset);
    }

    /**
     * Returns the sub-section of the buffer (copy operation!).
     *
     * @return		copy of the sub-section of the buffer
     */
    public byte[] getBufferSection() {
      byte[]	result;

      result = new byte[size()];
      System.arraycopy(m_Buffer, m_Start, result, 0, result.length);

      return result;
    }

    /**
     * Returns a short description of the block.
     *
     * @return		the block
     */
    @Override
    public String toString() {
      return "definition=" + m_Definition + ", type=" + m_Type + ", name=" + m_Name + ", start=" + m_Start + ", end=" + m_End;
    }
  }

  /**
   * Encapsulates and parses a commandline string.
   *
   * @author FracPete (fracpete at waikato dot ac dot nz)
   * @version $Revision$
   */
  public static class CommandlineData
    extends LoggingObject {

    /** the raw string. */
    protected String m_Raw;

    /** the operation. */
    protected String m_Operation;

    /** the type of the commandline. */
    protected String m_Type;

    /** the data. */
    protected HashMap<String,String> m_Values;

    /**
     * Initializes the object.
     *
     * @param raw	the raw string
     */
    public CommandlineData(String raw) {
      super();
      m_Raw = raw;
      try {
	parse();
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to parse: " + m_Raw, e);
      }
    }

    /**
     * Parses the commandline.
     */
    protected void parse() {
      String		intro;
      String		payload;
      String		data;
      StringBuilder	current;
      char		c;
      int		i;
      boolean		escaped;
      List<String>	pairs;
      String		key;
      String		value;

      m_Values    = new HashMap<>();
      intro       = m_Raw.substring(0, m_Raw.indexOf("("));
      payload     = m_Raw.substring(m_Raw.indexOf("(") + 1, m_Raw.lastIndexOf(")"));
      m_Operation = intro.substring(intro.indexOf(KEYWORD_CMDLINE) + KEYWORD_CMDLINE.length()).trim();
      m_Type      = payload.substring(payload.indexOf("[") + 1, payload.indexOf("]")).replace("\"::this::\":", "");
      data        = payload.substring(payload.indexOf("{") + 1, payload.lastIndexOf("}"));

      current = new StringBuilder();
      escaped = false;
      pairs   = new ArrayList<>();
      for (i = 0; i < data.length(); i++) {
	c = data.charAt(i);
	switch (c) {
	  case '\'':
	    escaped = !escaped;
	    break;
	  case ',':
	    if (!escaped) {
	      if (current.length() > 0)
		pairs.add(current.toString().trim());
	      current.delete(0, current.length());
	    }
	    else {
	      current.append(c);
	    }
	    break;
	  default:
	    current.append(c);
	    break;
	}
      }
      // left-over?
      if (current.length() > 0)
	pairs.add(current.toString().trim());

      // split into key-value pairs
      for (String pair: pairs) {
	if (pair.contains("=")) {
	  key   = pair.substring(0, pair.indexOf("="));
	  value = pair.substring(pair.indexOf("=") + 1);
	  m_Values.put(key.trim(), value.trim());
	}
	else {
	  getLogger().warning("Invalid key-value pair: '" + pair + "'");
	}
      }
    }

    /**
     * Returns the commandline operation.
     *
     * @return		the operation
     */
    public String getOperation() {
      return m_Operation;
    }

    /**
     * Returns the operation type.
     *
     * @return		the type
     */
    public String getType() {
      return m_Type;
    }

    /**
     * Returns the number of values.
     *
     * @return		the number of values
     */
    public int size() {
      return m_Values.size();
    }

    /**
     * Returns the keys of the values.
     *
     * @return		the keys
     */
    public Set<String> keySet() {
      return m_Values.keySet();
    }

    /**
     * Checks whether the key is present.
     *
     * @param key	the key to check
     * @return		true if present
     */
    public boolean has(String key) {
      return m_Values.containsKey(key);
    }

    /**
     * Returns the value associated with the key.
     *
     * @param key	the key of the value to retrieve
     * @return		the value, null if not available
     */
    public String get(String key) {
      return m_Values.get(key);
    }

    /**
     * Returns the raw data.
     *
     * @return		the raw datas
     */
    @Override
    public String toString() {
      return "operation=" + m_Operation + ", type=" + m_Type + ", values=" + m_Values;
    }
  }

  /** the offset for block definitions in the header. */
  public final static int BLOCK_OFFSET = 36;

  /** maximum length of header (made up value!). */
  public final static int HEADER_LENGTH = 500;

  /** the block definition length (type, length, offset). */
  public final static int BLOCK_DEFINITION_LENGTH = 12;

  /** the dummy block type (???). */
  public final static int BLOCKTYPE_DUMMY = 0;

  /** the text block type. */
  public final static int BLOCKTYPE_TEXT = 1080557568;

  /** the mask for the block type of the main spectrum. */
  public final static int BLOCKTYPE_SPEC_MASK = 0x000fffff;

  /** the mask for the block type of the main spectrum. */
  public final static int BLOCKTYPE_MAIN_MASK = 0x100f;

  /** increment from data to DPF block type. */
  public final static int BLOCKTYPE_INCREMENT_DATA_TO_DPF = 16;

  /** the END character sequence. */
  public final static byte[] END = new byte[]{'E', 'N', 'D', 0};

  /** the APT character sequence. */
  public final static byte[] APT = new byte[]{'A', 'P', 'T', 0};

  /** the APF character sequence. */
  public final static byte[] APF = new byte[]{'A', 'P', 'F', 0};

  /** the AQM character sequence. */
  public final static byte[] AQM = new byte[]{'A', 'Q', 'M', 0};

  /** the CNM character sequence. */
  public final static byte[] CNM = new byte[]{'C', 'N', 'M', 0};

  /** the DPF character sequence. */
  public final static byte[] DPF = new byte[]{'D', 'P', 'F', 0};

  /** the NPT character sequence. */
  public final static byte[] NPT = new byte[]{'N', 'P', 'T', 0};

  /** the FXV character sequence. */
  public final static byte[] FXV = new byte[]{'F', 'X', 'V', 0};

  /** the LXV character sequence. */
  public final static byte[] LXV = new byte[]{'L', 'X', 'V', 0};

  /** the CSF character sequence. */
  public final static byte[] CSF = new byte[]{'C', 'S', 'F', 0};

  /** the INS character sequence. */
  public final static byte[] INS = new byte[]{'I', 'N', 'S', 0};

  /** the PLF character sequence. */
  public final static byte[] PLF = new byte[]{'P', 'L', 'F', 0};

  /** the text separator. */
  public final static String TEXT_SEPARATOR = "\t\t";

  /** the command line keyword. */
  public final static String KEYWORD_CMDLINE = "COMMAND_LINE";

  /** the operation containing the sample ID. */
  public final static String OPERATION_MEASURESAMPLE = "MeasureSample";

  /** the key for the sample ID. */
  public final static String KEY_SAMPLEID = "NAM";

  /** the key for the sample ID (2). */
  public final static String KEY_SAMPLEID2 = "SNM";

  /**
   * Parses the opus header and returns the blocks definitions.
   *
   * @param file	the file
   * @param errors	for collecting errors
   * @return		the definitions
   */
  public static List<BlockDefinition> readDefinitions(File file, MessageCollection errors) {
    byte[]	buf;

    buf = FileUtils.loadFromBinaryFile(file.getAbsoluteFile());
    if (buf == null) {
      errors.add(OpusBlockHelper.class.getName() + ": Failed to read bytes from: " + file);
      return new ArrayList<>();
    }

    return readDefinitions(buf, errors);
  }

  /**
   * Parses the opus header and returns the blocks definitions.
   *
   * @param buf		the file content
   * @param errors	for collecting errors
   * @return		the definitions
   */
  public static List<BlockDefinition> readDefinitions(byte[] buf, MessageCollection errors) {
    List<BlockDefinition>	result;
    int				i;
    int				type;
    int				length;
    int				offset;
    BlockDefinition		def;

    result = new ArrayList<>();

    i = BLOCK_OFFSET;
    while (i < HEADER_LENGTH) {
      type   = (int) getLong(buf, i);
      length = (int) getLong(buf, i + 4);
      offset = (int) getLong(buf, i + 8);
      if (length == 0)
	break;

      def              = new BlockDefinition();
      def.type         = type;
      def.lengthBlocks = length;
      def.lengthBytes  = length * 4;
      def.offset       = offset;
      result.add(def);

      i += BLOCK_DEFINITION_LENGTH;
    }

    return result;
  }

  /**
   * Creates blocks from the definitions.
   *
   * @param buf		the file content
   * @param defs	the definitions to use
   * @return		the blocks
   */
  public static List<Block> readBlocks(byte[] buf, List<BlockDefinition> defs) {
    List<Block>		result;
    Block		block;
    int			index;

    result = new ArrayList<>();

    index = -1;
    for (BlockDefinition def: defs) {
      index++;
      if (def.type == BLOCKTYPE_DUMMY)
	continue;

      block = new Block(buf, index, def.offset, def.offset + def.lengthBytes - 1, def.type);
      result.add(block);
    }

    return result;
  }

  /**
   * Locates the first occurrence of the specified ID in the buffer after
   * the starting position.
   *
   * @param buf		the file content
   * @param id	the block type to find
   * @param start	the starting point
   * @return		the start/end, null if failed to detect
   */
  public static int findID(byte[] buf, byte[] id, int start) {
    int 	result;
    int 	blEnd;
    int		i;
    int		n;
    boolean	match;

    result = -1;

    for (i = start; i < buf.length - id.length; i++) {
      match = true;
      for (n = 0; n < id.length; n++) {
	if (buf[i+n] != id[n]) {
	  match = false;
	  break;
	}
      }
      if (match) {
	result = i;
	break;
      }
    }

    return result;
  }

  /**
   * Convert byte to unsigned byte.
   *
   * @param b 		the byte
   * @return 		the unsigned byte
   */
  public static int byteToUnsignedByte(byte b) {
    int result = b;
    if (result < 0)
      result = result + 256;
    return result;
  }

  /**
   * Returns the long at the specified offset.
   *
   * @param offset	the offset to use
   * @return		the long
   */
  public static long getLong(byte[] buf, int offset) {
    long	result;

    result  = (long) byteToUnsignedByte(buf[offset]);
    result += ((long) (byteToUnsignedByte(buf[offset + 1]))) * 256;
    result += ((long) (byteToUnsignedByte(buf[offset + 2]))) * 65536;
    result += ((long) (byteToUnsignedByte(buf[offset + 3]))) * 16777216;

    return result;
  }

  /**
   * Returns the double with the given ID.
   *
   * @param offset 	starting pos
   * @return 		the double value, null if failed to convert
   */
  public static Double getDouble(byte[] buf, int offset) {
    byte[] 	b;

    b    = new byte[8];
    b[0] = (byte) byteToUnsignedByte(buf[offset + 7]);
    b[1] = (byte) byteToUnsignedByte(buf[offset + 6]);
    b[2] = (byte) byteToUnsignedByte(buf[offset + 5]);
    b[3] = (byte) byteToUnsignedByte(buf[offset + 4]);
    b[4] = (byte) byteToUnsignedByte(buf[offset + 3]);
    b[5] = (byte) byteToUnsignedByte(buf[offset + 2]);
    b[6] = (byte) byteToUnsignedByte(buf[offset + 1]);
    b[7] = (byte) byteToUnsignedByte(buf[offset + 0]);

    DataInputStream d = new DataInputStream(new ByteArrayInputStream(b));
    try {
      return d.readDouble();
    }
    catch (IOException e) {
      System.err.println(OpusBlockHelper.class.getName()
	+ ": getDouble: offset=" + offset
	+ ", bytes=" + Utils.arrayToString(b));
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns the 0-terminated text at the specified offset.
   *
   * @param offset	the offset to use
   * @return		the text
   */
  public static String getText(byte[] buf, int offset) {
    StringBuilder	result;
    boolean		finished;
    int			i;
    int			b;

    result = new StringBuilder();

    finished = false;
    i        = offset;
    while (!finished) {
      b = byteToUnsignedByte(buf[i]);
      if (b != 0)
	result.append((char) b);
      i++;
      finished = (b == 0) || (i >= buf.length);
    }

    return result.toString();
  }
}
