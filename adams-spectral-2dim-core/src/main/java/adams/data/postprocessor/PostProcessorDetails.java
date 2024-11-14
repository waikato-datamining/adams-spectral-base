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
 * PostProcessorDetails.java
 * Copyright (C) 2024 University of Waikato, Hamilton, New Zealand
 */
package adams.data.postprocessor;

/**
 * Interface for post-processors that provide details about their internals
 * after they have been built.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @param <T> the type of output the post-processor provides as details
 */
public interface PostProcessorDetails<T> {
  
  /**
   * Returns details for the cleaner.
   * 
   * @return		the details
   */
  public T getDetails();
}
