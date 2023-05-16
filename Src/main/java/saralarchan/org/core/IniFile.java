/**
 * Copyright 2023 Infosys Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package saralarchan.org.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniFile {

   private Pattern  _section  = Pattern.compile( "\\s*\\[([^]]*)\\]\\s*" );
   private Pattern  _keyValue = Pattern.compile( "\\s*([^=]*)=(.*)" );
   private Map< String,
      Map< String,
         String >>  _entries  = new HashMap<>();

   public IniFile( String path ) throws IOException {
      load( path );
   }

   public void load( String path ) throws IOException {
      try( BufferedReader br = new BufferedReader( new FileReader( path ))) {
         String line;
         String section = null;
         while(( line = br.readLine()) != null ) {
            Matcher m = _section.matcher( line );
            if( m.matches()) {
               section = m.group( 1 ).trim();
            }
            else if( section != null ) {
               m = _keyValue.matcher( line );
               if( m.matches()) {
                  String key   = m.group( 1 ).trim();
                  String value = m.group( 2 ).trim();
                  Map< String, String > kv = _entries.get( section );
                  if( kv == null ) {
                     _entries.put( section, kv = new HashMap<>());   
                  }
                  kv.put( key, value );
               }
            }
         }
      }
   }

   public Map <String,String> getSection(String secName){
	   return _entries.get(secName);
   }
   
   
   public String getString( String section, String key, String defaultvalue ) {
      Map< String, String > kv = _entries.get( section );
      if( kv == null ) {
         return defaultvalue;
      }
      return kv.get( key );
   }

   public int getInt( String section, String key, int defaultvalue ) {
      Map< String, String > kv = _entries.get( section );
      if( kv == null ) {
         return defaultvalue;
      }
      return Integer.parseInt( kv.get( key ));
   }

   public float getFloat( String section, String key, float defaultvalue ) {
      Map< String, String > kv = _entries.get( section );
      if( kv == null ) {
         return defaultvalue;
      }
      return Float.parseFloat( kv.get( key ));
   }

   public double getDouble( String section, String key, double defaultvalue ) {
      Map< String, String > kv = _entries.get( section );
      if( kv == null ) {
         return defaultvalue;
      }
      return Double.parseDouble( kv.get( key ));
   }
}
