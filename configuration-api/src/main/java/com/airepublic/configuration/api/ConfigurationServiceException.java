/**
      Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.airepublic.configuration.api;

import com.airepublic.exception.ErrorCodeException;
import com.airepublic.exception.IErrorCode;

/**
 * Exceptions for the {@link IConfigurationService}.
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
public class ConfigurationServiceException extends ErrorCodeException {
	private static final long serialVersionUID = -7936622314047160092L;

	public enum ErrorCode implements IErrorCode {
		ERROR_LOADING_DEFAULT("1"), ERROR_SAVE("2"), ERROR_GET("3"), ERROR_REMOVE("4");
		
		private String code;
		
		private ErrorCode(String code) {
			this.code = code;
		}
		
		@Override
		public String getCode() {
			return code;
		}
		
	}
	
	public ConfigurationServiceException() {
	}
	
	public ConfigurationServiceException(IErrorCode errorCode, String message) {
		super(errorCode, message);
	}
	
	public ConfigurationServiceException(IErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
