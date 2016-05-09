package org.ehcache.demo.serializer;

import org.ehcache.core.spi.service.FileBasedPersistenceContext;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alsu on 23/09/15.
 */
// tag::thirdPartyPersistentSerializer[]
public class PersistentKryoSerializer extends TransientKryoSerializer {

  private final File stateFile;

  public PersistentKryoSerializer(ClassLoader loader, FileBasedPersistenceContext persistence) throws IOException, ClassNotFoundException {
    stateFile = new File(persistence.getDirectory(), "PersistentKryoSerializerState.ser");
    if(stateFile.exists()) {  // <1>
      restoreState();   // <2>
      for(Map.Entry<Class, Integer> entry: objectHeaderMap.entrySet()) {  // <3>
        kryo.register(entry.getKey(), entry.getValue());  // <4>
      }
    }
  }

  @Override
  public void close() throws IOException {
    persistState(); // <5>
  }

  private void persistState() throws FileNotFoundException {
    Output output = new Output(new FileOutputStream(stateFile));
    try {
      kryo.writeObject(output, objectHeaderMap);
    } finally {
      output.close();
    }
  }

  private void restoreState() throws FileNotFoundException {
    Input input = new Input(new FileInputStream(stateFile));
    try {
      objectHeaderMap = kryo.readObject(input, HashMap.class);
    } finally {
      input.close();
    }
  }

}
// end::thirdPartyPersistentSerializer[]
