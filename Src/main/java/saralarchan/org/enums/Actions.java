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
package saralarchan.org.enums;

import java.util.function.Supplier;
import saralarchan.org.processors.AddVarProcessor;
import saralarchan.org.processors.DelNodeProcessor;
import saralarchan.org.processors.FromDBProcessor;
import saralarchan.org.processors.FromReqProcessor;
import saralarchan.org.processors.FromVarProcessor;
import saralarchan.org.processors.GenericActionProcessor;
import saralarchan.org.processors.JavaProcessor;
import saralarchan.org.processors.RedisCallProcessor;
import saralarchan.org.processors.RestCallProcessor;
import saralarchan.org.processors.ScriptProcessor;
import saralarchan.org.processors.SetVarProcessor;
import saralarchan.org.processors.SetVarsFromRsProcessor;
import saralarchan.org.processors.LogProcessor;
public enum Actions {

  SETVAR(SetVarProcessor::new), 
  FROMREQ(FromReqProcessor::new), 
  FROMVAR(FromVarProcessor::new), 
  ADDVAR(AddVarProcessor::new), 
  DELNODE(DelNodeProcessor::new), 
  REDISCALL(RedisCallProcessor::new), 
  RESTCALL(RestCallProcessor::new), 
  FROMDB(FromDBProcessor::new), 
  SETVARFROMRS(SetVarsFromRsProcessor::new), 
  SCRIPT(ScriptProcessor::new), 
  JAVA(JavaProcessor::new),
  LOG(LogProcessor::new);

  Supplier<GenericActionProcessor> processorSupplier;

  Actions(Supplier<GenericActionProcessor> processorSupplier) {
    this.processorSupplier = processorSupplier;
  }

  public GenericActionProcessor getProcessor() {
    return processorSupplier.get();
  }

}
